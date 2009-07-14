
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: SequenceCmpEJB.java,v 1.1.1.1 2003/04/25 08:04:22 mdelgado Exp $
 *
 *
 */
package com.sun.ecperf.util.sequenceent.ejb;


import javax.ejb.*;

import javax.naming.*;

import java.rmi.*;

import java.util.*;

import java.io.*;

import com.sun.ecperf.common.*;
import com.sun.ecperf.util.helper.*;


/**
 * SequenceCmpEJB controls the sequence block requests.
 *
 * @author Akara Sucharitakul
 * @version %I%, %G%
 */
public class SequenceCmpEJB implements EntityBean {

    public String            id;
    public int               nextNumber;
    public int               blockSize;
    protected EntityContext  entCtx;
    protected InitialContext initCtx;
    protected Debug          debug;
    protected boolean        debugging;

    /**
     * Method ejbCreate
     *
     * @param id          Sequence identifier
     * @param firstNumber Starting number
     * @param blockSize   Sequence cache block size
     *
     * @return The sequence identifier
     *
     * @throws CreateException Create error
     */
    public String ejbCreate(String id, int firstNumber, int blockSize)
            throws CreateException {

        if (debugging)
            debug.println(3, "ejbCreate ");

        this.id         = id;
        this.nextNumber = firstNumber;
        this.blockSize  = blockSize;

        return id;
    }

    /**
     * Method ejbPostCreate
     *
     *
     * @param id          Sequence identifier
     * @param firstNumber First number
     * @param blockSize   Sequence cache block size
     *
     */
    public void ejbPostCreate(String id, int firstNumber, int blockSize) {}

    /**
     * Method ejbRemove
     *
     *
     * @throws RemoveException Remove error
     *
     */
    public void ejbRemove() throws RemoveException {
        if (debugging)
            debug.println(3, "ejbRemove");
    }

    /**
     * Method getId
     *
     *
     * @return The sequence identifier
     *
     */
    public String getId() {
        return id;
    }

    /**
     * Method getNextNumber gets the next number
     * without advancing the block.
     *
     * @return The next sequence number.
     *
     */
    public int getNextNumber() {
        return nextNumber;
    }

    /**
     * Method getBlockSize
     *
     *
     * @return Sequence cache block size.
     *
     */
    public int getBlockSize() {
        return blockSize;
    }

    /**
     * Method setBlockSize
     *
     *
     * @param blockSize The new sequence cache block size.
     *
     */
    public void setBlockSize(int blockSize) {

        if (blockSize > 0) {
            this.blockSize = blockSize;
        } else {
            throw new IllegalArgumentException(
                "Block size must be greater than 0");
        }
    }

    /**
     * Method nextSequenceBlock obtains the next sequence block
     * and advances the sequence number in the database.
     *
     * @return The next sequence block.
     *
     */
    public SequenceBlock nextSequenceBlock() {

        if (debugging)
            debug.println(3, "nextSequenceBlock");

        SequenceBlock block = new SequenceBlock();

        block.nextNumber = nextNumber;
        nextNumber       += blockSize;
        block.ceiling    = nextNumber;

        return block;
    }

    /**
     * Method ejbActivate
     *
     *
     */
    public void ejbActivate() {
        if (debugging)
            debug.println(3, "ejbActivate ");
    }

    /**
     * Method ejbPassivate
     *
     *
     */
    public void ejbPassivate() {
        if (debugging)
            debug.println(3, "ejbPassivate ");
    }

    /**
     * Method ejbLoad
     *
     *
     */
    public void ejbLoad() {
        if (debugging)
            debug.println(3, "ejbLoad ");
    }

    /**
     * Method ejbStore
     *
     *
     */
    public void ejbStore() {
        if (debugging)
            debug.println(3, "ejbStore ");
    }

    /**
     * Method setEntityContext
     *
     *
     * @param entCtx The context of this entity bean.
     *
     */
    public void setEntityContext(EntityContext entCtx) {

        this.entCtx = entCtx;

        try {
            initCtx = new InitialContext();
        } catch (NamingException e) {
            System.out.println("RuleCmpEJB: Cannot create InitialContext.");
            e.printStackTrace(System.err);

            throw new EJBException(e);
        }

        try {
            int debugLevel =
                ((Integer) initCtx.lookup("java:comp/env/debuglevel"))
                    .intValue();

            if (debugLevel > 0) {
                debug = new DebugPrint(debugLevel, this);
                debugging = true;
            } else {
                debug = new Debug();
                debugging = false;
            }
        } catch (NamingException ne) {
            System.out.println("RuleCmpEJB: debuglevel Property not set."
                               + "Turning off debug messages");

            debug = new Debug();
        }
    }

    /**
     * Method unsetEntityContext
     *
     *
     */
    public void unsetEntityContext() {
        if (debugging)
            debug.println(3, "unsetEntityContext ");
    }

    /****
    public String ejbFindByPrimaryKey(String key)
        throws FinderException;
    ****/
}

