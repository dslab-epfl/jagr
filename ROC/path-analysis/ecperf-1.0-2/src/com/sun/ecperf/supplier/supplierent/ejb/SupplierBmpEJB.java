
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.

 * $Id: SupplierBmpEJB.java,v 1.1.1.1 2002/11/16 05:35:30 emrek Exp $
 *
 */
package com.sun.ecperf.supplier.supplierent.ejb;


//Import statements
import javax.ejb.*;

import javax.naming.*;

import java.rmi.*;

import java.sql.*;

import com.sun.ecperf.common.*;

import java.util.*;


/**
 * Bean managed persistence implementation of Supplier Entity Bean.
 *
 *
 * @author Damian Guy
 */
public class SupplierBmpEJB extends SupplierCmpEJB {

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
                        .lookup("java:comp/env/SupplierDataSource");
        } catch (NamingException e) {
            debug.printStackTrace(e);

            throw new EJBException("Failure looking up DataSource " + e);
        }
    }

    /**
     * ejbCreate: create a new supplier.
     * @param suppID - id of supplier.
     * @param suppName - supplier name.
     * @param suppStreet1 - street line 1.
     * @param suppStreet2 - street line 2.
     * @param suppCity - city supplier is located.
     * @param suppState
     * @param suppCountry - country supplier is located.
     * @param suppZip - zip/postal code.
     * @param suppPhone - contact phone number.
     * @param suppContact - contact person.
     * @return SupplierEnt - newly created Supplier
     * @exception CreateException - if the create fails.
     */
    public Integer ejbCreate(int suppID, String suppName,
                             String suppStreet1, String suppStreet2, String suppCity, String suppState, String suppCountry, String suppZip, String suppPhone, String suppContact)
                                 throws CreateException {

        super.ejbCreate(suppID, suppName, suppStreet1, suppStreet2,
                        suppCity, suppState, suppCountry, suppZip,
                        suppPhone, suppContact);

        Connection        connection = null;
        PreparedStatement prep       = null;

        try {
            connection = dataSource.getConnection();

            String insert =
                "INSERT INTO S_supplier VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ? )";

            prep = connection.prepareStatement(insert);

            prep.setInt(1, suppID);
            prep.setString(2, suppName);
            prep.setString(3, suppStreet1);
            prep.setString(4, suppStreet2);
            prep.setString(5, suppCity);
            prep.setString(6, suppState);
            prep.setString(7, suppCountry);
            prep.setString(8, suppZip);
            prep.setString(9, suppPhone);
            prep.setString(10, suppContact);

            int retval = prep.executeUpdate();

            if (retval != 1) {
                throw new CreateException(
                    "insert into S_supplier ... : Failed");
            }
        } catch (SQLException se) {
            if (debugging)
                debug.println(1, "insert into S_supplier ... : Failed : "
                              + se.getMessage());
            debug.printStackTrace(se);

            throw new EJBException(se);
        } finally {
            Util.closeConnection(connection, prep);
        }
        return this.suppID;
    }

    /**
     * ejbFindByPrimaryKey: find the supplier whose id = pk.
     * @param pk - id of supplier.
     * @return SUpplierEnt.
     * @exception FinderException - if cannot find object for pk.
     */
    public Integer ejbFindByPrimaryKey(Integer pk) throws FinderException {

        Connection        connection = null;
        PreparedStatement prep       = null;

        try {
            connection = dataSource.getConnection();

            String select =
                "SELECT supp_id FROM S_supplier WHERE supp_id = ?";

            prep = connection.prepareStatement(select);

            prep.setInt(1, pk.intValue());

            ResultSet rs = prep.executeQuery();

            if (!rs.next()) {
                throw new ObjectNotFoundException(
                    "select suppID from S_supplier where suppID = " + pk
                    + " : Row not Found");
            }

            return pk;
        } catch (SQLException se) {
            if (debugging)
                debug.println(1, "select suppID from S_supplier where suppID = "
                              + pk + " : Failed : " + se.getMessage());
            debug.printStackTrace(se);

            throw new EJBException(se);
        } finally {
            Util.closeConnection(connection, prep);
        }
    }

    /**
     * ejbFindAll: find all suppliers.
     * @return Enumeration - of Suppliers.
     * @exception FinderException - if there are not any suppliers.
     */
    public Enumeration ejbFindAll() throws FinderException {

        if (debugging)
            debug.println(3, "finding All Suppliers");

        Connection        connection = null;
        PreparedStatement prep       = null;

        try {
            connection = dataSource.getConnection();

            String select = "SELECT supp_id FROM S_supplier";

            prep = connection.prepareStatement(select);

            ResultSet rs   = prep.executeQuery();
            Vector v       = new Vector();

            while (rs.next()) {
                Integer key = new Integer(rs.getInt(1));

                v.add(key);
            }
	    
            Enumeration en = v.elements();
            return en;
        } catch (SQLException se) {
            if (debugging)
                debug.println(1, "select suppID from S_supplier : Failed : "
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
        suppID = (Integer) entityContext.getPrimaryKey();
        super.ejbActivate();
    }

    /**
     * Method ejbPassivate
     *
     *
     */
    public void ejbPassivate() {
        suppID = null;
    }

    /**
     * Method ejbRemove
     *
     *
     * @throws RemoteException
     * @throws RemoveException
     *
     */
    public void ejbRemove() throws RemoteException, RemoveException {

        super.ejbRemove();

        Connection        connection = null;
        PreparedStatement prep       = null;

        try {
            String del = "DELETE FROM S_supplier WHERE supp_id = ?";

            connection = dataSource.getConnection();
            prep       = connection.prepareStatement(del);

            prep.setInt(1, suppID.intValue());

            int retval = prep.executeUpdate();

            if (retval != 1) {
                throw new RemoveException("delete from S_supplier where suppID = " 
                                          + suppID + " : Failed ");
            }
        } catch (SQLException se) {
            if (debugging)
                debug.println(1, "delete from S_supplier where suppID = "
                              + suppID + " : Failed : " + se.getMessage());
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
        int               key        = suppID.intValue();

        try {
            String sel = "SELECT * FROM S_supplier WHERE supp_id = ?";

            connection = dataSource.getConnection();
            prep       = connection.prepareStatement(sel);

            prep.setInt(1, key);

            ResultSet set = prep.executeQuery();

            if (!set.next()) {
                if (debugging)
                    debug.println(1, "Couldn't find row for suppID = " + key);

                throw new NoSuchEntityException("Couldn't find row for: "
                                                + key);
            }

            this.suppID      = new Integer(set.getInt(1));
            this.suppName    = set.getString(2);
            this.suppStreet1 = set.getString(3);
            this.suppStreet2 = set.getString(4);
            this.suppCity    = set.getString(5);
            this.suppState   = set.getString(6);
            this.suppCountry = set.getString(7);
            this.suppZip     = set.getString(8);
            this.suppPhone   = set.getString(9);
            this.suppContact = set.getString(10);
        } catch (SQLException se) {
            if (debugging)
                debug.println(1, "select * from S_supplier where suppID = "
                              + key + " : Failed : " + se.getMessage());
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

        Connection        connection = null;
        PreparedStatement prep       = null;

        try {
            StringBuffer update =
                new StringBuffer("UPDATE S_supplier SET supp_name = ?, ");

            update.append("supp_street1 = ?, supp_street2 = ?, supp_city = ?, ");
            update.append("supp_state = ?, supp_country = ?, supp_zip = ?, ");
            update.append("supp_phone = ?, supp_contact = ?");
            update.append(" WHERE supp_id = ?");

            connection = dataSource.getConnection();
            prep       = connection.prepareStatement(update.toString());

            prep.setString(1, suppName);
            prep.setString(2, suppStreet1);
            prep.setString(3, suppStreet2);
            prep.setString(4, suppCity);
            prep.setString(5, suppState);
            prep.setString(6, suppCountry);
            prep.setString(7, suppZip);
            prep.setString(8, suppPhone);
            prep.setString(9, suppContact);
            prep.setInt(10, suppID.intValue());
            prep.executeUpdate();
        } catch (SQLException se) {
            debug.printStackTrace(se);

            throw new EJBException(se);
        } finally {
            Util.closeConnection(connection, prep);
        }
    }
}

