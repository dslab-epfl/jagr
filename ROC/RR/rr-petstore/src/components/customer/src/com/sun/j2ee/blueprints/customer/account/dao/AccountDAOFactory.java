/*
 * $Id: AccountDAOFactory.java,v 1.1.1.1 2002/10/03 21:17:36 candea Exp $
 * Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 * Copyright 2001 Sun Microsystems, Inc. Tous droits r�serv�s.
 */

package com.sun.j2ee.blueprints.customer.account.dao;

import javax.naming.NamingException;
import javax.naming.InitialContext;

import com.sun.j2ee.blueprints.customer.util.JNDINames;
import com.sun.j2ee.blueprints.customer.account.exceptions.AccountDAOSysException;

import com.sun.j2ee.blueprints.util.tracer.Debug;

public class AccountDAOFactory {

    /**
     * This method instantiates a particular subclass implementing
     * the DAO methods based on the information obtained from the
     * deployment descriptor
     */
    public static AccountDAO getDAO() throws AccountDAOSysException {

        AccountDAO acctDao = null;
        try {
            InitialContext ic = new InitialContext();
            String className = (String) ic.lookup(JNDINames.ACCOUNT_DAO_CLASS);
            acctDao = (AccountDAO) Class.forName(className).newInstance();
        } catch (NamingException ne) {
            throw new AccountDAOSysException("AccountDAOFactory.getDAO:  NamingException while getting DAO type : \n" + ne.getMessage());
        } catch (Exception se) {
            throw new AccountDAOSysException("AccountDAOFactory.getDAO:  Exception while getting DAO type : \n" + se.getMessage());
        }
        return acctDao;
    }
}
