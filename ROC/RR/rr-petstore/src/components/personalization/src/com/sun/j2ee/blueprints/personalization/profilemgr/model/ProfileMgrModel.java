/*
 * $Id: ProfileMgrModel.java,v 1.1.1.1 2002/10/03 21:17:36 candea Exp $
 * Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 * Copyright 2001 Sun Microsystems, Inc. Tous droits r�serv�s.
 */

package com.sun.j2ee.blueprints.personalization.profilemgr.model;

import com.sun.j2ee.blueprints.personalization.profilemgr.model.ExplicitInformation;
import com.sun.j2ee.blueprints.personalization.profilemgr.dao.ProfileMgrDAOFactory;
import com.sun.j2ee.blueprints.personalization.profilemgr.dao.ProfileMgrDAO;
import com.sun.j2ee.blueprints.personalization.profilemgr.exceptions.ProfileMgrAppException;
import com.sun.j2ee.blueprints.personalization.profilemgr.exceptions.ProfileMgrDAOSysException;

/**
 * ProfileMgrModel is a class which models personal preferences data for a
 * particular user.
 */
public class ProfileMgrModel implements java.io.Serializable {

    public String userId;
    public ExplicitInformation eInfo;

    /**
     * Class constructor specifying the user and preference information.
     * @param userId    a string which represents the id of this user
     * @param eInfo     an <code>ExplicitInformation</code> structure
     *                  containing the personal preferences of this user
     */
    public ProfileMgrModel(String userId, ExplicitInformation eInfo) {
        this.userId = userId;
        this.eInfo = eInfo;
    }

    /**
     * Class constructor with no arguments, used by the web tier.
     */
    public ProfileMgrModel() {}

    /**
     * Gets the string which represents the id of this user.
     * @return  the string representing this user id
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Gets the <code>ExplicitInformation</code> structure which contains the
     * personal preferences for this user.
     * @return  the <code>ExplicitInformation</code> object for this user
     */
    public ExplicitInformation getExplicitInformation() {
        return eInfo;
    }

   /**
    * Gets the banner preference for this user.
    * @param    favCategory     the favorite category for this user.
    * @return   the string representing the name of the banner.
    * @throws   <code>Exception</code> is thrown if the banner could not be
    *           found.
    */
   public String getBanner(String favCategory) throws ProfileMgrAppException {
       try {
            ProfileMgrDAO dao = ProfileMgrDAOFactory.getDAO();
            return dao.getBanner(favCategory);
        } catch (ProfileMgrDAOSysException se) {
            throw new ProfileMgrAppException(se.getMessage());
        }
    }

   /**
    * Performs a shallow copy of a <code>ProfileMgrModel</code> to another.
    * Used only by the web tier.
    * @param    other   the <code>ProfileMgrModel</code> to copy to another
    */
    public void copy(ProfileMgrModel other) {
        this.userId = other.userId;
        this.eInfo = other.eInfo;
    }
}
