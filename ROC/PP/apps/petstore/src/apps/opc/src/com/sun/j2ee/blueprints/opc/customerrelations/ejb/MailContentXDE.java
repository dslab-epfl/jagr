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

package com.sun.j2ee.blueprints.opc.customerrelations.ejb;

import java.io.*;
import java.util.*;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.Source;

import com.sun.j2ee.blueprints.xmldocuments.XMLDocumentEditor;
import com.sun.j2ee.blueprints.xmldocuments.XMLDocumentException;


/**
 * A helper class which format the content of a mail by applying style sheets.
 */
public class MailContentXDE extends XMLDocumentEditor.DefaultXDE {
  private TransformerFactory transformerFactory;
  private Map transformers = new HashMap();
  private String styleSheetPath;
  private Source source = null;
  private String result = null;
  private Locale locale = null;


  public static class FormatterException extends Exception {
    private Exception exception;

    /**
     * Creates a new FormatterException wrapping another exception, and with a detail message.
     * @param message the detail message.
     * @param exception the wrapped exception.
     */
    public FormatterException(String message, Exception exception) {
      super(message);
      this.exception = exception;
      return;
    }

    /**
     * Creates a FormatterException with the specified detail message.
     * @param message the detail message.
     */
    public FormatterException(String message) {
      this(message, null);
      return;
    }

    /**
     * Creates a new FormatterException wrapping another exception, and with no detail message.
     * @param exception the wrapped exception.
     */
    public FormatterException(Exception exception) {
      this(null, exception);
      return;
    }

    /**
     * Gets the wrapped exception.
     *
     * @return the wrapped exception.
     */
    public Exception getException() {
      return exception;
    }

    /**
     * Retrieves (recursively) the root cause exception.
     *
     * @return the root cause exception.
     */
    public Exception getRootCause() {
      if (exception instanceof FormatterException) {
        return ((FormatterException) exception).getRootCause();
      }
      return exception == null ? this : exception;
    }

    public String toString() {
      if (exception instanceof FormatterException) {
        return ((FormatterException) exception).toString();
      }
      return exception == null ? super.toString() : exception.toString();
    }
  }

  public MailContentXDE(String styleSheetPath) throws FormatterException {
    this.styleSheetPath = styleSheetPath;
    try {
      transformerFactory = TransformerFactory.newInstance();
    } catch (Exception exception) {
      throw new FormatterException(exception);
    }
    return;
  }

  private Transformer getTransformer(Locale locale) throws FormatterException {
    Transformer transformer = (Transformer) transformers.get(locale);
    if (transformer == null) {
      InputStream stream = getClass().getResourceAsStream(getStyleSheetPath(styleSheetPath, locale));
      if (stream != null) {
        try {
          transformer = transformerFactory.newTransformer(new StreamSource(stream));
          transformers.put(locale, transformer);
        } catch (Exception exception) {
          throw new FormatterException(exception);
        }
      } else {
        throw new FormatterException("No style sheet found for locale: " + locale);
      }
    }
    return transformer;
  }

  private String getStyleSheetPath(String styleSheetPath, Locale locale) {
    if (locale != null) {
      int i = styleSheetPath.lastIndexOf('.');
      if (i >= 0) {
        String suffix = styleSheetPath.substring(i);
        String base = styleSheetPath.substring(0, i);
        return base + "_" + locale.toString() + suffix;
      }
      return styleSheetPath + "_" + locale.toString();
    }
    return styleSheetPath;
  }

  public void setLocale(Locale locale) {
    this.locale = locale;
    return;
  }

  public Locale getLocale() {
    return locale;
  }

  public void setDocument(Source source) {
    this.source = source;
    result = null;
        return;
  }

  public void setDocument(String text) {
        setDocument(new StreamSource(new StringReader(text)));
        return;
  }

  public Source getDocument() throws XMLDocumentException {
    if (result == null) {
      try {
        result = format(source, locale);
      } catch (FormatterException exception) {
        throw new XMLDocumentException(exception);
      }
    }
    return new StreamSource(new StringReader(result));
  }

  public String getDocumentAsString() throws XMLDocumentException {
    if (result == null) {
      try {
        result = format(source, locale);
      } catch (FormatterException exception) {
        throw new XMLDocumentException(exception);
      }
    }
    return result;
  }

  private String format(Source source, Locale locale) throws FormatterException {
    Transformer transformer = getTransformer(locale);
    StreamResult result = new StreamResult(new ByteArrayOutputStream());
    transformer.clearParameters();
    try {
      transformer.transform(source, result);
      String encoding = transformer.getOutputProperty(OutputKeys.ENCODING);
      return ((ByteArrayOutputStream) result.getOutputStream()).toString(encoding);
    } catch (Exception exception) {
      throw new FormatterException(exception);
    }
  }
}


