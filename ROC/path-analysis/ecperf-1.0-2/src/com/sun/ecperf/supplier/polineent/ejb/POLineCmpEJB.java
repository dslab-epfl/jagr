
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.

 * $Id: POLineCmpEJB.java,v 1.1.1.1 2002/11/16 05:35:30 emrek Exp $
 *
 */
package com.sun.ecperf.supplier.polineent.ejb;


//Import statements
import javax.ejb.*;

import javax.naming.*;

import java.sql.*;

import com.sun.ecperf.common.*;

import java.util.*;

import java.rmi.*;

import com.sun.ecperf.supplier.helper.*;
import com.sun.ecperf.supplier.scomponentent.ejb.*;


/**
 * This class implements the Purchase Order Line entity bean.
 *
 * @author Damian Guy
 */
public class POLineCmpEJB implements EntityBean {

    public int              poLineNumber;
    public int              poLinePoID;
    public String           poLineID;
    public int              poLineQty;
    public double           poLineBalance;
    public java.sql.Date    poLineDelDate;
    public String           poLineMsg;
    private int             leadTime;
    protected Debug         debug;
    protected boolean       debugging;
    protected EntityContext entityContext;
    protected SComponentEntHome componentHome;

    /**
     * createXml: creates the XML for a Line item.
     * @return String - the generated XML.
     * TODO -  See if XML parser is part of J2EE
     */
    public String createXml() {

        StringBuffer xml = new StringBuffer(XmlTags.POLINESTART);

        xml.append(XmlTags.LINENUMSTART);
        xml.append(poLineNumber);
        xml.append(XmlTags.LINENUMEND);
        xml.append(XmlTags.PARTIDSTART);
        xml.append(poLineID);
        xml.append(XmlTags.PARTIDEND);
        xml.append(XmlTags.QTYSTART);
        xml.append(poLineQty);
        xml.append(XmlTags.QTYEND);
        xml.append(XmlTags.BALANCESTART);
        xml.append(poLineBalance);
        xml.append(XmlTags.BALANCEEND);
        xml.append(XmlTags.LEADTIMESTART);
        xml.append(leadTime);
        xml.append(XmlTags.LEADTIMEEND);
        xml.append(XmlTags.POLINEEND);

        return xml.toString();
    }

    /**
     * setDeliveredDate: Sets the date that a Line item was delivered.
     * @param date  - the date that the delivery was made.
     */
    public void setDeliveredDate(java.sql.Date date) {
        poLineDelDate = date;
    }

    /**** ejbXxxxx methods ****/

    /**
     * ejbCreate - create a POLine
     * @param poLineNumber
     * @param poID
     * @param pID
     * @param qty
     * @param balance
     * @param leadTime
     * @param message
     * @return POLineEntPK
     * @exception CreateException - if there is a create failure.
     */
    public POLineEntPK ejbCreate(
            int poLineNumber, int poID, String pID, int qty, double balance, int leadTime, String message)
                throws CreateException {

        this.poLineNumber  = poLineNumber;
        this.poLinePoID   = poID;
        this.poLineID    = pID;
        this.poLineQty     = qty;
        this.poLineBalance = balance;

        Calendar cal        = Calendar.getInstance();
        int      hoursToADD = leadTime / 24;

        this.leadTime = leadTime;

        cal.add(Calendar.HOUR, hoursToADD);

        this.poLineDelDate = new java.sql.Date(cal.getTime().getTime());
        this.poLineMsg = message;

        return null;
    }

    /**
     * Method ejbPostCreate
     *
     *
     * @param poLineNumber
     * @param poID
     * @param pID
     * @param qty
     * @param balance
     * @param leadTime
     * @param message
     *
     */
    public void ejbPostCreate(int poLineNumber, int poID, String pID,
                              int qty, double balance, int leadTime,
                              String message) {}

    /**
     * Method ejbRemove
     *
     *
     * @throws RemoveException
     *
     */
    public void ejbRemove() throws RemoveException {}

    /**
     * Method ejbActivate
     *
     *
     */
    public void ejbActivate() {
        leadTime = Integer.MIN_VALUE;
    }

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
    public void ejbLoad() {
        // Recheck the leadTime only if the bean instance is just activated.
        if (leadTime == Integer.MIN_VALUE) {
            try {
                leadTime = componentHome.findByPrimaryKey(poLineID)
                           .getLeadTime();
            } catch (ObjectNotFoundException e) {
                throw new EJBException("Referenced item " + poLineID +
                                       " not found");
            } catch (FinderException e) {
                throw new EJBException(e);
            } catch (RemoteException e) {
                throw new EJBException(e);
            }
        }
    }

    /**
     * Method ejbStore
     *
     *
     */
    public void ejbStore() {}

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
            context    = new InitialContext();
            int     debugLevel =
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
        } catch (NamingException e) {
            throw new EJBException("Failure looking up home " + e);
        }
        this.entityContext = entityContext;
    }

    /**
     * Method unsetEntityContext
     *
     *
     */
    public void unsetEntityContext() {}
}

