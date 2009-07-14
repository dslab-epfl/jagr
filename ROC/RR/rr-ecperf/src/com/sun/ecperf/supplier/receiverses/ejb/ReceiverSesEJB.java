
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.

 * $Id: ReceiverSesEJB.java,v 1.1.1.1 2003/04/25 08:04:22 mdelgado Exp $
 *
 */
package com.sun.ecperf.supplier.receiverses.ejb;


//Import statements
import javax.ejb.*;

import java.rmi.*;

import javax.naming.*;

import com.sun.ecperf.mfg.receiveses.ejb.*;
import com.sun.ecperf.common.*;
import com.sun.ecperf.supplier.poent.ejb.*;
import com.sun.ecperf.supplier.scomponentent.ejb.*;
import com.sun.ecperf.supplier.helper.*;


/**
 * Implementation of the ReceiverSes Stateless Session Bean.
 *
 * @author Damian Guy
 */
public class ReceiverSesEJB implements SessionBean {

    private SessionContext    sessionContext;
    private POEntHome         poHome;
    private SComponentEntHome componentHome;
    private ReceiveSesHome    receiveHome;
    private Debug             debug;
    protected boolean         debugging;

    /**
     * deliverPO - indicate the part of a PO has been delivered.
     *
     * @param info
     * @exception ECperfException an application exception occurred.
     */
    public void deliverPO(DeliveryInfo info) throws ECperfException {

        if (debugging)
            debug.println(3, "deliverPO");

        try {

            /* We need to call inventory in mfg first so that
             * containers that lock the entity based on sequence
             * of first access to transactional method will not
             * result in a deadlock
             */
            ReceiveSes re = receiveHome.create();
            re.addInventory(info.partID, info.qty);
            re.remove();

            SComponentEnt comp = componentHome.findByPrimaryKey(info.partID);
            comp.deliveredQuantity(info.qty);

            // POs can get committed after the delivery attempts.
            // We allow delivery retries in such cases.
            // We use a separate try clause just to ensure this is only
            // allowed for POs and not for other finders.
            try {
                POEnt po = poHome.findByPrimaryKey(new Integer(info.poId));
                po.poLineDelivered(info.lineNumber);
            } catch (ObjectNotFoundException e) {
                sessionContext.setRollbackOnly();
                throw new NotReadyException("PO " + info.poId + " not found");
            }
        } catch (CreateException e) {
            debug.printStackTrace(e);

            throw new EJBException(e);
        } catch (RemoveException e) {
            debug.printStackTrace(e);

            throw new EJBException(e);
        } catch (FinderException e) {
            debug.printStackTrace(e);

            throw new EJBException(e);
        } catch (RemoteException e) {
            debug.printStackTrace(e);

            throw new EJBException(e);
        }
    }

    /**
     * Method ejbCreate
     *
     *
     */
    public void ejbCreate() {}

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

        InitialContext context = null;

        try {
            context = new InitialContext();

            int debugLevel =
                ((Integer) context.lookup("java:comp/env/debuglevel"))
                    .intValue();

            if (debugLevel > 0) {
                debug = new DebugPrint(debugLevel, this);
                debugging = true;
            } else {
                debug = new Debug();
                debugging = false;
            }
        } catch (NamingException ne) {
            debug = new Debug();
        }

        try {
            context       = new InitialContext();
            componentHome =
                (SComponentEntHome) javax.rmi.PortableRemoteObject.narrow(
                    context.lookup("java:comp/env/ejb/SComponent"),
                    SComponentEntHome.class);
            poHome        = (POEntHome) javax.rmi.PortableRemoteObject
                .narrow(context
                    .lookup("java:comp/env/ejb/POEnt"), POEntHome.class);
            receiveHome   =
                (ReceiveSesHome) javax.rmi.PortableRemoteObject
                    .narrow(context.lookup("java:comp/env/ejb/ReceiveSes"),
                    ReceiveSesHome.class);
        } catch (NamingException ne) {
            throw new EJBException("Naming error: " + ne.getMessage());
        }
    }
}

