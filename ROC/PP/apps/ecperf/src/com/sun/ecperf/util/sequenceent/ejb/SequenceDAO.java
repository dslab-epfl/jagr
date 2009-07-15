
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: SequenceDAO.java,v 1.2 2003/03/22 04:55:01 emrek Exp $
 *
 */
package com.sun.ecperf.util.sequenceent.ejb;


import javax.ejb.*;

import javax.naming.*;

import java.rmi.*;

import java.util.*;

import java.sql.*;

import javax.sql.*;

import com.sun.ecperf.common.*;
import com.sun.ecperf.util.helper.*;


/**
 * Class SequenceDAO
 *
 *
 * @author
 * @version %I%, %G%
 */
public class SequenceDAO {

    SequenceCmpEJB bean;
    DataSource     dataSrc;

    /**
     * Constructor SequenceDAO
     *
     *
     * @param bean
     *
     */
    public SequenceDAO(SequenceCmpEJB bean) {

        this.bean = bean;

        try {
            dataSrc =
                (DataSource) bean.initCtx
                    .lookup("java:comp/env/UtilDataSource");
        } catch (NamingException e) {
            bean.debug.printStackTrace(e);

            throw new EJBException(e);
        }
    }

    /**
     * Method ejbCreate
     *
     *
     * @param id
     * @param firstNumber
     * @param blockSize
     *
     * @return
     *
     * @throws CreateException
     *
     */
    public String ejbCreate(String id, int firstNumber, int blockSize)
            throws CreateException {

        Connection        conn = null;
        PreparedStatement st   = null;

        try {
            conn = dataSrc.getConnection();
            st   = conn.prepareStatement(
                "INSERT INTO U_sequences (s_id, s_nextnum, s_blocksize) "
                + "VALUES (?, ?, ?)");

            st.setString(1, id);
            st.setInt(2, firstNumber);
            st.setInt(3, blockSize);

            if (st.executeUpdate() < 1) {
                throw new CreateException("S_ID: " + id);
            }
        } catch (SQLException e) {
            if (bean.debugging)
                bean.debug.println(1, "INSERT INTO U_sequences ... : Failed : "
                                   + e.getMessage());
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
            st   = conn
                .prepareStatement("DELETE FROM U_sequences WHERE s_id = ?");

            st.setString(1, bean.id);

            if (st.executeUpdate() < 1) {
                throw new RemoveException("Delete error!");
            }
        } catch (SQLException e) {
            if (bean.debugging)
                bean.debug.println(1, "DELETE FROM U_sequences WHERE S_ID ="
                                   + bean.id + " : Failed : " + e.getMessage());
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

        Connection        conn = null;
        PreparedStatement st   = null;
        String            id   = (String) bean.entCtx.getPrimaryKey();

        try {
            conn = dataSrc.getConnection();
            st   = conn.prepareStatement(
                "SELECT s_nextnum, s_blocksize FROM U_sequences WHERE s_id = ?");

            st.setString(1, id);

            ResultSet r = st.executeQuery();

            if (!r.next()) {
                throw new NoSuchEntityException("S_ID: " + id);
            }

            bean.nextNumber = r.getInt(1);
            bean.blockSize  = r.getInt(2);
            bean.id         = id;

            r.close();
        } catch (SQLException e) {
            if (bean.debugging)
                bean.debug.println(1,
                    "SELECT S_NEXTNUM, S_BLOCKSIZE FROM U_sequences "
                    + "WHERE S_ID =" + id + " : Failed : " + e.getMessage());

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

        Connection        conn = null;
        PreparedStatement st   = null;

        try {
            conn = dataSrc.getConnection();
            st   = conn.prepareStatement(
                "UPDATE U_sequences SET s_nextnum = ? , s_blocksize = ? "
                + "WHERE s_id = ?");

            st.setInt(1, bean.nextNumber);
            st.setInt(2, bean.blockSize);
            st.setString(3, bean.id);

            if (st.executeUpdate() < 1) {
                throw new NoSuchEntityException("S_ID: " + bean.id);
            }
        } catch (SQLException e) {
            if (bean.debugging)
                bean.debug.println(1,
                         "UPDATE U_sequences SET S_NEXTNUM ... : Failed : "
                         + e.getMessage());
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
            st   = conn.prepareStatement(
                "SELECT s_id FROM U_sequences WHERE s_id = ?");

            st.setString(1, key);

            ResultSet r = st.executeQuery();

            if (!r.next()) {
                throw new FinderException("S_ID: " + key);
            }

            r.close();
        } catch (SQLException e) {
            if (bean.debugging)
                bean.debug.println(1,
                         " SELECT S_ID FROM U_sequences WHERE S_ID = "
                         + key + " : Failed : " + e.getMessage());
            bean.debug.printStackTrace(e);

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
            st   = conn.prepareStatement("SELECT s_id FROM U_sequences");

            ResultSet r = st.executeQuery();

            while (r.next()) {
                results.addElement(r.getString(1));
            }

            r.close();
        } catch (SQLException e) {
            if (bean.debugging)
                bean.debug.println(1,
                         "SELECT S_ID FROM U_sequences ... : Failed : "
                         + e.getMessage());
            bean.debug.printStackTrace(e);

            throw new EJBException(e);
        } finally {
            Util.closeConnection(conn, st);
        }

        return results.elements();
    }

    /**
     * Method getNextNumber
     *
     *
     * @return
     *
     */
    public int getNextNumber() {

        Connection        conn       = null;
        PreparedStatement st         = null;
        int               nextNumber = 0;

        try {
            conn = dataSrc.getConnection();
            st   = conn.prepareStatement(
                "SELECT s_nextnum FROM U_sequences WHERE s_id = ?");

            st.setString(1, bean.id);

            ResultSet r = st.executeQuery();

            if (!r.next()) {
                throw new NoSuchEntityException("S_ID: " + bean.id);
            }

            nextNumber = r.getInt(1);

            r.close();
        } catch (SQLException e) {
            if (bean.debugging)
                bean.debug.println(1,
                         "SELECT S_NEXTNUM FROM U_sequences WHERE S_ID = "
                         + bean.id + " : Failed : " + e.getMessage());
            bean.debug.printStackTrace(e);

            throw new EJBException(e);
        } finally {
            Util.closeConnection(conn, st);
        }

        return nextNumber;
    }

    /**
     * Method setBlockSize
     *
     *
     * @param blockSize
     *
     */
    public void setBlockSize(int blockSize) {

        Connection        conn = null;
        PreparedStatement st   = null;

        try {
            conn = dataSrc.getConnection();
            st   = conn.prepareStatement(
                "UPDATE U_sequences SET s_blocksize = ? WHERE s_id = ?");

            st.setInt(1, blockSize);
            st.setString(2, bean.id);

            if (st.executeUpdate() < 1) {
                throw new NoSuchEntityException("S_ID: " + bean.id);
            }
        } catch (SQLException e) {
            if (bean.debugging)
                bean.debug.println(1,
                    "UPDATE U_sequences SET S_BLOCKSIZE ... : Failed : "
                    + e.getMessage());
            bean.debug.printStackTrace(e);

            throw new EJBException(e);
        } finally {
            Util.closeConnection(conn, st);
        }
    }

    /**
     * Method nextSequenceBlock
     *
     *
     * @return
     *
     */
    public SequenceBlock nextSequenceBlock() {

        SequenceBlock       block = new SequenceBlock();
        Connection          conn  = null;
        PreparedStatement[] st    = new PreparedStatement[2];

        try {
            conn  = dataSrc.getConnection();
            st[0] = conn.prepareStatement(
                "UPDATE U_sequences SET s_nextnum = s_nextnum + s_blocksize "
                + "WHERE s_id = ?");
            st[1] = conn.prepareStatement(
                "SELECT s_nextnum, s_blocksize FROM U_sequences WHERE s_id = ?");

            st[0].setString(1, bean.id);
            st[1].setString(1, bean.id);
            st[0].executeUpdate();

            ResultSet r = st[1].executeQuery();

            if (!r.next()) {
                throw new NoSuchEntityException("S_ID: " + bean.id);
            }

            block.ceiling    = r.getInt(1);
            block.nextNumber = block.ceiling - r.getInt(2);

            r.close();
        } catch (SQLException e) {
            if (bean.debugging)
                bean.debug.println(1,
                               "UPDATE U_sequences SET ... : Failed : "
                               + e.getMessage());
            bean.debug.printStackTrace(e);

            throw new EJBException(e);
        } finally {
            Util.closeConnection(conn, st);
        }

        return block;
    }
}

