
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.

 *
 * $Id: SequenceEnt.java,v 1.2 2003/03/22 04:55:01 emrek Exp $
 *
 */
package com.sun.ecperf.util.sequenceent.ejb;


import javax.ejb.*;

import java.rmi.*;

import com.sun.ecperf.util.helper.*;


/**
 * This is the public interface of the Sequence entity bean. It provides
 * access to the various fields of an item.
 */
public interface SequenceEnt extends EJBObject {

    /**
     * Method getId
     *
     *
     * @return
     *
     * @throws RemoteException
     *
     */
    public String getId() throws RemoteException;

    /**
     * Method getNextNumber
     *
     *
     * @return
     *
     * @throws RemoteException
     *
     */
    public int getNextNumber() throws RemoteException;

    /**
     * Method getBlockSize
     *
     *
     * @return
     *
     * @throws RemoteException
     *
     */
    public int getBlockSize() throws RemoteException;

    /**
     * Method setBlockSize
     *
     *
     * @param blockSize
     *
     * @throws RemoteException
     *
     */
    public void setBlockSize(int blockSize) throws RemoteException;

    /**
     * Method nextSequenceBlock
     *
     *
     * @return
     *
     * @throws RemoteException
     *
     */
    public SequenceBlock nextSequenceBlock() throws RemoteException;
}

