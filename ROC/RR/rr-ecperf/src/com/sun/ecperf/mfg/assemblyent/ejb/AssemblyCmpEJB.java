
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * akmits@eng.sun.com 03/16/2000
 *
 *  $Id: AssemblyCmpEJB.java,v 1.1.1.1 2003/04/25 08:04:22 mdelgado Exp $
 */
package com.sun.ecperf.mfg.assemblyent.ejb;


import javax.ejb.*;

import javax.naming.*;

import java.rmi.*;

import java.util.*;

import com.sun.ecperf.mfg.componentent.ejb.*;
import com.sun.ecperf.mfg.boment.ejb.*;
import com.sun.ecperf.common.*;


/**
 * This class implements the Assembly entity bean. It is
 * responsible for performing all the transactions on the Assemblys
 *
 * @author Ajay Mitall
 *
 */
public class AssemblyCmpEJB extends ComponentCmpEJB {

    private BomEntHome bomEntHome;

    /**
     * Method setEntityContext
     *
     *
     * @param entityContext
     *
     */
    public void setEntityContext(EntityContext entityContext) {

        super.setEntityContext(entityContext);
	if (debugging)
	    debug.println(3, "Asmbl:setEntityContext");

        // we need to get bomEntHome home object
        try {
            Context context = new InitialContext();

            bomEntHome = (BomEntHome) javax.rmi.PortableRemoteObject
                .narrow(context
                    .lookup("java:comp/env/ejb/BomEnt"), BomEntHome.class);
        } catch (NamingException e) {
            throw new EJBException("Failure looking up home " + e);
        }
    }

    /**
      * Method to get BOMs for this type of the Assembly
     *
     * @return
      * @exception RemoteException if there is a system failure
      */
    public java.util.Enumeration getBoms() throws RemoteException {

	if (debugging)
	    debug.println(3, "getBoms for assembly:" + id);

        try {
            java.util.Enumeration enum =
                (java.util.Enumeration) bomEntHome.findBomForAssembly(id);

            return enum;
        } catch (FinderException e) {
            throw new EJBException(e);
        }
    }
}

