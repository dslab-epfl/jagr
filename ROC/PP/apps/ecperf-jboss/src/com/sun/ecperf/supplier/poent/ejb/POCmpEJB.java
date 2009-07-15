
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.

 * $Id: POCmpEJB.java,v 1.1 2004/02/19 14:45:11 emrek Exp $
 *
 */
package com.sun.ecperf.supplier.poent.ejb;


//Import statements
import javax.ejb.*;

import javax.naming.*;

import java.rmi.*;

import java.util.*;

import com.sun.ecperf.common.*;
import com.sun.ecperf.util.sequenceses.ejb.*;
import com.sun.ecperf.supplier.polineent.ejb.*;
import com.sun.ecperf.supplier.helper.*;


/**
 * This class implements the Purchase Order Entity Bean
 * Container Managed.
 *
 * @author Damian Guy
 */
public class POCmpEJB implements EntityBean {

    public Integer            poNumber;
    public int                poSuppID;
    public int                poSiteID;
    protected EntityContext   entityContext;
    protected Debug           debug;
    protected boolean         debugging;
    private String            poDTD;
    protected SequenceSesHome sequenceHome;
    protected POLineEntHome   poLineHome;
    protected HashMap         poLineCache;
   protected boolean isDirty = true;

    /**
     * poLineDeliverd - indicate that a POline has been delivered.
     * @param lineNumber - line number of delivered line.
     */
    public void poLineDelivered(int lineNumber) {

        try {
            POLineEnt pol =
                poLineHome.findByPrimaryKey(new POLineEntPK(lineNumber,
                    poNumber.intValue()));
            Calendar  cal = Calendar.getInstance();

            pol.setDeliveredDate(new java.sql.Date(cal.getTime().getTime()));
        } catch (FinderException e) {
            debug.printStackTrace(e);

            throw new EJBException(e);
        } catch (RemoteException e) {
            debug.printStackTrace(e);

            throw new EJBException(e);
        }
    }

    /**
     * generateXml - generates the  XML for this Purchase Order.
     * @return String - containing XML.
     */
    public String generateXml() throws ECperfException {

        StringBuffer xml = new StringBuffer(XmlTags.XMLVERSION);

        xml.append(XmlTags.PODOC);
        xml.append("\"");
        xml.append(poDTD);
        xml.append("\">");
        xml.append(XmlTags.POSTART);
        xml.append(XmlTags.PONUMBERSTART);
        xml.append(poNumber);
        xml.append(XmlTags.PONUMBEREND);
        xml.append(XmlTags.SITESTART);
        xml.append(poSiteID);
        xml.append(XmlTags.SITEEND);

        try {
            Collection lines = poLineHome.findByPO(poNumber.intValue());

            if (poLineCache == null)
                poLineCache = new HashMap();

            for (Iterator lineIter = lines.iterator(); lineIter.hasNext();) {
                POLineEnt line = (POLineEnt) javax.rmi.PortableRemoteObject
                                 .narrow(lineIter.next(), POLineEnt.class);
                poLineCache.put(line.getPrimaryKey(), line);
            }

            if (poLineCache.size() <= 0)
                throw new DataIntegrityException("No PO lines found!");

            xml.append(XmlTags.NUMLINESSTART);
            xml.append(lines.size());
            xml.append(XmlTags.NUMLINESEND);

            Iterator lineIter = poLineCache.values().iterator();

            while (lineIter.hasNext()) {
                POLineEnt line =
                    (POLineEnt) javax.rmi.PortableRemoteObject
                        .narrow(lineIter.next(), POLineEnt.class);

                xml.append(line.createXml());
            }

            xml.append(XmlTags.POEND);

            return xml.toString();
        } catch (RemoteException re) {
            throw new EJBException(re);
        } catch (FinderException fe) {
            throw new EJBException(fe);
        }
    }

    /**
     * ejbCreate: creates new Purchase Order + PO Lines.
     * @param suppID  -  the id of the supplier.
     * @param siteID  -  id of site that has ordered components.
     * @param orders - Array of Objects containing qty + pricing information for components
     *                     that are being ordered.
     * @return Integer - id of this PO.
     * @exception CreateException - if there is a create failure.
     */
    public Integer ejbCreate(
            int suppID, int siteID, ComponentOrder[] orders)
                throws CreateException {

        if (debugging)
            debug.println(3, "ejbCreate with " + orders.length + "lines.");

        try {
            SequenceSes sequence = sequenceHome.create();

            this.poNumber = new Integer(sequence.nextKey("purchaseorder"));
        } catch (CreateException e) {
            debug.printStackTrace(e);

            throw new EJBException(e);
        } catch (FinderException e) {
            debug.printStackTrace(e);

            throw new EJBException(e);
        } catch (RemoteException e) {
            debug.printStackTrace(e);

            throw new EJBException(e);
        }

        this.poSuppID = suppID;
        this.poSiteID = siteID;

        poLineCache = new HashMap(orders.length);

        try {
            for (int i = 0; i < orders.length; i++) {
                String id       = orders[i].id;
                int    qty      = orders[i].qty;
                double balance  = orders[i].balance;
                int    leadTime = orders[i].leadTime;

                POLineEnt poLine = poLineHome.create(i + 1, poNumber
                                   .intValue(), id, qty, balance, leadTime,
                                   "testing");
                poLineCache.put(poLine.getPrimaryKey(), poLine);
            }
        } catch (CreateException e) {
            debug.printStackTrace(e);

            throw new EJBException(e);
        } catch (RemoteException e) {
            debug.printStackTrace(e);

            throw new EJBException(e);
        }

        isDirty = false;
        return null;
    }

    /**
     * Method ejbPostCreate
     *
     *
     * @param suppID
     * @param siteID
     * @param orders
     *
     */
    public void ejbPostCreate(int suppID, int siteID,
                              ComponentOrder[] orders) {}

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
        } catch (Exception e) {
            e.printStackTrace(System.err);

            throw new EJBException(e);
        }

        try {
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
            System.out.println("POCmpEJB: debuglevel Property "
                               + "not set. Turning off debug messages");

            debug = new Debug();
        }

        this.entityContext = entityContext;

        try {
            poDTD = (String) context.lookup("java:comp/env/poDTD");

            if (debugging)
                debug.println(3, "found poDTD reference");

            sequenceHome =
                (SequenceSesHome) javax.rmi.PortableRemoteObject.narrow(
                    context.lookup("java:comp/env/ejb/SequenceSes"),
                    SequenceSesHome.class);

            if (debugging)
                debug.println(3, "found SequenceSesHome interface");

            poLineHome =
                (POLineEntHome) javax.rmi.PortableRemoteObject.narrow(
                    context.lookup("java:comp/env/ejb/POLineEnt"),
                    POLineEntHome.class);

            if (debugging)
                debug.println(3, "found OrderLineEntHome interface");
        } catch (NamingException ex) {
            ex.printStackTrace(System.err);

            throw new EJBException(ex);
        }
    }

    /**
     * Method ejbRemove
     *
     *
     * @throws RemoteException
     * @throws RemoveException
     *
     */
    public void ejbRemove() throws RemoveException, RemoteException {

        if (debugging)
            debug.println(3, "ejbRemove ");

        try {
            Iterator poLines =
                poLineHome.findByPO(poNumber.intValue()).iterator();

            poLineCache = null;

            while (poLines.hasNext()) {
                POLineEnt line =
                    (POLineEnt) javax.rmi.PortableRemoteObject
                        .narrow(poLines.next(), POLineEnt.class);

                line.remove();
            }
        } catch (FinderException e) {
            throw new EJBException(e);
        }
    }

    /**
     * Method ejbActivate
     *
     *
     */
    public void ejbActivate() {
        if (debugging)
            debug.println(3, "ejbActivate ");
        poLineCache = null;
    }

    /**
     * Method ejbPassivate
     *
     *
     */
    public void ejbPassivate() {
        if (debugging)
            debug.println(3, "ejbPassivate ");
    }

    /**
     * Method ejbLoad
     *
     *
     */
    public void ejbLoad() {
        if (debugging)
            debug.println(3, "ejbLoad ");
        isDirty = false;
    }

    /**
     * Method ejbStore
     *
     *
     */
    public void ejbStore() {
        if (debugging)
            debug.println(3, "ejbStore ");
        isDirty = false;
    }

    /**
     * Method unsetEntityContext
     *
     *
     */
    public void unsetEntityContext() {

        if (debugging)
            debug.println(3, "unsetEntityContext ");

        entityContext = null;
    }

   public boolean isModified() { return isDirty; }
}

