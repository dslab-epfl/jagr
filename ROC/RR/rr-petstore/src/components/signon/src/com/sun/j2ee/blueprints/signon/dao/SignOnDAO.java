/*
 * $Id: SignOnDAO.java,v 1.1.1.1 2002/10/03 21:17:36 candea Exp $
 * Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 * Copyright 2001 Sun Microsystems, Inc. Tous droits r�serv�s.
 */

package com.sun.j2ee.blueprints.signon.dao;

import com.sun.j2ee.blueprints.signon.model.MutableSignOnModel;
import com.sun.j2ee.blueprints.signon.exceptions.SignOnDAOSysException;
import com.sun.j2ee.blueprints.signon.exceptions.SignOnDAOAppException;
import com.sun.j2ee.blueprints.signon.exceptions.SignOnDAODBUpdateException;
import com.sun.j2ee.blueprints.signon.exceptions.SignOnDAODupKeyException;
import com.sun.j2ee.blueprints.signon.exceptions.SignOnDAOFinderException;

/**
 * This encapsulates all the JDBC calls made by the SignOnEJB.
 * This is an interface and the actual logic of inserting,
 * fetching, updating, or deleting  the data in
 * relational database is done SignOnDAOImpl.java
 */
public interface SignOnDAO {

    public void create(MutableSignOnModel details) throws
                                 SignOnDAOSysException,
                                 SignOnDAODBUpdateException,
                                 SignOnDAODupKeyException,
                                 SignOnDAOAppException;
    public MutableSignOnModel load (String id) throws SignOnDAOSysException,
                                        SignOnDAOFinderException;
    public void store(MutableSignOnModel user) throws
                               SignOnDAODBUpdateException,
                               SignOnDAOAppException,
                               SignOnDAOSysException;
    public void remove(String id) throws SignOnDAOSysException,
                                SignOnDAODBUpdateException;

    public String findByPrimaryKey(String key) throws SignOnDAOSysException,
                                            SignOnDAOFinderException;
}
