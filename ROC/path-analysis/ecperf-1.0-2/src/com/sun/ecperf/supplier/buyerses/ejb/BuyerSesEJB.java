
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 *
 */
package com.sun.ecperf.supplier.buyerses.ejb;


// Import statements
import javax.ejb.*;

import javax.naming.*;

import java.rmi.*;

import java.sql.*;

import com.sun.ecperf.common.*;

import java.util.*;

import java.io.*;

import java.net.*;

import com.sun.ecperf.supplier.scomponentent.ejb.*;
import com.sun.ecperf.supplier.poent.ejb.*;
import com.sun.ecperf.supplier.supplierent.ejb.*;
import com.sun.ecperf.supplier.helper.*;


/**
 * This class is the implementation of the Buyer Stateful
 * Session Bean.
 *
 *
 * @author Damian Guy
 */
public class BuyerSesEJB implements SessionBean {

    private Vector            required;
    private SessionContext    sessionContext;
    private SComponentEntHome componentHome;
    private POEntHome         poHome;
    private int               siteID;
    private SupplierEntHome   supplierHome;
    protected Debug           debug;
    protected boolean         debugging;
    private SupplierEnt       chosen = null;
    private String            servlet;
    private String            servletHost;
    private int               servletPort;
    private boolean           isSecure;
    private Vector            compIDs;

    /**
     * add: add a component to the collection of components to be ordered.
     * First check if the component exists, and if there is an existing PO.
     * @param componentID - the Id of the component to be ordered.
     * @param quantityRequired - the qty that is required.
     *
     * @throws RemoteException
     */
    public void add(String componentID, int quantityRequired)
            throws RemoteException {

        try {
            SComponentEnt component =
                componentHome.findByPrimaryKey(componentID);

            // See if there is an outstanding order
            if (component.checkForPO(quantityRequired) == false) {
                compIDs.addElement(componentID);
                required
                    .addElement(new ComponentQuantities(component,
                                                        quantityRequired));
            } else {
                component.updateDemand(quantityRequired);
            }
        } catch (FinderException e) {
            if (debugging)
                debug.println(1, "Finder Exception");

            throw new EJBException(e);
        }
    }

    /**
     * purchase: chose the supplier that can supply all parts that
     * are needed withn the required leadTime and for the cheapest price.
     *
     */
    public void purchase()
            throws FinderException, CreateException, ECperfException {

        if (required.size() == 0) {
            if (debugging)
                debug.println(2, "No parts to order");

            return;
        }

        Enumeration suppliers;

        try {
            suppliers = supplierHome.findAll();
        } catch (FinderException fe) {
            throw new EJBException(fe);
        } catch (RemoteException re) {
            throw new EJBException(re);
        }

        if (!suppliers.hasMoreElements()) {

            /** there are not any suppliers loaded into the DB **/
            throw new DataIntegrityException(
                "There are not any suppliers in Database");
        }

        /** For each supplier, see if they can deliver all parts within leadTime.
         *  If they can deliver parts, determine if they are the cheapest.
         *  Choose the cheapest as the supplier.
         */
        double     minPrice          = -0.5;
        PartSpec[] cheapestSuppliers = new PartSpec[required.size()];

        while (suppliers.hasMoreElements()) {
            double      totalPrice = 0.0;
            SupplierEnt supplier   =
                (SupplierEnt) javax.rmi.PortableRemoteObject.narrow(suppliers.nextElement(),
                    SupplierEnt.class);
            boolean     canSupply = true;
            int         i          = 0;

            while ( /*canSupply &&*/i < required.size()) {
                try {
                    SComponentEnt component =
                        ((ComponentQuantities) required.elementAt(i))
                            .component;
                    PartSpec      ps        =
                        supplier.getPartSpec((String) compIDs.elementAt(i));

                    if (ps.delDate > component.getLeadTime()) {
                        canSupply = false;
                    } else {
                        double price = ps.calculatePrice();

                        totalPrice += price;

                        /** check if cheapest supplier for this part*/
                        PartSpec cheapest = cheapestSuppliers[i];

                        if (cheapest != null) {
                            if (price < cheapest.calculatePrice()) {
                                cheapestSuppliers[i] = ps;
                            }
                        } else {
                            cheapestSuppliers[i] = ps;
                        }
                    }
                } catch (FinderException fe) {
                    canSupply = false;
                } catch (RemoteException re) {
                    throw new EJBException(re);
                }

                i++;
            }

            if (canSupply && ((minPrice < 0) || (totalPrice < minPrice))) {
                minPrice = totalPrice;
                chosen   = supplier;
            }
        }

        if (chosen != null) {
            createPO();
        } else {
            doIndividualPOs(cheapestSuppliers);
        }

        if (debugging)
            debug.println(3, "Order processing completed");
    }

    /**
     * doIndividualPOs: Creates and sends 1 PO per part. Added to take care
 * of the Case when a supplier cannot be found that can supply all parts
     * required.
     *
     * Damian.
     */
    private void doIndividualPOs(PartSpec[] cheapestSuppliers)
            throws CreateException, ECperfException {

        if (debugging)
            debug.println(3, "doIndividualPOs");

        for (int i = 0; i < cheapestSuppliers.length; i++) {
            try {
                ComponentOrder[] order = new ComponentOrder[1];

                order[0] = getOrderInfo(i, cheapestSuppliers[i]);

                POEnt purchOrd =
                    poHome.create(cheapestSuppliers[i].suppID.intValue(),
                                  siteID, order);

                sendPO(purchOrd);
            } catch (RemoteException e) {
                debug.printStackTrace(e);

                throw new EJBException(e);
            }
        }
    }

    /**
     * getOrderInfo: get information required to place an order for a component
     */
    private ComponentOrder getOrderInfo(int index, PartSpec ps)
            throws RemoteException {

        ComponentQuantities componentQuant =
            (ComponentQuantities) required.elementAt(index);
        SComponentEnt       component      = componentQuant.component;
        String              compID        =
            (String) compIDs.elementAt(index);
        int                 qtyToOrder   = 0;

        if (componentQuant.quantity <= ps.qty) {
            qtyToOrder = ps.qty;
        } else {
            qtyToOrder =
                (int) Math
                .ceil((double) componentQuant.quantity / (double) ps.qty)
                    * ps.qty;
        }

        double balance = (qtyToOrder / ps.qty) * (1 - ps.disc) * ps.price;

        component.updateQuantities(qtyToOrder, componentQuant.quantity);

        return new ComponentOrder(compID, qtyToOrder,
                                  component.getLeadTime(), balance);
    }

    /**
     * createPO: create the Purchase Order for required components.
     */
    private void createPO()
            throws FinderException, CreateException, ECperfException {

        ComponentOrder[] cq = new ComponentOrder[required.size()];

        if (debugging)
            debug.println(3, "createPO");

        try {
            for (int i = 0; i < cq.length; i++) {

                /*******
                 *              ComponentQuantities componentQuant = (ComponentQuantities)
             *                                  required.elementAt(i);

                        SComponentEnt component = componentQuant.component;

                        String compID = component.getID();

                        PartSpec ps = chosen.getPartSpec(compID);
                        int qtyToOrder = 0;

                        if(componentQuant.quantity <= ps.qty)
                                qtyToOrder = ps.qty;
                        else
                                qtyToOrder = (int) Math.ceil((double)componentQuant.quantity/
                                                        (double)ps.qty) * ps.qty;

                        double balance = (qtyToOrder / ps.qty) * (1 - ps.disc) * ps.price;
                        cq[i] = new ComponentOrder(compID,
                                        qtyToOrder, component.getLeadTime(), balance);
                 *
                 *
                 *******/
                String compID = (String) compIDs.elementAt(i);

                cq[i] = getOrderInfo(i, chosen.getPartSpec(compID));

                // component.updateQuantities(qtyToOrder, componentQuant.quantity);
            }

            POEnt purchOrd = poHome.create(chosen.getID(), siteID, cq);

            sendPO(purchOrd);
        } catch (RemoteException re) {
            throw new EJBException(re);
        }
    }

    /**
     * sendPO: send the purchase order to the supplier.
     * @param purchOrd - the purchase order.
     *
     */
    private void sendPO(POEnt purchOrd) throws ECperfException {

        if (debugging)
            debug.println(3, "sendPO");

        try {
            String         xml = purchOrd.generateXml();
            SendXmlCommand xmlComm;

            if (isSecure) {
                xmlComm = new SecureXmlCommand(servletHost, xml, servlet,
                                               servletPort);
            } else {
                xmlComm = new NonSecureXmlCommand(servletHost, xml, servlet,
                                                  servletPort);
            }

            xmlComm.execute();
            if (debugging)
                debug.println(3, "PO has Been sent");
        } catch (IOException io) {
            if (debugging)
                debug.println(
                1, "IOException. Unable to delivery PurchaseOrder to Supplier");
            debug.printStackTrace(io);

            throw new EJBException(io);
        }
    }

    protected void closeConnection(PreparedStatement prep,
                                   Connection connection) {

        try {
            if (connection != null) {
                connection.close();
            }

            if (prep != null) {
                prep.close();
            }
        } catch (SQLException e) {
            if (debugging)
                debug.println(1, "Exception closing connections " + e.getMessage());
            debug.printStackTrace(e);
        }
    }

    /**
     * Method ejbCreate
     *
     *
     * @param siteID
     *
     * @throws CreateException
     *
     */
    public void ejbCreate(int siteID) throws CreateException {

        this.siteID = siteID;
        required     = new Vector();
        compIDs     = new Vector();
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
            context       = (context == null)
                            ? new InitialContext()
                            : context;
            componentHome =
                (SComponentEntHome) javax.rmi.PortableRemoteObject.narrow(
                    context.lookup("java:comp/env/ejb/SComponent"),
                    SComponentEntHome.class);
            poHome        = (POEntHome) javax.rmi.PortableRemoteObject
                .narrow(context
                    .lookup("java:comp/env/ejb/POEnt"), POEntHome.class);
            supplierHome  =
                (SupplierEntHome) javax.rmi.PortableRemoteObject.narrow(
                    context.lookup("java:comp/env/ejb/Supplier"),
                    SupplierEntHome.class);
            servlet       = (String) context.lookup("java:comp/env/servlet");

            // A new env PREFIX is added which will include a "/" at the 
            // start which should be removed. RFE 4491953
            if(servlet.startsWith("/"))
                servlet = servlet.substring(1);

            servletHost   =
                (String) context.lookup("java:comp/env/servletHost");
            servletPort   =
                ((Integer) context.lookup("java:comp/env/servletPort"))
                    .intValue();
            isSecure      =
                ((Boolean) context.lookup("java:comp/env/secureHTTP"))
                    .booleanValue();
        } catch (NamingException e) {
            throw new EJBException("Failure looking up home " + e);
        }
    }
}

