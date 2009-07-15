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

package com.sun.j2ee.blueprints.uidgen.ejb;

import javax.ejb.SessionContext;
import javax.ejb.RemoveException;
import javax.ejb.CreateException;
import javax.ejb.FinderException;
import javax.ejb.EJBException;
import javax.naming.NamingException;
import javax.naming.InitialContext;

import com.sun.j2ee.blueprints.uidgen.counter.ejb.CounterLocal;
import com.sun.j2ee.blueprints.uidgen.counter.ejb.CounterLocalHome;

public class UniqueIdGeneratorEJB implements javax.ejb.SessionBean {

    private  InitialContext ic;
    private  CounterLocalHome clh;

    public void ejbCreate() {
      try {
        ic = new InitialContext();
        clh = (CounterLocalHome) ic.lookup("java:comp/env/ejb/Counter");
      } catch (NamingException ne) {
         throw new EJBException("UniqueIdGeneratorEJB Got naming exception! " + ne.getMessage());
      }
    }

    // Business Methods
    //=================
    public String getUniqueId(String idPrefix) {
        return getCounter(idPrefix).getNextValue();
    }


    // Misc Method
    //=============
    private CounterLocal getCounter(String name) {
        try {
            CounterLocal counter = null;
            try {
                counter = clh.findByPrimaryKey(name);
            } catch (FinderException fe) {
                counter = clh.create(name);
            }
            return counter;
        } catch (CreateException ce) {
            throw new EJBException("Could not create counter " + name + ". Error: " + ce.getMessage());
        }
    }

    public void setSessionContext(SessionContext c) { }
    public void ejbRemove() { }
    public void ejbActivate() { }
    public void ejbPassivate() { }
}
