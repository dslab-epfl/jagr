
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: DiscountDAO.java,v 1.2 2003/03/22 04:55:01 emrek Exp $
 *
 */
package com.sun.ecperf.corp.discountent.ejb;


import javax.ejb.*;

import javax.naming.*;

import java.rmi.*;

import java.util.*;

import java.sql.*;

import javax.sql.*;

import com.sun.ecperf.common.*;


/**
 * Class DiscountDAO
 *
 *
 * @author
 * @version %I%, %G%
 */
public class DiscountDAO {

    DiscountCmpEJB bean;
    DataSource     dataSrc;

    /**
     * Constructor DiscountDAO
     *
     *
     * @param bean
     *
     */
    public DiscountDAO(DiscountCmpEJB bean) {

        this.bean = bean;

        try {
            dataSrc =
                (DataSource) bean.initCtx
                    .lookup("java:comp/env/CorpDataSource");
        } catch (NamingException e) {
            if (bean.debugging)
                bean.debug.println(1, e.getMessage());
            bean.debug.printStackTrace(e);

            throw new EJBException(e);
        }
    }

    /**
     * Method ejbCreate
     *
     *
     * @param id
     * @param percent
     *
     * @return
     *
     * @throws CreateException
     *
     */
    public String ejbCreate(String id, double percent)
            throws CreateException {

        Connection        conn = null;
        PreparedStatement st   = null;

        try {
            conn = dataSrc.getConnection();
            st   = conn
                .prepareStatement("INSERT INTO C_discount (d_id, d_percent) "
                                  + "VALUES ( ?, ? )");

            st.setString(1, id);
            st.setInt(2, (new Double(percent * 100)).intValue());

            if (st.executeUpdate() < 1) {
                throw new CreateException("d_id: " + id);
            }
        } catch (SQLException e) {
            if (bean.debugging)
                bean.debug.println(1, e.getMessage());
            bean.debug.printStackTrace(e);

            throw new EJBException(e);
        } finally {
            Util.closeConnection(conn, st);
            ;
        }

        return id;
    }

    /**
     * Method ejbRemove
     *
     *
     * @throws RemoveException
     *
     */
    public void ejbRemove() throws RemoveException {

        Connection        conn = null;
        PreparedStatement st   = null;

        try {
            conn = dataSrc.getConnection();
            st   = conn
                .prepareStatement("DELETE FROM C_discount WHERE d_id = ?");

            st.setString(1, bean.id);

            if (st.executeUpdate() < 1) {
                throw new RemoveException(
                    "Delete error : DELETE FROM C_discount WHERE d_id = "
                    + bean.id + " Failed");
            }
        } catch (SQLException e) {
            if (bean.debugging)
                bean.debug.println(1, "DELETE FROM C_discount WHERE d_id = "
                                   + bean.id + " Failed : " + e);
            bean.debug.printStackTrace(e);

            throw new EJBException(e);
        } finally {
            Util.closeConnection(conn, st);
            ;
        }
    }

    /**
     * Method ejbLoad
     *
     *
     */
    public void ejbLoad() {

        Connection        conn = null;
        PreparedStatement st   = null;

        try {
            conn = dataSrc.getConnection();
            st   = conn.prepareStatement(
                "SELECT d_percent FROM C_discount WHERE d_id = ?");

            st.setString(1, bean.id);

            ResultSet r = st.executeQuery();

            if (!r.next()) {
                throw new NoSuchEntityException("D_ID: " + bean.id);
            }

            bean.percent = r.getInt(1);
            r.close();
        } catch (SQLException e) {
            if (bean.debugging)
                bean.debug.println(1,
                    "SELECT d_percent FROM C_discount WHERE d_id = "
                    + bean.id + "Failed : " + e);
            bean.debug.printStackTrace(e);

            throw new EJBException(e);
        } finally {
            Util.closeConnection(conn, st);
            ;
        }
    }

    /**
     * Method ejbStore
     *
     *
     */
    public void ejbStore() {

        Connection        conn = null;
        PreparedStatement st   = null;

        try {
            conn = dataSrc.getConnection();
            st   = conn.prepareStatement(
                "UPDATE C_discount SET d_percent = ? WHERE d_id = ?");

            st.setInt(1, bean.percent);
            st.setString(2, bean.id);

            if (st.executeUpdate() < 1) {
                throw new NoSuchEntityException("D_ID: " + bean.id);
            }
        } catch (SQLException e) {
            if (bean.debugging)
                bean.debug.println(1, e.getMessage());
            bean.debug.printStackTrace(e);

            throw new EJBException(e);
        } finally {
            Util.closeConnection(conn, st);
            ;
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
    public String ejbFindByPrimaryKey(String key) throws FinderException {

        Connection        conn = null;
        PreparedStatement st   = null;

        try {
            conn = dataSrc.getConnection();
            st   = conn.prepareStatement(
                "SELECT d_id FROM C_discount WHERE d_id = ?");

            st.setString(1, key);

            ResultSet r = st.executeQuery();

            if (!r.next()) {
                throw new ObjectNotFoundException("r_id: " + key);
            }

            r.close();
        } catch (SQLException e) {
            if (bean.debugging)
                bean.debug.println(1, e.getMessage());
            bean.debug.printStackTrace(e);

            throw new EJBException(e);
        } finally {
            Util.closeConnection(conn, st);
            ;
        }

        return key;
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
    public Enumeration ejbFindAll() throws FinderException {

        Connection        conn    = null;
        PreparedStatement st      = null;
        Vector            results = new Vector();

        try {
            conn = dataSrc.getConnection();
            st   = conn.prepareStatement("SELECT d_id FROM C_discount");

            ResultSet r = st.executeQuery();

            while (r.next()) {
                results.addElement(r.getString(1));
            }

            r.close();
        } catch (SQLException e) {
            if (bean.debugging)
                bean.debug.println(1, e.getMessage());
            bean.debug.printStackTrace(e);

            throw new EJBException(e);
        } finally {
            Util.closeConnection(conn, st);
            ;
        }

        return results.elements();
    }
}

