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

package com.sun.j2ee.blueprints.xmldocuments;

import java.net.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.sax.*;
import javax.xml.transform.stream.*;


public interface XMLDocumentEditor {

  public class DefaultXDE implements XMLDocumentEditor {
    private boolean validating = false;
    private URL entityCatalogURL = null;
    private boolean supportingXSD = false;


    public void setDocument(Source source) throws XMLDocumentException {
      throw new UnsupportedOperationException("Can't set the source on this document editor.");
    }

    public Source getDocument() throws XMLDocumentException {
      throw new UnsupportedOperationException("Can't get the source on this document editor.");
    }

    public void copyDocument(Result result) throws XMLDocumentException {
      throw new UnsupportedOperationException("Can't copy from this document editor.");
    }

    public void setValidating(boolean validating) {
      this.validating = validating;
      return;
    }

    public boolean isValidating() {
      return validating;
    }

    public void setEntityCatalogURL(URL entityCatalogURL) {
      this.entityCatalogURL = entityCatalogURL;
      return;
    }

    public URL getEntityCatalogURL() {
      return entityCatalogURL;
    }

    public void setSupportingXSD(boolean supportingXSD) {
      this.supportingXSD = supportingXSD;
      return;
    }

    public boolean isSupportingXSD() {
      return supportingXSD;
    }
  }

  public void setDocument(Source source) throws XMLDocumentException;

  public Source getDocument() throws XMLDocumentException;

  public void copyDocument(Result result) throws XMLDocumentException;

}
