package roc.loadgen.http;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.List;

import roc.loadgen.InitializationException;
import roc.loadgen.Engine;
import swig.util.StringHelper;

import org.apache.log4j.Logger;

public class CheckSumHelper {

    MessageDigest digest;
    private static Logger log = Logger.getLogger( "ChecksumHelper" );

    TextMunger[] mungers = new TextMunger[] {

	/**
	 *  remove basic dynamic data -- username, password, etc from checksum
	 */
	new TextMunger() {
		public String munge( Engine engine, HttpRequest req, HttpResponse resp,
			       String buf ) {
		    String dynamicdata = 
			(String)req.getMetadata().get("dynamicdata");

		    if( dynamicdata == null )
			return buf;

		    List ddlist =
			StringHelper.SeparateStrings(dynamicdata,',');
		    Iterator iter = ddlist.iterator();
		    while (iter.hasNext()) {
			String dd = (String) iter.next();
			buf =
			    StringHelper.ReplaceAll(buf, dd, "DYNAMICDATA");
		    }
		    return buf;
		}
	    },


	/**
	 * special case for pestore 1.3.x.  fix non-deterministic ordering
	 * of POST forms in many pages
	 */
	new TextMunger() {

		static final String FORMSTART="<form method=\"POST\"";
		static final String FORMEND="</form>";
		static final String INPUTSTART="<input";
		static final String INPUTEND="</input>";

		public String munge( Engine engine, HttpRequest req, HttpResponse resp,
				     String buf ) {

		    StringBuffer strbuf = new StringBuffer( buf );

		    int debugcount=0;

		    while( true ) {
			int formstarttagidx = strbuf.indexOf( FORMSTART );
			if( formstarttagidx == -1 )
			    break;

			int formendtagidx = strbuf.indexOf( FORMEND,
							 formstarttagidx );
			if( formendtagidx == -1 )
			    break;

			// MAJOR TODO: eventually pull out input tags
			//       and alphabetize them to make it deterministic
			// for now, just remove the whole thing

			strbuf.replace( formstarttagidx, formendtagidx,
					"<form></form>" );
			debugcount++;
		    }
		    

		    log.debug( "ps13munger: munged " + debugcount + " sets of form tags" );
		    return strbuf.toString();
		}
	    },



	/**
	 * special case for pestore 1.3.x.  remove some dynamic content (order id)
	 * of /petstore/order.do url.
	 */
	new TextMunger() {

		static final String YOURORDERID="Your order Id is ";

		String[] mungeableURL = new String[] {
		    "/petstore/order.do " };

		public String munge( Engine engine, HttpRequest req, HttpResponse resp,
				     String buf ) {

		    log.debug( "ps13munger: running on request: " + req.url.getFile() );


		    boolean munge = false;
		    for( int i=0; i<mungeableURL.length; i++ ) {
			if( req.url.getFile().endsWith( mungeableURL[i] )) {
			    log.debug( "ps13munger: matches " + mungeableURL[i] );
			    munge = true;
			}
		    }

		    if( !munge) {
			log.debug( "ps13munger: doesn't any mungeable URLs" );
			return buf;
		    }

		    StringBuffer strbuf = new StringBuffer( buf );

		    int idx = strbuf.indexOf( YOURORDERID );
		    if( idx != -1 ) {
			int startIDidx = idx + YOURORDERID.length();
			int endIDidx =strbuf.indexOf( "\n", startIDidx );

			strbuf.replace(startIDidx,endIDidx, "DYNDATA" );
		    }

		    return strbuf.toString();
		}
	    }



    };


    CheckSumHelper() throws InitializationException {
        try {
            digest = MessageDigest.getInstance("MD5");
        }
        catch(NoSuchAlgorithmException e) {
            throw new InitializationException(e);
        }
    }

    public CheckSumResult calculateCheckSum( Engine engine,
				     HttpRequest req, HttpResponse resp ) {

        byte[] buf = resp.getRespBuf();

	log.debug( "calculateCheckSum: considering running mungers on http response" );
        
        if(resp.contentType != null && resp.contentType.startsWith("text")) {
            String strbuf = new String(buf);
	    
	    log.debug( "calculateCheckSum: about to run mungers on http response" );
	 
	    for( int i=0; i<mungers.length; i++ ) {
		strbuf = mungers[i].munge(engine,req,resp,strbuf);
	    }

            buf = strbuf.getBytes();
        }

	if( buf == null ) {
	    buf = new byte[0];
	}     

        byte[] md5sum = digest.digest(buf);

        StringBuffer md5sumBuffer = new StringBuffer(md5sum.length*2);
        for(int i=0; i<md5sum.length;i++) {
            md5sumBuffer.append( Integer.toString(md5sum[i],16));
        }

	CheckSumResult ret = new CheckSumResult();
	ret.digestedBuf = buf;
	ret.digest = md5sumBuffer.toString();

	return ret;
    }

    public class CheckSumResult {
	public byte[] digestedBuf;
	public String digest;
    }
  
    interface TextMunger {

	String munge( Engine engine, HttpRequest req, HttpResponse resp,
			    String buf );

    }

}
