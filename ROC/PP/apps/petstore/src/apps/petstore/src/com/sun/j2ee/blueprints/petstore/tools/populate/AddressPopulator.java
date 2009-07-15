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
import com.sun.j2ee.blueprints.address.ejb.*;


public class AddressPopulator {
  public static final String JNDI_ADDRESS_HOME = "java:comp/env/ejb/Address";
  private static final String XML_ADDRESS = "Address";
  private static final String XML_STREETNAME1 = "StreetName[0]";
  private static final String XML_STREETNAME2 = "StreetName[1]";
  private static final String XML_CITY = "City";
  private static final String XML_STATE = "State";
  private static final String XML_ZIPCODE = "ZipCode";
  private static final String XML_COUNTRY = "Country";
  private String rootTag;
  private AddressLocalHome addressHome = null;
  private AddressLocal address ;

  public AddressPopulator(String rootTag) {
    this.rootTag = rootTag;
    return;
  }

  public XMLFilter setup(XMLReader reader) throws PopulateException {
    return new XMLDBHandler(reader, rootTag, XML_ADDRESS) {

      public void update() throws PopulateException {}

      public void create() throws PopulateException {
        address = createAddress(getValue(XML_STREETNAME1),
                                getValue(XML_STREETNAME2),
                                getValue(XML_CITY),
                                getValue(XML_STATE),
                                getValue(XML_ZIPCODE),
                                getValue(XML_COUNTRY));
        return;
      }
    };
  }

  public boolean check() throws PopulateException {
    return true;
  }

  private AddressLocal createAddress(String streetName1, String streetName2, String city, String state, String zipCode, String country) throws PopulateException {
    try {
      if (addressHome == null) {
        InitialContext context = new InitialContext();
        addressHome = (AddressLocalHome) context.lookup(JNDI_ADDRESS_HOME);
      }
      AddressLocal address = addressHome.create();
      address.setStreetName1(streetName1);
      address.setStreetName2(streetName2);
      address.setCity(city);
      address.setState(state);
      address.setZipCode(zipCode);
      address.setCountry(country);
      return address;
    } catch (Exception exception) {
      throw new PopulateException ("Could not create: " + exception.getMessage(), exception);
    }

  }

  public AddressLocal getAddress() {
    return address;
  }
}



