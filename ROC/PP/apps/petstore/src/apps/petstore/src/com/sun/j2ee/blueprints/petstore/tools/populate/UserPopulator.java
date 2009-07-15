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

package com.sun.j2ee.blueprints.petstore.tools.populate;

import javax.ejb.*;
import javax.naming.*;
import java.util.*;
import java.sql.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import com.sun.j2ee.blueprints.signon.ejb.*;
import com.sun.j2ee.blueprints.signon.user.ejb.*;


public class UserPopulator {
  public static final String JNDI_USER_HOME = "java:comp/env/ejb/User";
  public static final String XML_USERS = "Users";
  private static final String XML_USER = "User";
  private static final String XML_ID = "User/@id";
  private static final String XML_PASSWORD = "Password";
  private UserLocalHome userHome = null;
  private String rootTag;


  public UserPopulator() {
    this(XML_USERS);
    return;
  }

  public UserPopulator(String rootTag) {
    this.rootTag = rootTag;
    return;
  }

  public XMLFilter setup(XMLReader reader) throws PopulateException {
    return new XMLDBHandler(reader, rootTag, XML_USER) {

      public void update() throws PopulateException {}

      public void create() throws PopulateException {
        createUser(getValue(XML_ID), getValue(XML_PASSWORD));
        return;
      }
    };

  }

  public boolean check() throws PopulateException {
    try {
          InitialContext context = new InitialContext();
          UserLocalHome userHome = (UserLocalHome) context.lookup(JNDI_USER_HOME);
          Collection users = userHome.findAllUsers();
          if ((users == null) || (users.size() == 0)) {
        return false;
      }
    } catch (Exception exception) {
          return false;
    }
    return true;
  }

  private UserLocal createUser(String id, String password) throws PopulateException {
    try {
      if (userHome == null) {
        InitialContext context = new InitialContext();
        userHome = (UserLocalHome) context.lookup(JNDI_USER_HOME);
      }
      UserLocal user;
      try {
        user = userHome.findByPrimaryKey(id);
        user.remove();
      } catch (Exception exception) {}
      user = userHome.create(id, password);
      return user;
    } catch (Exception exception) {
      throw new PopulateException ("Could not create: " + exception.getMessage(), exception);
    }
  }

  public static void main(String[] args) {
    if (args.length <= 1) {
      String fileName = args.length > 0 ? args[0] : "User.xml";
      try {
        UserPopulator userPopulator = new UserPopulator();
        SAXParserFactory parserFactory = SAXParserFactory.newInstance();
        parserFactory.setValidating(true);
        userPopulator.setup(parserFactory.newSAXParser().getXMLReader()).parse(new InputSource(fileName));
        System.exit(0);
      } catch (Exception exception) {
        System.err.println(exception.getMessage() + ": " + exception);
        System.exit(2);
      }
    }
    System.err.println("Usage: " + UserPopulator.class.getName() + " [file-name]");
    System.exit(1);
  }
}

