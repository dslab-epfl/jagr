
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.

 * $Id: POLineBmpEJB.java,v 1.1.1.1 2002/11/16 05:35:30 emrek Exp $
 *
 */
package com.sun.ecperf.supplier.polineent.ejb;


//Import statements
import javax.ejb.*;

import javax.naming.*;

import java.rmi.*;

import java.sql.*;

import java.util.*;

import com.sun.ecperf.common.*;
import com.sun.ecperf.supplier.helper.*;


/**
 * This is the Bean managed persistence Implementation
 * of the POLine Entity bean.
 *
 * @author Damian Guy
 */
public class POLineBmpEJB extends POLineCmpEJB {

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
                        .lookup("java:comp/env/SupplierDataSource");
        } catch (NamingException e) {
            debug.printStackTrace(e);

            throw new EJBException("Failure looking up DataSource " + e);
        }
    }

    /**
     * ejbCreate: create a Purchase Order lIne.
     * @param poLineNumber - line number.
     * @param poID - purhcase order id.
     * @param pID - id of part being ordered.
     * @param qty - quantity ordered.
     * @param balance - cost of order.
     * @param leadTime - time that parts must be delivered within.
     * @param message -
     * @return POLineEntPK
     * @exception CreateException - if there is a create failure.
     */
    public POLineEntPK ejbCreate(
            int poLineNumber, int poID, String pID, int qty, double balance, int leadTime, String message)
                throws CreateException {

        super.ejbCreate(poLineNumber, poID, pID, qty, balance, leadTime,
                        message);

        Connection        connection = null;
        PreparedStatement prep       = null;

        try {
            StringBuffer insert =
                new StringBuffer("INSERT INTO S_purchase_orderline VALUES (");

            insert.append(" ?, ?, ?, ?, ?, ?, ? )");

            connection = dataSource.getConnection();
            prep       = connection.prepareStatement(insert.toString());

            prep.setInt(1, poLineNumber);
            prep.setInt(2, poID);
            prep.setString(3, pID);
            prep.setInt(4, qty);
            prep.setDouble(5, balance);
            prep.setDate(6, poLineDelDate);
            prep.setString(7, message);

            int retval = prep.executeUpdate();

            if (retval != 1) {
                throw new CreateException();
            }
        } catch (SQLException se) {
            if (debugging)
                debug.println(
                    1, "INSERT INTO S_purchase_orderline VALUES ... : Failed : "
                    + se.getMessage());
            debug.printStackTrace(se);

            throw new EJBException(se);
        } finally {
            Util.closeConnection(connection, prep);
        }

        return new POLineEntPK(poLineNumber, poID);
    }

    /**
     * ejbFindByPrimaryKey - find the POLIne that matches key.
     * @param key - Key of POLineEnt to find.
     * @return POLineEntPK
     * @exception FinderException - if there is a find exception.
     */
    public POLineEntPK ejbFindByPrimaryKey(POLineEntPK key)
            throws FinderException {

        Connection        connection = null;
        PreparedStatement prep       = null;

        try {
            String select = "SELECT pol_number FROM S_purchase_orderline "
                            + "WHERE pol_number = ? AND pol_po_id = ?";

            connection = dataSource.getConnection();
            prep       = connection.prepareStatement(select);

            prep.setInt(1, key.poLineNumber);
            prep.setInt(2, key.poLinePoID);

            ResultSet set = prep.executeQuery();

            if (!set.next()) {
                throw new ObjectNotFoundException(
                    "Row not found for poLineNumber = ");
            }

            return key;
        } catch (SQLException se) {
            if (debugging)
                debug.println(
                    1, "SELECT pol_number FROM S_purchase_orderline ... : Failed : "
                    + se.getMessage());
            debug.printStackTrace(se);

            throw new EJBException(se);
        } finally {
            Util.closeConnection(connection, prep);
        }
    }

    /**
     * ejbFindByPO - find all of the PO lines for a
     * given Purchase Order.
     * @param poID - id of the purchase order
     * @return Collection
     * @exception FinderException - if there are not any order lines for poID
     */
    public Collection ejbFindByPO(int poID) throws FinderException {

        Connection        connection = null;
        PreparedStatement prep       = null;

        try {
            String select = "SELECT pol_number FROM S_purchase_orderline "
                            + "WHERE pol_po_id = ?";

            connection = dataSource.getConnection();
            prep       = connection.prepareStatement(select);

            prep.setInt(1, poID);

            ResultSet set  = prep.executeQuery();
            ArrayList list = new ArrayList();

            while (set.next()) {
                POLineEntPK key = new POLineEntPK(set.getInt(1), poID);

                list.add(key);
            }
            return list;
        } catch (SQLException se) {
            if (debugging)
                debug.println(
                    1, " SELECT pol_number FROM S_purchase_orderline WHERE pol_po_id = "
                    + poID + " : Failed : " + se.getMessage());
            debug.printStackTrace(se);

            throw new EJBException(se);
        } finally {
            Util.closeConnection(connection, prep);
        }
    }

    /**
     * Method ejbRemove
     *
     *
     * @throws RemoveException
     *
     */
    public void ejbRemove() throws RemoveException {

        super.ejbRemove();

        Connection        connection = null;
        PreparedStatement prep       = null;

        try {
            String del = "DELETE FROM S_purchase_orderline WHERE "
                         + "pol_number = ? AND pol_po_id = ?";

            connection = dataSource.getConnection();
            prep       = connection.prepareStatement(del);

            prep.setInt(1, poLineNumber);
            prep.setInt(2, poLinePoID);

            int retval = prep.executeUpdate();

            if (retval != 1) {
                throw new RemoveException();
            }
        } catch (SQLException se) {
            if (debugging)
                debug.println(
                    1, "DELETE FROM S_purchase_orderline ... : Failed : "
                    + se.getMessage());
            debug.printStackTrace(se);

            throw new EJBException(se);
        } finally {
            Util.closeConnection(connection, prep);
        }
    }

    /**
     * Method ejbActivate
     *
     *
     */
    public void ejbActivate() {

        POLineEntPK key = (POLineEntPK) entityContext.getPrimaryKey();

        this.poLineNumber = key.poLineNumber;
        this.poLinePoID  = key.poLinePoID;
        super.ejbActivate();
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
            StringBuffer update =
                new StringBuffer("UPDATE S_purchase_orderline ");

            update.append("SET pol_p_id = ?, pol_qty = ?, pol_balance = ?, ");
            update.append("pol_deldate = ?, pol_message = ? WHERE pol_number = ? ");
            update.append("AND pol_po_id = ?");

            connection = dataSource.getConnection();
            prep       = connection.prepareStatement(update.toString());

            prep.setString(1, poLineID);
            prep.setInt(2, poLineQty);
            prep.setDouble(3, poLineBalance);
            prep.setDate(4, poLineDelDate);
            prep.setString(5, poLineMsg);
            prep.setInt(6, poLineNumber);
            prep.setInt(7, poLinePoID);
            prep.executeUpdate();
        } catch (SQLException se) {
            if (debugging)
                debug.println(1, "UPDATE S_purchase_orderline ... : Failed : "
                              + se.getMessage());
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
            String select = "SELECT * FROM S_purchase_orderline "
                            + "WHERE pol_number = ? AND pol_po_id = ?";

            connection = dataSource.getConnection();
            prep       = connection.prepareStatement(select);

            prep.setInt(1, poLineNumber);
            prep.setInt(2, poLinePoID);

            ResultSet set = prep.executeQuery();

            if (!set.next()) {
                throw new NoSuchEntityException(
                    "SELECT * FROM S_purchase_orderline ... : Row not found");
            }

            poLineNumber  = set.getInt(1);
            poLinePoID   = set.getInt(2);
            poLineID    = set.getString(3);
            poLineQty     = set.getInt(4);
            poLineBalance = set.getDouble(5);
            poLineDelDate = set.getDate(6);
            poLineMsg = set.getString(7);
            super.ejbLoad();
        } catch (SQLException se) {
            if (debugging)
                debug.println(
                    1, "SELECT * FROM S_purchase_orderline ... : Failed : "
                    + se.getMessage());
            debug.printStackTrace(se);

            throw new EJBException(se);
        } finally {
            Util.closeConnection(connection, prep);
        }
    }
}

