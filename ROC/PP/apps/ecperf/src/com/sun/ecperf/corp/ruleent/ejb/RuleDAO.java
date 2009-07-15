
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: RuleDAO.java,v 1.2 2003/03/22 04:55:01 emrek Exp $
 *
 * $Mod: RulCmpEJB.java,v 1.5 2001/04/14 08:30:00 hogstrom - Modified
 *       bean to support single row entities.
 */
package com.sun.ecperf.corp.ruleent.ejb;


import javax.ejb.*;

import javax.naming.*;

import java.rmi.*;

import java.util.*;

import java.sql.*;

import javax.sql.*;

import com.sun.ecperf.common.*;


/**
 * Class RuleDAO
 *
 *
 * @author
 * @version %I%, %G%
 */
public class RuleDAO {

    RuleCmpEJB bean;
    DataSource dataSrc;

    /**
     * Constructor RuleDAO
     *
     *
     * @param bean
     *
     */
    public RuleDAO(RuleCmpEJB bean) {

        this.bean = bean;

        try {
            dataSrc =
                (DataSource) bean.initCtx
                    .lookup("java:comp/env/CorpDataSource");
        } catch (NamingException e) {
            e.printStackTrace(System.err);

            throw new EJBException(e);
        }
    }

    /**
     * Method ejbCreate
     *
     *
     * @param id
     * @param rules
     *
     * @return
     *
     * @throws CreateException
     *
     */
    public String ejbCreate(String id, String rules)
            throws CreateException {

        Connection        conn = null;
        PreparedStatement st   = null;

        try {
            conn = dataSrc.getConnection();
            st   = conn.prepareStatement(
                "INSERT INTO C_rule (r_id, r_text) "
                + "VALUES (?, ?)");

            st.setString(1, id);
            st.setString(2, rules);

            int resultCode = st.executeUpdate();
            if (resultCode < 1) {
                throw new CreateException("Insert failed for rule \""+id+"\"");
            }
        } catch (SQLException e) {
            if (bean.debugging)
                bean.debug.println(1,
                         "INSERT INTO C_rule (r_id, r_text) "
                         + " Failed : " + e);
            bean.debug.printStackTrace(e);

            throw new EJBException(e);
        } finally {
            Util.closeConnection(conn, st);
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
            st   = conn.prepareStatement("DELETE FROM C_rule WHERE r_id = ?");

            st.setString(1, bean.id);
                                                       
            if (st.executeUpdate() < 1) {
                throw new RemoveException("DELETE FROM C_rule WHERE r_id = "
                                          + bean.id + " : Failed");
            }
        } catch (SQLException e) {
            if (bean.debugging)
                bean.debug.println(1, "DELETE FROM C_rule WHERE r_id = "
                                   + bean.id + " Failed : " + e);
            bean.debug.printStackTrace(e);

            throw new EJBException(e);
        } finally {
            Util.closeConnection(conn, st);
        }
    }

    /**
     * Method ejbLoad
     *
     *
     */
    public void ejbLoad() {

        if (bean.debugging)
            bean.debug.println(3, "ejbLoad");

        Connection        conn = null;
        PreparedStatement st   = null;

        bean.ruleBuffer = "";

        try {
            conn = dataSrc.getConnection();
            st   = conn.prepareStatement(
                "SELECT r_text FROM C_rule WHERE r_id = ?");

            st.setString(1, bean.id);

            ResultSet r = st.executeQuery();
                        
            while (r.next()) {
                bean.ruleBuffer = r.getString(1);
            }

            r.close();

            if (bean.ruleBuffer.length() == 0) {
                if (bean.debugging)
                    bean.debug.println(1, "R_ID: " + bean.id + " not found!");

                throw new NoSuchEntityException("R_ID: " + bean.id);
            }
        } catch (SQLException e) {
            if (bean.debugging)
                bean.debug.println(1,
                               "SELECT r_text FROM C_rule WHERE r_id = "
                               + bean.id + " ORDER BY r_sequence : Failed : "
                               + e);
            bean.debug.printStackTrace(e);

            throw new EJBException(e);
        } finally {
            Util.closeConnection(conn, st);
        }
    }

    /**
     * Method ejbStore
     *
     *
     */
    public void ejbStore() {

        Connection          conn = null;
        PreparedStatement[] st   = new PreparedStatement[3];

        try {
            conn  = dataSrc.getConnection();
            st[0] =
                conn.prepareStatement("UPDATE C_rule SET r_text = ? "
                                      + "WHERE r_id = ?");
            st[1] = conn.prepareStatement(
                "INSERT INTO C_rule (r_text, r_id) "
                + "VALUES (?, ?)");
            st[2] = conn.prepareStatement(
                "DELETE FROM C_rule WHERE r_id = ?");

            st[0].setString(2, bean.id);
            st[1].setString(2, bean.id);

            st[0].setString(1, (String) bean.ruleBuffer);

            // Try to update the existing record.  If it doesn't exist let's insert it.
            if (st[0].executeUpdate() < 1) {
               st[1].setString(1, (String) bean.ruleBuffer);
               st[1].executeUpdate();
            }
        } catch (SQLException e) {
            if (bean.debugging)
                bean.debug.println(1,
                    "INSERT INTO C_rule (r_text, r_id : Failed :  "
                    + e);
            bean.debug.printStackTrace(e);

            throw new EJBException(e);
        } finally {
            Util.closeConnection(conn, st);
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
            st   = conn
                .prepareStatement("SELECT r_id FROM C_rule WHERE r_id = ?");

            st.setString(1, key);

            ResultSet r = st.executeQuery();

            if (!r.next()) {
                throw new FinderException("r_id: " + key);
            }

            r.close();
        } catch (SQLException e) {
            e.printStackTrace(System.err);

            throw new EJBException(e);
        } finally {
            Util.closeConnection(conn, st);
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
            st   = conn.prepareStatement("SELECT DISTINCT r_id FROM C_rule");

            ResultSet r = st.executeQuery();

            while (r.next()) {
                results.addElement(r.getString(1));
            }

            r.close();
        } catch (SQLException e) {
            e.printStackTrace(System.err);

            throw new EJBException(e);
        } finally {
            Util.closeConnection(conn, st);
        }

        return results.elements();
    }
}
