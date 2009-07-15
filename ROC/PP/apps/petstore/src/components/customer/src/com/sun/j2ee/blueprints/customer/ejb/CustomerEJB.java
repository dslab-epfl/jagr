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

package com.sun.j2ee.blueprints.customer.ejb;

import javax.ejb.EntityContext;
import javax.ejb.RemoveException;
import javax.ejb.CreateException;
import javax.naming.NamingException;
import javax.naming.InitialContext;

import com.sun.j2ee.blueprints.customer.account.ejb.AccountLocal;
import com.sun.j2ee.blueprints.customer.account.ejb.AccountLocalHome;
import com.sun.j2ee.blueprints.customer.profile.ejb.ProfileLocal;
import com.sun.j2ee.blueprints.customer.profile.ejb.ProfileLocalHome;

public abstract class CustomerEJB implements javax.ejb.EntityBean {

    private EntityContext context = null;

    // getters and setters for CMP fields
    //====================================
    public abstract String getUserId();
    public abstract void setUserId(String userId);

    // CMR Fields
    public abstract AccountLocal getAccount();
    public abstract void setAccount(AccountLocal account);

    public abstract ProfileLocal getProfile();
    public abstract void setProfile(ProfileLocal profile);

    // EJB create method
    //===================
    public String ejbCreate(String userId) throws CreateException {
        setUserId(userId);
        return null;
    }

    public void ejbPostCreate(String userId) throws CreateException {
        try {
        InitialContext ic = new InitialContext();
        AccountLocalHome alh = (AccountLocalHome) ic.lookup("java:comp/env/ejb/Account");
        AccountLocal account = alh.create(AccountLocalHome.Active);
        setAccount(account);

        ProfileLocalHome plh = (ProfileLocalHome) ic.lookup("java:comp/env/ejb/Profile");
        ProfileLocal profile = plh.create(ProfileLocalHome.DefaultPreferredLanguage,
        ProfileLocalHome.DefaultFavoriteCategory, ProfileLocalHome.DefaultMyListPreference,
        ProfileLocalHome.DefaultBannerPreference);
        setProfile(profile);
        } catch (NamingException ne) {
            throw new CreateException ("could not lookup ejb. Exception is " + ne.getMessage());
        }
    }

    // Misc Method
    //=============
    public void setEntityContext(EntityContext c) {
        context = c;
    }
    public void unsetEntityContext() { }
    public void ejbRemove() throws RemoveException { }
    public void ejbActivate() { }
    public void ejbPassivate() { }
    public void ejbStore() { }
    public void ejbLoad() { }
}
