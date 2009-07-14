
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: ItemBmpEJB.java,v 1.1.1.1 2002/11/16 05:35:27 emrek Exp $
 *
 */
package com.sun.ecperf.orders.itement.ejb;


import javax.ejb.*;

import java.rmi.*;

import java.sql.*;

import java.util.Vector;

import javax.naming.*;

import com.sun.ecperf.common.*;
import com.sun.ecperf.orders.helper.*;


/**
 * This class is the BMP version of the Item EJB  and must be used by
 * vendors who want to run the BMP implementation. The persistence code is
 * allowed to be modified only for non-SQL databases.
 * @see ItemCmpEJB
 */
public class ItemBmpEJB extends ItemCmpEJB {

    /**
     * The dirty flag. This is transitional and involves BMP functionality.
     * It is provided to avoid the very expensive ejbStore calls if the
     * bean has not been changed. Newer EJB specifications should take care
     * of such optimization. Only ejbLoad and ejbCreate sets it to false.
     */
    private boolean dirty = true;


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

            // rethrow wrapped in EJBException
            throw new EJBException("Failure looking up DataSource " + e);
        }
    }

    /**
     * Note that a new item is never actually created in ECperf, so
     * this method will never be called.
     */
    public String ejbCreate(
            String id, double price, String name, String description, float discount)
                throws CreateException {

        Connection        connection = null;
        PreparedStatement statement  = null;

        if(debugging)
            debug.println(3, "ejbCreate ");
        super.ejbCreate(id, price, name, description, discount);

        try {
            connection = dataSource.getConnection();
            statement  = connection.prepareStatement(
                "INSERT INTO O_item (i_id, i_name, i_desc, i_price, i_discount) VALUES (?, ?, ?, ?, ?)");

            statement.setString(1, id);
            statement.setString(2, name);
            statement.setString(3, description);
            statement.setDouble(4, price);
            statement.setFloat(5, discount);

            int ret = statement.executeUpdate();

            if (ret != 1) {
                if(debugging)
                    debug.println(1, "executeUpdate() into O_item returned "
                                  + ret);

                throw new CreateException();
            }
        } catch (SQLException e) {
	    if(debugging) {
                debug.println(1, "SQLException from INSERT INTO O_item (" + id
                              + ", " + name + ", " + description + ", " + price
                              + ", " + discount + ")");
                debug.println(1, e.toString());
	    }

            debug.printStackTrace(e);

            throw new EJBException(e.toString());
        } finally {
            Util.closeConnection(connection, statement);
        }
        dirty = false;
        return (id);
    }

    /**
     * Method ejbFindByPrimaryKey
     *
     *
     * @param pk
     *
     * @return
     *
     * @throws FinderException
     *
     */
    public String ejbFindByPrimaryKey(String pk) throws FinderException {

        if(debugging)
            debug.println(3, "ejbFindByPrimaryKey ");

        Connection        connection = null;
        PreparedStatement statement  = null;

        try {
            connection = dataSource.getConnection();
            statement  = connection
                .prepareStatement("SELECT i_id FROM O_item WHERE i_id = ?");

            statement.setString(1, pk);

            ResultSet resultSet = statement.executeQuery();

            if (!resultSet.next()) {
                throw new ObjectNotFoundException("Item not found : " + pk);
            }

            return pk;
        } catch (SQLException e) {
            debug.printStackTrace(e);

            throw new EJBException(e);
        } finally {
            Util.closeConnection(connection, statement);
        }
    }

    /**
     * Method ejbFindAll
     *
     *
     * @return
     *
     * @throws FinderException
     *
     */
    public java.util.Enumeration ejbFindAll() throws FinderException {

        if(debugging)
            debug.println(3, "ejbFindAll()");

        Connection        connection = null;
        PreparedStatement statement  = null;

        try {
            connection = dataSource.getConnection();
            statement  =
                connection.prepareStatement("SELECT i_id FROM O_item");

            ResultSet resultSet = statement.executeQuery();
            Vector    keys      = new Vector();

            while (resultSet.next()) {
                id = resultSet.getString(1);

                keys.addElement(id);
            }

            return keys.elements();
        } catch (SQLException e) {
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
            statement  =
                connection
                    .prepareStatement("DELETE FROM O_item WHERE i_id = ?");

            statement.setString(1, id);

            if (statement.executeUpdate() < 1) {
                throw new RemoveException();
            }
        } catch (SQLException e) {
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
        id = (String) entityContext.getPrimaryKey();
        super.ejbActivate();
    }

    /**
     * Method ejbLoad
     *
     *
     */
    public void ejbLoad() {

        debug.println(3, "ejbLoad" + "id = " + id);

        Connection        connection = null;
        PreparedStatement statement  = null;

        try {
            connection = dataSource.getConnection();
            statement  = connection.prepareStatement(
                "SELECT i_id, i_name, i_desc, i_price, i_discount FROM O_item WHERE i_id = ? ");

            statement.setString(1, id.trim());

            ResultSet resultSet = statement.executeQuery();

            if (!resultSet.next()) {
                throw new NoSuchEntityException("Row not found for id: "
                                                + id);
            }

            id          = resultSet.getString(1);
            name        = resultSet.getString(2);
            description = resultSet.getString(3);
            price       = resultSet.getDouble(4);
            discount    = resultSet.getFloat(5);
        } catch (SQLException e) {
            debug.printStackTrace(e);

            throw new NoSuchEntityException("Row not found " + e);
        } finally {
            Util.closeConnection(connection, statement);
        }

        super.ejbLoad();
        dirty = false;
    }

    /**
     * Method ejbStore
     *
     *
     */
    public void ejbStore() {

        if(debugging)
            debug.println(3, "ejbStore");

        // Avoid write to DB if nothing has changed
        if (!dirty) 
            return;

        super.ejbStore();

        if(debugging)
            debug.println(3, "ejbStore" + "ibet id = " + id);

        Connection        connection = null;
        PreparedStatement statement  = null;

        try {
            connection = dataSource.getConnection();
            statement  = connection.prepareStatement(
                "UPDATE O_item SET i_name = ?, i_desc = ? , i_price = ? , i_discount = ? WHERE i_id = ?");

            statement.setString(1, name);
            statement.setString(2, description);
            statement.setDouble(3, price);
            statement.setFloat(4, discount);
            statement.setString(5, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            debug.printStackTrace(e);

            throw new NoSuchEntityException(e);
        } finally {
            Util.closeConnection(connection, statement);
        }
    }
}

