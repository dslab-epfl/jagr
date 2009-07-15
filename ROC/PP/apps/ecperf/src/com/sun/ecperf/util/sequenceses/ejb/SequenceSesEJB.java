
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: SequenceSesEJB.java,v 1.2 2003/03/22 04:55:02 emrek Exp $
 *
 */
package com.sun.ecperf.util.sequenceses.ejb;


import javax.ejb.*;

import javax.naming.*;

import java.rmi.*;

import java.util.*;

import com.sun.ecperf.common.*;
import com.sun.ecperf.util.helper.*;
import com.sun.ecperf.util.sequenceent.ejb.*;


/**
 * The SequenceSessionBean is a wrapper for the Sequence and Sequenceline entity beans.
 * The session bean is what is accessed by the SequenceEntry application. This
 * bean also implements the getCustStatus method to retrieve all common
 * belonging to a particular customer.
 */
public class SequenceSesEJB implements SessionBean {

    private SessionContext  sessionContext;
    private SequenceEntHome sequenceHome;
    private HashMap         sequences;
    protected Debug         debug;
    protected boolean       debugging;

    /**
     * Method ejbCreate
     *
     *
     */
    public void ejbCreate() {
        if (debugging)
            debug.println(3, "ejbCreate ");
    }

    /**
     * newSequence: create a new sequence. This method is not called
     * by the benchmark.
     * @param id   - Id of sequence
     * @param firstKey  - First valid key
     * @param blockSize  - Block size of keys to be cached
     * @exception CreateException - creation of the new sequence fails
     */
    public void newSequence(String id, int firstKey, int blockSize)
            throws CreateException {

        if (debugging)
            debug.println(3, "newSequence ");

        try {
            sequenceHome.create(id, firstKey, blockSize);
        } catch (RemoteException e) {
            debug.printStackTrace(e);

            throw new EJBException(e);
        }
    }

    /**
     * nextKey: provides the next unique key from a sequence id.
     * keys are not guaranteed to be issued in order and without gaps.
     * The only guarantee is that the key is unique in this sequence id.
     * @param id  - Id of the sequence
     * @return  - an available integer key
     * @exception FinderException - the sequence id is invalid
     */
    public int nextKey(String id) throws FinderException {

        if (debugging)
            debug.println(3, "Getting next key for " + id);

        SequenceEnt   sequence = null;
        SequenceBlock block    = (SequenceBlock) sequences.get(id);

        // If we do not have a reference at all, we do a find.
        if (block == null) {
            try {
                if (debugging)
                    debug.println(4, "Finding SequenceEnt");

                sequence = sequenceHome.findByPrimaryKey(id);
            } catch (RemoteException e) {
                debug.printStackTrace(e);

                throw new EJBException(e);
            }

            // Otherwise we might have run out of our block.
            // Here we use our saved reference instead of finding.
        } else if (block.nextNumber >= block.ceiling) {
            sequence = block.sequence;
        }

        // If sequence is set, it means we have to get a new block
        if (sequence != null) {

	    int retries = 10;

            /* In optimistic concurrency controlled servers
             * with CMP, there's alway a chance the tx will
             * get rolled back which will show up as a
             * RemoteException. In our BMP implementation
             * of SequenceEnt, we enforce pessimistic
             * concurrency control. So it should not be
             * retrying at all.
             */

            for (int i = 0; i < retries; i++) {
                try {
                    if (debugging)
                        debug.println(4, "Fetching nextSequenceBlock");

                    block = sequence.nextSequenceBlock();

                    break;
                } catch (RemoteException e) {
                    if (i == retries - 1) {
                        debug.printStackTrace(e);
                        throw new EJBException(e);
                    } else {
                        if (debugging)
                            debug.println(4, "Retrying nextSequenceBlock..."
                                          + i);
                    }
                }
            }

            // Save the reference and the block itself
            block.sequence = sequence;

            sequences.put(id, block);
        }

        if (debugging)
            debug.println(5, toString() + " next " + id + " key: "
                          + block.nextNumber);

        return block.nextNumber++;
    }

    /**
     * removeSequence: removes a sequence. This method is not called
     * by the benchmark.
     * @param id  - Id of the sequence to be removed
     * @exception FinderException - the sequence id is invalid
     * @exception RemoveException - remove error
     */
    public void removeSequence(String id)
            throws FinderException, RemoveException {

        try {
            sequenceHome.findByPrimaryKey(id).remove();
        } catch (RemoteException e) {
            debug.printStackTrace(e);

            throw new EJBException(e);
        }
    }

    /**
     * Method ejbRemove
     *
     *
     */
    public void ejbRemove() {}

    /**
     * Method ejbActivate
     *
     *
     */
    public void ejbActivate() {}

    /**
     * Method ejbPassivate
     *
     *
     */
    public void ejbPassivate() {}

    /**
     * Method setSessionContext
     *
     *
     * @param sessionContext
     *
     */
    public void setSessionContext(SessionContext sessionContext) {

        this.sessionContext = sessionContext;

        InitialContext initCtx = null;

        try {
            initCtx = new InitialContext();
        } catch (NamingException ne) {
            ne.printStackTrace(System.err);

            throw new EJBException(ne);
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
        } catch (NamingException e) {
            System.out.println("SequenceSesEJB: Error getting debuglevel "
                               + "property! Turning off debug messages");

            debug = new Debug();
        }

        try {

            // the homes are available via EJB links
            sequenceHome =
                (SequenceEntHome) javax.rmi.PortableRemoteObject.narrow(
                    initCtx.lookup("java:comp/env/ejb/SequenceEnt"),
                    SequenceEntHome.class);
        } catch (NamingException e) {
            debug.printStackTrace(e);

            throw new EJBException(e);
        }

        sequences = new HashMap();
    }
}

