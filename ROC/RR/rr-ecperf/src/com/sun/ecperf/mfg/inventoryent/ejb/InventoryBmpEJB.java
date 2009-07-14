
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 */
package com.sun.ecperf.mfg.inventoryent.ejb;


import javax.ejb.*;

import java.rmi.*;

import javax.naming.*;

import javax.sql.*;

import java.sql.*;

import java.util.*;

import com.sun.ecperf.common.*;


/**
 * This class implements the Inventory Entity Bean. Bean managed.
 * If the container supports Container Managed persistence
 * then the superclass will be called. This is specified during
 * deployment time.
 *
 * @author Agnes Jacob
 *
 * @see InventoryCmpEJB
 * @see InventoryDumEJB
 */
public class InventoryBmpEJB extends InventoryCmpEJB {

    private static final String    className = "InventoryBmpEJB";
    protected javax.sql.DataSource dataSource;

    private String               partIdCache;
    private int                  qtyCache;
    private int                  in_orderedCache;
    private String               locationCache;
    private int                  accCodeCache;
    private java.sql.Date                 accDateCache;
    
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
                        .lookup("java:comp/env/MfgDataSource");
        } catch (NamingException e) {
            debug.printStackTrace(e);

            throw new EJBException("Failure looking up DataSource " + e);
        }
    }

    /**
     * Constructs the Inventory Bean persistent object
     * and stores the information into the DB.
     *
     * @param partId
     * @param qty           Quantity
     * @param in_ordered
     * @param location      Warehouse location
     * @param accCode       Account Finance Code
     * @param accDate       Date/time of last activity
     * @return primary key which is the partId. (String)
     */
    public String ejbCreate(
            String partId, int qty, int in_ordered, String location, int accCode, java
                .sql.Date accDate) throws RemoteException, CreateException {

        super.ejbCreate(partId, qty, in_ordered, location, accCode, accDate);
        partIdCache = partId;
        qtyCache = qty;
        in_orderedCache = in_ordered;
        locationCache = location;
        accCodeCache = accCode;
        accDateCache = accDate;

        Connection        conn = null;
        PreparedStatement stmt = null;
        int               ret;

	if (debugging)
	    debug.println(3, "ejbCreate");

        try {
            conn = dataSource.getConnection();
            stmt = conn.prepareStatement(
                "INSERT INTO M_inventory(in_p_id, in_qty, in_ordered, in_location, in_acc_code, in_act_date) VALUES(?, ?, ?, ?, ?, ?)");

            stmt.setString(1, partId);
            stmt.setInt(2, qty);
            stmt.setInt(3, in_ordered);
            stmt.setString(4, location);
            stmt.setInt(5, accCode);
            stmt.setDate(6, accDate);

            if ((ret = stmt.executeUpdate()) != 1) {
		if (debugging)
		    debug.println(1, "executeUpdate() into M_inventory returned "
                              + ret);

                throw new CreateException(className + "(ejbCreate): ");
            }

            return (new String(partId));
        } catch (SQLException e) {
	    if (debugging)
		debug.println(
                1, "INSERT INTO M_inventory(in_p_id, in_qty, .... : Failed : "
                + e.getMessage());
            debug.printStackTrace(e);

            throw new EJBException(className + " (ejbCreate): " + e);
        } finally {
            Util.closeConnection(conn, stmt);
        }
    }

    /**
     * Finds the object in the DB based on primary key.
     * If object is found, then it will just return the
     * primary key passed in otherwise throws a FinderException.
     *
     *
     * @param pk
     * @return the primary key of object which is the partId (String).
     */
    public String ejbFindByPrimaryKey(String pk) throws FinderException {

        Connection        conn = null;
        PreparedStatement stmt = null;

	if (debugging)
	    debug.println(3, "ejbFindByPrimaryKey");

        try {
            conn = dataSource.getConnection();
            stmt = conn.prepareStatement(
                "SELECT in_p_id FROM M_inventory WHERE in_p_id = ?");

            stmt.setString(1, pk);

            ResultSet resultSet = stmt.executeQuery();

            if (!resultSet.next()) {
		if (debugging)
		    debug.println(1, "No keys found in M_inventory where pk = "
                              + pk);

                throw new FinderException(className
                                          + "(ejbFindByPrimaryKey)");
            }

            return (pk);
        } catch (SQLException e) {
	    if (debugging)
		debug.println(
                1, "SQLException in SELECT in_p_id FROM M_inventory WHERE in_p_id = "
                + pk);
            debug.printStackTrace(e);

            throw new EJBException(className + "(ejbFindByPrimaryKey): " + e);
        } finally {
            Util.closeConnection(conn, stmt);
        }
    }

    /**
     * Finds all the inventory objects in the DB and returns
     * a list of primary keys.
     *
     * @return an enumeration of primary keys (partId) of
     *         all inventory objects in the Db.
     */
    public java.util.Enumeration ejbFindAll() throws FinderException {

        Connection        conn = null;
        PreparedStatement stmt = null;

	if (debugging)
	    debug.println(3, "ejbFindAll");

        try {
            String id;

            conn = dataSource.getConnection();
            stmt = conn.prepareStatement("SELECT in_p_id FROM M_inventory");

            ResultSet resultSet = stmt.executeQuery();
            Vector    keys      = new Vector();

            while (resultSet.next()) {
                id = resultSet.getString(1);

                keys.addElement(id);
            }

            return keys.elements();
        } catch (SQLException e) {
	    if (debugging)
		debug.println(1, "No objects found in M_inventory");
            debug.printStackTrace(e);

            throw new EJBException(className + "(ejbFindAll): " + e);
        } finally {
            Util.closeConnection(conn, stmt);
        }
    }

    /**
     * Deletes this inventory object from the DB.
     */
    public void ejbRemove() throws RemoveException {

        super.ejbRemove();

        Connection        conn = null;
        PreparedStatement stmt = null;

	if (debugging)
	    debug.println(3, "ejbRemove");

        try {
            conn = dataSource.getConnection();
            stmt = conn.prepareStatement(
                "DELETE FROM M_inventory WHERE in_p_id = ? " +
                "AND in_qty = ? AND in_ordered = ? AND in_location = ? " +
                "AND in_acc_code = ? " +
                "AND  in_act_date = ?"
            );

            stmt.setString(1, partId);
            stmt.setInt(2, qtyCache);
            stmt.setInt(3, in_orderedCache);
            stmt.setString(4, locationCache);
            stmt.setInt(5, accCodeCache);
            stmt.setDate(6, (java.sql.Date) accDateCache);

            if (stmt.executeUpdate() < 1) {
                throw new RemoveException(className + "(ejbRemove): ");
            }
        } catch (SQLException e) {
	    if (debugging)
		debug.println(1, " DELETE FROM M_inventory WHERE in_p_id = "
                          + partId + " : Failed : " + e.getMessage());
            debug.printStackTrace(e);

            throw new EJBException(className + "(ejbRemove): " + e);
        } finally {
            Util.closeConnection(conn, stmt);
        }
    }

    /**
     * Updates this inventory object in the DB. This is
     * called if the information in the pool needs to be updated.
     */
    public void ejbStore() {

	if (debugging)
	    debug.println(3, "ejbStore");
        super.ejbStore();

        Connection        conn = null;
        PreparedStatement stmt = null;

        try {
            conn = dataSource.getConnection();
            stmt = conn.prepareStatement(
               "UPDATE M_inventory SET in_qty = ?, in_ordered = ?, " + 
               "in_location = ?, in_acc_code = ?, in_act_date = ? WHERE in_p_id = ? "
               + "AND in_qty = ? "
               + "AND in_location = ? "
               + " AND in_ordered = ? "
               + "AND in_acc_code = ? "  
               + "AND  in_act_date = ?"
               );
            int i = 1;
            stmt.setInt(i++, qty);
            stmt.setInt(i++, in_ordered);
            stmt.setString(i++, location);
            stmt.setInt(i++, accCode);
            stmt.setDate(i++, (java.sql.Date) accDate);
            stmt.setString(i++, partId);
            stmt.setInt(i++, qtyCache);
            stmt.setString(i++, locationCache);
            stmt.setInt(i++, in_orderedCache);
            stmt.setInt(i++, accCodeCache);
            stmt.setDate(i++, (java.sql.Date) accDateCache);
            
            int ret = stmt.executeUpdate();
            
            if(ret != 1) {
                if(debugging) {
                     debug.println(1,"Optimistic concurrency control failed " + 
                                     "in InventoryEnt.ejbStore() for id = " + partId);
                }
                throw new EJBException("Optimistic concurrency control failed ");
           }            

            partIdCache = partId;
            qtyCache = qty;
            in_orderedCache = in_ordered;
            locationCache = location;
            accCodeCache = accCode;
            accDateCache = accDate;
            // Need to verify if the execute succeeded.
        } catch (SQLException e) {
            if (debugging)
    		    debug.println(1, "SQLException UPDATE M_inventory  SET in_p_id = "
                          + partId + " in_qty = " + qty + "in_location = "
                          + location + "in_acc_code = " + accCode
                          + " in_act_date = " + accDate + "WHERE in_p_id = "
                          + partId);
            debug.printStackTrace(e);

            throw new EJBException(className + "(ejbStore):" + e);
        } finally {
            Util.closeConnection(conn, stmt);
        }
    }
    /**
     * Method ejbActivate
     *
     *
     */
    public void ejbActivate() {
        partId = (String) entityContext.getPrimaryKey();
        super.ejbActivate();
    }

    /**
     * Loads this inventory object from the DB into the pool.
     * Called after an ejbCreate/ejbActivate is done.
     */
    public void ejbLoad() {
        Connection        conn = null;
        PreparedStatement stmt = null;

	if (debugging)
	    debug.println(3, "ejbLoad");

        try {
            conn = dataSource.getConnection();
            stmt = conn
                .prepareStatement("SELECT in_p_id, in_qty, in_ordered, "
                                  + "in_location, in_acc_code, in_act_date "
                                  + "FROM M_inventory WHERE in_p_id = ? ");

            stmt.setString(1, partId);

            ResultSet resultSet = stmt.executeQuery();

            if (!resultSet.next()) {
		if (debugging)
		    debug.println(
                    1, "No rows found in M_inventory where partId = "
                    + partId);

                throw new NoSuchEntityException("ejbload: Row not found ");
            }

            partId     = resultSet.getString(1);
            qty        = resultSet.getInt(2);
            in_ordered = resultSet.getInt(3);
            location   = resultSet.getString(4);
            accCode    = resultSet.getInt(5);
            accDate    = resultSet.getDate(6);
            
            partIdCache = partId;
            qtyCache = qty;
            in_orderedCache = in_ordered;
            locationCache = location;
            accCodeCache = accCode;
            accDateCache = accDate;
        } catch (SQLException e) {
	    if (debugging)
		debug.println(1, "SQLException in SELECT from M_inventory "
                          + "WHERE in_p_id = " + partId + " "
                          + e.getMessage());
            debug.printStackTrace(e);

            throw new EJBException(className + "(ejbLoad): " + e);
        } finally {
            Util.closeConnection(conn, stmt);
        }

        super.ejbLoad();
    }
}

