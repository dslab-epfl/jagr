
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
 * This is the Home interface of the SequenceSession bean.
 */
public interface SequenceSesHome extends EJBHome {
    SequenceSes create() throws RemoteException, CreateException;
}

