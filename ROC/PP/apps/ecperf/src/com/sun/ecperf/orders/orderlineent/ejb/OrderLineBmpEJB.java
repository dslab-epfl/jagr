
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: OrderLineBmpEJB.java,v 1.2 2003/03/22 04:55:01 emrek Exp $
 *
 */
package com.sun.ecperf.orders.orderlineent.ejb;


import java.sql.*;

import javax.ejb.*;

import javax.naming.*;

import java.util.*;

import com.sun.ecperf.common.*;


/**
 * This class is the BMP version of the OrderLineBean and must be used by
 * vendors who want to run the BMP implementation. The persistence code is
 * allowed to be modified only for non-SQL databases.
 */
public class OrderLineBmpEJB extends OrderLineCmpEJB {

    protected javax.sql.DataSource dataSource;

    // Added by Ramesh
    private int              idCache;
    private int              orderIdCache;
    private String           itemIdCache;
    private int              quantityCache;
    private java.sql.Date    shipDateCache;

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
                        .lookup("java:comp/env/OrdersDataSource");
        } catch (NamingException e) {
            if(debugging)
                debug.println(1, "NaimgException occured " + e);
            debug.printStackTrace(e);

            throw new EJBException("Failure looking up DataSource " + e);
        }
    }

    /**
     * Create a new row in the orderline table
     *
     * @param inId
     * @param inOrderId
     * @param inItemId
     * @param inQuantity
     * @param inShipDate
     * @return OrderLine
     * @exception CreateException - if the create fails
     */
    public OrderLineEntPK ejbCreate(
            int inId, int inOrderId, String inItemId, int inQuantity, java.sql
                .Date inShipDate) throws CreateException {

        if(debugging)
            debug.println(3, "ejbCreate");
        super.ejbCreate(inId, inOrderId, inItemId, inQuantity, inShipDate);

        // Added by Ramesh
        idCache = inId;
        orderIdCache = inOrderId;
        itemIdCache = inItemId;
        quantityCache = inQuantity;
        shipDateCache = inShipDate;


        Connection        connection = null;
        PreparedStatement statement  = null;

        try {
            connection = dataSource.getConnection();
            statement  = connection.prepareStatement(
                "INSERT INTO O_orderline (ol_id, ol_o_id, ol_i_id, ol_qty, ol_ship_date) VALUES (?, ?, ?, ?, ?)");

            statement.setInt(1, id);
            statement.setInt(2, orderId);
            statement.setString(3, itemId);
            statement.setInt(4, quantity);

            // SetDate null doesn't work in the Merant driver
            // We just try this workaround.
            if (shipDate == null) {
                statement.setNull(5, Types.DATE);
            } else {
                statement.setDate(5, shipDate);
            }

            int ret = statement.executeUpdate();

            if (ret != 1) {
                if(debugging)
                    debug.println(1, "executeUpdate() into O_orderline returned "
                                  + ret);

                throw new CreateException();
            }

            return new OrderLineEntPK(id, orderId);
        } catch (SQLException e) {
            if(debugging)
                debug.println(1, "SQLException from INSERT INTO O_orderline ("
                              + id + ", " + orderId + ", " + itemId + ", "
                              + quantity + ", " + shipDate + ")");
            debug.printStackTrace(e);

            throw new EJBException(e);
        } finally {
            Util.closeConnection(connection, statement);
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

        if(debugging)
            debug.println(3, "ejbRemove");
        super.ejbRemove();

        Connection        connection = null;
        PreparedStatement statement  = null;

        try {
            connection = dataSource.getConnection();
            statement  = connection.prepareStatement(
                "DELETE FROM O_orderline WHERE ol_id = ? AND ol_o_id = ? " + 
                "AND ol_i_id = ? AND ol_qty = ?");
//  AND ol_ship_date = ?");

            statement.setInt(1, id);
            statement.setInt(2, orderId);

            // Added by Ramesh
            statement.setString(3, itemIdCache);
            statement.setInt(4, quantityCache);

/*
            if (shipDateCache == null) {
                statement.setNull(5, Types.DATE);
            } else {
                statement.setDate(5, shipDateCache);
            }
*/

            int ret = statement.executeUpdate();

            if (ret != 1) {
	        if(debugging) {
                    debug.println(1, "executeUpdate() returned " + ret
                                  + " for: ");
                    debug.println(1, "  DELETE FROM O_orderline WHERE ol_id = "
                                  + id + " AND ol_o_id = " + orderId);
		}

                throw new RemoveException(
                    "Error in delete O_orderline for ol_id = " + id
                    + ", o_id = " + orderId);
            }
        } catch (SQLException e) {
            if(debugging)
                debug.println(
                    1, "SQLException in DELETE FROM O_orderline WHERE ol_id = "
                    + id + " AND ol_o_id = " + orderId);
            debug.println(1, e.toString());

            throw new EJBException(
                "SQLException in delete O_orderline for ol_id = " + id
                + ", o_id = " + orderId + e);
        } finally {
            Util.closeConnection(connection, statement);
        }
    }

    /**
     * Method ejbFindByPrimaryKey
     *
     *
     * @param key
     *
     * @return
     *
     * @throws FinderException
     *
     */
    public OrderLineEntPK ejbFindByPrimaryKey(OrderLineEntPK key)
            throws FinderException {

        if(debugging)
            debug.println(3, "OrderLineEntPK");

        Connection        connection = null;
        PreparedStatement statement  = null;

        try {
            connection = dataSource.getConnection();
            statement  = connection.prepareStatement(
                "SELECT ol_id FROM O_orderline WHERE ol_id = ? AND ol_o_id = ?");

            statement.setInt(1, key.id);
            statement.setInt(2, key.orderId);

            ResultSet resultSet = statement.executeQuery();

            if (!resultSet.next()) {
                if(debugging)
                    debug.println(1, "No rows found on O_orderline for ol_id ="
                                  + key.id + " , ol_o_id = " + key.orderId);

                throw new ObjectNotFoundException(
                    "O_orderline row not found for o_id : " + key.orderId
                    + ", ol_id = " + key.id);
            }

            return key;
        } catch (SQLException e) {
            if(debugging)
                debug.println(
                    1, "SQLException in SELECT FROM O_orderline WHERE ol_id = "
                    + key.id + ", ol_o_id = " + key.orderId);
            debug.printStackTrace(e);

            throw new EJBException(e);
        } finally {
            Util.closeConnection(connection, statement);
        }
    }

    // Find all orderlines for a particular order

    /**
     * @param orderId - id of order for which orderlines are required
     * @return Enumeration of orderlines
     * @exception FinderException - if the find fails
     */
    public java.util.Enumeration ejbFindByOrder(int orderId)
            throws FinderException {

        if(debugging)
            debug.println(3, "ejbFindByOrder for order " + orderId);

        Connection        connection = null;
        PreparedStatement statement  = null;

        try {
            connection = dataSource.getConnection();
            statement  = connection.prepareStatement(
                "SELECT ol_id FROM O_orderline WHERE ol_o_id = ?");

            statement.setInt(1, orderId);

            ResultSet resultSet = statement.executeQuery();
            Vector    keys      = new Vector();
            int       olcnt     = 0;

            while (resultSet.next()) {
                int id = resultSet.getInt(1);

                keys.addElement(new OrderLineEntPK(id, orderId));

                olcnt++;
            }

            if (olcnt == 0) {
                if(debugging)
                    debug.println(
                        2, "No rows found for SELECT FROM O_orderline WHERE ol_o_id = "
                        + orderId);
            }

            return keys.elements();
        } catch (SQLException e) {
            if(debugging)
                debug.println(
                    1, "SQLException in SELECT FROM O_orderline WHERE ol_o_id = "
                    + orderId);
            debug.printStackTrace(e);

            throw new EJBException(e);
        } finally {
            Util.closeConnection(connection, statement);
        }
    }

    /**
     * Find a particular item in an order
     * @param orderId - id of order for which orderline is required
     * @param itemId - id of item in order to find
     * @return OrderLine
     * @exception FinderException - if the find fails
     */
    public OrderLineEntPK ejbFindByOrderAndItem(int orderId, String itemId)
            throws FinderException {

        if(debugging)
            debug.println(3, "ejbFindByOrderAndItem");

        Connection        connection = null;
        PreparedStatement statement  = null;

        try {
            connection = dataSource.getConnection();
            statement  = connection.prepareStatement(
                "SELECT ol_id FROM O_orderline WHERE ol_o_id = ? AND ol_i_id = ?");

            statement.setInt(1, orderId);
            statement.setString(2, itemId);

            ResultSet resultSet = statement.executeQuery();

            if (!resultSet.next()) {
                if(debugging)
                    debug.println(
                        1, "No rows found for SELECT FROM O_orderline WHERE ol_o_id = "
                        + orderId + " AND ol_i_id = " + itemId);

                throw new ObjectNotFoundException();
            }

            int id = resultSet.getInt(1);

            return new OrderLineEntPK(id, orderId);
        } catch (SQLException e) {
      	    if(debugging) {
                debug.println(
                    1, "SQLException in SELECT FROM O_orderline WHERE ol_o_id = "
                    + orderId + " AND ol_i_id = " + itemId);
                debug.println(1, e.toString());
            }

            throw new EJBException(e);
        } finally {
            Util.closeConnection(connection, statement);
        }
    }

    /**
     * Method ejbActivate
     *
     *
     */
    public void ejbActivate() {

        OrderLineEntPK key = (OrderLineEntPK) entityContext.getPrimaryKey();
        id      = key.id;
        orderId = key.orderId;

        super.ejbActivate();
    }

    /**
     * Method ejbLoad
     *
     *
     */
    public void ejbLoad() {

        if(debugging)
            debug.println(3, "ejbLoad");

        Connection        connection = null;
        PreparedStatement statement  = null;

        try {
            connection = dataSource.getConnection();
            statement  = connection.prepareStatement(
                "SELECT ol_i_id, ol_qty, ol_ship_date FROM O_orderline WHERE ol_id = ? AND ol_o_id = ? ");

            statement.setInt(1, id);
            statement.setInt(2, orderId);

            ResultSet resultSet = statement.executeQuery();

            if (!resultSet.next()) {
                if(debugging)
                    debug.println(1, "No rows found in O_orderline for ol_id = "
                                  + id + ", ol_o_id = " + orderId);

                throw new EJBException("Row not found");
            }

            itemId   = resultSet.getString(1);
            quantity = resultSet.getInt(2);
            shipDate = resultSet.getDate(3);
            
            // Added by Ramesh
            itemIdCache = itemId;
            quantityCache = quantity;
            shipDateCache = shipDate;

        } catch (SQLException e) {
            if(debugging) {
                debug.println(
                    1, "SQLException in SELECT FROM O_orderline for ol_id = "
                    + id + ", ol_o_id = " + orderId);
                debug.println(1, e.toString());
            }

            throw new EJBException(e);
        } finally {
            Util.closeConnection(connection, statement);
        }

        super.ejbLoad();
    }

    /**
     * Method ejbStore
     *
     *
     */
    public void ejbStore() {

        if(debugging)
            debug.println(3, "ejbStore");
        super.ejbStore();

        Connection        connection = null;
        PreparedStatement statement  = null;

        try {
            connection = dataSource.getConnection();

            if(debugging)
                debug.println(3, "Updating O_orderline ol_id = " + id
                          + ", ol_o_id = " + orderId + ". ol_i_id set to "
                          + itemId + ", ol_qty set to " + quantity);

            // Modified by Ramesh
            statement = connection.prepareStatement(
                "UPDATE O_orderline SET ol_i_id = ?, ol_qty = ?, ol_ship_date = ? WHERE ol_id = ? AND ol_o_id = ? AND ol_i_id = ? AND ol_qty = ?");

//  AND ol_ship_date = ?");

            statement.setString(1, itemId);
            statement.setInt(2, quantity);

            // SetDate null doesn't work in the Merant driver
            // We just try this workaround.
            if (shipDate == null) {
                statement.setNull(3, Types.DATE);
            } else {
                statement.setDate(3, shipDate);
            }
            statement.setInt(4, id);
            statement.setInt(5, orderId);
            
            // Added by Ramesh
            statement.setString(6, itemIdCache);
            statement.setInt(7, quantityCache);
/*
            if (shipDateCache == null) {
                statement.setNull(8, Types.DATE);
            } else {
                statement.setDate(8, shipDateCache);
            }
*/
            int ret = statement.executeUpdate();
            if(ret != 1) {
                if(debugging) {
                     debug.println(1,"Optimistic concurrency control failed " + 
                                     "in OrderLineEnt.ejbStore() for id = " + id);
                }
                throw new EJBException("Optimistic concurrency control failed ");
           }
            itemIdCache = itemId;
            quantityCache = quantity;
            shipDateCache = shipDate;
                     
        } catch (SQLException e) {
            if(debugging) {
                debug.println(1, "SQLException from UPDATE O_orderline (" + id
                          + ", " + orderId + ", " + itemId + ", " + quantity
                          + ", " + shipDate + ")");
                debug.printStackTrace(e);
            }

            throw new NoSuchEntityException(e.toString());
        } finally {
            Util.closeConnection(connection, statement);
        }
    }
}

