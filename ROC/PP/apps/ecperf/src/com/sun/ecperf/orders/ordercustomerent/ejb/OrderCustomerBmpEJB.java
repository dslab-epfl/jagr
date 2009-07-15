
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
 *
 * @modified by Henry Chen 3/15/02
 *
 * @see OrderCustomerCmpEJB
 */
public class OrderCustomerBmpEJB extends OrderCustomerCmpEJB {

    protected javax.sql.DataSource dataSource;

    private int              customerIdCache;
    private String           firstNameCache;
    private String           lastNameCache;
    private String           street1Cache;
    private String           street2Cache;
    private String           cityCache;
    private String           stateCache;
    private String           countryCache;
    private String           zipCache;
    private String           phoneCache;
    private String           contactCache;
    private java.sql.Date    customerSinceCache;
    private boolean          beingCreated = false;

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

		beingCreated = true;

        if(debugging)
            debug.println(3, "ejbCreate ");

        super.ejbCreate(info);

        Connection        connection = null;
        PreparedStatement statement  = null;

        customerIdCache = customerId.intValue();
        firstNameCache = firstName;
        lastNameCache = lastName;
        street1Cache = address.street1;
        street2Cache = address.street2;
        cityCache = address.city;
        stateCache = address.state;
        countryCache = address.country;
        zipCache = address.zip;
        phoneCache = address.phone;
        contactCache = contact;
        customerSinceCache = customerSince;

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

            // SetDate null doesn't work in the Merant driver
            // We just try this workaround.
            if (customerSince == null) {
                statement.setNull(i++, Types.DATE);
            } else {
                statement.setDate(i++, customerSince);
            }

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
	    	beingCreated = false;
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
                .prepareStatement("DELETE FROM O_customer WHERE c_id = ?" +
				  " AND c_id = ? AND c_first = ? AND c_last = ? AND c_street1 = ?" +
				  " AND c_street2 = ? AND c_city = ? AND c_state = ? AND c_country = ?" +
				  " AND c_zip = ? AND c_phone = ? AND c_contact = ?");

            int i = 1;
            statement.setInt(i++, customerId.intValue());
            statement.setInt(i++, customerIdCache);
            statement.setString(i++, firstNameCache);
            statement.setString(i++, lastNameCache);
            statement.setString(i++, street1Cache);
            statement.setString(i++, street2Cache);
            statement.setString(i++, cityCache);
            statement.setString(i++, stateCache);
            statement.setString(i++, countryCache);
            statement.setString(i++, zipCache);
            statement.setString(i++, phoneCache);
            statement.setString(i++, contactCache);

            int ret = statement.executeUpdate();

            if (ret != 1) {
	        if(debugging) {
                    debug.println(1, "executeUpdate() returned " + ret + " for :");
                    debug.println(1, " DELETE FROM O_customer WHERE c_id = " + customerId.intValue() +
		                     " AND c_id = " +  customerIdCache + " AND c_first = " + firstNameCache +
				     " AND c_last = " + lastNameCache + " AND c_street1 = " + street1Cache +
				     " AND c_street2 = " + street2Cache + " AND c_city = " + cityCache +
				     " AND c_state = " + stateCache + " AND c_country = " + countryCache +
				     " AND c_zip = " + zipCache + " AND c_phone = " + phoneCache +
				     " AND c_contact = " + contactCache);
		}
                throw new RemoveException();
            }
        } catch (SQLException e) {
	    if(debugging) {
                debug.println(1, "SQLException in DELETE FROM O_customer WHERE c_id = " + customerId.intValue() +
			         " AND c_id = " +  customerIdCache + " AND c_first = " + firstNameCache +
			         " AND c_last = " + lastNameCache + " AND c_street1 = " + street1Cache +
			         " AND c_street2 = " + street2Cache + " AND c_city = " + cityCache +
			         " AND c_state = " + stateCache + " AND c_country = " + countryCache +
			         " AND c_zip = " + zipCache + " AND c_phone = " + phoneCache +
			         " AND c_contact = " + contactCache);
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

        if (beingCreated)
	  return;

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

	    customerIdCache = customerId.intValue();
	    firstNameCache = firstName;
	    lastNameCache = lastName;
	    street1Cache = address.street1;
	    street2Cache = address.street2;
	    cityCache = address.city;
	    stateCache = address.state;
	    countryCache = address.country;
	    zipCache = address.zip;
	    phoneCache = address.phone;
	    contactCache = contact;
	    customerSinceCache = customerSince;

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

        if (beingCreated)
	  return;

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
            sqlbuf.append("WHERE c_id = ? AND ");
            sqlbuf.append("c_first = ? AND ");
            sqlbuf.append("c_last = ? AND ");
            sqlbuf.append("c_street1 = ? AND ");
            sqlbuf.append("c_street2 = ? AND ");
            sqlbuf.append("c_city = ? AND ");
            sqlbuf.append("c_state = ? AND ");
            sqlbuf.append("c_country = ? AND ");
            sqlbuf.append("c_zip = ? AND ");
            sqlbuf.append("c_phone = ? AND ");
            sqlbuf.append("c_contact = ? AND ");
            sqlbuf.append("c_since = ? ");
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

            // SetDate null doesn't work in the Merant driver
            // We just try this workaround.
            if (customerSince == null) {
                statement.setNull(i++, Types.DATE);
            } else {
                statement.setDate(i++, customerSince);
            }

            statement.setInt(i++, customerId.intValue());
            statement.setString(i++, firstNameCache);
            statement.setString(i++, lastNameCache);
            statement.setString(i++, street1Cache);
            statement.setString(i++, street2Cache);
            statement.setString(i++, cityCache);
            statement.setString(i++, stateCache);
            statement.setString(i++, countryCache);
            statement.setString(i++, zipCache);
            statement.setString(i++, phoneCache);
            statement.setString(i++, contactCache);
            statement.setDate(i++, customerSinceCache);

            int ret = statement.executeUpdate();

            if (ret != 1) {
	        if(debugging) {
                    debug.println(1,"Optimistic concurrency control failed " +
                                    "in OrderCustomerEnt.ejbStore() for id " + customerId);
		    debug.println(1, "SQLException in  UPDATE O_customer SET c_first = " + firstName +
				  ", c_last = " + lastName + ", c_street1 = " + address.street1 +
				  ", c_street2 = " + address.street2 + ", c_city = " + address.city +
				  ", c_state = " + address.state + ", c_country = " + address.country +
				  ", address.c_zip = " + address.zip + ", c_phone = " + address.phone +
				  ", c_contact = " + contact + ", c_since = " + customerSince +
				  " WHERE c_id = " + customerId.intValue() + 
				  " AND c_first = " + firstNameCache + " AND c_last = " + lastNameCache +
				  " AND c_street1 = " + street1Cache + " AND c_street2 = " + street2Cache +
				  " AND c_city = " + cityCache + " AND c_state = " + stateCache +
				  " AND c_country = " + countryCache + " AND c_zip = " + zipCache +
				  " AND c_phone = " + phoneCache + " AND c_contact = " + contactCache);
			}
		    throw new EJBException("Optimistic concurrency control failed ");

            }
        } catch (SQLException e) {
	    if(debugging) {
                debug.println(1, "SQLException in UPDATE O_customer for id " + customerId);
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
