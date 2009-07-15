
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 */
package com.sun.ecperf.mfg.workorderent.ejb;


import javax.ejb.*;

import java.rmi.*;

import javax.naming.*;

import javax.sql.*;

import java.sql.*;

import java.util.*;

import com.sun.ecperf.common.*;


/**
 * This class implements the WorkOrder Entity Bean. Bean managed.
 * If the container supports Container Managed persistence
 * then the superclass will be called. This is specified during
 * deployment time.
 *
 * @author Agnes Jacob
 *
 * @see WorkOrderCmpEJB
 */
public class WorkOrderBmpEJB extends WorkOrderCmpEJB {

    private static final String    className = "WorkOrderBmpEJB";
    protected javax.sql.DataSource dataSource;

    private Integer              idCache;
    private int                  salesIdCache;
    private int                  oLineIdCache;
    private int                  statusCache;
    private int                  origQtyCache;
    private int                  compQtyCache;
    private String               assemblyIdCache;
    private java.sql.Date        dueDateCache;
    private java.sql.Timestamp   startDateCache;

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
     * Constructs the WorkOrder object (Bean managed)
     * and stores the information into the DB.
     * @param salesId       Sales order id
     * @param oLineId       Order Line ID
     * @param assemblyId            Assembly Id
     * @param origQty       Original Qty
     * @param dueDate       Date when order is due
     * @return primary key of WorkOrder which is composed of id.
     */
    public Integer ejbCreate(
            int salesId, int oLineId, String assemblyId, int origQty, java.sql
                .Date dueDate) throws CreateException {

        super.ejbCreate(salesId, oLineId, assemblyId, origQty, dueDate);

        idCache = id;
        salesIdCache = salesId;
        oLineIdCache = oLineId;
        statusCache = status;
        origQtyCache = origQty;
        compQtyCache = compQty;
        assemblyIdCache = assemblyId;
        dueDateCache = dueDate;
        startDateCache = startDate;

        Connection        conn = null;
        PreparedStatement stmt = null;

        try {
            int ret;

            conn = dataSource.getConnection();
            stmt = conn.prepareStatement(
                "INSERT INTO M_workorder (wo_number, wo_o_id,wo_ol_id, wo_status, wo_assembly_id, wo_orig_qty, wo_comp_qty, wo_due_date, wo_start_date) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)");

            stmt.setInt(1, id.intValue());
            stmt.setInt(2, salesId);
            stmt.setInt(3, oLineId);
            stmt.setInt(4, status);
            stmt.setString(5, assemblyId);
            stmt.setInt(6, origQty);
            stmt.setInt(7, compQty);

            // SetDate null doesn't work in the Merant driver
            // We just try this workaround.
            if (dueDate == null) {
                stmt.setNull(8, Types.DATE);
            } else {
                stmt.setDate(8, dueDate);
            }

            if (startDate == null) {
                stmt.setNull(9, Types.TIMESTAMP);
            } else {
                stmt.setTimestamp(9, startDate);
            }

            if ((ret = stmt.executeUpdate()) != 1) {
		if (debugging)
		    debug.println(
                    1, "executeUpdate failed for INSERT into M_workorder, returned value "
                    + ret);

                throw new CreateException(className + "(ejbCreate):");
            }
            debug.println(3,"Insert M_workorder SET wo_number = "  + id.intValue() +
                ", wo_o_id = " + salesId + " , wo_ol_id = " + oLineId +
                " , wo_status = " + status + ", wo_assembly_id = " + assemblyId +
                " , wo_orig_qty = " + origQty + " , wo_comp_qty = " + compQty +
                " , wo_due_date = " + dueDate + ", wo_start_date = " + startDate + " wo_number = " + id.intValue() +
                " AND wo_o_id = " + salesIdCache + " AND wo_ol_id = " + oLineIdCache +
                " AND wo_status = " +statusCache + " AND wo_assembly_id = " + assemblyIdCache +
                " AND wo_orig_qty = " + origQtyCache + " AND wo_comp_qty = " + compQtyCache );

            return (id);
        } catch (SQLException e) {
	    if (debugging)
		debug.println(1, "SQLException: INSERT into M_workorder for id "
                          + id + " : Failed : " + e.getMessage());
            debug.printStackTrace(e);

            throw new EJBException(e);
        } finally {
            Util.closeConnection(conn, stmt);
        }
    }

    /**
     * Constructs the WorkOrder object (Bean managed)
     * and stores the information into the DB.
     * @param assemblyId            Assembly Id
     * @param origQty       Original Qty
     * @param dueDate       Date when order is due
     * @return primary key of WorkOrder which is composed of id.
     */
    public Integer ejbCreate(String assemblyId, int origQty, java.sql
            .Date dueDate) throws CreateException {

        super.ejbCreate(assemblyId, origQty, dueDate);

        idCache = id;
        salesIdCache = salesId;
        oLineIdCache = oLineId;
        statusCache = status;
        origQtyCache = origQty;
        compQtyCache = compQty;
        assemblyIdCache = assemblyId;
        dueDateCache = dueDate;
        startDateCache = startDate;

        Connection        conn = null;
        PreparedStatement stmt = null;

        try {
            int ret;

            conn = dataSource.getConnection();
            stmt = conn.prepareStatement(
                "INSERT INTO M_workorder (wo_number, wo_o_id, wo_ol_id, wo_status, wo_assembly_id, wo_orig_qty, wo_comp_qty, wo_due_date, wo_start_date) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)");

            stmt.setInt(1, id.intValue());
            stmt.setInt(2, salesId);
            stmt.setInt(3, oLineId);
            stmt.setInt(4, status);
            stmt.setString(5, assemblyId);
            stmt.setInt(6, origQty);
            stmt.setInt(7, compQty);

            // SetDate null doesn't work in the Merant driver
            // We just try this workaround.
            if (dueDate == null) {
                stmt.setNull(8, Types.DATE);
            } else {
                stmt.setDate(8, dueDate);
            }

            if (startDate == null) {
                stmt.setNull(9, Types.TIMESTAMP);
            } else {
                stmt.setTimestamp(9, startDate);
            }

            if ((ret = stmt.executeUpdate()) != 1) {
		if (debugging)
		    debug.println(
                    1, "executeUpdate failed for INSERT into M_workorder, returned value "
                    + ret);

                throw new CreateException(className + "(ejbCreate):");
            }

            return (id);
        } catch (SQLException e) {
	    if (debugging)
		debug.println(
                1, "SQLException: INSERT into M_workorder : Failed : "
                + e.getMessage());
            debug.printStackTrace(e);

            throw new EJBException(e);
        } finally {
            Util.closeConnection(conn, stmt);
        }
    }

    /**
     * Finds the object in the DB based on primary key.
     * If object is found, then it will just return the
     * primary key passed in otherwise throws a FinderException.
     *
     * @param pk
     * @return the primary key of object which is of type Integer.
     */
    public Integer ejbFindByPrimaryKey(Integer pk) throws FinderException {

        Connection        conn = null;
        PreparedStatement stmt = null;

	if (debugging)
	    debug.println(3, "ejbFindByPrimaryKey");

        try {
            conn = dataSource.getConnection();
            stmt = conn.prepareStatement(
                "SELECT * FROM M_workorder WHERE wo_number = ?");

            stmt.setInt(1, pk.intValue());

            ResultSet resultSet = stmt.executeQuery();

            if (!resultSet.next()) {
		if (debugging)
		    debug.println(1, "No rows found in M_workorder where pk = "
                              + pk);

                throw new FinderException(className
                                          + "(ejbFindByPrimaryKey)");
            } else {
                return (pk);
            }
        } catch (SQLException e) {
	    if (debugging)
		debug.println(
                1, "SQLException: Select * from M_workorder where pk = " + pk
                + " : Failed : " + e.getMessage());
            debug.printStackTrace(e);

            throw new EJBException(e);
        } finally {
            Util.closeConnection(conn, stmt);
        }
    }

    /**
     * Finds all the WorkOrder objects in the DB and returns
     * an enumeration of primary keys.
     *
     * @return an enumeration of primary keys (Integer) of
     *         all WorkOrder objects in the Db.
     */
    public java.util.Enumeration ejbFindAll() throws FinderException {

        Connection        conn = null;
        PreparedStatement stmt = null;

	if (debugging)
	    debug.println(3, "ejbFindAll");

        try {
            int wid;

            conn = dataSource.getConnection();
            stmt = conn.prepareStatement("SELECT * FROM M_workorder");

            ResultSet resultSet = stmt.executeQuery();
            Vector    keys      = new Vector();

            while (resultSet.next()) {
                wid = resultSet.getInt(1);

                keys.addElement(new Integer(wid));
            }

            return keys.elements();
        } catch (SQLException e) {
	    if (debugging)
		debug.println(1, "SELECT * FROM M_workorder" + " : Failed : "
                          + e.getMessage());
            debug.printStackTrace(e);

            throw new EJBException(e);
        } finally {
            Util.closeConnection(conn, stmt);
        }
    }

    /**
     * Deletes this BOM object from the DB.
     */
    public void ejbRemove() throws RemoveException {

	if (debugging)
	    debug.println(3, "ejbRemove");
        super.ejbRemove();

        Connection        conn = null;
        PreparedStatement stmt = null;

        try {
            int ret;

            conn = dataSource.getConnection();
            stmt = conn.prepareStatement(
                "DELETE FROM M_workorder WHERE wo_number = ? " +
                "AND wo_o_id = ? AND wo_ol_id = ? " +
                "AND wo_assembly_id = ? AND wo_orig_qty = ? " + 
                "AND wo_comp_qty = ? " +
                "AND wo_due_date = ? AND wo_start_date = ?");

            stmt.setInt(1, id.intValue());

            stmt.setInt(2, salesIdCache);
            stmt.setInt(3, oLineIdCache);
            stmt.setString(4, assemblyIdCache);
            stmt.setInt(5, origQtyCache);
            stmt.setInt(6, compQtyCache);
            stmt.setDate(7, dueDateCache);
            stmt.setTimestamp(8, startDateCache);

            if ((ret = stmt.executeUpdate()) < 1) {
		if (debugging)
		    debug.println(1, "executeUpdate failed during delete of pk "
                              + id);

                throw new RemoveException(className + "(ejbRemove): ");
            }
        } catch (SQLException e) {
	    if (debugging) {
		debug.println(1, "SQLException: Delete from M_workorder where id "
                          + id);
		debug.println(1, "DELETE FROM M_workorder WHERE wo_number = "
			      + id + " : Failed : " + e.getMessage());
		}

            debug.printStackTrace(e);

            throw new EJBException(e);
        } finally {
            Util.closeConnection(conn, stmt);
        }
    }

    /**
     * Updates this WorkOrder object in the DB. This is
     * called if the information in the pool needs to be updated.
     */
    public void ejbStore() {

        super.ejbStore();

        Connection        conn = null;
        PreparedStatement stmt = null;
	PreparedStatement stmt0 = null;

	if (debugging)
	    debug.println(3, "ejbStore");

        try {
            conn = dataSource.getConnection();

            stmt = conn.prepareStatement(
                "UPDATE M_workorder SET wo_o_id = ?, wo_ol_id = ?, " +
                "wo_status = ?, wo_assembly_id = ?, wo_orig_qty = ?, wo_comp_qty = ?, " +
                "wo_due_date = ?, wo_start_date = ? WHERE wo_number = ? " +
                "AND wo_o_id = ? AND wo_ol_id = ? " +
                "AND wo_assembly_id = ? AND wo_orig_qty = ? " + 
                "AND wo_comp_qty = ? " +
                "AND wo_status = ? " +
                "AND wo_start_date = ?");
//                "AND wo_due_date = ? AND wo_start_date = ?");
            int i = 1; 
            stmt.setInt(i++, salesId);
            stmt.setInt(i++, oLineId);
            stmt.setInt(i++, status);
            stmt.setString(i++, assemblyId);
            stmt.setInt(i++, origQty);
            stmt.setInt(i++, compQty);

            // SetDate null doesn't work in the Merant driver
            // We just try this workaround.
            if (dueDate == null) {
                stmt.setNull(i++, Types.DATE);
            } else {
                stmt.setDate(i++, dueDate);
            }

            if (startDate == null) {
                stmt.setNull(i++, Types.TIMESTAMP);
            } else {
                stmt.setTimestamp(i++, startDate);
            }

            stmt.setInt(i++, id.intValue());


            stmt.setInt(i++, salesIdCache);
            stmt.setInt(i++, oLineIdCache);
            stmt.setString(i++, assemblyIdCache);
            stmt.setInt(i++, origQtyCache);
            stmt.setInt(i++, compQtyCache);
//            stmt.setDate(i++, dueDateCache);
            stmt.setInt(i++, statusCache);
            stmt.setTimestamp(i++, startDateCache);

            int ret = stmt.executeUpdate();

            debug.println(3, "Updated " + ret + " record(s)");

            if(ret != 1) {
                if(debugging) {
                     debug.println(1,"Optimistic concurrency control failed " +
                                     "in WorkOrderEnt.ejbStore() for id = " + id);

                debug.println(1,"UPDATE M_workorder SET wo_number = "  + id.intValue() +
                ", wo_o_id = " + salesId + " , wo_ol_id = " + oLineId +
                " , wo_status = " + status + ", wo_assembly_id = " + assemblyId +
                " , wo_orig_qty = " + origQty + " , wo_comp_qty = " + compQty +
                " , wo_due_date = " + dueDate + ", wo_start_date = " + startDate + " WHERE wo_number = " + id.intValue() +
                " AND wo_assembly_id = " + assemblyIdCache + " AND wo_orig_qty = " + origQtyCache +
                " AND wo_comp_qty = " + compQtyCache + " AND wo_o_id = " + salesIdCache);
                }
                throw new EJBException("Optimistic concurrency control failed ");
           }

            debug.println(3,"UPDATE M_workorder SET wo_number = "  + id.intValue() +
                ", wo_o_id = " + salesId + " , wo_ol_id = " + oLineId +
                " , wo_status = " + status + ", wo_assembly_id = " + assemblyId +
                " , wo_orig_qty = " + origQty + " , wo_comp_qty = " + compQty +
                " , wo_due_date = " + dueDate + ", wo_start_date = " + startDate + " WHERE wo_number = " + id.intValue() +
                " AND wo_assembly_id = " + assemblyIdCache + " AND wo_orig_qty = " + origQtyCache +
                " AND wo_comp_qty = " + compQtyCache + " AND wo_o_id = " + salesIdCache);
                //" AND wo_ol_id = " + oLineIdCache);
                // " AND wo_status = " +statusCache +

            idCache = id;
            salesIdCache = salesId;
            oLineIdCache = oLineId;
            statusCache = status;
            origQtyCache = origQty;
            compQtyCache = compQty;
            assemblyIdCache = assemblyId;
            dueDateCache = dueDate;
            startDateCache = startDate;
            // Need to verify if the execute succeeded.
        } catch (SQLException e) {
	    if (debugging)
		debug.println(1, "UPDATE M_workorder SET wo_number ..."
                          + " : Failed : " + e.getMessage());
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
     * Loads this WorkOrder object from the DB into the pool.
     * Called after an ejbCreate/ejbActivate is done.
     */
    public void ejbLoad() {

        Connection        conn = null;
        PreparedStatement stmt = null;

	if (debugging)
	    debug.println(3, "ejbLoad");


        try {
            conn = dataSource.getConnection();
            stmt = conn.prepareStatement(
                "SELECT wo_number, wo_o_id, wo_ol_id, wo_status, wo_assembly_id, wo_orig_qty, wo_comp_qty, wo_due_date, wo_start_date FROM M_workorder WHERE wo_number = ? ");

            stmt.setInt(1, id.intValue());

            ResultSet resultSet = stmt.executeQuery();

            if (!resultSet.next()) {
		if (debugging)
		    debug.println(1, "No rows found from M_workorder where id = "
                              + id.intValue());

                throw new NoSuchEntityException("ejbload: Row not found ");
            }

            id         = new Integer(resultSet.getInt(1));
            salesId    = resultSet.getInt(2);
            oLineId    = resultSet.getInt(3);
            status     = resultSet.getInt(4);
            assemblyId = resultSet.getString(5);
            origQty    = resultSet.getInt(6);
            compQty    = resultSet.getInt(7);
            dueDate    = resultSet.getDate(8);
            startDate  = resultSet.getTimestamp(9);

            idCache = id;
            salesIdCache = salesId;
            oLineIdCache = oLineId;
            statusCache = status;
            origQtyCache = origQty;
            compQtyCache = compQty;
            assemblyIdCache = assemblyId;
            dueDateCache = dueDate;
            startDateCache = startDate;

            super.ejbLoad();
        } catch (SQLException e) {
            if (debugging)
                debug.println(
                    1, "SELECT wo_number, wo_o_id, wo_ol_id,... : Failed : "
                    + e.getMessage());
                    debug.printStackTrace(e);

            throw new EJBException(e);
        } finally {
            Util.closeConnection(conn, stmt);
        }
    }


    public void testRecord() {

        Connection        conn = null;
        PreparedStatement stmt = null;

	if (debugging)
	    debug.println(3, "testRecord");


        try {
            conn = dataSource.getConnection();
            stmt = conn.prepareStatement(
                "SELECT wo_number, wo_o_id, wo_ol_id, wo_status, wo_assembly_id, wo_orig_qty, wo_comp_qty, wo_due_date, wo_start_date FROM M_workorder WHERE wo_number = ?");

            stmt.setInt(1, id.intValue());

            ResultSet resultSet = stmt.executeQuery();

            if (!resultSet.next()) {
		if (debugging)
		    debug.println(1, "No rows found from M_workorder where id = "
                              + id.intValue());

                throw new NoSuchEntityException("ejbload: Row not found ");
            }

            debug.println(3,
            "Record data is... " +
            " id:" + resultSet.getInt(1) +
            ", salesId: " + resultSet.getInt(2) +
            ", oLineId: " + resultSet.getInt(3) +
            ", status: " + resultSet.getInt(4) +
            ", assemblyId: " + resultSet.getString(5) +
            ", origQty: " + resultSet.getInt(6) +
            ", compQty: " + resultSet.getInt(7) +
            ", dueDate: " + resultSet.getDate(8) +
            ", startDate: " + resultSet.getTimestamp(9)
            );

        } catch (SQLException e) {
            if (debugging)
                debug.println(
                    1, "SELECT wo_number, wo_o_id, wo_ol_id,... : Failed : "
                    + e.getMessage());
                    debug.printStackTrace(e);

            throw new EJBException(e);
        } finally {
            Util.closeConnection(conn, stmt);
        }
    }
}

