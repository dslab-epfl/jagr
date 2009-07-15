
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: DiscountBmpEJB.java,v 1.2 2003/03/22 04:55:01 emrek Exp $
 *
 */
package com.sun.ecperf.corp.discountent.ejb;


import javax.ejb.*;

import javax.naming.*;

import java.rmi.*;

import java.util.*;


/**
 * Class DiscountBmpEJB
 *
 *
 * @author
 * @version %I%, %G%
 */
public class DiscountBmpEJB extends DiscountCmpEJB {

    DiscountDAO dao;

    /**
     * Method setEntityContext
     *
     *
     * @param entityContext
     *
     */
    public void setEntityContext(EntityContext entityContext) {

        super.setEntityContext(entityContext);

        dao = new DiscountDAO(this);
    }

    /**
     * Method ejbCreate
     *
     *
     * @param id
     * @param percent
     *
     * @return
     *
     * @throws CreateException
     *
     */
    public String ejbCreate(String id, double percent)
            throws CreateException {

        super.ejbCreate(id, percent);

        return dao.ejbCreate(id, percent);
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

    public void ejbActivate() {
        id = (String) entCtx.getPrimaryKey();
        super.ejbActivate();

    }
}
