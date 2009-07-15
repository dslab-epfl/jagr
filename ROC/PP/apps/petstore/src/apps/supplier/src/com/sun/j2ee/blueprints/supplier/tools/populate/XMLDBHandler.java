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

package com.sun.j2ee.blueprints.supplier.tools.populate;

import java.util.*;
import java.io.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;


public abstract class XMLDBHandler extends XMLFilterImpl {
  private static final int OFF = 0;
  private static final int READY = 1;
  private static final int PARSING = 2;
  private int state = OFF;
  private StringBuffer buffer = new StringBuffer();
  private boolean parsing = false;
  private HashMap context = new HashMap();
  private HashMap values = new HashMap();
  private String rootElementTag;
  private String elementTag;
  private boolean lazyInstantiation;


  public XMLDBHandler(XMLReader parent, String rootElementTag, String elementTag) {
    this(parent, rootElementTag, elementTag, false);
    return;
  }

  public XMLDBHandler(XMLReader parent, String rootElementTag, String elementTag, boolean lazyInstantiation) {
    super(parent);
    this.rootElementTag = rootElementTag;
    this.elementTag = elementTag;
    this.lazyInstantiation = lazyInstantiation;
    return;
  }

  public void startDocument() throws SAXException {
    state = OFF;
    values.clear();
    buffer.setLength(0);
    super.startDocument();
    return;
  }

  public void startElement(String uri, String localName, String qualifiedName, Attributes attributes) throws SAXException {
    if (state == READY) {
      if (qualifiedName.equals(elementTag)) {
        state = PARSING;
        values.clear();
        for (int i = 0; i < attributes.getLength(); i++) {
          setValue(qualifiedName + "/@" + attributes.getQName(i), attributes.getValue(i), state == PARSING ? values : context);
        }
        try {
          if (lazyInstantiation) {
            create();
          }
        } catch (PopulateException exception) {
          throw new SAXException(exception.getMessage(), exception.getRootCause());
        }
        return;
      }
    } else if (state == OFF) {
      if (qualifiedName.equals(rootElementTag)) {
        state = READY;
        context.clear();
      }
    }
    if (state == READY) {
      for (int i = 0; i < attributes.getLength(); i++) {
        setValue(qualifiedName + "/@" + attributes.getQName(i), attributes.getValue(i), state == PARSING ? values : context);
      }
    }
    if (state == READY || state == OFF) {
      super.startElement(uri, localName, qualifiedName, attributes);
    }
    return;
  }

  public void characters(char[] chars, int start, int length) throws SAXException {
    if (state == PARSING) {
      buffer.append(new String(chars, start, length));
    } else if (state == READY) {
      buffer.append(new String(chars, start, length));
      super.characters(chars, start, length);
    } else {
      super.characters(chars, start, length);
    }
    return;
  }

  public void endElement(String uri, String localName, String qualifiedName) throws SAXException {
    if (state == READY) {
      if (qualifiedName.equals(rootElementTag)) {
        state = OFF;
      } else {
        setValue(qualifiedName, buffer.toString(), context);
        buffer.setLength(0);
      }
    }
    if (state == PARSING) {
      if (qualifiedName.equals(elementTag)) {
        try {
          if (!lazyInstantiation) {
            create();
          } else {
            update();
          }
        } catch (PopulateException exception) {
          throw new SAXException(exception.getMessage(), exception.getRootCause());
        }
        state = READY;
      } else {
        setValue(qualifiedName, buffer.toString(), values);
      }
      buffer.setLength(0);
    } else {
      super.endElement(uri, localName, qualifiedName);
    }
    return;
  }

  public void warning(SAXParseException exception) {
    System.err.println("[Warning]: " + exception.getMessage());
    return;
  }

  public void error(SAXParseException exception) {
    System.err.println("[Error]: " + exception.getMessage());
    return;
  }

  public void fatalError(SAXParseException exception) throws SAXException {
    System.err.println("[Fatal Error]: " + exception.getMessage());
    throw exception;
  }

  private void setValue(String name, String value, Map map) {
    String key = name;
    if (values.get(key) == null) {
      map.put(key, value);
      key = name + "[" + 0 + "]";
      map.put(key, value);
    } else {
      for (int i = 1;; i++) {
          key = name + "[" + i + "]";
          if (map.get(key) == null) {
            map.put(key, value);
            break;
          }
      }
    }
    return;
  }

  public String getValue(String name) {
    String value = (String) values.get(name);
    if (value == null) {
      value = (String) context.get(name);
    }
    return value;
  }

  public String getValue(String name, String defaultValue) {
    String value = getValue(name);
    return value != null ? value : defaultValue;
  }

  public boolean getValue(String name, boolean defaultValue) {
    String value = getValue(name);
    return value != null ? Boolean.valueOf(value).booleanValue() : defaultValue;
  }

  public int getValue(String name, int defaultValue) {
    String value = getValue(name);
    try {
      if (value != null) {
        return Integer.valueOf(value).intValue();
      }
    } catch (NumberFormatException exception) {}
    return defaultValue;
  }

  public abstract void create() throws PopulateException;

  public abstract void update() throws PopulateException;

}



