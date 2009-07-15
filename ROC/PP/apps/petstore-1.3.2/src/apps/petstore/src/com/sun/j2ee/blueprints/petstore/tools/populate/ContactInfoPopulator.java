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

import javax.naming.*;
import javax.ejb.*;
import java.util.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import com.sun.j2ee.blueprints.contactinfo.ejb.*;
import com.sun.j2ee.blueprints.address.ejb.*;


public class ContactInfoPopulator {
  public static final String JNDI_CONTACT_INFO_HOME = "java:comp/env/ejb/ContactInfo";
  private static final String XML_CONTACTINFO = "ContactInfo";
  private static final String XML_FAMILYNAME = "FamilyName";
  private static final String XML_GIVENNAME = "GivenName";
  private static final String XML_EMAIL = "Email";
  private static final String XML_PHONE = "Phone";
  private String rootTag;
  private ContactInfoLocalHome contactInfoHome = null;
  private ContactInfoLocal contactInfo ;
  private AddressPopulator addressPopulator;

  public ContactInfoPopulator(String rootTag) {
    this.rootTag = rootTag;
    addressPopulator = new AddressPopulator(rootTag);
    return;
  }

  public XMLFilter setup(XMLReader reader) throws PopulateException {
    return new XMLDBHandler(addressPopulator.setup(reader), rootTag, XML_CONTACTINFO) {

      public void update() throws PopulateException {}

      public void create() throws PopulateException {
        contactInfo = createContactInfo(getValue(XML_GIVENNAME),
                                        getValue(XML_FAMILYNAME),
                                        getValue(XML_PHONE),
                                        getValue(XML_EMAIL),
                                        addressPopulator.getAddress());
        return;
      }
    };
  }

  public boolean check() throws PopulateException {
    return addressPopulator.check();
  }

  private ContactInfoLocal createContactInfo(String givenName, String familyName, String phone, String email, AddressLocal address) throws PopulateException {
    try {
      if (contactInfoHome == null) {
        InitialContext context = new InitialContext();
        contactInfoHome = (ContactInfoLocalHome) context.lookup(JNDI_CONTACT_INFO_HOME);
      }
      return contactInfoHome.create(givenName, familyName, phone, email, address);
    } catch (Exception exception) {
      throw new PopulateException ("Could not create: " + exception.getMessage(), exception);
    }

  }

  public ContactInfoLocal getContactInfo() {
    return contactInfo;
  }
}



