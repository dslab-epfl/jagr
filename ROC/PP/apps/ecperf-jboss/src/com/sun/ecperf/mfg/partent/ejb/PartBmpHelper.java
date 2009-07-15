
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * akmits@eng.sun.com 03/17/2000
 *
 * $Id: PartBmpHelper.java,v 1.1 2004/02/19 14:45:12 emrek Exp $
 *
 */
package com.sun.ecperf.mfg.partent.ejb;


import javax.ejb.*;

import javax.naming.*;

import java.rmi.*;

import java.util.*;

import com.sun.ecperf.common.*;

import java.sql.*;


/**
 * This class is helper class for Part, Component and Assembly
 * A separate helper class is needed because ComponentEntBmpEJB and
 * AssemblyEntBmpEJB are already subclasses of ComponentEntCmpEJB
 * and AssemblyEntCmpEJB repectively. If Java supported multiple inherintance
 * somebody might subclass ComponentEntCmpEJB and PartEntBmpEJB
 * to construct ComponentEntBmpEJB.
 *
 * @author Ajay Mittal
 *
 * @see PartCmpEJB for more details
 * @see PartBmpEJB for more details
 */
public class PartBmpHelper {

    protected String               className = "PartBmpHelper";
    protected PartCmpEJB           pce       = null;
    protected javax.sql.DataSource dataSource;
    
    
    // PartCmpEJB is parent of all the classes. No matter who creates
    // it with *this* reference we can store it as PartCmpEJB

    /**
     * Constructor PartBmpHelper
     *
     *
     * @param pce
     *
     */
    public PartBmpHelper(PartCmpEJB pce) {

        this.pce = pce;

        Context context = null;

        try {
            context    = new InitialContext();
            dataSource =
                (javax.sql
                    .DataSource) context
                        .lookup("java:comp/env/MfgDataSource");
        } catch (NamingException e) {
            pce.debug.printStackTrace(e);

            throw new EJBException("Failure looking up DataSource " + e);
        }
    }

    /**
     * The helperEjbCreate method gets called when a new part needs to
     * be created. This should never be called in ECperf.
     * @return String
     * @exception CreateException if the create fails
     * @exception RemoteException if there is a system failure
     */
    public String helperEjbCreate() throws CreateException, RemoteException {

        className = "PartBmpHelper";

	if (pce.debugging) {
	    pce.debug.println(3, "helperEjbCreate ");
	    pce.debug.println(2, "helperEjbCreate called when not expected !");
	}
	
        PreparedStatement statement  = null;
        Connection        connection = null;

        try {
            connection = dataSource.getConnection();

            StringBuffer sqlbuf = new StringBuffer("");

            sqlbuf.append("INSERT INTO M_parts (p_id, p_name, ");
            sqlbuf.append("p_desc, p_rev, ");
            sqlbuf.append("p_planner, p_type, ");
            sqlbuf.append(
                "p_ind, p_lomark, p_himark) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");

            String sqlstr = sqlbuf.toString();

            statement = connection.prepareStatement(sqlstr);

            int i = 1;

            statement.setString(i++, pce.id);
            statement.setString(i++, pce.name);
            statement.setString(i++, pce.description);
            statement.setString(i++, pce.revision);
            statement.setInt(i++, pce.planner);
            statement.setInt(i++, pce.type);
            statement.setInt(i++, pce.purchased);
            statement.setInt(i++, pce.lomark);
            statement.setInt(i++, pce.himark);

            if (statement.executeUpdate() != 1) {
                throw new CreateException(
                    "INSERT INTO M_parts (p_id, p_name, ... : Failed ");
            }

            return new String(pce.id);
        } catch (SQLException e) {
	    if (pce.debugging)
		pce.debug.println(
                1, "INSERT INTO M_parts (p_id, p_name, ...  : Failed : "
                + e.getMessage());
            pce.debug.printStackTrace(e);

            throw new EJBException(e);
        } finally {
            Util.closeConnection(connection, statement);
        }
    }

    /**
     * helperEjbFindByPrimaryKey
     */
    public String helperEjbFindByPrimaryKey(String key)
            throws FinderException {

        Connection        connection = null;
        PreparedStatement statement  = null;

	if (pce.debugging)
	    pce.debug.println(3, "helperEjbFindbyPrimaryKey " + "Key is " + key);

        try {
            connection = dataSource.getConnection();
            statement  = connection
                .prepareStatement("SELECT p_id FROM M_parts  WHERE p_id = ?");

            statement.setString(1, key);

            ResultSet resultSet = statement.executeQuery();

            if (!resultSet.next()) {
                throw new FinderException("primary key not found:" + key);
            }

            return key;
        } catch (SQLException e) {
	    if (pce.debugging)
		pce.debug.println(1, "SELECT p_id FROM M_parts  WHERE p_id = "
                              + key + " : Failed : " + e.getMessage());
            pce.debug.printStackTrace(e);

            throw new EJBException("SQL Exception in find by primary key "
                                   + key);
        } finally {
            Util.closeConnection(connection, statement);
        }
    }

    /**
     * helperEjbFindAll
     */
    public java.util.Enumeration helperEjbFindAll() throws FinderException {

        Connection        connection = null;
        PreparedStatement statement  = null;

	if (pce.debugging)
	    pce.debug.println(3, "helperEjbFindAll()");

        try {
            connection = dataSource.getConnection();
            statement  =
                connection.prepareStatement("SELECT p_id FROM M_parts");

            ResultSet resultSet = statement.executeQuery();
            Vector    keys      = new Vector();

            while (resultSet.next()) {
                String pceid = resultSet.getString(1);

                keys.addElement(pceid);
            }

            return keys.elements();
        } catch (SQLException e) {
	    if (pce.debugging)
		pce.debug.println(1, "SELECT p_id FROM M_parts : Failed : "
                              + e.getMessage());
            pce.debug.printStackTrace(e);

            throw new EJBException(e);
        } finally {
            Util.closeConnection(connection, statement);
        }
    }

    /**
     * Method helperEjbRemove
     *
     *
     * @throws RemoveException
     *
     */
    public void helperEjbRemove() throws RemoveException {

	if (pce.debugging)
	    pce.debug.println(3, "helperEjbRemove");

        Connection        connection = null;
        PreparedStatement statement  = null;

        try {
            connection = dataSource.getConnection();
            statement  =
                connection
                    .prepareStatement("DELETE FROM M_parts WHERE p_id = ?");

            statement.setString(1, pce.id);

            if (statement.executeUpdate() < 1) {
                throw new RemoveException("DELETE FROM M_parts WHERE p_id = "
                                          + pce.id + " : Failed");
            }
        } catch (SQLException e) {
	    if (pce.debugging)
		pce.debug.println(1, "DELETE FROM M_parts WHERE p_id = " + pce.id
                              + " : Failed : " + e.getMessage());
            pce.debug.printStackTrace(e);

            throw new EJBException(e);
        } finally {
            Util.closeConnection(connection, statement);
        }
    }

    /**
     * Method helperEjbActivate
     *
     *
     */
    public void helperEjbActivate() {
	pce.id = (String) pce.entityContext.getPrimaryKey();
    }


    /**
     * Method helperEjbLoad
     *
     *
     */
    public void helperEjbLoad() {

	if (pce.debugging)
	    pce.debug.println(3, "helperEjbLoad ");

        Connection        connection = null;
        PreparedStatement statement  = null;
        StringBuffer      sqlbuf     = null;
        try {
            connection = dataSource.getConnection();
            sqlbuf     = new StringBuffer("");

            sqlbuf.append("SELECT ");
            sqlbuf.append("p_id, ");
            sqlbuf.append("p_name, ");
            sqlbuf.append("p_desc, ");
            sqlbuf.append("p_rev, ");
            sqlbuf.append("p_planner, ");
            sqlbuf.append("p_type, ");
            sqlbuf.append("p_ind, ");
            sqlbuf.append("p_lomark, ");
            sqlbuf.append("p_himark ");
            sqlbuf.append("FROM M_parts WHERE p_id = ? ");

            String sqlstr = sqlbuf.toString();

            statement = connection.prepareStatement(sqlstr);

            statement.setString(1, pce.id);    /* ?ajay? */

            ResultSet resultSet = statement.executeQuery();

            if (!resultSet.next()) {
		if (pce.debugging)
		    pce.debug.println(1, "No row found in M_parts where p_id =  "
                                  + pce.id);

                throw new NoSuchEntityException(
                    "Row not found in M_parts where p_id = " + pce.id);
            }

            int i = 1;

            pce.id          = resultSet.getString(i++);
            pce.name        = resultSet.getString(i++);
            pce.description = resultSet.getString(i++);
            pce.revision    = resultSet.getString(i++);
            pce.planner     = resultSet.getInt(i++);
            pce.type        = resultSet.getInt(i++);
            pce.purchased   = resultSet.getInt(i++);
            pce.lomark      = resultSet.getInt(i++);
            pce.himark      = resultSet.getInt(i++);
        } catch (SQLException e) {
	    if (pce.debugging) {
		pce.debug.println(
                1, "SQLException in SELECT from M_parts where p_id = " + pce.id
                + " : " + e.getMessage());
		pce.debug.println(1, e.toString());
	    }
	    
            pce.debug.printStackTrace(e);

            throw new EJBException(e);
        } finally {
            Util.closeConnection(connection, statement);
        }
    }

    /**
     * Method helperEjbStore
     *
     *
     */
    public void helperEjbStore() {

	if (pce.debugging)
	    pce.debug.println(3, "helperEjbStore ");

        Connection        connection = null;
        PreparedStatement statement  = null;
        StringBuffer      sqlbuf     = null;

        try {
            connection = dataSource.getConnection();
            sqlbuf     = new StringBuffer("");

            sqlbuf.append("UPDATE M_parts SET ");
            sqlbuf.append("p_name = ?, ");
            sqlbuf.append("p_desc = ?, ");
            sqlbuf.append("p_rev = ?, ");
            sqlbuf.append("p_planner = ?, ");
            sqlbuf.append("p_type = ?, ");
            sqlbuf.append("p_ind = ?, ");
            sqlbuf.append("p_lomark = ?, ");
            sqlbuf.append("p_himark = ? ");
            sqlbuf.append("WHERE p_id = ?");

            String sqlstr = sqlbuf.toString();

            statement = connection.prepareStatement(sqlstr);

            int i = 1;

            statement.setString(i++, pce.name);
            statement.setString(i++, pce.description);
            statement.setString(i++, pce.revision);
            statement.setInt(i++, pce.planner);
            statement.setInt(i++, pce.type);
            statement.setInt(i++, pce.purchased);
            statement.setInt(i++, pce.lomark);
            statement.setInt(i++, pce.himark);
            statement.setString(i++, pce.id);
            statement.executeUpdate();
        } catch (SQLException e) {
	    if (pce.debugging)
		pce.debug.println(1, 
			  "SQLException in update of M_parts where p_id = "
                         + pce.id + " : " + e.getMessage());
            pce.debug.printStackTrace(e);

            throw new EJBException(e);
        } finally {
            Util.closeConnection(connection, statement);
        }
    }
}

