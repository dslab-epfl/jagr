
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.

 * $Id: SecureXmlCommand.java,v 1.1 2004/02/19 14:45:05 emrek Exp $
 *
 */
package com.sun.ecperf.supplier.helper;


//Import statements

/**
 * This class will implement secure sockets.
 *
 *
 * @author Damian Guy
 */
public class SecureXmlCommand extends SendXmlCommand {

    /**
     * Constructor SecureXmlCommand
     *
     *
     * @param hostname
     * @param xml
     * @param servlet
     * @param port
     *
     */
    public SecureXmlCommand(String hostname, String xml, String servlet,
                            int port) {
        super(hostname, xml, servlet, port);
    }

    /**
     * Method execute
     *
     *
     */
    public void execute() {}
}

