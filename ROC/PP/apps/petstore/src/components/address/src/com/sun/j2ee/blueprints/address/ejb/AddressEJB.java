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

package com.sun.j2ee.blueprints.address.ejb;

import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.RemoveException;
import javax.ejb.CreateException;
import javax.naming.NamingException;
import javax.naming.InitialContext;

import com.sun.j2ee.blueprints.pkgen.PKGen;

public abstract class AddressEJB implements EntityBean {

  private EntityContext context = null;

  // getters and setters for CMP fields
  //====================================

  // primary key field
  public abstract Integer getId();
  public abstract void setId(Integer id);

  public abstract String getStreetName1();
  public abstract void setStreetName1(String streetName1);

  public abstract String getStreetName2();
  public abstract void setStreetName2(String streetName2);

  public abstract String getCity();
  public abstract void setCity(String city);

  public abstract String getState();
  public abstract void setState(String state);

  public abstract String getZipCode();
  public abstract void setZipCode(String zipCode);

  public abstract String getCountry();
  public abstract void setCountry(String country);

  // EJB create method
  //===================
  public Object ejbCreate(String streetName1, String streetName2, String city,
                          String state, String zipCode, String country) throws CreateException {
	                    setId (PKGen.getNextKeyValue ("AddressEJB"));
      			    //System.err.println( "AddressEJB.ejbCreate: idCounter = " + getId() );
                            setStreetName1(streetName1);
                            setStreetName2(streetName2);
                            setCity(city);
                            setState(state);
                            setZipCode(zipCode);
                            setCountry(country);
                            return null;
  }

  public void ejbPostCreate(String streetName1, String streetName2, String city,
                            String state, String zipCode, String country) throws CreateException { }

  public Object ejbCreate(Address address) throws CreateException {
    setId (PKGen.getNextKeyValue ("AddressEJB"));
    //System.err.println( "AddressEJB.ejbCreate: idCounter = " + getId() );
    setStreetName1(address.getStreetName1());
    setStreetName2(address.getStreetName2());
    setCity(address.getCity());
    setState(address.getState());
    setZipCode(address.getZipCode());
    setCountry(address.getCountry());
    return null;
  }

  public void ejbPostCreate(Address address) throws CreateException { }

  public Object ejbCreate() throws CreateException {
    setId (PKGen.getNextKeyValue ("AddressEJB"));
    //System.err.println( "AddressEJB.ejbCreate: idCounter = " + getId() );
    return null;
  }

  public void ejbPostCreate() throws CreateException { }

  public Address getData() {
    Address address = new Address();
    address.setStreetName1(getStreetName1());
    address.setStreetName2(getStreetName2());
    address.setCity(getCity());
    address.setState(getState());
    address.setZipCode(getZipCode());
    address.setCountry(getCountry());
    return address;
  }

  // Misc Method
  //=============
  public void setEntityContext(EntityContext c) {
    context = c;
  }

  public void unsetEntityContext() {
    context = null;
  }

  public void ejbRemove() throws RemoveException { }
  public void ejbActivate() { }
  public void ejbPassivate() { }
  public void ejbStore() { }
  public void ejbLoad() { }
}
