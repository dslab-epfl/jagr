
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: OrderBmpEJB.java,v 1.1 2004/02/19 14:45:05 emrek Exp $
 *
 *
 */
package com.sun.ecperf.orders.orderent.ejb;


import javax.ejb.*;

import javax.naming.*;

import java.rmi.*;

import java.util.*;

//import mfg.interfaces.*;
import com.sun.ecperf.common.*;

import java.sql.*;

import com.sun.ecperf.orders.helper.*;

/**
 * This class is the BMP version of the OrderLineCmpEJB and must be used by
 * vendors who want to run the BMP implementation. The persistence code is
 * allowed to be modified only for non-SQL databases.
 * @see OrderCmpEJB for more details
 */
public class OrderBmpEJB extends OrderCmpEJB
{

    protected javax.sql.DataSource dataSource;

    // Optimistic concurrency control
    private Integer idCache;
    private int customerIdCache;
    private int orderLineCountCache;
    private double discountCache;
    private double totalCache;
    private java.sql.Timestamp entryDateCache;
    private java.sql.Date shipDateCache;
    private int orderStatusCache;

    /**
     * The ejbCreate method gets called when a new order needs to
     * be created. This method first generates a new unique order id.
     * It then checks the customer's credit by making a call to the
     * customer bean in the Corp domain. If the customer has sufficient
     * credit, the orderlines that make up this order are created.
     * @param customerId - id of customer creating the order
     * @param quantities - item,qty pairs for orderlines
     * @return order id
     * @exception InsufficientCreditException if customer
     *             doesn't have sufficient credit for the order total
     * @exception CreateException if the create fails
     */
    public Integer ejbCreate(int customerId, ItemQuantity[] quantities)
            throws InsufficientCreditException, CreateException
    {

        if (debugging)
            debug.println(3, "ejbCreate");
        super.ejbCreate(customerId, quantities);

        idCache = id;
        customerIdCache = customerId;
        orderLineCountCache = orderLineCount;
        discountCache = discount;
        totalCache = total;
        entryDateCache = entryDate;
        shipDateCache = shipDate;
        orderStatusCache = orderStatus;

        /*
         * According to the EJB1.1 spec, we should first check
         * whether the given order id already exists and if so
         * issue a DuplicateKeyException.
         * However, since we are generating the order id key in
         * the ejbCreate method of OrderCmpEJB, we know for certain
         * that the key is unique, so we are bypassing the code
         * to do the FindByPrimaryKey
         * The above applies all entity beans where ejbCreate is
         * called within the context of ECperf.
         */
        Connection connection = null;
        PreparedStatement statement = null;

        try
        {
            connection = dataSource.getConnection();

            StringBuffer sqlbuf = new StringBuffer("");

            sqlbuf.append("INSERT INTO O_orders (o_id, o_c_id, o_ol_cnt, ");
            sqlbuf.append("o_discount, o_total, o_status, o_entry_date, ");
            sqlbuf.append("o_ship_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");

            String sqlstr = sqlbuf.toString();

            statement = connection.prepareStatement(sqlstr);

            int i = 1;

            statement.setInt(i++, id.intValue());
            statement.setInt(i++, customerId);
            statement.setInt(i++, orderLineCount);
            statement.setDouble(i++, discount);
            statement.setDouble(i++, total);
            statement.setInt(i++, orderStatus);

            // SetDate null doesn't work in the Merant driver
            // We just try this workaround.
            if (entryDate == null)
            {
                statement.setNull(i++, Types.TIMESTAMP);
            }
            else
            {
                statement.setTimestamp(i++, entryDate);
            }

            if (shipDate == null)
            {
                statement.setNull(i++, Types.DATE);
            }
            else
            {
                statement.setDate(i++, shipDate);
            }

            int ret = statement.executeUpdate();

            if (ret != 1)
            {
                if (debugging)
                    debug.println(1,
                            "executeUpdate() returned " + ret + " for :");
                if (debugging)
                    debug.println(1,
                            "  INSERT INTO O_orders(" + id + ", " +
                            customerId + ", " + orderLineCount +
                            ", " + discount + ", " + total + ", " +
                            orderStatus + ", " + entryDate + ", " +
                            shipDate + ")");

                throw new CreateException("INSERT INTO O_orders(" +
                        id + ", " + customerId + ", " +
                        orderLineCount + ", " + discount + ", " +
                        total + ", " + orderStatus + ", " +
                        entryDate + ", " + shipDate + ")");
            }

            return id;
        }
        catch (SQLException e)
        {
            if (debugging)
                debug.println(1,
                        "SQLException in INSERT INTO O_orders(" + id +
                        ", " + customerId + ", " + orderLineCount +
                        ", " + discount + ", " + total + ", " +
                        orderStatus + ", " + entryDate + ", " +
                        shipDate + ")");
            debug.printStackTrace(e);

            throw new EJBException(e);
        }
        finally { Util.closeConnection(connection, statement);
                } }

    /**
      * ejbFindByPrimaryKey
      */
    public Integer ejbFindByPrimaryKey(Integer key) throws FinderException
    {

        Connection connection = null;
        PreparedStatement statement = null;

        if (debugging)
            debug.println(3, "ejbFindbyPrimaryKey: " + key);

        try
        {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("SELECT o_id FROM O_orders  WHERE o_id = ?");

            statement.setInt(1, key.intValue());

            ResultSet resultSet = statement.executeQuery();

            if (!resultSet.next())
            {
                if (debugging)
                    debug.println(2,
                            "O_orders row not found for o_id : " + key);

                throw new ObjectNotFoundException(
                        "O_orders row not found for o_id : " + key);
            }

            return key;
        }
        catch (SQLException e)
        {
            if (debugging)
                debug.println(1,
                        "SQLException in find by primary key for o_id : " +
                        key);

            throw new EJBException(e);
        }
        finally { Util.closeConnection(connection, statement);
                } }

    /**
      * ejbFindByCustomer
      */
    public java.util.Enumeration ejbFindByCustomer(int cid)
            throws FinderException
    {

        if (debugging)
            debug.println(3, "ejbFindByCustomer ");

        Connection connection = null;
        PreparedStatement statement = null;

        try
        {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("SELECT o_id FROM O_orders  WHERE o_c_id = ?");

            statement.setInt(1, cid);

            ResultSet resultSet = statement.executeQuery();
            Vector keys = new Vector();

            while (resultSet.next())
            {
                int id = resultSet.getInt(1);

                keys.addElement(new Integer(id));
            }
            return keys.elements();

        }
        catch (SQLException e)
        {
            if (debugging)
                debug.println(1,
                        "SQLException in SELECT orders by customer " + cid);
            debug.printStackTrace(e);

            throw new EJBException(e);
        }
        finally { Util.closeConnection(connection, statement);
                } }

    /**
      * Method ejbRemove
      *
      *
      * @throws RemoveException
      *
      */
    public void ejbRemove() throws RemoveException
    {

        if (debugging)
            debug.println(3, "ejbRemove ");

        // call the superclass to remove orderlines
        super.ejbRemove();

        // now remove the order from the database
        if (debugging)
            debug.println(3, "ejbRemove " + "id = " + id);

        Connection connection = null;
        PreparedStatement statement = null;

        try
        {

            // remove the Order EJB when (if) all the OrderLines are gone
            connection = dataSource.getConnection();
            statement =
               /**
                  Postgress has problems with rounding errors for doubles.
               connection .prepareStatement("DELETE FROM O_orders WHERE o_id = ? " +
                                            "AND  o_c_id = ? AND o_ol_cnt = ?  AND o_discount = ? " +
                                            "AND o_total = ? AND o_status = ? " + 
                                            "AND o_entry_date = ?");
               */
               connection .prepareStatement("DELETE FROM O_orders WHERE o_id = ? " +
                                            "AND  o_c_id = ? AND o_ol_cnt = ?  " +
                                            "AND o_status = ? " + 
                                            "AND o_entry_date = ?");
            //                     AND o_ship_date = ? ");

            int i = 1;
            statement.setInt(i++, id.intValue());

            statement.setInt(i++, customerIdCache);
            statement.setInt(i++, orderLineCountCache);
            //statement.setDouble(i++, discountCache);
            //statement.setDouble(i++, totalCache);
            statement.setInt(i++, orderStatusCache);
            statement.setTimestamp(i++, entryDateCache);

/*
            statement.setDate(i++, shipDateCache);
*/
            int ret = statement.executeUpdate();

            if (ret != 1)
            {
                if (debugging)
                {
                    debug.println(1,
                            "executeUpdate() returned " + ret + " for :");
                    debug.println(1,
                            "DELETE from O_orders WHERE o_id = " + id);
                }

                throw new RemoveException();
            }
        }
        catch (SQLException e)
        {
            if (debugging)
                debug.println(1,
                        "SQLException in DELETE from O_orders WHERE o_id = " +
                        id);
            debug.printStackTrace(e);

            throw new EJBException(e);
        }
        finally { Util.closeConnection(connection, statement);
                } }

    /**
      * Method ejbActivate
      *
      *
      */
    public void ejbActivate()
    {
        id = (Integer) entityContext.getPrimaryKey();
        super.ejbActivate();
    }

    /**
      * Method ejbLoad
      *
      *
      */
    public void ejbLoad()
    {

        int pkey = id.intValue();

        if (debugging)
            debug.println(3, ":ejbLoad for key " + pkey);

        Connection connection = null;
        PreparedStatement statement = null;

        try
        {
            connection = dataSource.getConnection();

            StringBuffer sqlbuf = new StringBuffer("");

            sqlbuf.append("SELECT ");
            sqlbuf.append("o_id, ");
            sqlbuf.append("o_c_id, ");
            sqlbuf.append("o_ol_cnt, ");
            sqlbuf.append("o_discount, ");
            sqlbuf.append("o_total, ");
            sqlbuf.append("o_status, ");
            sqlbuf.append("o_entry_date, ");
            sqlbuf.append("o_ship_date ");
            sqlbuf.append("FROM O_orders WHERE o_id = ? ");

            String sqlstr = sqlbuf.toString();

            statement = connection.prepareStatement(sqlstr);

            statement.setInt(1, pkey);

            ResultSet resultSet = statement.executeQuery();

            if (!resultSet.next())
            {
                if (debugging)
                    debug.println(1,
                            "O_orders row not found for o_id = " + pkey);

                throw new NoSuchEntityException(
                        "O_orders row not found for o_id = " + pkey);
            }

            int i = 1;

            id = new Integer(resultSet.getInt(i++));
            customerId = resultSet.getInt(i++);
            orderLineCount = resultSet.getInt(i++);
            discount = resultSet.getDouble(i++);
            total = resultSet.getDouble(i++);
            orderStatus = resultSet.getInt(i++);
            entryDate = resultSet.getTimestamp(i++);
            shipDate = resultSet.getDate(i++);

            idCache = id;
            customerIdCache = customerId;
            orderLineCountCache = orderLineCount;
            discountCache = discount;
            totalCache = total;
            entryDateCache = entryDate;
            shipDateCache = shipDate;
            orderStatusCache = orderStatus;

        }
        catch (SQLException e)
        {
            if (debugging)
                debug.println(1,
                        "SQLException in SELECT FROM O_orders WHERE o_id = " +
                        pkey);
            debug.printStackTrace(e);

            throw new EJBException(e);
        }
        finally { Util.closeConnection(connection, statement);
                }
        super.ejbLoad();
    }

    /**
      * Method ejbStore
      *
      *
      */
    public void ejbStore()
    {

        if (debugging)
            debug.println(3, "ejbStore ");
        super.ejbStore();

        Connection connection = null;
        PreparedStatement statement = null;

        try
        {
            connection = dataSource.getConnection();

            StringBuffer sqlbuf = new StringBuffer("");

            sqlbuf.append("UPDATE O_orders SET o_c_id = ?, o_ol_cnt = ?, ");
            sqlbuf.append("o_discount = ?, o_total = ?, o_status = ?, ");
            sqlbuf.append("o_entry_date = ?, o_ship_date = ? ");
            /*
              Postgres has problems with rounding errors for doubles.
            sqlbuf.append("WHERE o_id = ? AND  o_c_id = ? AND o_ol_cnt = ? ");
            sqlbuf.append("AND o_discount = ? AND o_total = ? AND o_status = ? ");
            sqlbuf.append("AND o_entry_date = ?");
            */
            sqlbuf.append("WHERE o_id = ? AND  o_c_id = ? AND o_ol_cnt = ? ");
            sqlbuf.append("AND o_status = ? ");
            sqlbuf.append("AND o_entry_date = ?");

//            sqlbuf.append("AND o_entry_date = ? AND o_ship_date = ? ");

            String sqlstr = sqlbuf.toString();

            statement = connection.prepareStatement(sqlstr);

            int i = 1;

            statement.setInt(i++, customerId);
            statement.setInt(i++, orderLineCount);
            statement.setDouble(i++, discount);
            statement.setDouble(i++, total);
            statement.setInt(i++, orderStatus);

            // SetDate null doesn't work in the Merant driver
            // We just try this workaround.
            if (entryDate == null)
            {
                statement.setNull(i++, Types.TIMESTAMP);
            }
            else
            {
                statement.setTimestamp(i++, entryDate);
            }

            if (shipDate == null)
            {
                statement.setNull(i++, Types.DATE);
            }
            else
            {
                statement.setDate(i++, shipDate);
            }

            statement.setInt(i++, id.intValue());

            statement.setInt(i++, customerIdCache);
            statement.setInt(i++, orderLineCountCache);

            //statement.setDouble(i++, discountCache);
            //statement.setDouble(i++, totalCache);
            statement.setInt(i++, orderStatusCache);
            statement.setTimestamp(i++, entryDateCache);

/*
            statement.setDate(i++, shipDateCache);
*/
            int ret = statement.executeUpdate();

            if (ret != 1)
            {
                if (debugging)
                    debug.println(1,
                            "executeUpdate() returned " + ret + " in ejbStore() of O_orders");

                throw new NoSuchEntityException("O_orders " + id);
            }
            idCache = id;
            customerIdCache = customerId;
            orderLineCountCache = orderLineCount;
            discountCache = discount;
            totalCache = total;
            entryDateCache = entryDate;
            shipDateCache = shipDate;
            orderStatusCache = orderStatus;
        }
        catch (SQLException e)
        {
            if (debugging)
                debug.println(1,
                        "SQLException in UPDATE O_orders row " + id +
                        " SET o_c_id = " + customerId +
                        ", o_ol_cnt = " + orderLineCount +
                        ", o_discount = " + discount +
                        ", o_total = " + total + ", o_status = " +
                        orderStatus + ", o_entry_date = " +
                        entryDate + ", o_ship_date = " + shipDate);
            debug.printStackTrace(e);

            throw new EJBException(e);
        }
        finally { Util.closeConnection(connection, statement);
                } }

    /**
      * Method setEntityContext
      *
      *
      * @param entityContext
      *
      */
    public void setEntityContext(EntityContext entityContext)
    {

        super.setEntityContext(entityContext);

        Context context = null;

        try
        {
            context = new InitialContext();
            dataSource = (javax.sql .DataSource) context .lookup("java:comp/env/OrdersDataSource");
        }
        catch (NamingException e)
        {
            if (debugging)
                debug.println(1, "Failure looking up DataSource " + e);
            debug.printStackTrace(e);

            throw new EJBException("Failure looking up DataSource " + e);
        }
    }
}


