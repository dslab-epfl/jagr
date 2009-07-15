/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: DBLoader.java,v 1.2 2003/03/22 04:55:01 emrek Exp $
 *
 */
package com.sun.ecperf.launcher;

import java.io.*;
import java.util.ArrayList;

/**
 * Script to populate the database.
 * @author Akara Sucharitakul
 */
public class DBLoader extends Script {

    /**
     * Method contains all the scripts to populate the database.
     * @exception Exception Any uncaught exception in script.
     */
    public void runScript() throws Exception {

        String scale = "1";

        if (args.length > 0) {
            scale = args[0];
            System.out.println("Loading database with orders_injection_rate = "
                               + scale);
        } else {
            System.out.println(
                          "orders_injection_rate not specified, setting to 1");
        }

        String jdbcClassPath = env.get("JDBC_CLASSPATH");

        if (jdbcClassPath == null) {
            System.err.println("JDBC_CLASSPATH not set!");
            System.exit(1);
        }

        env.set("CLASSPATH", jdbcClassPath + ps + ecperfHome
                + "jars" + fs + "load.jar");

        String[] environment = env.getList();
        String javaHome = env.get("JAVA_HOME");
        if (javaHome == null)
            System.exit(0);

        if (!javaHome.endsWith(fs))
            javaHome += fs;

        String loadPkg = "com.sun.ecperf.load.";

        ArrayList cmd = new ArrayList(5);

        cmd.add(javaHome + "bin" + fs + "java");
        cmd.add("-Decperf.home=" + ecperfHome);
        cmd.add(loadPkg + "LoadCorp");
        cmd.add(scale);
            
        System.out.println("Loading Corp Database...");
        new Launcher(cmd, environment).exec();

        cmd.set(2, loadPkg + "LoadOrds");

        System.out.println("Loading Orders Database...");
        new Launcher(cmd, environment).exec();

        cmd.set(2, loadPkg + "LoadMfg");

        System.out.println("Loading Manufacturing Database...");
        new Launcher(cmd, environment).exec();

        cmd.set(2, loadPkg + "LoadSupp");

        System.out.println("Loading Supplier Database...");
        new Launcher(cmd, environment).exec();

        cmd.set(2, loadPkg + "LoadRules");
        cmd.set(3, "discount");
        cmd.add(ecperfHome + "schema" + fs + "discount.rules");

        System.out.println("Loading Discount Rules...");
        new Launcher(cmd, environment).exec();
    }
}
