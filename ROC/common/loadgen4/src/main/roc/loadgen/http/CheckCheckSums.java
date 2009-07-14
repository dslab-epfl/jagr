/*
 * Created on Apr 12, 2004
 * 
 * To change the template for this generated file go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
package roc.loadgen.http;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import roc.loadgen.AbortRequestException;
import roc.loadgen.AbortSessionException;
import roc.loadgen.Arg;
import roc.loadgen.Request;
import roc.loadgen.RequestInterceptor;
import roc.loadgen.Response;

/**
 * @author emrek
 * 
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class CheckCheckSums extends RequestInterceptor {

    public static final String ARG_INPUTFILE = "checksumfile";

    CheckSumHelper helper;

    Arg[] argDefinitions =
        {
             new Arg(
                ARG_INPUTFILE,
                "from which files to load the list of valid checksums",
                Arg.ARG_LIST,
                true,
                null)};

    Map validchecksums;

    public void start() throws AbortSessionException {

        List inputfiles = (List) args.get(ARG_INPUTFILE);
        validchecksums = new HashMap();

        try {
            Iterator iter = inputfiles.iterator();
            while (iter.hasNext()) {
                loadCheckSumFile((String) iter.next());
            }

        }
        catch (IOException e) {
            throw new AbortSessionException(e);
        }

	helper = new CheckSumHelper();
    }

    public void loadCheckSumFile(String inputfile) throws IOException {
        LineNumberReader in =
            new LineNumberReader(new FileReader(new File(inputfile)));

        while (true) {
            String l = in.readLine();
            if (l == null)
                break;

            int idx = l.indexOf(" ");
            if (idx == -1) {
                engine.logError(
                    "CHECKSUM FILE HAS INCORRECT FORMAT: "
                        + "line "
                        + in.getLineNumber());
            }
            else {
                String path = l.substring(0, idx);
                String checksum = l.substring(idx + 1);
                List v = (List) validchecksums.get(path);
                if (v == null) {
                    v = new ArrayList();
                    validchecksums.put(path, v);
                }
                v.add(checksum);
            }
        }

    }

    public Arg[] getArguments() {
        return argDefinitions;
    }

    /*
     * (non-Javadoc)
     * 
     * @see roc.loadgen.RequestInterceptor#invoke(roc.loadgen.Request)
     */
    public Response invoke(Request req)
        throws AbortRequestException, AbortSessionException {

        HttpRequest httpreq = (HttpRequest) req;
        HttpResponse httpresp = (HttpResponse) invokeNext(req);

	CheckSumHelper.CheckSumResult checksum =
	    helper.calculateCheckSum(engine,httpreq,httpresp);

        String stringid = httpreq.getStringID();
        List validchecksumlist = (List) validchecksums.get(stringid);
        if ( validchecksumlist != null && 
	     validchecksumlist.contains(checksum.digest)) {
	    doCheckSumPassed( httpreq, httpresp, stringid, checksum.digest );
        }
        else {
	    doCheckSumFailed( httpreq, httpresp, validchecksumlist,
			      stringid, checksum.digestedBuf, checksum.digest );
        }

        return httpresp;
    }


    void doCheckSumPassed( HttpRequest httpreq, HttpResponse httpresp,
			   String id, String md5sum ) {
	engine.logStats("CHECKSUM(PASS): request "
			+ httpreq.toString()
			+ " passed checksum for '"
			+ id
			+ "' PP-Requestid=" + httpresp.getHeaders().get("PP-Request"));
    }


    void doCheckSumFailed( HttpRequest httpreq, HttpResponse httpresp,
			   List validchecksumlist,
			   String stringid, byte[] buf, String md5sumString ) {

            engine.logStats(
                "CHECKSUM(FAIL): request "
                    + httpreq.toString()
                    + " FAILED checksum for '"
                    + stringid
                    + "' PP-Requestid="+httpresp.getHeaders().get("PP-Request"));
            engine.logStats(
                "\tcurrent md5 = '"
                    + md5sumString
                    + "', and old checksums are '"
                    + validchecksumlist.toString()
                    + "'");
	    List ppreqlist = (List)httpresp.getHeaders().get("PP-Request");
	    String pprequest = 
		(ppreqlist!=null && ppreqlist.size()>0)?
		(String)ppreqlist.get(0):"NOREQUESTID";
            engine.logData(
                new String[] {
                    "checksum-failure",
                    httpreq.getStringID(),
                    pprequest},
                httpreq.url.getFile(),
                buf);
    }



}


