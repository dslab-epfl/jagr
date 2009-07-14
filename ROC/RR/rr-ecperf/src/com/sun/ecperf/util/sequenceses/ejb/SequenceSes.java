
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.

 *
 * $Id:
 *
 */
package com.sun.ecperf.util.sequenceses.ejb;


import javax.ejb.*;

import java.rmi.*;


/**
 * This is the interface of the SequenceSessionBean, which is
 * a key cache for the Sequence entity bean.
 */
public interface SequenceSes extends EJBObject {

    /**
     * newSequence: create a new sequence. This method is not called
     * by the benchmark.
     * @param id   - Id of sequence
     * @param firstKey  - First valid key
     * @param blockSize  - Block size of keys to be cached
     * @exception RemoteException - system or network error occurred
     * @exception CreateException - creation of the new sequence fails
     */
    public void newSequence(String id, int firstKey, int blockSize)
        throws RemoteException, CreateException;

    /**
     * nextKey: provides the next unique key from a sequence id.
     * keys are not guaranteed to be issued in order and without gaps.
     * The only guarantee is that the key is unique in this sequence id.
     * @param id  - Id of the sequence
     * @return  - an available integer key
     * @exception RemoteException - system or network error occurred
     * @exception FinderException - the sequence id is invalid
     */
    public int nextKey(String id) throws RemoteException, FinderException;

    /**
     * removeSequence: removes a sequence. This method is not called
     * by the benchmark.
     * @param id  - Id of the sequence to be removed
     * @exception RemoteException - system or network error occurred
     * @exception FinderException - the sequence id is invalid
     * @exception RemoveException - remove error
     */
    public void removeSequence(String id)
        throws RemoteException, FinderException, RemoveException;
}

