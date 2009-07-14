/*
 * $Id: ProfileMgr.java,v 1.1.1.1 2002/10/03 21:17:36 candea Exp $
 * Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 * Copyright 2001 Sun Microsystems, Inc. Tous droits r�serv�s.
 */

package com.sun.j2ee.blueprints.personalization.profilemgr.ejb;

import java.rmi.RemoteException;
import javax.ejb.EJBObject;

import com.sun.j2ee.blueprints.personalization.profilemgr.model.ProfileMgrModel;
import com.sun.j2ee.blueprints.personalization.profilemgr.model.ExplicitInformation;


/**
 * ProfileMgr is an interface which provides methods to view and update
 * personal preferences for a particular user.
*/

public interface ProfileMgr extends EJBObject {

    /**
     * Returns  the personal preferences model for this user.
     * @returns the <code>ProfileMgrModel</code> personal prefrences data
     *          for this user
     * @throws  <code>RemoteException</code> is thrown if the personal
     *          preferences data could not be found for this user
     */
    public ProfileMgrModel getDetails() throws RemoteException;

    /**
     * Updates the <code>ExplicitInformation</code> personal preferences data
     * for this user.
     * @param   eInfo   an <code>ExplicitInformation</code> structure
     *          containing the new personal preferences of this user
     * @throws  <code>RemoteException</code> is thrown if the personal
     *          preferences data could not be updated for this user
     */
    public void updateExplicitInformation(ExplicitInformation eInfo)
        throws RemoteException;
}
