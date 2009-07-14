
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id $
 *
 */
package com.sun.ecperf.orders.ordercustomerent.ejb;


import javax.ejb.*;

import javax.naming.*;

import java.rmi.*;

import java.util.*;

import com.sun.ecperf.orders.helper.*;
import com.sun.ecperf.common.*;

import java.sql.*;


/**
 * This class implements the BMP version of the OrdersCustomerEnt entity bean.
 * It is responsible for performing operations on the customer table
 * in the Orders database.
 * @see OrderCustomerCmpEJB
 */
public class OrderCustomerBmpEJB extends OrderCustomerCmpEJB {

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
                        .lookup("java:comp/env/OrdersDataSource");
        } catch (NamingException e) {
            debug.printStackTrace(e);

            throw new EJBException("Failure looking up DataSource " + e);
        }
    }

    /**
     * The ejbCreate method gets called when a new customer needs to
     * be created. This method first generates a new unique customer id.
     * It then validates the various fields passed (done solely to
     * introduce some business logic in the bean).
     * Finally, it adds the new customer record into the database.
     * @param info - CustomerInfo object
     * @return customer id
     * @exception InvalidInfoException if CustomerInfo
     *             fails validation checks
     * @exception CreateException if the create fails
     */
    public Integer ejbCreate(CustomerInfo info)
            throws InvalidInfoException, DataIntegrityException, CreateException {

        if(debugging)
            debug.println(3, "ejbCreate ");
        super.ejbCreate(info);

        Connection        connection = null;
        PreparedStatement statement  = null;

        try {
            connection = dataSource.getConnection();

            StringBuffer sqlbuf = new StringBuffer("");

            sqlbuf.append("INSERT INTO O_customer (");
            sqlbuf.append("c_id, ");
            sqlbuf.append("c_first, ");
            sqlbuf.append("c_last, ");
            sqlbuf.append("c_street1, ");
            sqlbuf.append("c_street2, ");
            sqlbuf.append("c_city, ");
            sqlbuf.append("c_state, ");
            sqlbuf.append("c_country, ");
            sqlbuf.append("c_zip, ");
            sqlbuf.append("c_phone, ");
            sqlbuf.append("c_contact, ");
            sqlbuf.append("c_since ");
            sqlbuf.append(") VALUES ( ?,?,?,?,?,?,?,?,?,?,?,? ) ");

            String sqlstr = sqlbuf.toString();

            statement = connection.prepareStatement(sqlstr);

            int i = 1;

            statement.setInt(i++, customerId.intValue());
            statement.setString(i++, firstName);
            statement.setString(i++, lastName);
            statement.setString(i++, address.street1);
            statement.setString(i++, address.street2);
            statement.setString(i++, address.city);
            statement.setString(i++, address.state);
            statement.setString(i++, address.country);
            statement.setString(i++, address.zip);
            statement.setString(i++, address.phone);
            statement.setString(i++, contact);
            statement.setDate(i++, customerSince);

            int ret = statement.executeUpdate();

            if (ret != 1) {
	        if(debugging) {
                    debug.println(1, "executeUpdate() returned " + ret
                                  + " for :");
                    debug.println(1, "  INSERT INTO O_customer(" + customerId
                                  + ", " + firstName + ", " + lastName + ", "
                                  + address.street1 + ", " + address.street2
                                  + ", " + address.city + ", " + address.state
                                  + ", " + address.country + ", " + address.zip
                                  + ")");
                }

                throw new CreateException();
            }

            return customerId;
        } catch (SQLException e) {
	    if(debugging) {
                debug.println(1, "SQLException in INSERT INTO O_customer("
                              + customerId + ", " + firstName + ", " + lastName
                              + ", " + address.street1 + ", " + address.street2
                              + ", " + address.city + ", " + address.state + ", "
                              + address.country + ", " + address.zip + ")");
                debug.println(1, e.toString());
            }
            debug.printStackTrace(e);

            throw new EJBException(e);
        } finally {
            Util.closeConnection(connection, statement);
        }
    }

    /**
     * Method ejbFindByPrimaryKey
     *
     *
     * @param id
     *
     * @return
     *
     * @throws FinderException
     *
     */
    public Integer ejbFindByPrimaryKey(Integer id) throws FinderException {

        if(debugging)
            debug.println(3, "ejbFindByPrimaryKey: " + id);

        Connection        connection = null;
        PreparedStatement statement  = null;

        try {
            connection = dataSource.getConnection();
            statement  = connection.prepareStatement(
                "SELECT c_id FROM O_customer WHERE c_id = ?");

            statement.setInt(1, id.intValue());

            ResultSet resultSet = statement.executeQuery();

            if (!resultSet.next()) {
                if(debugging)
                    debug.println(
                        1, "SELECT from O_customer returned no rows for c_id: "
                        + id);

                throw new ObjectNotFoundException("Customer " + id
                                                  + " not found");
            }

            return id;
        } catch (SQLException e) {
	    if(debugging) {
                debug.println(
                    1, "SQLException in SELECT FROM O_customer WHERE c_id = "
                    + id);
                debug.println(1, e.toString());
            }
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
            debug.println(3, "ejbRemove ");
        super.ejbRemove();

        Connection        connection = null;
        PreparedStatement statement  = null;

        try {
            connection = dataSource.getConnection();
            statement  = connection
                .prepareStatement("DELETE FROM O_customer WHERE c_id = ?");

            statement.setInt(1, customerId.intValue());

            int ret = statement.executeUpdate();

            if (ret != 1) {
	        if(debugging) {
                    debug.println(1, "executeUpdate() returned " + ret
                                  + " for :");
                    debug.println(1, "  DELETE FROM O_customer WHERE c_id = "
                                  + customerId);
		}

                throw new RemoveException();
            }
        } catch (SQLException e) {
	    if(debugging) {
                debug.println(
                    1, "SQLException in  DELETE FROM O_customer WHERE c_id = "
                    + customerId);
                debug.println(1, e.toString());
	    }
            debug.printStackTrace(e);

            throw new EJBException(e.toString());
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
        customerId = (Integer) entityContext.getPrimaryKey();
        super.ejbActivate();
    }


    /**
     * Method ejbLoad
     *
     *
     */
    public void ejbLoad() {

        if(debugging)
            debug.println(3, "ejbLoad ");

        Connection        connection = null;
        PreparedStatement statement  = null;
        int               pkey       = customerId.intValue();

        try {
            connection = dataSource.getConnection();

            StringBuffer sqlbuf = new StringBuffer("");

            sqlbuf.append("SELECT ");
            sqlbuf.append("c_id, ");
            sqlbuf.append("c_first, ");
            sqlbuf.append("c_last, ");
            sqlbuf.append("c_street1, ");
            sqlbuf.append("c_street2, ");
            sqlbuf.append("c_city, ");
            sqlbuf.append("c_state, ");
            sqlbuf.append("c_country, ");
            sqlbuf.append("c_zip, ");
            sqlbuf.append("c_phone, ");
            sqlbuf.append("c_contact, ");
            sqlbuf.append("c_since ");
            sqlbuf.append("FROM O_customer WHERE ");
            sqlbuf.append("c_id = ?  ");

            String sqlstr = sqlbuf.toString();

            statement = connection.prepareStatement(sqlstr);

            statement.setInt(1, pkey);

            ResultSet resultSet = statement.executeQuery();

            if (!resultSet.next()) {
                if(debugging)
                    debug.println(
                        1, "SELECT from O_customer returned no rows for c_id: "
                        + pkey);

                throw new NoSuchEntityException("Customer id " + pkey
                                                + " not found");
            }

            int i = 1;

            customerId      = new Integer(resultSet.getInt(i++));
            firstName       = resultSet.getString(i++);
            lastName        = resultSet.getString(i++);
            address.street1 = resultSet.getString(i++);
            address.street2 = resultSet.getString(i++);
            address.city    = resultSet.getString(i++);
            address.state   = resultSet.getString(i++);
            address.country = resultSet.getString(i++);
            address.zip     = resultSet.getString(i++);
            address.phone   = resultSet.getString(i++);
            contact         = resultSet.getString(i++);
            customerSince   = resultSet.getDate(i++);
        } catch (SQLException e) {
            debug.printStackTrace(e);

            throw new NoSuchEntityException(e.toString());
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
            debug.println(3, "ejbStore ");
        super.ejbStore();

        Connection        connection = null;
        PreparedStatement statement  = null;

        try {
            connection = dataSource.getConnection();

            StringBuffer sqlbuf = new StringBuffer("");

            sqlbuf.append("UPDATE  O_customer SET ");
            sqlbuf.append("c_first = ?, ");
            sqlbuf.append("c_last = ?, ");
            sqlbuf.append("c_street1 = ?, ");
            sqlbuf.append("c_street2 = ?, ");
            sqlbuf.append("c_city = ?, ");
            sqlbuf.append("c_state = ?, ");
            sqlbuf.append("c_country = ?, ");
            sqlbuf.append("c_zip = ?, ");
            sqlbuf.append("c_phone = ?, ");
            sqlbuf.append("c_contact = ?, ");
            sqlbuf.append("c_since = ? ");
            sqlbuf.append("WHERE c_id = ? ");

            String sqlstr = sqlbuf.toString();

            statement = connection.prepareStatement(sqlstr);

            int i = 1;

            statement.setString(i++, firstName);
            statement.setString(i++, lastName);
            statement.setString(i++, address.street1);
            statement.setString(i++, address.street2);
            statement.setString(i++, address.city);
            statement.setString(i++, address.state);
            statement.setString(i++, address.country);
            statement.setString(i++, address.zip);
            statement.setString(i++, address.phone);
            statement.setString(i++, contact);
            statement.setDate(i++, customerSince);
            statement.setInt(i++, customerId.intValue());

            int ret = statement.executeUpdate();

            if (ret != 1) {
	        if(debugging) {
                    debug.println(1, "executeUpdate() returned " + ret
                                  + " for c_id " + customerId + ": ");
                    debug.println(1, "UPDATE O_customer ( " + firstName + ", "
                                  + lastName + ", " + address.street1 + ", "
                                  + address.street2 + ", " + address.city + ", "
                                  + address.state + ", " + address.country + ", "
                                  + address.zip + ")");
		}
            }
        } catch (SQLException e) {
	    if(debugging) {
                debug.println(1, "SQLException in UPDATE O_customer for c_id "
                              + customerId);
                debug.println(1, firstName + ", " + lastName + ", "
                              + address.street1 + ", " + address.street2 + ", "
                              + address.city + ", " + address.state + ", "
                              + address.country + ", " + address.zip + ")");
	    }
            debug.printStackTrace(e);

            throw new NoSuchEntityException(e.toString());
        } finally {
            Util.closeConnection(connection, statement);
        }
    }
}

