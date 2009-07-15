
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: CustomerBmpEJB.java,v 1.1 2004/02/19 14:45:08 emrek Exp $
 *
 */
package com.sun.ecperf.corp.customerent.ejb;


import javax.ejb.*;

import javax.naming.*;

import java.rmi.*;

import java.sql.*;

import com.sun.ecperf.common.*;


/**
 * This class implements the Customer entity Bean
 */
public class CustomerBmpEJB extends CustomerCmpEJB {

    private java.sql.Date      sinceCache;
    private double             balanceCache;
    private String             creditCache;
    private double             creditLimitCache;
    private double             ytdPaymentCache;

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
                        .lookup("java:comp/env/CorpDataSource");
        } catch (NamingException e) {
            debug.printStackTrace(e);

            throw new EJBException("Failure looking up DataSource " + e);
        }
    }

    /**
     * Method ejbCreate
     *
     *
     * @param info
     *
     * @return
     *
     * @throws CreateException
     * @throws RemoteException
     *
     */
    public Integer ejbCreate(CustomerInfo info)
            throws RemoteException, CreateException {

        Connection        connection = null;
        PreparedStatement statement  = null;
      
        // These are NUMBER(9,2)
        info.balance = Rounding.round(info.balance, 2);
        info.creditLimit = Rounding.round(info.creditLimit, 2);
        info.YtdPayment = Rounding.round(info.YtdPayment, 2);

        super.ejbCreate(info);

        sinceCache = since;
        balanceCache = balance;
        creditCache = credit;
        creditLimitCache = creditLimit;
        ytdPaymentCache = ytdPayment;

        try {
            connection = dataSource.getConnection();
            statement  =
                connection.prepareStatement("INSERT INTO C_customer VALUES ("
                                            + " ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,"
                                            + " ?, ?, ?, ?, ? )");

            int i = 1;

            statement.setInt(i++, info.customerId.intValue());
            statement.setString(i++, info.firstName);
            statement.setString(i++, info.lastName);
            statement.setString(i++, info.address.street1);
            statement.setString(i++, info.address.street2);
            statement.setString(i++, info.address.city);
            statement.setString(i++, info.address.state);
            statement.setString(i++, info.address.country);
            statement.setString(i++, info.address.zip);
            statement.setString(i++, info.address.phone);
            statement.setString(i++, info.contact);
            statement.setDate(i++, info.since);
            statement.setDouble(i++, info.balance);
            statement.setString(i++, info.credit);
            statement.setDouble(i++, info.creditLimit);
            statement.setDouble(i++, info.YtdPayment);

            int ret = statement.executeUpdate();

            if (ret != 1) {
                if (debugging)
                    debug.println(1, "executeUpdate() returned " + ret
                                  + " for :insert into C_customer ");

                throw new CreateException();
            }

            return info.customerId;
        } catch (SQLException e) {
            if (debugging)
                debug.println(1, "SQLException in INSERT INTO C_customer : "
                              + e.toString());
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
     * @param key
     *
     * @return
     *
     * @throws FinderException
     *
     */
    public Integer ejbFindByPrimaryKey(Integer key) throws FinderException {

        Connection        connection = null;
        PreparedStatement statement  = null;

        if (debugging)
            debug.println(3, "ejbFindByPrimaryKey: " + key);

        try {
            connection = dataSource.getConnection();
            statement  = connection.prepareStatement(
                "SELECT c_id FROM C_customer WHERE c_id = ?");

            statement.setInt(1, key.intValue());

            ResultSet resultSet = statement.executeQuery();

            if (!resultSet.next()) {
                if (debugging)
                    debug.println(1,
                        "SELECT from C_customer returned no rows for c_id: "
                        + key);

                throw new ObjectNotFoundException("C_customer not found :"
                                                  + key);
            }

            return key;
        } catch (SQLException e) {
            if (debugging)
                debug.println(1,
                    "SQLException in SELECT FROM C_customer WHERE c_id = "
                    + key);
            debug.println(1, e.toString());
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
     */
    public void ejbRemove() throws RemoveException {
        if (debugging) {
            debug.println(3, "ejbRemove ");
            debug.println(2, "ejbRemove should not be called !");
        }

        Connection connection = null;
        PreparedStatement statement = null;

        try
        {
            connection = dataSource.getConnection();
            /**
               Postgres has problems with rounding errors for doubles
            statement =
                    connection .prepareStatement("DELETE FROM C_customer " + 
                    "WHERE c_id = ? AND  c_since = ? AND c_balance = ? " +
                    "AND c_credit = ? AND c_credit_limit = ? " +
                    "AND c_ytd_payment = ?");
            */

            statement =
                    connection .prepareStatement("DELETE FROM C_customer " + 
                                                 "WHERE c_id = ? AND  c_since = ?  " +
                                                 "AND c_credit = ? ");

            int i = 1;
            statement.setInt(i++, customerId.intValue());
            statement.setDate(i++, sinceCache);
            //statement.setDouble(i++, balanceCache);
            statement.setString(i++, creditCache);
            //statement.setDouble(i++, creditLimitCache);
            //statement.setDouble(i++, ytdPaymentCache);

            int ret = statement.executeUpdate();

            if (ret != 1)
            {
                if (debugging)
                {
                    debug.println(1,
                            "executeUpdate() returned " + ret + " for :");
                    debug.println(1,
                            "DELETE from  C_customer WHERE c_id = " + customerId);
                }
                throw new RemoveException();
            }
        }
        catch (SQLException e)
        {
            if (debugging)
                debug.println(1,
                        "SQLException in DELETE from C_customer WHERE c_id = " +
                        customerId);
            debug.printStackTrace(e);

            throw new EJBException(e);
        }
        finally { 
            Util.closeConnection(connection, statement);
        }
    }

    /**
     * Method ejbLoad
     *
     *
     */
    public void ejbLoad() {

        Connection        connection = null;
        PreparedStatement statement  = null;

        if (debugging)
            debug.println(3, "ejbLoad ");

        try {
            connection = dataSource.getConnection();

            StringBuffer sqlbuf = new StringBuffer("");

            sqlbuf.append("SELECT ");
            sqlbuf.append("c_id, ");
            sqlbuf.append("c_since, ");
            sqlbuf.append("c_balance, ");
            sqlbuf.append("c_credit, ");
            sqlbuf.append("c_credit_limit, ");
            sqlbuf.append("c_ytd_payment ");
            sqlbuf.append("FROM C_customer WHERE ");
            sqlbuf.append("c_id = ?  ");

            String sqlstr = sqlbuf.toString();

            statement = connection.prepareStatement(sqlstr);

            statement.setInt(1, customerId.intValue());

            ResultSet resultSet = statement.executeQuery();

            if (!resultSet.next()) {
                if (debugging)
                    debug.println(1,
                        "SELECT from C_customer returned no rows for c_id: "
                        + customerId);

                throw new NoSuchEntityException("Customer id " + customerId
                                                + " not found");
            }

            int i = 1;

            customerId  = new Integer(resultSet.getInt(i++));
            since       = resultSet.getDate(i++);
            balance     = resultSet.getDouble(i++);
            credit      = resultSet.getString(i++);
            creditLimit = resultSet.getDouble(i++);
            ytdPayment  = resultSet.getDouble(i++);

            sinceCache = since;
            balanceCache = balance;
            creditCache = credit;
            creditLimitCache = creditLimit;
            ytdPaymentCache = ytdPayment;
            isDirty = false;

        } catch (SQLException e) {
            if (debugging)
                debug.println(1,
                    "SQLException in SELECT from C_customer for c_id = "
                    + customerId + " : " + e);
            debug.printStackTrace(e);

            throw new EJBException(e);
        } finally {
            Util.closeConnection(connection, statement);
        }
    }

    /**
     * We don't need to do anything in ejbStore as no data should be
     * modified
     */
    public void ejbStore() {
        if (debugging)
            debug.println(3, "ejbStore ");

        Connection connection = null;
        PreparedStatement statement = null;

        try
        {
            connection = dataSource.getConnection();

            StringBuffer sqlbuf = new StringBuffer("");

            sqlbuf.append("UPDATE C_customer SET c_since = ?, ");
            sqlbuf.append("c_balance = ?, c_credit = ?, ");
            sqlbuf.append("c_credit_limit = ?, c_ytd_payment = ? ");
            /*
              Postgres maybe has rounding problems with doubles.
            sqlbuf.append("WHERE c_id = ? AND  c_since = ? AND c_balance = ? ");
            sqlbuf.append("AND c_credit = ? AND c_credit_limit = ? AND c_ytd_payment = ? ");
            */
            sqlbuf.append("WHERE c_id = ? AND  c_since = ? ");
            sqlbuf.append("AND c_credit = ? ");

            String sqlstr = sqlbuf.toString();

            statement = connection.prepareStatement(sqlstr);

            int i = 1;
            statement.setDate(i++, since);
            statement.setDouble(i++, balance);
            statement.setString(i++, credit);
            statement.setDouble(i++, creditLimit);
            statement.setDouble(i++, ytdPayment);

            statement.setInt(i++, customerId.intValue());
            statement.setDate(i++, sinceCache);
            //statement.setDouble(i++, balanceCache);
            statement.setString(i++, creditCache);
            //statement.setDouble(i++, creditLimitCache);
            //statement.setDouble(i++, ytdPaymentCache);

            int ret = statement.executeUpdate();

            if (ret != 1)
            {
                if (debugging)
                {
                    debug.println(1,
                                  "executeUpdate() returned " + ret + " in ejbStore() of C_customer");
                }

                throw new EJBException("OCC Failed for C_customer, id = " + customerId);
            }
            sinceCache = since;
            balanceCache = balance;
            creditCache = credit;
            creditLimitCache = creditLimit;
            ytdPaymentCache = ytdPayment;
            isDirty = false;
        }
        catch (SQLException e)
        {
            if (debugging)
                debug.println(1,
                        "SQLException in UPDATE C_customer for id = " + customerId); 
            debug.printStackTrace(e);

            throw new EJBException(e);
        }
        finally { 
            Util.closeConnection(connection, statement);
        }
    }

    public void ejbActivate() {
        customerId = (Integer) entityContext.getPrimaryKey();
        super.ejbActivate();
    }
}
