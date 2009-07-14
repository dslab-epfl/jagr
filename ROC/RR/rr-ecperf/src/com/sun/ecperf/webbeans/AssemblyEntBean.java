
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: AssemblyEntBean.java,v 1.1.1.1 2003/04/25 08:04:22 mdelgado Exp $
 *
 *
 */
package com.sun.ecperf.webbeans;


import javax.ejb.*;

import javax.naming.*;

import java.rmi.RemoteException;

import javax.rmi.PortableRemoteObject;

import java.io.Serializable;

import com.sun.ecperf.mfg.assemblyent.ejb.*;
import com.sun.ecperf.common.*;

import java.util.Vector;
import java.util.Enumeration;

import javax.servlet.*;
import javax.servlet.http.*;


/**
 * This is a helper bean used by jsp files to get a list of all assembly
 * ids. These ids are later used to schedule a work order.
 *
 * @author Venkata Yella - yella@eng.sun.com
 *
 */
public class AssemblyEntBean implements Serializable {

    private final String    jndiname = "java:comp/env/ejb/AssemblyEnt";
    private AssemblyEntHome assembly_ent_home;
    private Debug           debug;
    protected boolean       debugging;

    /**
     * Constructor AssemblyEntBean
     *
     *
     * @throws OtherException
     *
     */
    public AssemblyEntBean() throws OtherException {

        try {
            Context context    = new InitialContext();
            int     debugLevel = 0;

            try {
                debugLevel =
                    ((Integer) context.lookup("java:comp/env/debuglevel"))
                        .intValue();
            } catch (Exception e) {

                // If there's an error looking up debuglevel,
                // just leave it as the default - 0
            }
            if (debugLevel > 0) {
                debug = new DebugPrint(debugLevel, this);
                debugging = true;
            } else {
                debug = new Debug();
                debugging = false;
            }
            if (debugging)
                debug.println(3, "In constructor of AssemblyEntBean");

            Object obj = context.lookup(jndiname);

            if (debugging)
                debug.println(3, "Looked up " + jndiname);

            assembly_ent_home =
                (AssemblyEntHome) PortableRemoteObject.narrow(obj,
                    AssemblyEntHome.class);
        } catch (NamingException e) {
            throw new OtherException("Naming Exception in AssemblyEntBean", e);
        } catch (ClassCastException e) {
            throw new OtherException("Class cast Exception in AssemblyEntBean",
                                     e);
        } catch (Exception e) {
            throw new OtherException(
                "Some Other  Exception in AssemblyEntBean", e);
        }
    }

    /**
     * Method getAssemblyIds - Get a list of all assembly ids
     *
     *
     * @return Vector - A string list of assembly ids 
     *
     * @throws OtherException
     *
     */
    public Vector getAssemblyIds() throws OtherException {

        Vector      assembly_ids;
        AssemblyEnt assembly_ent;
        Enumeration assemblies;

        try {
            assemblies   = assembly_ent_home.findAll();
            assembly_ids = new Vector();

            while (assemblies.hasMoreElements()) {
                assembly_ent =
                    (AssemblyEnt) javax.rmi.PortableRemoteObject
                        .narrow(assemblies.nextElement(), AssemblyEnt.class);

                assembly_ids.add((String) assembly_ent.getId());
            }
        } catch (RemoteException e) {
            throw new OtherException(
                " Remote Exception occured for the request.", e);
        } catch (FinderException e) {
            throw new OtherException(
                " Finder Exception occured for the request.", e);
        }

        return assembly_ids;
    }
}

