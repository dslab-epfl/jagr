
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 */
package com.sun.ecperf.mfg.largeorderent.ejb;


import javax.ejb.*;

import java.rmi.*;

import javax.naming.*;

import javax.sql.*;

import java.sql.*;

import java.util.*;

import com.sun.ecperf.common.*;
import com.sun.ecperf.mfg.helper.*;


/**
 * This class implements the Large Order Entity Bean. Bean managed.
 * If the container supports Container Managed persistence
 * then the superclass should be used. This is specified during
 * deployment time.
 *
 * @author Ajay Mittal
 *
 * @see LargeOrderCmpEJB
 * @see LargeOrderDumEJB
 */
public class LargeOrderBmpEJB extends LargeOrderCmpEJB {

    private static final String    className = "LargeOrderBmpEJB";
    protected javax.sql.DataSource dataSource;

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
     * Constructs the LargeOrder object
     * @param salesOrderId the id of sales order that caused this wo to be created
     * @param orderLineNumber line (row) number in salesOrder identified by salesOrderId
     * @param assemblyId assembly that is going to be manufactured
     * @param qty number of assemblies to be manufactured by this wo
     * @param dueDate date when this order is due
     */
    public Integer ejbCreate(
            int salesOrderId, int orderLineNumber, String assemblyId, short qty, java
                .sql.Date dueDate) throws CreateException {

        super.ejbCreate(salesOrderId, orderLineNumber, assemblyId, qty,
                        dueDate);
	if (debugging)
	    debug.println(3, "ejbCreate ");

        Connection        conn = null;
        PreparedStatement stmt = null;

        try {
            conn = dataSource.getConnection();
            stmt = conn.prepareStatement(
                "INSERT INTO M_largeorder(lo_id, lo_o_id, lo_ol_id, lo_assembly_id, lo_qty, lo_due_date) VALUES(?, ?, ?, ?, ?, ?)");

            stmt.setInt(1, id.intValue());
            stmt.setInt(2, salesOrderId);
            stmt.setInt(3, orderLineNumber);
            stmt.setString(4, assemblyId);
            stmt.setShort(5, qty);

            // SetDate null doesn't work in the Merant driver
            // We just try this workaround.
            if (dueDate == null) {
                stmt.setNull(6, Types.DATE);
            } else {
                stmt.setDate(6, dueDate);
            }

            int ret = stmt.executeUpdate();

            if (ret != 1) {
		if (debugging)
		    debug.println(1, "Insert into M_largeorder(" + id + ", "
                              + salesOrderId + ", " + orderLineNumber + ", "
                              + assemblyId + ", " + qty + ", " + dueDate
                              + ") returned " + ret);

                throw new CreateException("Insert into M_largeorder(" + id
                                          + ", " + salesOrderId + ", "
                                          + orderLineNumber + ", "
                                          + assemblyId + ", " + qty + ", "
                                          + dueDate + ") returned " + ret);
            }

            return id;
        } catch (SQLException e) {
	    if (debugging) {
		debug.println(1, "SQLEXception in insert into M_largeorder(" + id
                          + ", " + salesOrderId + ", " + orderLineNumber
                          + ", " + assemblyId + ", " + qty + ", " + dueDate
                          + ")");
		debug.println(1, e.getMessage());
	    }
	    
            debug.printStackTrace(e);

            throw new EJBException(e.getMessage());
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
     * @return the primary key of object which is the id
     */
    public Integer ejbFindByPrimaryKey(Integer pk) throws FinderException {

	if (debugging)
	    debug.println(3, "ejbFindByPrimaryKey: " + pk);

        Connection        conn = null;
        PreparedStatement stmt = null;

        try {
            conn = dataSource.getConnection();
            stmt = conn.prepareStatement(
                "SELECT lo_id FROM M_largeorder WHERE lo_id = ?");

            stmt.setInt(1, pk.intValue());

            ResultSet resultSet = stmt.executeQuery();

            if (!resultSet.next()) {
		if (debugging)
		    debug.println(1, "select from M_largeorder where lo_id = "
                              + pk + " returned no rows");

                throw new FinderException(
                    "select from M_largeorder where lo_id = " + pk
                    + " returned no rows");
            }

            return (pk);
        } catch (SQLException e) {
	    if (debugging) {
		debug.println(
                1, "SQLException in :select from M_largeorder where lo_id = "
                + pk);
		debug.println(1, e.getMessage());
	    }
            debug.printStackTrace(e);

            throw new EJBException(e);
        } finally {
            Util.closeConnection(conn, stmt);
        }
    }

    /**
     * Finds one single large order by the sales order and line
     * number.
     *
     *
     * @param salesId
     * @param oLineId
     * @return primary key
     */
    public Integer ejbFindByOrderLine(int salesId, int oLineId)
            throws FinderException {

	if (debugging)
	    debug.println(3, "ejbFindByOrderLine: " + salesId + ", " + oLineId);

        Connection        conn = null;
        PreparedStatement stmt = null;
        Integer           pk   = null;

        try {
            conn = dataSource.getConnection();
            stmt = conn
                .prepareStatement("SELECT lo_id FROM M_largeorder "
                                  + "WHERE lo_o_id = ? AND lo_ol_id = ?");

            stmt.setInt(1, salesId);
            stmt.setInt(2, oLineId);

            ResultSet resultSet = stmt.executeQuery();

            if (resultSet.next()) {
                pk = new Integer(resultSet.getInt(1));
            } else {
                String mesg = "select from M_largeorder "
                              + "where lo_o_id = " + salesId
                              + " and lo_ol_id = " + oLineId
                              + " returned no rows";

		if (debugging)
		    debug.println(1, mesg);

                throw new FinderException(mesg);
            }

            return (pk);
        } catch (SQLException e) {
	    if (debugging) {
		
		debug.println(
                1, "SQLException in :select from M_largeorder where lo_id = "
                + pk);
		debug.println(1, e.getMessage());
	    }
	    
            debug.printStackTrace(e);

            throw new EJBException(e);
        } finally {
            Util.closeConnection(conn, stmt);
        }
    }

    /**
     * Finds all the large order objects in the DB and returns
     * a list of primary keys.
     *
     * @return an enumeration of primary keys (id) of
     *         all large order objects in the Db.
     */
    public java.util.Enumeration ejbFindAll() throws FinderException {

	if (debugging)
	    debug.println(3, "ejbFindAll ");

        Connection        conn = null;
        PreparedStatement stmt = null;

        try {
            conn = dataSource.getConnection();
            stmt = conn.prepareStatement("SELECT lo_id FROM M_largeorder");

            ResultSet resultSet = stmt.executeQuery();
            Vector    keys      = new Vector();

            while (resultSet.next()) {
                int id = resultSet.getInt(1);

                keys.addElement(new Integer(id));
            }

            return keys.elements();
        } catch (SQLException e) {
	    if (debugging)
		debug.println(1, "SELECT lo_id FROM M_largeorder : Failed : "
                          + e.getMessage());
            debug.printStackTrace(e);

            throw new EJBException(className + "(ejbFindAll): " + e);
        } finally {
            Util.closeConnection(conn, stmt);
        }
    }

    /**
     * Deletes this large order object from the DB.
     */
    public void ejbRemove() throws RemoveException {

	if (debugging)
	    debug.println(3, "ejbRemove ");
        super.ejbRemove();

        Connection        conn = null;
        PreparedStatement stmt = null;

        try {
            conn = dataSource.getConnection();
            stmt = conn
                .prepareStatement("DELETE FROM M_largeorder WHERE lo_id = ?");

            stmt.setInt(1, id.intValue());

            if (stmt.executeUpdate() < 1) {
                throw new RemoveException(className + "(ejbRemove): ");
            }
        } catch (SQLException e) {
	    if (debugging) {
		debug.println(
                1, "SQLException in delete from M_largeorder where lo_id = "
                + id);
		debug.println(1, e.getMessage());
	    }
	    
            debug.printStackTrace(e);

            throw new EJBException(e);
        } finally {
            Util.closeConnection(conn, stmt);
        }
    }

    /**
     * Updates this large order object in the DB. This is
     * called if the information in the pool needs to be updated.
     */
    public void ejbStore() {

	if (debugging)
	    debug.println(3, "ejbStore ");
        super.ejbStore();

        Connection        conn = null;
        PreparedStatement stmt = null;

        try {
            conn = dataSource.getConnection();
            stmt = conn.prepareStatement(
                "UPDATE M_largeorder SET lo_id = ?, lo_o_id = ?, lo_ol_id = ?, lo_assembly_id = ?, lo_qty = ?, lo_due_date = ? WHERE lo_id = ?");

            stmt.setInt(1, id.intValue());
            stmt.setInt(2, salesOrderId);
            stmt.setInt(3, orderLineNumber);
            stmt.setString(4, assemblyId);
            stmt.setShort(5, qty);

            // SetDate null doesn't work in the Merant driver
            // We just try this workaround.
            if (dueDate == null) {
                stmt.setNull(6, Types.DATE);
            } else {
                stmt.setDate(6, dueDate);
            }

            stmt.setInt(7, id.intValue());
            stmt.executeUpdate();

            // Need to verify if the execute succeeded.
        } catch (SQLException e) {
	    if (debugging)
		debug.println(1, "UPDATE M_largeorder SET lo_id ... : Failed : "
                          + e.getMessage());
            debug.printStackTrace(e);

            throw new EJBException(e);
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
        id = (Integer) entityContext.getPrimaryKey();
        super.ejbActivate();
    }


    /**
     * Loads this large order object from the DB into the pool.
     * Called after an ejbCreate/ejbActivate is done.
     */
    public void ejbLoad() {

	if (debugging)
	    debug.println(3, "ejbLoad ");

        Connection        conn = null;
        PreparedStatement stmt = null;

        try {
            conn = dataSource.getConnection();
            stmt = conn.prepareStatement(
                "SELECT lo_id, lo_o_id, lo_ol_id, lo_assembly_id, lo_qty, lo_due_date FROM M_largeorder WHERE lo_id = ?");

            stmt.setInt(1, id.intValue());

            ResultSet resultSet = stmt.executeQuery();

            if (!resultSet.next()) {
                throw new NoSuchEntityException("ejbload: Row not found ");
            }

            id              = new Integer(resultSet.getInt(1));
            salesOrderId    = resultSet.getInt(2);
            orderLineNumber = resultSet.getInt(3);
            assemblyId      = resultSet.getString(4);
            qty             = resultSet.getShort(5);
            dueDate         = resultSet.getDate(6);
        } catch (SQLException e) {
	    if (debugging)
		debug.println(
                1, "SELECT lo_id, lo_o_id, lo_ol_id, ...  : Failed : "
                + e.getMessage());
            debug.printStackTrace(e);

            throw new EJBException(className + "(ejbLoad): " + e);
        } finally {
            Util.closeConnection(conn, stmt);
        }

        super.ejbLoad();
    }
}

