/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN
 * OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR
 * FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR
 * PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF
 * LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE SOFTWARE,
 * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that Software is not designed, licensed or intended
 * for use in the design, construction, operation or maintenance of
 * any nuclear facility.
 */


package com.sun.j2ee.blueprints.opc.webservice.invoicercvr;


import java.io.*;
import java.util.*;
import java.net.*;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

import com.sun.j2ee.blueprints.xmldocuments.*;


public class TPAInvoiceXDE extends XMLDocumentEditor.DefaultXDE {
    public static final String DTD_PUBLIC_ID = "-//Sun Microsystems, Inc. - J2EE Blueprints Group//DTD TPA-Invoice 1.0//EN";
    public static final String XSD_PUBLIC_ID = "http://blueprints.j2ee.sun.com/TPAInvoice";
    public static final String DTD_SYSTEM_ID = "/com/sun/j2ee/blueprints/xmldocuments/rsrc/schemas/TPAInvoice.dtd";
    public static final String XSD_SYSTEM_ID = "/com/sun/j2ee/blueprints/xmldocuments/rsrc/schemas/TPAInvoice.xsd";
    private Transformer transformer;
    private Source source = null;


    public TPAInvoiceXDE(URL entityCatalogURL, boolean validating) throws XMLDocumentException {
        this(entityCatalogURL, validating, false);
        return;
    }

    public TPAInvoiceXDE() throws XMLDocumentException {
        this(null, true, false);
        return;
    }

    public TPAInvoiceXDE(URL entityCatalogURL, boolean validating, boolean xsdValidation) throws XMLDocumentException {
        setEntityCatalogURL(entityCatalogURL);
        setValidating(validating);
        setSupportingXSD(xsdValidation);
        transformer = XMLDocumentUtils.createTransformer();
        return;
    }

    public void setDocument(String buffer) throws XMLDocumentException {
        setDocument(new StreamSource(new StringReader(buffer)));
        return;
    }

    public void setDocument(Source source) throws XMLDocumentException {
        this.source = source;
        return;
    }

    public void copyDocument(Result result) throws XMLDocumentException {
        XMLDocumentUtils.transform(transformer, source, result,
                                   (isSupportingXSD() ? XSD_PUBLIC_ID : DTD_PUBLIC_ID),
                                   getEntityCatalogURL(), isValidating(), isSupportingXSD());
        return;
    }

    public Source getDocument() throws XMLDocumentException {
        return new StreamSource(new StringReader(getDocumentAsString()));
    }

    public String getDocumentAsString() throws XMLDocumentException {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            copyDocument(new StreamResult(stream));
            return stream.toString(XMLDocumentUtils.DEFAULT_ENCODING);
        } catch (Exception exception) {
            throw new XMLDocumentException(exception);
        }
    }
}

