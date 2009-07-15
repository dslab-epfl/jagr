
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.

 * $Id: SupplierCompBmpEJB.java,v 1.2 2003/03/22 04:55:01 emrek Exp $
 *
 */
package com.sun.ecperf.supplier.suppliercompent.ejb;


//Import statements
import javax.ejb.*;

import javax.naming.*;

import java.sql.*;

import java.rmi.*;

import java.util.*;

import com.sun.ecperf.common.*;


/**
 * This is the Bean Managed Persistence Implementation of
 * the SupplierComp Entity Bean.
 *
 *
 * @author Damian Guy
 * @modified by Henry Chen 3/15/02
 *
 */
public class SupplierCompBmpEJB extends SupplierCompCmpEJB {

    protected javax.sql.DataSource dataSource;

    private String               suppCompIDCache;
    private int                  suppCompSuppIDCache;
    private double               suppCompPriceCache;
    private int                  suppCompQtyCache;
    private double               suppCompDiscountCache;
    private int                  suppCompDelDateCache;
    private boolean              beingCreated = false;

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
                        .lookup("java:comp/env/SupplierDataSource");
        } catch (NamingException e) {
            debug.printStackTrace(e);

            throw new EJBException("Failure looking up DataSource " + e);
        }
    }

    /**
     * ejbCreate: Corresponds to create in the Home interface.
     * @param suppCompID - part number.
     * @param suppCompSuppID - supplier id.
     * @param suppCompPrice - price of supplied qty (suppCompQty).
     * @param suppCompQty - quantity that is supplied.
     * @param suppCompDiscount - discount the applies.
     * @param suppCompDelDate - probably should be lead time.
     * @return SuppCompEntPK - primary Key for this object (suppCompID + suppCompSuppID).
     * @exception CreateException - if there is a create failure.
     */
    public SuppCompEntPK ejbCreate(
            String suppCompID, int suppCompSuppID, double suppCompPrice, int suppCompQty, double suppCompDiscount, int suppCompDelDate)
                throws CreateException {
		beingCreated = true;

        super.ejbCreate(suppCompID, suppCompSuppID, suppCompPrice, suppCompQty, suppCompDiscount,
                        suppCompDelDate);

        Connection        connection = null;
        PreparedStatement prep       = null;

		suppCompIDCache = suppCompID;
		suppCompSuppIDCache = suppCompSuppID;
		suppCompPriceCache = suppCompPrice;
		suppCompQtyCache = suppCompQty;
		suppCompDiscountCache = suppCompDiscount;
		suppCompDelDateCache = suppCompDelDate;

        try {
            String insert =
                "INSERT INTO S_supp_component VALUES ( ?, ?, ?, ?, ?, ? )";

            connection = dataSource.getConnection();
            prep       = connection.prepareStatement(insert);

            prep.setString(1, suppCompID);
            prep.setInt(2, suppCompSuppID);
            prep.setDouble(3, suppCompPrice);
            prep.setInt(4, suppCompQty);
            prep.setDouble(5, suppCompDiscount);
            prep.setInt(6, suppCompDelDate);

            int retval = prep.executeUpdate();

            if (retval != 1) {
                throw new CreateException(
                    "INSERT INTO S_supp_component VALUES ... : Failed ");
            }

            return new SuppCompEntPK(suppCompID, suppCompSuppID);
        } catch (SQLException se) {
            if (debugging)
                debug.println(
                    1, "INSERT INTO S_supp_component VALUES ... : Failed : "
                    + se.getMessage());
            debug.printStackTrace(se);

            throw new EJBException(se);
        } finally {
	    	beingCreated = false;
            Util.closeConnection(connection, prep);
        }
    }

    /**
     * ejbFindByPrimaryKey
     * @retrun SupplierCompEnt
     *
     * @param pk
     *
     * @return
     *
     * @throws FinderException
     */
    public SuppCompEntPK ejbFindByPrimaryKey(SuppCompEntPK pk)
            throws FinderException {

        if (debugging)
            debug.println(3, "ejbFindByPrimaryKey");

        Connection        connection = null;
        PreparedStatement prep       = null;

        try {
            String select =
                "SELECT sc_p_id, sc_supp_id FROM S_supp_component "
                + "WHERE sc_p_id = ? AND sc_supp_id = ?";

            connection = dataSource.getConnection();
            prep       = connection.prepareStatement(select);

            prep.setString(1, pk.suppCompID);
            prep.setInt(2, pk.suppCompSuppID);

            ResultSet set = prep.executeQuery();

            if (!set.next()) {
                throw new ObjectNotFoundException("Row not found suppCompID = "
                                                  + pk.suppCompID
                                                  + " suppCompSuppID = "
                                                  + suppCompSuppID);
            }

            return pk;
        } catch (SQLException se) {
            if (debugging)
                debug.println(
                    1, "SELECT suppCompID, suppCompSuppID FROM S_supp_component ... : Failed : "
                    + se.getMessage());
            debug.printStackTrace(se);

            throw new EJBException(se);
        } finally {
            Util.closeConnection(connection, prep);
        }
    }

    /**
     * ejbFindAllBySupplier: find all components for supplier.
     *
     * @param suppID
     * @return Enumeration.
     * @exception FinderException - if there are not any rows found.
     */
    public Enumeration ejbFindAllBySupplier(int suppID)
            throws FinderException {

        if (debugging)
            debug.println(3, "ejbFindAllBySupplier");

        Connection        connection = null;
        PreparedStatement prep       = null;

        try {
            String select = "SELECT sc_p_id FROM S_supp_component "
                            + "WHERE sc_supp_id = ?";

            connection = dataSource.getConnection();
            prep       = connection.prepareStatement(select);

            prep.setInt(1, suppID);

            ResultSet set  = prep.executeQuery();
            Vector v = new Vector();

            while (set.next()) {
                String pID = set.getString(1);

                v.add(new SuppCompEntPK(pID, suppID));
            }

            if (v.isEmpty()) {
                throw new ObjectNotFoundException(
                    "No Objects found for suppID = " + suppID);
            }

	    Enumeration en  = v.elements();
            return en;
        } catch (SQLException se) {
            if (debugging)
                debug.println(
                    1, "select suppCompID from S_supp_component where suppCompSuppID = "
                    + suppID + " : Failed : " + se.getMessage());
            debug.printStackTrace(se);

            throw new EJBException(se);
        } finally {
            Util.closeConnection(connection, prep);
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

        if (debugging)
            debug.println(3, "ejbRemove");

        Connection        connection = null;
        PreparedStatement prep       = null;

        try {
           /*
             Postgres seems to have rounding errors with doubles
            String delStmt = "DELETE FROM S_supp_component WHERE "
                                   + "sc_p_id = ? AND sc_supp_id = ?"
                                   + " AND sc_p_id = ? AND sc_supp_id = ?"
	                           	   + " AND sc_price = ? AND sc_qty = ?"
                                   + " AND sc_discount = ? AND sc_del_date = ?";
           */

            String delStmt = "DELETE FROM S_supp_component WHERE "
               + "sc_p_id = ? AND sc_supp_id = ?"
               + " AND sc_p_id = ? AND sc_supp_id = ?"
               + " AND sc_qty = ?"
               + " AND sc_del_date = ?";

            connection = dataSource.getConnection();
            prep       = connection.prepareStatement(delStmt);
            int i = 1;
            prep.setString(i++, suppCompID);
            prep.setInt(i++, suppCompSuppID);

            prep.setString(i++, suppCompIDCache);
            prep.setInt(i++, suppCompSuppIDCache);
            //prep.setDouble(i++, suppCompPriceCache);
            prep.setInt(i++, suppCompQtyCache);
            //prep.setDouble(i++, suppCompDiscountCache);
            prep.setInt(i++, suppCompDelDateCache);

            int retVal = prep.executeUpdate();

            if (retVal != 1) {
                throw new RemoveException(
                    "delete from S_supp_component where .. : Failed ");
            }
        } catch (SQLException se) {
            if (debugging)
                debug.println(
                    1, "delete from S_supp_component where ... :  Failed : "
                    + se.getMessage());
            debug.printStackTrace(se);

            throw new EJBException(se);
        } finally {
            Util.closeConnection(connection, prep);
        }
    }

    /**
     * Method ejbActivate
     *
     *
     */
    public void ejbActivate() {

        SuppCompEntPK pk = (SuppCompEntPK) entityContext.getPrimaryKey();

        suppCompID    = pk.suppCompID;
        suppCompSuppID = pk.suppCompSuppID;
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

        if (debugging)
            debug.println(3, "ejbLoad");

        Connection        connection = null;
        PreparedStatement prep       = null;

        try {
            String select = "SELECT * FROM S_supp_component "
                            + "WHERE sc_p_id = ? AND sc_supp_id = ? ";

            connection = dataSource.getConnection();
            prep       = connection.prepareStatement(select);

            prep.setString(1, suppCompID);
            prep.setInt(2, suppCompSuppID);

            ResultSet set = prep.executeQuery();

            if (!set.next()) {
                throw new NoSuchEntityException(
                    "select * from S_supp_component ... : Row not found");
            }

            suppCompID       = set.getString(1);
            suppCompSuppID   = set.getInt(2);
            suppCompPrice    = set.getDouble(3);
            suppCompQty      = set.getInt(4);
            suppCompDiscount = set.getDouble(5);
            suppCompDelDate  = set.getInt(6);

	    suppCompIDCache = suppCompID;
	    suppCompSuppIDCache = suppCompSuppID;
	    suppCompPriceCache = suppCompPrice;
	    suppCompQtyCache = suppCompQty;
	    suppCompDiscountCache = suppCompDiscount;
	    suppCompDelDateCache = suppCompDelDate;
            super.ejbLoad();
        } catch (SQLException se) {
            if (debugging)
                debug.println(1, "select * from S_supp_component ... : Failed : "
                              + se.getMessage());
            debug.printStackTrace(se);

            throw new EJBException(se);
        } finally {
            Util.closeConnection(connection, prep);
        }
    }

    /**
     * Method ejbStore
     *
     *
     */
    public void ejbStore() {

        if (beingCreated)
	  return;

        if (debugging)
            debug.println(3, "ejbStore");

        Connection        connection = null;
        PreparedStatement prep       = null;

        try {
            StringBuffer update =
                new StringBuffer("UPDATE S_supp_component ");
            update.append("SET sc_price = ?, sc_qty = ?, ");
            update.append("sc_discount = ?, sc_del_date = ?");
            //update.append("sc_p_id = ?, sc_supp_id = ? ");
            
            /*
              Postgres seems to have rounding errors with doubles.
            update.append(" WHERE sc_p_id = ? AND sc_supp_id = ?");
            update.append(" AND sc_price = ? AND sc_qty = ?");
            update.append(" AND sc_discount = ? AND sc_del_date = ?");
            */
            update.append(" WHERE sc_p_id = ? AND sc_supp_id = ?");
            update.append(" AND sc_qty = ?");
            update.append(" AND sc_del_date = ?");

            connection = dataSource.getConnection();
            prep       = connection.prepareStatement(update.toString());
            int i = 1;
            prep.setDouble(i++, suppCompPrice);
            prep.setInt(i++, suppCompQty);
            prep.setDouble(i++, suppCompDiscount);
            prep.setInt(i++, suppCompDelDate);
            //prep.setString(i++, suppCompID);
            //prep.setInt(i++, suppCompSuppID);

            prep.setString(i++, suppCompIDCache);
            prep.setInt(i++, suppCompSuppIDCache);
            //            prep.setDouble(i++, suppCompPriceCache);
            prep.setInt(i++, suppCompQtyCache);
            //prep.setDouble(i++, suppCompDiscountCache);
            prep.setInt(i++, suppCompDelDateCache);

            int retVal = prep.executeUpdate();

            if (retVal != 1) {
                if(debugging) {
                     debug.println(1,"Optimistic concurrency control failed " +
                                     "in SupplierCompEnt.ejbStore() for sc_p_id = " + suppCompID +
				     "and sc_supp_id = " + suppCompSuppID);

                debug.println(1,"UPDATE S_supp_component SET sc_price = " + suppCompPrice +
			      ", sc_qty = " + suppCompQty + ", sc_discount = " + suppCompDiscount +
			      ", sc_del_date = " + suppCompDelDate + " WHERE sc_p_id = " + suppCompID +
			      " AND sc_supp_id = " + suppCompSuppID + " AND sc_p_id = " + suppCompIDCache +
			      " AND sc_supp_id = " + suppCompSuppIDCache + " AND sc_price = " + suppCompPriceCache +
			      " AND sc_qty = " + suppCompQtyCache + " AND sc_discount = " + suppCompDiscountCache +
			      " AND sc_del_date = " + suppCompDelDateCache);
			  }
                throw new EJBException("Optimistic concurrency control failed ");
	    }
	    suppCompIDCache = suppCompID;
	    suppCompSuppIDCache = suppCompSuppID;
	    suppCompPriceCache = suppCompPrice;
	    suppCompQtyCache = suppCompQty;
	    suppCompDiscountCache = suppCompDiscount;
	    suppCompDelDateCache = suppCompDelDate;
            super.ejbStore();
        } catch (SQLException se) {
            if (debugging)
                debug.println(1, "update S_supp_component ... : Failed : "
                              + se.getMessage());
            debug.printStackTrace(se);

            throw new EJBException(se);
        } finally {
            Util.closeConnection(connection, prep);
        }
    }
}

