
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.

 *
 * $Id: SequenceEntHome.java,v 1.2 2003/03/22 04:55:01 emrek Exp $
 *
 */
package com.sun.ecperf.util.sequenceent.ejb;


import javax.ejb.*;

import java.rmi.*;


/**
 * This is the home interface of the Sequence entity bean in the Customer
 * domain.
 */
public interface SequenceEntHome extends EJBHome {

    SequenceEnt create(String id, int firstNumber, int blockSize)
        throws RemoteException, CreateException;

    SequenceEnt findByPrimaryKey(String id)
        throws RemoteException, FinderException;

    java.util.Enumeration findAll() throws RemoteException, FinderException;
}

