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

package com.sun.j2ee.blueprints.contactinfo.ejb;

import javax.ejb.EntityContext;
import javax.ejb.RemoveException;
import javax.ejb.CreateException;
import javax.naming.NamingException;
import javax.naming.InitialContext;
import com.sun.j2ee.blueprints.address.ejb.AddressLocal;
import com.sun.j2ee.blueprints.address.ejb.AddressLocalHome;
import com.sun.j2ee.blueprints.address.ejb.Address;
import com.sun.j2ee.blueprints.servicelocator.ejb.ServiceLocator;
import com.sun.j2ee.blueprints.servicelocator.ServiceLocatorException;
import com.sun.j2ee.blueprints.pkgen.PKGen;


public abstract class ContactInfoEJB implements javax.ejb.EntityBean {

  private EntityContext context = null;

  // getters and setters for CMP fields
  //====================================

  // primary key field
  public abstract Integer getId();
  public abstract void setId( Integer id );
 
  public abstract String getFamilyName();
  public abstract void setFamilyName(String familyName);

  public abstract String getGivenName();
  public abstract void setGivenName(String givenName);

  public abstract String getTelephone();
  public abstract void setTelephone(String telephone);

  public abstract String getEmail();
  public abstract void setEmail(String email);

  // CMR fields
  public abstract AddressLocal getAddress();
  public abstract void setAddress(AddressLocal address);

  // EJB create methods
  //===================
  public Object ejbCreate () throws CreateException {
    setId (PKGen.getNextKeyValue ("ContactInfoEJB"));
    return null;
  }

  public void ejbPostCreate () throws CreateException {
    try {
      ServiceLocator serviceLocator = new ServiceLocator();
      AddressLocalHome adh = (AddressLocalHome)
          serviceLocator.getLocalHome(JNDINames.ADDR_EJB);
      AddressLocal address = adh.create();
      setAddress(address);
    } catch (ServiceLocatorException ne) {
      throw new CreateException("ContactInfoEJB error: ServiceLocator exception looking up address");
    }
  }

  public Object ejbCreate(String givenName, String familyName,
                          String telephone, String email, AddressLocal address)
                          throws CreateException {
    setId (PKGen.getNextKeyValue ("ContactInfoEJB"));
    setGivenName(givenName);
    setFamilyName(familyName);
    setTelephone(telephone);
    setEmail(email);
    return null;
  }

  public void ejbPostCreate(String givenName, String familyName,
                            String telephone, String email,
                            AddressLocal address) throws CreateException {
    setAddress(address);
  }

  public Object ejbCreate(ContactInfo contactInfo) throws CreateException {
    setId (PKGen.getNextKeyValue ("ContactInfoEJB"));
    setGivenName(contactInfo.getGivenName());
    setFamilyName(contactInfo.getFamilyName());
    setTelephone(contactInfo.getPhone());
    setEmail(contactInfo.getEmail());
    return null;
  }

  public void ejbPostCreate(ContactInfo contactInfo) throws CreateException {
    try {
      ServiceLocator serviceLocator = new ServiceLocator();
      AddressLocalHome adh = (AddressLocalHome)
          serviceLocator.getLocalHome(JNDINames.ADDR_EJB);
      AddressLocal address = adh.create(contactInfo.getAddress());
      setAddress(address);
    } catch (ServiceLocatorException ne) {
      throw new CreateException("ContactInfoEJB error: ServiceLocator exception looking up address");
    }
  }

  public ContactInfo getData() {
    ContactInfo contactInfo = new ContactInfo();
    contactInfo.setGivenName(getGivenName());
    contactInfo.setFamilyName(getFamilyName());
    contactInfo.setPhone(getTelephone());
    contactInfo.setEmail(getEmail());
    contactInfo.setAddress(getAddress().getData());
    return contactInfo;
  }

  // Misc Method
  //=============
  public void setEntityContext(EntityContext c) {
    context = c;
  }
  public void unsetEntityContext() {
    context = null;
  }
  public void ejbRemove() throws RemoveException {
  }
  public void ejbActivate() {
  }
  public void ejbPassivate() {
  }
  public void ejbStore() {
  }
  public void ejbLoad() {
  }
}
