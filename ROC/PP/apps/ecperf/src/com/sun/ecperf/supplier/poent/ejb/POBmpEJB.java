
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.

 * $Id: POBmpEJB.java,v 1.2 2003/03/22 04:55:01 emrek Exp $
 *
 */
package com.sun.ecperf.supplier.poent.ejb;


//Import statements
import javax.ejb.*;

import javax.naming.*;

import java.rmi.*;

import com.sun.ecperf.common.*;

import java.util.*;

import java.sql.*;

import com.sun.ecperf.supplier.helper.*;


/**
 * This is the Bean Managed Persistence implentation of th POEnt Bean.
 *
 *
 * @author Damian Guy
 */
public class POBmpEJB extends POCmpEJB {

    protected javax.sql.DataSource dataSource;

    private Integer            poNumberCache;
    private int                poSuppIDCache;
    private int                poSiteIDCache;
    
    /**
     * Method setEntityContext
     *
     *
     * @param entityContext
     *
     */
    public void setEntityContext(EntityContext entityContext) {

        super.setEntityContext(entityContext);

        Context context = null;

        try {
            context    = new InitialContext();
            dataSource =
                (javax.sql
                    .DataSource) context
                        .lookup("java:comp/env/SupplierDataSource");
        } catch (NamingException e) {
            debug.printStackTrace(e);

            throw new EJBException("Failure looking up DataSource " + e);
        }
    }

    /**
     * ejbCreate: create a new Purchase Order.
     * @param suppID - id of supplier.
     * @param siteID - site id of Mfg.
     * @param orders - component,qty pairs.
     * @return POEnt
     * @exception CreateException - if there is a create failure.
     */
    public Integer ejbCreate(
            int suppID, int siteID, ComponentOrder[] orders)
                throws CreateException {

        super.ejbCreate(suppID, siteID, orders);

        poNumberCache = poNumber;
        poSuppIDCache = poSuppID;
        poSiteIDCache = poSiteID;
    
        Connection        connection = null;
        PreparedStatement prep       = null;

        try {
            String insertStatement =
                "INSERT INTO S_purchase_order VALUES (?, ?, ?)";

            connection = dataSource.getConnection();
            prep       = connection.prepareStatement(insertStatement);

            prep.setInt(1, poNumber.intValue());
            prep.setInt(2, poSuppID);
            prep.setInt(3, poSiteID);

            int retval = prep.executeUpdate();

            if (retval != 1) {
                throw new CreateException(
                    "insert into S_purchase_order : Failed ");
            }
        } catch (SQLException se) {
            if (debugging)
                debug.println(1, "INSERT INTO S_purchase_order : Failed "
                              + se.getMessage());
            debug.printStackTrace(se);

            throw new EJBException(se);
        } finally {
            Util.closeConnection(connection, prep);
        }

        return poNumber;
    }

    /**
     * ejbFindByPrimaryKey: find the PO that is identified by pk.
     * @param pk - find PO with the primary key.
     * @return POEnt.
     * @exception FinderException - if cannot find PO.
     */
    public Integer ejbFindByPrimaryKey(Integer pk) throws FinderException {

        Connection        connection = null;
        PreparedStatement prep       = null;

        try {
            connection = dataSource.getConnection();

            String select =
                "SELECT po_number FROM S_purchase_order WHERE po_number = ?";

            prep = connection.prepareStatement(select);

            prep.setInt(1, pk.intValue());

            ResultSet rs    = prep.executeQuery();
            boolean   found = rs.next();

            if (!found) {
                throw new ObjectNotFoundException("Purchase Order Not Found: "
                                                  + pk);
            }

            return pk;
        } catch (SQLException se) {
            if (debugging)
                debug.println(
                    1, "SELECT poNumber FROM S_purchase_order WHERE po_number = "
                    + pk + " Failed " + se.getMessage());
            debug.printStackTrace(se);

            throw new EJBException(se);
        } finally {
            Util.closeConnection(connection, prep);
        }
    }

    /**
     * Method unsetEntityContext
     *
     */
    public void unsetEntityContext() {
        super.unsetEntityContext();
    }

    /**
     * Method ejbActivate
     *
     */
    public void ejbActivate() {
        poNumber = (Integer) entityContext.getPrimaryKey();
        super.ejbActivate();
    }

    /**
     * Method ejbPassivate
     *
     */
    public void ejbPassivate() {
        super.ejbPassivate();
    }

    /**
     * Method ejbRemove
     *
     *
     * @throws RemoteException
     * @throws RemoveException
     *
     */
    public void ejbRemove() throws RemoteException, RemoveException {

        super.ejbRemove();

        Connection        connection = null;
        PreparedStatement prep       = null;

        try {
            connection = dataSource.getConnection();

            String delstatement =
                "DELETE FROM S_purchase_order WHERE po_number = ? " + 
                "AND po_supp_id = ? AND po_site_id = ?";

            prep = connection.prepareStatement(delstatement);

            prep.setInt(1, poNumber.intValue());
            prep.setInt(2, poSuppIDCache);
            prep.setInt(3, poSiteIDCache);

            if (prep.executeUpdate() < 1) {
                throw new RemoveException(delstatement + poNumber
                                          + " Failed ");
            }
        } catch (SQLException se) {
            if (debugging)
                debug.println(1, "DELETE FROM S_purchase_order WHERE po_number = "
                              + poNumber + " Failed " + se.getMessage());
            debug.printStackTrace(se);

            throw new EJBException(se);
        } finally {
            Util.closeConnection(connection, prep);
        }
    }

    /**
     * Method ejbLoad
     *
     *
     */
    public void ejbLoad() {

        Connection        connection = null;
        PreparedStatement prep       = null;

        try {
            connection = dataSource.getConnection();

            String select =
                "SELECT * FROM S_purchase_order WHERE po_number = ? ";

            prep = connection.prepareStatement(select);

            prep.setInt(1, poNumber.intValue());

            ResultSet rs = prep.executeQuery();

            if (!rs.next()) {
                throw new NoSuchEntityException(
                    "SELECT * FROM S_purchase_order WHERE po_number = "
                    + poNumber + " : Row not found");
            }

            poNumber  = new Integer(rs.getInt(1));
            poSuppID = rs.getInt(2);
            poSiteID = rs.getInt(3);

            poNumberCache = poNumber;
            poSuppIDCache = poSuppID;
            poSiteIDCache = poSiteID;
        isDirty = false;
                
        } catch (SQLException se) {
            if (debugging)
                debug.println(
                    1, "SELECT * FROM S_purchase_order WHERE po_number = "
                    + poNumber + " Failed : " + se.getMessage());
            debug.printStackTrace(se);

            throw new EJBException(se);
        } finally {
            Util.closeConnection(connection, prep);
        }
    }

    /**
     * Method ejbStore
     *
     *
     */
    public void ejbStore() {

        Connection        connection = null;
        PreparedStatement prep       = null;

        try {
            connection = dataSource.getConnection();

            String update = "UPDATE S_purchase_order SET po_supp_id = ?, " +
                            "po_site_id = ? WHERE po_number = ? " + 
                            "AND po_supp_id = ? AND po_site_id = ?";

            prep = connection.prepareStatement(update);

            prep.setInt(1, poSuppID);
            prep.setInt(2, poSiteID);
            prep.setInt(3, poNumber.intValue());
            
            prep.setInt(4, poSuppIDCache);
            prep.setInt(5, poSiteIDCache);
            
            int ret = prep.executeUpdate();
            
            if(ret != 1) {
                if(debugging) {
                     debug.println(1,"Optimistic concurrency control failed " + 
                                     "in POEnt.ejbStore() for id = " + poNumber);
                }
                throw new EJBException("Optimistic concurrency control failed ");
           }            
            poNumberCache = poNumber;
            poSuppIDCache = poSuppID;
            poSiteIDCache = poSiteID;
            
        isDirty = false;
        } catch (SQLException se) {
            if (debugging)
                debug.println(
                    1, "UPDATE S_purchase_order SET po_supp_id = ... : Failed : "
                    + se.getMessage());
            debug.printStackTrace(se);

            throw new EJBException(se);
        } finally {
            Util.closeConnection(connection, prep);
        }
    }
}

