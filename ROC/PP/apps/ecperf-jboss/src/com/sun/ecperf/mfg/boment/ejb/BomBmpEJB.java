
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 */
package com.sun.ecperf.mfg.boment.ejb;


import javax.ejb.*;

import java.rmi.*;

import javax.naming.*;

import javax.sql.*;

import java.sql.*;

import java.util.*;

import com.sun.ecperf.common.*;


/**
 * This class implements the Bom Entity Bean. Bean managed.
 * If the container supports Container Managed persistence
 * then the superclass will be called. This is specified during
 * deployment time.
 *
 * @author Agnes Jacob
 *
 * @see BomCmpEJB
 */
public class BomBmpEJB extends BomCmpEJB {
    /**
     * The dirty flag. This is transitional and involves BMP functionality.
     * It is provided to avoid the very expensive ejbStore calls if the
     * bean has not been changed. Newer EJB specifications should take care
     * of such optimization. Only ejbLoad and ejbCreate sets it to false.
     */
    private boolean dirty = true;


    private static final String    className = "BomBmpEJB";
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
                        .lookup("java:comp/env/MfgDataSource");
        } catch (NamingException e) {
            debug.printStackTrace(e);

            throw new EJBException("Failure looking up DataSource " + e);
        }
    }

    /**
     * Constructs the BOM object (Bean managed)
     * and stores the information into the DB.
     * @param assemblyId    Assembly Id of bom
     * @param componentId
     * @param lineNo        Line No
     * @param qty
     * @param engChange - Engineering change reference
     * @param opsNo - Op# - which step in the process this is used
     * @param opsDesc - Operation description
     * @return primary key of BOM which is composed of componentId,
     *              assemblyId, and lineNo (BomEntPK).
     * @see BomEntPK
     */
    public BomEntPK ejbCreate(String assemblyId, String componentId, int lineNo, int qty, int opsNo, String engChange, String opsDesc)
                throws CreateException, RemoteException {

        super.ejbCreate(assemblyId, componentId, lineNo, qty, opsNo,
                        engChange, opsDesc);

        Connection        conn = null;
        PreparedStatement stmt = null;

        try {
            int ret;

            conn = dataSource.getConnection();
            stmt = conn.prepareStatement(
                "INSERT INTO M_bom (b_comp_id, b_assembly_id, b_line_no, b_qty, b_ops, b_eng_change, b_ops_desc) VALUES(?, ?, ?, ?, ?, ?, ?)");

            stmt.setString(1, componentId);
            stmt.setString(2, assemblyId);
            stmt.setInt(3, lineNo);
            stmt.setInt(4, qty);
            stmt.setInt(5, opsNo);
            stmt.setString(6, engChange);
            stmt.setString(7, opsDesc);

            if ((ret = stmt.executeUpdate()) != 1) {
		if (debugging)
		    debug.println(1, "execute Update into M_bom : Failed ");

                throw new CreateException(className + "(ejbCreate): Failed ");
            }

            dirty = false;
            return (new BomEntPK(assemblyId, componentId, lineNo));
        } catch (SQLException e) {
	    if (debugging)
		debug.println(
                1, "SQLException INSERT INTO M_bom with componentId "
                + componentId + " : Failed ");
            debug.printStackTrace(e);

            throw new EJBException(e);
        } finally {
            Util.closeConnection(conn, stmt);
        }
    }

    /**
     * Finds the object in the DB based on primary key.
     * If object is found, then it will just return the
     * primary key passed in otherwise throws a FinderException.
     * @see BomEntPK
     *
     * @param pk
     * @return the primary key of object which is of type BomEntPK.
     */
    public BomEntPK ejbFindByPrimaryKey(BomEntPK pk) throws FinderException {

        Connection        conn = null;
        PreparedStatement stmt = null;

	if (debugging)
	    debug.println(3, "ejbFindByPrimaryKey");

        try {
            conn = dataSource.getConnection();
            stmt = conn.prepareStatement(
                "SELECT * FROM M_bom WHERE b_comp_id = ? AND b_assembly_id = ? AND b_line_no = ?");

            stmt.setString(1, pk.componentId);
            stmt.setString(2, pk.assemblyId);
            stmt.setInt(3, pk.lineNo);

            ResultSet resultSet = stmt.executeQuery();

            if (!resultSet.next()) {
		if (debugging)
		    debug.println(
                    1, "No keys found in M_bom where componentId  = "
                    + componentId);

                throw new FinderException(className
                                          + "(ejbFindByPrimaryKey)");
            } else {
                return (pk);
            }
        } catch (SQLException e) {
	    if (debugging)
		debug.println(
                1, "SQLException SELECT * from M_bom where b_comp_id = "
                + componentId + " : failed");
            debug.printStackTrace(e);

            throw new EJBException(e);
        } finally {
            Util.closeConnection(conn, stmt);
        }
    }

    /**
     * Finds all the BOM objects in the DB and returns
     * an enumeration of primary keys.
     *
     * @return an enumeration of primary keys (BomEntPK) of
     *         all inventory objects in the Db.
     */
    public java.util.Enumeration ejbFindAll() throws FinderException {

        Connection        conn = null;
        PreparedStatement stmt = null;

	if (debugging)
	    debug.println(3, "ejbFindAll");

        try {
            String cid;
            String aid;
            int    lnum;

            conn = dataSource.getConnection();
            stmt = conn.prepareStatement("SELECT * FROM M_bom");

            ResultSet resultSet = stmt.executeQuery();
            Vector    keys      = new Vector();

            while (resultSet.next()) {
                cid  = resultSet.getString(1).trim();
                aid  = resultSet.getString(2).trim();
                lnum = resultSet.getInt(3);

                keys.addElement(new BomEntPK(aid, cid, lnum));
            }

            return keys.elements();
        } catch (SQLException e) {
	    if (debugging)
		debug.println(1, "No objects found in M_bom" + e.getMessage());
            debug.printStackTrace(e);

            throw new EJBException(e);
        } finally {
            Util.closeConnection(conn, stmt);
        }
    }

    /**
     * This method looks for the Bill of Materials based on assemblyId
     * and lineNo. Called from AssemblyEntityBean after a work order has
     * been scheduled.
     * @param assemblyId
     * @return a collection of BOMs that is needed for this particular
     *      assembly
     */
    public java.util.Enumeration ejbFindBomForAssembly(String assemblyId)
            throws FinderException {

        Connection        conn = null;
        PreparedStatement stmt = null;

	if (debugging)
	    debug.println(3, "ejbFindBomForAssembly");

        try {
            conn = dataSource.getConnection();
            stmt = conn.prepareStatement(
                "SELECT b_comp_id,b_line_no FROM M_bom WHERE b_assembly_id = ?");

            stmt.setString(1, assemblyId);

            ResultSet resultSet = stmt.executeQuery();

            if (!resultSet.next()) {
		if (debugging)
		    debug.println(
                    1, "No rows found from M_bom where b_asssemblyId = "
                    + assemblyId);

                throw new FinderException(
                    "ejbFindBomForAssembly: Row not found ");
            }

            Vector al = new Vector();
            String cId;
            int    lnum;

            do {
                cId  = resultSet.getString(1).trim();
                lnum = resultSet.getInt(2);

                al.addElement(new BomEntPK(assemblyId, cId, lnum));
            } while (resultSet.next());

            return (al.elements());
        } catch (SQLException e) {
	    if (debugging)
		debug.println(
                1, "SQLException: SELECT b_comp_id, b_line_no from M_bom where b_assembly_id = "
                + assemblyId);
            debug.printStackTrace(e);

            throw new EJBException(e);
        } finally {
            Util.closeConnection(conn, stmt);
        }
    }

    /**
     * Deletes this BOM object from the DB.
     */
    public void ejbRemove() throws RemoveException {

        Connection        conn = null;
        PreparedStatement stmt = null;

	if (debugging)
	    debug.println(3, "ejbRemove");
        super.ejbRemove();

        try {
            int ret;

            conn = dataSource.getConnection();
            stmt = conn.prepareStatement(
                "DELETE FROM M_bom WHERE b_comp_id = ? AND b_assembly_id = ? AND b_line_no = ?");

            stmt.setString(1, componentId);
            stmt.setString(2, assemblyId);
            stmt.setInt(3, lineNo);

            if ((ret = stmt.executeUpdate()) < 1) {
		if (debugging)
		    debug.println(
                    1, "executeUpdate: DELETE from M_bom where b_comp_id = "
                    + componentId + " returned " + ret);

                throw new RemoveException(className + "(ejbRemove): ");
            }
        } catch (SQLException e) {
	    if (debugging)
		debug.println(
                1, "SQLException:  DELETE from M_bom where b_comp_id = "
                + componentId);
            debug.printStackTrace(e);

            throw new EJBException(e);
        } finally {
            Util.closeConnection(conn, stmt);
        }
    }

    /**
     * Updates this BOM object in the DB. This is
     * called if the information in the pool needs to be updated.
     */
    public void ejbStore() {

        Connection        conn = null;
        PreparedStatement stmt = null;

	if (debugging)
	    debug.println(3, "ejbStore");

        if(!dirty) 
            return;

        super.ejbStore();

        try {
            conn = dataSource.getConnection();
            stmt = conn.prepareStatement(
                "UPDATE M_bom SET b_comp_id = ?, b_assembly_id = ?, b_line_no = ?, b_qty = ?, b_ops = ?, b_eng_change = ?, b_ops_desc = ? WHERE b_comp_id = ? AND b_assembly_id = ? AND b_line_no = ?");

            stmt.setString(1, componentId);
            stmt.setString(2, assemblyId);
            stmt.setInt(3, lineNo);
            stmt.setInt(4, qty);
            stmt.setInt(5, opsNo);
            stmt.setString(6, engChange);
            stmt.setString(7, opsDesc);
            stmt.setString(8, componentId);
            stmt.setString(9, assemblyId);
            stmt.setInt(10, lineNo);
            stmt.executeUpdate();

            // Need to verify if the execute succeeded.
        } catch (SQLException e) {
	    if (debugging)
		debug.println(1, "UPDATE M_bom SET b_comp_id ... : Failed : "
                          + e.getMessage());
            debug.printStackTrace(e);

            throw new EJBException(e);
        } finally {
            Util.closeConnection(conn, stmt);
        }
    }

    /**
     * Method ejbActivate
     *
     *
     */
    public void ejbActivate() {
        pkey = (BomEntPK) entityContext.getPrimaryKey();
        super.ejbActivate();
    }

    /**
     * Loads this BOM object from the DB into the pool.
     * Called after an ejbCreate/ejbActivate is done.
     */
    public void ejbLoad() {

        Connection        conn = null;
        PreparedStatement stmt = null;

	if (debugging)
	    debug.println(3, "ejbLoad");

        try {
            conn = dataSource.getConnection();
            stmt = conn.prepareStatement(
                "SELECT * FROM M_bom WHERE b_comp_id = ? AND b_assembly_id = ? AND b_line_no = ?");

            stmt.setString(1, pkey.componentId);
            stmt.setString(2, pkey.assemblyId);
            stmt.setInt(3, pkey.lineNo);

            ResultSet resultSet = stmt.executeQuery();

            if (!resultSet.next()) {
                throw new NoSuchEntityException("ejbload: Row not found ");
            }

            componentId = resultSet.getString(1);
            assemblyId  = resultSet.getString(2);
            lineNo      = resultSet.getInt(3);
            qty         = resultSet.getInt(4);
            opsNo       = resultSet.getInt(5);
            engChange   = resultSet.getString(6);
            opsDesc     = resultSet.getString(7);
        } catch (SQLException e) {
	    if (debugging)
		debug.println(
                1, "SQLException: SELECT * from M_bom where b_comp_id "
                + componentId);
            debug.printStackTrace(e);

            throw new EJBException(e);
        } finally {
            Util.closeConnection(conn, stmt);
        }

        super.ejbLoad();
        dirty = false;
    }
}

