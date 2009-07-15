
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: SequenceBmpEJB.java,v 1.1 2004/02/19 14:45:07 emrek Exp $
 *
 */
package com.sun.ecperf.util.sequenceent.ejb;


import javax.ejb.*;

import javax.naming.*;

import java.rmi.*;

import java.util.*;

import com.sun.ecperf.util.helper.*;


/**
 * Class SequenceBmpEJB controls the sequence block requests.
 * The BMP code provides the implementation for BMP deployments.
 *
 * @author Akara Sucharitakul
 * @version %I%, %G%
 */
public class SequenceBmpEJB extends SequenceCmpEJB {

    SequenceDAO dao;

    /**
     * Method setEntityContext
     *
     *
     * @param entityContext The context of this entity bean.
     *
     */
    public void setEntityContext(EntityContext entityContext) {

        super.setEntityContext(entityContext);

        dao = new SequenceDAO(this);
    }

    /**
     * Method ejbCreate
     *
     *
     * @param id          Sequence identifier
     * @param firstNumber Starting number
     * @param blockSize   Sequence cache block size
     *
     * @return The sequence identifier
     *
     * @throws CreateException Create error
     *
     */
    public String ejbCreate(String id, int firstNumber, int blockSize)
            throws CreateException {

        super.ejbCreate(id, firstNumber, blockSize);

        return dao.ejbCreate(id, firstNumber, blockSize);
    }

    /**
     * Method ejbRemove
     *
     *
     * @throws RemoveException Remove error
     *
     */
    public void ejbRemove() throws RemoveException {

        id = (String) entCtx.getPrimaryKey();

        super.ejbRemove();
        dao.ejbRemove();
    }

    /**
     * Method ejbLoad
     *
     *
     */
    public void ejbLoad() {}

    /**
     * Method ejbStore
     *
     *
     */
    public void ejbStore() {}

    /**
     * Method ejbFindByPrimaryKey
     *
     *
     * @param key The sequence indentifier.
     *
     * @return The sequence identifier.
     *
     * @throws FinderException Identifier does not identify a valid sequence.
     *
     */
    public String ejbFindByPrimaryKey(String key) throws FinderException {
        return dao.ejbFindByPrimaryKey(key);
    }

    /**
     * Method ejbFindAll
     *
     *
     * @return An emuneration of the valid identifiers.
     *
     * @throws FinderException Error finding identifiers
     *
     */
    public Enumeration ejbFindAll() throws FinderException {
        return dao.ejbFindAll();
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
     * Method getNextNumber gets the next number
     * without advancing the block.
     *
     * @return The next sequence number.
     *
     */
    public int getNextNumber() {
        return dao.getNextNumber();
    }

    /**
     * Method setBlockSize
     *
     *
     * @param blockSize The new sequence cache block size.
     *
     */
    public void setBlockSize(int blockSize) {
        super.setBlockSize(blockSize);
        dao.setBlockSize(blockSize);
    }

    /**
     * Method nextSequenceBlock obtains the next sequence block
     * and advances the sequence number in the database.
     *
     * @return The next sequence block.
     *
     */
    public SequenceBlock nextSequenceBlock() {

        return dao.nextSequenceBlock();
    }
}

