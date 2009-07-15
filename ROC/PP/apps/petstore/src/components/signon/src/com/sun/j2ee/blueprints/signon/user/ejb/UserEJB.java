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

package com.sun.j2ee.blueprints.signon.user.ejb;

import javax.ejb.EntityBean;
import javax.ejb.EntityContext;

import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.CreateException;
import javax.ejb.RemoveException;
import javax.ejb.DuplicateKeyException;

public abstract class UserEJB implements EntityBean {

    private EntityContext context = null;

    // CMP fields
    public abstract String getUserName();
    public abstract void setUserName(String userName);

    public abstract String getPassword();
    public abstract void setPassword(String password);

    // EJB create methods
    public String ejbCreate(String userName, String password) throws CreateException {

        // check the input data
        if(userName.length() > UserLocal.MAX_USERID_LENGTH) {
            throw new CreateException("User ID cant be more than " +
            UserLocal.MAX_USERID_LENGTH + " chars long");
        }
        if(password.length() > UserLocal.MAX_PASSWD_LENGTH) {
            throw new CreateException("Password cant be more than " +
            UserLocal.MAX_PASSWD_LENGTH + " chars long");
        }
        if( (userName.indexOf('%') != -1) ||
        (userName.indexOf('*') != -1) ) {
            throw new CreateException("User Id cannot " +
            "have '%' or '*' characters");
        }

        setUserName(userName);
        setPassword(password);
        return null;
    }

    public void ejbPostCreate(String userName, String password) throws CreateException {
    }

    // Business methods

    public boolean matchPassword(String password) {
        return password.equals(getPassword());
    }

    // Misc Method
    //=============
    public void setEntityContext(EntityContext c) {
        context = c;
    }
    public void unsetEntityContext() {
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
