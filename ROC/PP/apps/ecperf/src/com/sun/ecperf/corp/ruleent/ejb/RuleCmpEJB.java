/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: RuleCmpEJB.java,v 1.2 2003/03/22 04:55:01 emrek Exp $
 *
 * $Mod: RulCmpEJB.java,v 1.5 2001/04/14 08:30:00 hogstrom - Modified
 *       bean to support single row entities.
 *
 *
 */
package com.sun.ecperf.corp.ruleent.ejb;


import javax.ejb.*;

import javax.naming.*;

import java.rmi.*;

import java.util.*;

import java.io.*;

import com.sun.ecperf.common.*;


/**
 * Class RuleCmpEJB
 *
 *
 * @author
 * @version %I%, %G%
 */
public class RuleCmpEJB implements EntityBean {

    public String            id;
    public String            ruleBuffer;
    protected EntityContext  entCtx;
    protected InitialContext initCtx;
    protected Debug          debug;
   protected boolean        debugging;
   protected boolean isDirty = true;
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

        if (debugging)
            debug.println(3, "ejbCreate ");

        this.id = id;
        ruleBuffer = rules;
        isDirty = false;
        return id;
    }

    /**
     * Method ejbPostCreate
     *
     *
     * @param id
     * @param rules
     *
     */
    public void ejbPostCreate(String id, String rules) {}

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
    }

    /**
     * Method getId
     *
     *
     * @return
     *
     */
    public String getId() {
        return id;
    }

    /**
     * Method getBytes
     *
     *
     * @return
     *
     */
    public byte[] getBytes() {

        if (debugging)
            debug.println(3, "getBytes");

        byte[]                rule;
        ByteArrayOutputStream out  = new ByteArrayOutputStream();
        PrintStream           pout = new PrintStream(out);

        pout.println(ruleBuffer);

        rule = out.toByteArray();

        pout.close();

        return rule;
    }
    
    /**
     * Method ejbActivate
     *
     *
     */
    public void ejbActivate() {
        if (debugging)
            debug.println(3, "ejbActivate ");
    }

    /**
     * Method ejbPassivate
     *
     *
     */
    public void ejbPassivate() {
        if (debugging)
            debug.println(3, "ejbPassivate ");
    }

    /**
     * Method ejbLoad
     *
     *
     */
    public void ejbLoad() {
        if (debugging)
            debug.println(3, "ejbLoad ");
        isDirty = false;
    }

    /**
     * Method ejbStore
     *
     *
     */
    public void ejbStore() {
        if (debugging)
            debug.println(3, "ejbStore ");
        isDirty = false;
    }

   public boolean isModified()
   {
      return isDirty;
   }

    /**
     * Method setEntityContext
     *
     *
     * @param entCtx
     *
     */
    public void setEntityContext(EntityContext entCtx) {

        this.entCtx = entCtx;

        try {
            initCtx = new InitialContext();

            int debugLevel =
                ((Integer) initCtx.lookup("java:comp/env/debuglevel"))
                    .intValue();

            if (debugLevel > 0) {
                debug = new DebugPrint(debugLevel, this);
                debugging = true;
            } else {
                debug = new Debug();
                debugging = false;
            }
        } catch (NamingException ne) {
            System.out.println("RuleCmpEJB: debuglevel Property not set."
                               + "Turning off debug messages");

            debug = new Debug();
        }

        if (debugging)
            debug.println(3, "setEntityContext");
    }

    /**
     * Method unsetEntityContext
     *
     *
     */
    public void unsetEntityContext() {

        if (debugging)
            debug.println(3, "unsetEntityContext ");

        ruleBuffer = null;
    }
}
