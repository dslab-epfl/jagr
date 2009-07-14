
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.

 * $Id: SComponentBmpEJB.java,v 1.1.1.1 2002/11/16 05:35:30 emrek Exp $
 *
 */
package com.sun.ecperf.supplier.scomponentent.ejb;


//Import statements
import javax.ejb.*;

import javax.naming.*;

import java.rmi.*;

import java.sql.*;

import java.util.*;

import java.util.Enumeration; 

import com.sun.ecperf.common.*;


/**
 * This is the Bean managed persistence implementation of the Component Entity Bean
 * in the Supplier Domain.
 *
 *
 * @author Damian Guy
 */
public class SComponentBmpEJB extends SComponentCmpEJB {

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
            dataSource = (javax.sql.DataSource) context
                        .lookup("java:comp/env/SupplierDataSource");
        } catch (NamingException e) {
            debug.printStackTrace(e);

            throw new EJBException("Failure looking up DataSource " + e);
        }
    }

    /**
     * create: Create new Component.
     *
     * @param compID
     * @param compName
     * @param compDesc
     * @param compUnit
     * @param compCost
     * @param qtyOnOrder
     * @param qtyDemanded
     * @param leadTime
     * @param containerSize
     * @return ComponentEnt
     * @exception CreateException - if the create fails.
     */
    public String ejbCreate(
            String compID, String compName, String compDesc, String compUnit, double compCost, int qtyOnOrder, int qtyDemanded, int leadTime, int containerSize)
                throws CreateException {

        Connection        connection = null;
        PreparedStatement prep       = null;

        super.ejbCreate(compID, compName, compDesc, compUnit, compCost,
                        qtyOnOrder, qtyDemanded, leadTime, containerSize);

        try {
            String insertStatement =
                "INSERT INTO S_component VALUES (?, ?, ?, ?, ?, ?, ?, ?, ? )";

            connection = dataSource.getConnection();
            prep       = connection.prepareStatement(insertStatement);

            prep.setString(1, compID);
            prep.setString(2, compName);
            prep.setString(3, compDesc);
            prep.setString(4, compUnit);
            prep.setDouble(5, compCost);
            prep.setInt(6, qtyOnOrder);
            prep.setInt(7, qtyDemanded);
            prep.setInt(8, leadTime);
            prep.setInt(9, containerSize);

            int retval = prep.executeUpdate();

            if (retval != 1) {
                throw new CreateException(
                    "INSERT INTO S_component VALUES ... : Failed ");
            }
        } catch (SQLException se) {
            if (debugging)
                debug.println(1, "INSERT INTO S_component VALUES ... : Failed : "
                              + se.getMessage());
            debug.printStackTrace(se);

            throw new EJBException(se);
        } finally {
            Util.closeConnection(connection, prep);
        }

        return this.compID;
    }

    /**
     * ejbFindByPrimaryKey: Find the component for the given Primary Key.
     * @param pk - find component that matches pk
     * @return String
     * @exception FinderException - if cannot find component for pk.
     */
    public String ejbFindByPrimaryKey(String pk) throws FinderException {

        if (debugging)
            debug.println(3, "ejbFindByPrimaryKey");

        Connection        connection = null;
        PreparedStatement prep       = null;

        try {
            connection = dataSource.getConnection();

            String select =
                "SELECT comp_id FROM S_component WHERE comp_id = ?";

            prep = connection.prepareStatement(select);

            prep.setString(1, pk);

            ResultSet rs = prep.executeQuery();

            if (!rs.next()) {
                throw new ObjectNotFoundException(
                    "SELECT comp_id FROM S_component WHERE " + pk
                    + " Component Not Found");
            }

            return pk;
        } catch (SQLException se) {
            if (debugging)
                debug.println(
                    1, "SELECT comp_id FROM S_component WHERE comp_id = " + pk
                    + " : Failed : " + se.getMessage());
            debug.printStackTrace(se);

            throw new EJBException(se);
        } finally {
            Util.closeConnection(connection, prep);
        }
    }

    /**
     * ejbFindAll: find all the components that we know about.
     * @return Emumeration - Enumeration of PKs.
     * @exception FinderException - if none found.
     */
    public Enumeration ejbFindAll() throws FinderException {

        Connection        connection = null;
        PreparedStatement prep       = null;
	
        try {
            connection = dataSource.getConnection();

            String select = "SELECT comp_id FROM S_component";

            prep = connection.prepareStatement(select);

            ResultSet rs         = prep.executeQuery();
            Vector v             = new Vector();

            while (rs.next()) {
                v.add(rs.getString(1));
            }

	    Enumeration en = v.elements();
            return en;
        } catch (SQLException se) {
            if (debugging)
                debug.println(1, "SELECT comp_id FROM S_component : Failed : "
                              + se.getMessage());
            debug.printStackTrace(se);

            throw new EJBException(se);
        } finally {
            Util.closeConnection(connection, prep);
        }
    }

    /**
     * Method unsetEntityContext
     *
     *
     */
    public void unsetEntityContext() {
        entityContext = null;
    }

    /**
     * Method ejbActivate
     *
     *
     */
    public void ejbActivate() {
        compID = (String) entityContext.getPrimaryKey();
        super.ejbActivate();
    }

    /**
     * Method ejbPassivate
     *
     *
     */
    public void ejbPassivate() {
        compID = null;
    }

    /**
     * Method ejbRemove
     *
     *
     * @throws RemoveException
     *
     */
    public void ejbRemove() throws RemoveException {

        Connection        connection = null;
        PreparedStatement prep       = null;

        try {
            connection = dataSource.getConnection();

            String delStatement = "DELETE FROM S_component WHERE comp_id = ?";

            prep = connection.prepareStatement(delStatement);

            prep.setString(1, compID);

            if (prep.executeUpdate() < 1) {
                throw new RemoveException(
                    "DELETE FROM S_component WHERE comp_id = " + compID
                    + " : Failed ");
            }
        } catch (SQLException se) {
            if (debugging)
                debug.println(1, "DELETE FROM S_component WHERE comp_id = "
                              + compID + " : Failed : " + se.getMessage());
            debug.printStackTrace(se);

            throw new EJBException(se);
        } finally {
            Util.closeConnection(connection, prep);
        }
    }

    /**
     * Method ejbLoad
     *
     *
     */
    public void ejbLoad() {

        Connection        connection = null;
        PreparedStatement prep       = null;

        try {
            connection = dataSource.getConnection();

            String select = "SELECT * FROM S_component WHERE comp_id = ?";

            prep = connection.prepareStatement(select);

            prep.setString(1, compID.trim());

            ResultSet rs = prep.executeQuery();

            if (!rs.next()) {
                throw new NoSuchEntityException(
                    "SELECT * FROM S_component WHERE comp_id = " + compID
                    + " : Row not found");
            }

            compID        = rs.getString(1);
            compName      = rs.getString(2);
            compDesc      = rs.getString(3);
            compUnit      = rs.getString(4);
            compCost      = rs.getDouble(5);
            qtyOnOrder   = rs.getInt(6);
            qtyDemanded   = rs.getInt(7);
            leadTime      = rs.getInt(8);
            containerSize = rs.getInt(9);
        } catch (SQLException se) {
            if (debugging)
                debug.println(1, "SELECT * FROM S_component WHERE comp_id = "
                              + compID + " : Failed : " + se.getMessage());
            debug.printStackTrace(se);

            throw new EJBException(se);
        } finally {
            Util.closeConnection(connection, prep);
        }
    }

    /**
     * updateDemand: update the qtyDemanded for a component.
     * @param qtyRequired - quantity to add to existing qtyDemanded.
     */
    public void updateDemand(int qtyRequired) {
        super.updateDemand(qtyRequired);
    }

    /**
     * updateQuantities: update the qtyOnOrder and qtyDemanded fields.
     *
     * @param qtyOrdered
     * @param qtyDemanded - qty to add to qtyDemanded.
     */
    public void updateQuantities(int qtyOrdered, int qtyDemanded) {
        super.updateQuantities(qtyOrdered, qtyDemanded);
    }

    /**
     * deliveredQuantity: used to update the qtyOnOrder and
     * qtyDemanded fields when an order has been delivered.
     */
    public void deliveredQuantity(int quantityDelivered) {
        super.deliveredQuantity(quantityDelivered);
    }

    /**
     * Method ejbStore
     *
     *
     */
    public void ejbStore() {


        Connection        connection = null;
        PreparedStatement prep       = null;

        try {
            connection = dataSource.getConnection();

            String update =
                "UPDATE S_component SET comp_name = ?, "
                + "comp_desc = ?, comp_unit = ?, "
                + "comp_cost = ?, qty_on_order = ?, "
                + "qty_demanded = ?, lead_time = ?, container_size = ?"
                + " WHERE comp_id = ?";

            prep = connection.prepareStatement(update);

            prep.setString(1, compName);
            prep.setString(2, compDesc);
            prep.setString(3, compUnit);
            prep.setDouble(4, compCost);
            prep.setInt(5, qtyOnOrder);
            prep.setInt(6, qtyDemanded);
            prep.setInt(7, leadTime);
            prep.setInt(8, containerSize);
            prep.setString(9, compID);
            prep.executeUpdate();
        } catch (SQLException se) {
            if (debugging)
                debug.println(
                    1, "UPDATE S_component SET compName ... : Failed : "
                    + se.getMessage());
            debug.printStackTrace(se);

            throw new EJBException(se);
        } finally {
            Util.closeConnection(connection, prep);
        }
    }
}

