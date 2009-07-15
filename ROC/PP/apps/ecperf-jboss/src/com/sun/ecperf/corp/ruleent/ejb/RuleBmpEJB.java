/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: RuleBmpEJB.java,v 1.1 2004/02/19 14:45:06 emrek Exp $
 *
 * $Mod: RulBmpEJB.java,v 1.5 2001/04/14 08:30:00 hogstrom - Modified
 *       bean to support single row entities.
 */
package com.sun.ecperf.corp.ruleent.ejb;


import javax.ejb.*;

import javax.naming.*;

import java.rmi.*;

import java.util.*;


/**
 * Class RuleBmpEJB
 *
 *
 * @author
 * @version %I%, %G%
 */
public class RuleBmpEJB extends RuleCmpEJB {

    RuleDAO dao;

    /**
     * Method setEntityContext
     *
     *
     * @param entityContext
     *
     */
    public void setEntityContext(EntityContext entityContext) {

        super.setEntityContext(entityContext);

        dao = new RuleDAO(this);
    }

    /**
     * Method ejbCreate
     *
     *
     * @param id
     * @param rules
     *
     * @return
     *
     * @throws CreateException
     *
     */
    public String ejbCreate(String id, String rules)
            throws CreateException {

        super.ejbCreate(id, rules);
        String ret = dao.ejbCreate(id, rules);
        return ret;
    }

    /**
     * Method ejbRemove
     *
     *
     * @throws RemoveException
     *
     */
    public void ejbRemove() throws RemoveException {
        super.ejbRemove();
        dao.ejbRemove();
    }

    /**
     * Method ejbLoad
     *
     *
     */
    public void ejbLoad() {
        dao.ejbLoad();
        super.ejbLoad();
    }

   

    /**
     * Method ejbStore
     *
     *
     */
    public void ejbStore() {
        super.ejbStore();
        dao.ejbStore();
    }

    /**
     * Method ejbActivate
     *
     *
     */
    public void ejbActivate() {
        id = (String) entCtx.getPrimaryKey();
        super.ejbActivate();
    }

    /**
     * Method ejbFindByPrimaryKey
     *
     *
     * @param key
     *
     * @return
     *
     * @throws FinderException
     *
     */
    public String ejbFindByPrimaryKey(String key) throws FinderException {
        return dao.ejbFindByPrimaryKey(key);
    }

    /**
     * Method ejbFindAll
     *
     *
     * @return
     *
     * @throws FinderException
     *
     */
    public Enumeration ejbFindAll() throws FinderException {
        return dao.ejbFindAll();
    }
}
