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

package com.sun.j2ee.blueprints.uidgen.counter.ejb;

import javax.ejb.EntityContext;
import javax.ejb.RemoveException;
import javax.ejb.CreateException;

public abstract class CounterEJB implements javax.ejb.EntityBean {

    private EntityContext context = null;

    // getters and setters for CMP fields
    //====================================
    public abstract int getCounter();
    public abstract void setCounter(int counter);

    public abstract String getName();
    public abstract void setName(String name);

    // EJB create method
    //===================
    public String ejbCreate(String name) throws CreateException {
        setName(name);
        setCounter(0);
        return null;
    }
    public void ejbPostCreate(String name) throws CreateException { }

    // Business methods
    //==================
    public String getNextValue() {
        int count = getCounter() + 1;
        setCounter(count);
        return getName() + String.valueOf(count);
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
