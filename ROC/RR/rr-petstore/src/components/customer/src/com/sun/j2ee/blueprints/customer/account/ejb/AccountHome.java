/*
 * $Id: AccountHome.java,v 1.1.1.1 2002/10/03 21:17:36 candea Exp $
 * Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 * Copyright 2001 Sun Microsystems, Inc. Tous droits réservés.
 */

package com.sun.j2ee.blueprints.customer.account.ejb;


import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.FinderException;
import javax.ejb.DuplicateKeyException;
import javax.ejb.EJBHome;

import com.sun.j2ee.blueprints.customer.util.ContactInformation;
import com.sun.j2ee.blueprints.customer.account.exceptions.AccountAppLongIdException;
import com.sun.j2ee.blueprints.customer.account.exceptions.AccountAppInvalidCharException;
import com.sun.j2ee.blueprints.customer.account.exceptions.AccountAppException;

/**
 * The home interface of the Account EJB.
 */

public interface AccountHome extends EJBHome {

    public Account create(String userId, String password,
                          String status, ContactInformation info)
        throws RemoteException,DuplicateKeyException,CreateException,
               AccountAppLongIdException, AccountAppInvalidCharException,
               AccountAppException;

    public Account findByPrimaryKey (String userId)
        throws RemoteException, FinderException;
}
