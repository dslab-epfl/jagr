
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.

 * $Id: SendXmlCommand.java,v 1.2 2003/03/22 04:55:01 emrek Exp $
 *
 */
package com.sun.ecperf.supplier.helper;


//Import statements
import java.io.*;

import java.net.*;

import com.sun.ecperf.common.*;


/**
 * This is the abstract base class for
 * all HTTP transactions in the supplier domain.
 * Implements the Command Pattern [GOF]
 *
 *
 * @author Damian Guy
 */
public abstract class SendXmlCommand {

    String       hostname;
    String       xml;
    String       servlet;
    int          port;
    final String HTTP           = "HTTP/1.1";
    final String METHOD         = "POST ";
    final String CONTENT_TYPE   =
        "Content-type: application/x-www-form-urlencoded";
    final String CONTENT_LENGTH = "Content-length: ";

    /**
     * Constructor SendXmlCommand
     *
     *
     * @param hostname
     * @param xml
     * @param servlet
     * @param port
     *
     */
    public SendXmlCommand(String hostname, String xml, String servlet,
                          int port) {

        this.hostname = hostname;
        this.xml      = xml;
        this.servlet  = servlet;
        this.port     = port;
    }

    /**
     * Method execute
     *
     *
     * @throws ECperfException
     *
     */
    public abstract void execute() throws IOException, ECperfException;
}

