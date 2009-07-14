
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.

 * $Id: SupplierCmpEJB.java,v 1.1.1.1 2002/11/16 05:35:30 emrek Exp $
 *
 */
package com.sun.ecperf.supplier.supplierent.ejb;


//Import statements
import javax.ejb.*;

import java.rmi.*;

import javax.naming.*;

import com.sun.ecperf.common.*;
import com.sun.ecperf.supplier.suppliercompent.ejb.*;
import com.sun.ecperf.supplier.helper.*;

import java.util.*;


/**
 * CMP version of the Supplier Entity Bean
 *
 *
 * @author Damian Guy
 */
public class SupplierCmpEJB implements EntityBean {

    public Integer                suppID;
    public String                 suppName;
    public String                 suppStreet1;
    public String                 suppStreet2;
    public String                 suppCity;
    public String                 suppState;
    public String                 suppCountry;
    public String                 suppZip;
    public String                 suppPhone;
    public String                 suppContact;
    protected EntityContext       entityContext;
    protected Debug               debug;
    protected boolean             debugging;
    protected SupplierCompEntHome suppCompEntHome;

    /**
     * getID - get the suppliers ID
     * @return int - id of supplier.
     */
    public int getID() {
        return suppID.intValue();
    }

    /**
     * getPartSpec - return information about a part that
     * supplier supplies.
     * @param pID - id of part to get Spec for
     * @return PartSpec
     * @exception RemoteException
     * @exception FinderException
     */
    public PartSpec getPartSpec(String pID)
            throws RemoteException, FinderException {

        SupplierCompEnt suppComp =
            suppCompEntHome.findByPrimaryKey(new SuppCompEntPK(pID,
                suppID.intValue()));
        double          price    = suppComp.getPrice();
        double          disc     = suppComp.getDiscount();
        int             delDate = suppComp.getDeliveryDate();
        int             qty      = suppComp.getQuantity();

        /** return new PartSpec(pID, price, qty, disc, delDate); **/
        return new PartSpec(suppID, pID, price, qty, disc, delDate);
    }

    /**
     * ejbCreate: create a new supplier.
     * @param suppID - id of supplier.
     * @param suppName - supplier name.
     * @param suppStreet1 - street line 1.
     * @param suppStreet2 - street line 2.
     * @param suppCity - city supplier is located.
     * @param suppState
     * @param suppCountry - country supplier is located.
     * @param suppZip - zip/postal code.
     * @param suppPhone - contact phone number.
     * @param suppContact - contact person.
     * @return SupplierEnt - newly created Supplier
     * @exception CreateException - if the create fails.
     */
    public Integer ejbCreate(int suppID, String suppName,
                             String suppStreet1, String suppStreet2, String suppCity, String suppState, String suppCountry, String suppZip, String suppPhone, String suppContact)
                                 throws CreateException {

        this.suppID      = new Integer(suppID);
        this.suppName    = suppName;
        this.suppStreet1 = suppStreet1;
        this.suppStreet2 = suppStreet2;
        this.suppState   = suppState;
        this.suppCity    = suppCity;
        this.suppState   = suppState;
        this.suppCountry = suppCountry;
        this.suppZip     = suppZip;
        this.suppPhone   = suppPhone;
        this.suppContact = suppContact;

        return this.suppID;
    }

    /**
     * Method ejbPostCreate
     *
     *
     * @param suppID
     * @param suppName
     * @param suppStreet1
     * @param suppStreet2
     * @param suppCity
     * @param suppState
     * @param suppCountry
     * @param suppZip
     * @param suppPhone
     * @param suppContact
     *
     */
    public void ejbPostCreate(int suppID, String suppName,
                              String suppStreet1, String suppStreet2,
                              String suppCity, String suppState,
                              String suppCountry, String suppZip,
                              String suppPhone, String suppContact) {}

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
     * Method ejbRemove
     *
     *
     * @throws RemoteException
     * @throws RemoveException
     *
     */
    public void ejbRemove() throws RemoteException, RemoveException {

        try {
            Enumeration enum =
                suppCompEntHome.findAllBySupplier(suppID.intValue());

            while (enum.hasMoreElements()) {
                SupplierCompEnt suppComp =
                    (SupplierCompEnt) javax.rmi.PortableRemoteObject
                        .narrow(enum.nextElement(), SupplierCompEnt.class);

                suppComp.remove();
            }
        } catch (FinderException fe) {
            throw new EJBException(fe);
        }
    }

    /**
     * Method setEntityContext
     *
     *
     * @param entityContext
     *
     */
    public void setEntityContext(EntityContext entityContext) {

        Context context = null;

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

        this.entityContext = entityContext;

        try {
            context         = new InitialContext();
            suppCompEntHome =
                (SupplierCompEntHome) javax.rmi.PortableRemoteObject.narrow(
                    context.lookup("java:comp/env/ejb/SupplierCompEnt"),
                    SupplierCompEntHome.class);
        } catch (NamingException ex) {
            throw new EJBException(ex);
        }
    }

    /**
     * Method unsetEntityContext
     *
     *
     */
    public void unsetEntityContext() {}
}

