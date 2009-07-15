
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * akmits@eng.sun.com 04/03/2000
 * @author Ajay Mittal
 *
 * $Id: AssemblyBmpHelper.java,v 1.2 2003/03/22 04:55:01 emrek Exp $
 */
package com.sun.ecperf.mfg.assemblyent.ejb;


import javax.ejb.*;

import javax.naming.*;

import java.rmi.*;

import java.util.*;

import com.sun.ecperf.mfg.componentent.ejb.*;
import com.sun.ecperf.mfg.partent.ejb.*;
import com.sun.ecperf.common.*;

import java.sql.*;


/**
 * This class is helper class for Assembly
 * A separate helper class is needed because ComponentEntBmpEJB and
 * AssemblyEntBmpEJB are already subclasses of ComponentEntCmpEJB
 * and AssemblyEntCmpEJB repectively. If Java supported multiple inherintance
 * somebody might subclass ComponentEntCmpEJB and PartEntBmpEJB
 * to construct ComponentEntBmpEJB.
 * @see AssemblyCmpEJB for more details
 * @see AssemblyBmpEJB for more details
 */
public class AssemblyBmpHelper extends ComponentBmpHelper {

    /**
     * Constructor AssemblyBmpHelper
     *
     *
     * @param pce
     *
     */
    public AssemblyBmpHelper(PartCmpEJB pce) {
        super(pce);
    }

    /**
     * helperEjbFindAll: This method is overridden because the SQL query is different
     * from Parts query
     */
    public java.util.Enumeration helperEjbFindAll() throws FinderException {

	if (pce.debugging)
	    pce.debug.println(3, "helperEjbFindAll()");

        Connection        connection = null;
        PreparedStatement statement  = null;

        try {
            connection = dataSource.getConnection();
            statement  = connection.prepareStatement(
                "SELECT p_id FROM M_parts WHERE p_id IN (SELECT b_assembly_id FROM M_bom)");

            ResultSet resultSet = statement.executeQuery();
            Vector    keys      = new Vector();

            while (resultSet.next()) {
                String pceid = resultSet.getString(1);

                keys.addElement(pceid);
            }

            return keys.elements();
        } catch (SQLException e) {
	    if (pce.debugging)
		pce.debug.println(
                1, "SELECT p_id FROM M_parts WHERE p_id IN (SELECT b_assembly_id FROM M_bom) : Failed : "
                + e.getMessage());
            pce.debug.printStackTrace(e);

            throw new EJBException(e);
        } finally {
            Util.closeConnection(connection, statement);
        }
    }
}

