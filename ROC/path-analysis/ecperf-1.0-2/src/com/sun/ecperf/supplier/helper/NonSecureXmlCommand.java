
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.

 * $Id: NonSecureXmlCommand.java,v 1.1.1.1 2002/11/16 05:35:30 emrek Exp $
 *
 */
package com.sun.ecperf.supplier.helper;


//Import statements
import java.io.*;

import java.net.*;

import com.sun.ecperf.common.*;


/**
 * This is the class for non https communication
 * with the Supplier Emulator.
 *
 *
 * @author Damian Guy
 */
public class NonSecureXmlCommand extends SendXmlCommand {

    public static final int ERROR = -1;
    public static final int OK    = 0;
    public static final int RETRY = 1;

    /**
     * Constructor NonSecureXmlCommand
     *
     *
     * @param hostname
     * @param xml
     * @param servlet
     * @param port
     *
     */
    public NonSecureXmlCommand(String hostname, String xml, String servlet,
                               int port) {
        super(hostname, xml, servlet, port);
    }

    /**
     * execute: connect to a servlet and send XML
     */
    public void execute() throws IOException, ECperfException {

        StringBuffer b = new StringBuffer("http://");

        b.append(hostname);
        b.append(':');
        b.append(port);
        b.append('/');
        b.append(servlet);

        URL               url  = new URL(b.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        //conn.setDoInput(true);
        // Work around for Bug Id 4501025
        conn.setRequestProperty("Connection","close");
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setRequestMethod("POST");
        // Modified code to ensure proper encoding for systems that don't use 8859 by default $MRH
        PrintWriter stream = new PrintWriter(new OutputStreamWriter(conn.getOutputStream(),"8859_1"),
                                                 true);

        // Send the XML
        stream.println("xml=" + URLEncoder.encode(xml));
        stream.close();

        // Modified code to ensure proper encoding for systems that don't use 8859 by default $MRH
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(conn
                .getInputStream(),"8859_1"));

        // Check for response "200 OK" and "502 RETRY"
        int ok = ERROR;

        // System.err.println("Reading response");
        StringBuffer response = new StringBuffer();
        String       r        = null;

        for (;;) {
            r = reader.readLine();

            if (r == null) {
                break;
            }

            response.append(r);

            if (r.indexOf("200 OK") != -1) {
                ok = OK;
                break;
            } else if (r.indexOf("502 RETRY") != -1) {
                ok = RETRY;
                break;
            }
        }

        // System.err.println("Response received");
        reader.close();

        // System.err.println("Connection closed");
        switch (ok) {
            case RETRY :
                throw new NotReadyException(response.toString());
            case ERROR : 
                throw new ECperfException("Remote side error: " + response);
        }
    }
}

