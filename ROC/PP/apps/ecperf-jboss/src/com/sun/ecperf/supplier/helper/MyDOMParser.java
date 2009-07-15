
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 */
package com.sun.ecperf.supplier.helper;


import org.w3c.dom.Document;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.InputSource;

import org.apache.xerces.parsers.DOMParser;


/**
 * This is a wrapper for the xerces xml parser (or any other parser
 * that we choose to use)
 */
public class MyDOMParser implements ErrorHandler {

    DOMParser parser;

    /**
     * Constructor MyDOMParser
     *
     *
     */
    public MyDOMParser() {

        parser = new DOMParser();

        try {
            parser.setFeature("http://xml.org/sax/features/validation", true);
        } catch (SAXException e) {}

        parser.setErrorHandler(this);
    }

    /**
     * Method parse
     *
     *
     * @param source
     *
     * @return
     *
     * @throws Exception
     *
     */
    public Document parse(InputSource source) throws Exception {

        parser.parse(source);

        return parser.getDocument();
    }

    /**
     * Method warning
     *
     *
     * @param ex
     *
     */
    public void warning(SAXParseException ex) {
        System.err.println("WARNING: " + ex.getMessage());
    }

    /**
     * Method error
     *
     *
     * @param ex
     *
     */
    public void error(SAXParseException ex) {
        System.err.println("ERROR: " + ex.getMessage());
    }

    /**
     * Method fatalError
     *
     *
     * @param ex
     *
     * @throws SAXException
     *
     */
    public void fatalError(SAXParseException ex) throws SAXException {

        System.err.println("FATAL ERROR: " + ex.getMessage());

        throw ex;
    }
}

