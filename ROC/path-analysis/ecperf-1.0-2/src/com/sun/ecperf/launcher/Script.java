/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: Script.java,v 1.1.1.1 2002/11/16 05:35:26 emrek Exp $
 *
 */
package com.sun.ecperf.launcher;

import java.io.*;
import java.util.ArrayList;

/**
 * Superclass of all scripts. Provides framework, initialization,
 * and termination services, and utility methods. All scripts are
 * supposed to inherit from this class.
 * @author Akara Sucharitakul
 */
public abstract class Script {

    String[] args;
    String ecperfHome = null;
    String fs = null;
    String ps = null;
    Environment env = null;

    /**
     * Method main contains initialization and termination services.
     * @param args Arguments passed in from the command line.
     */
    public static void main(String[] args) {
        if (args.length > 0) {
            try {
                Script script = (Script) Class.forName(
                                "com.sun.ecperf.launcher." +
                                args[0]) .newInstance();

                script.args = new String[args.length - 1];

                System.arraycopy(args, 1, script.args,
                             0, script.args.length);
            
                script.runScript();

            } catch (Throwable e) {
                e.printStackTrace();
            } finally {
                Launcher.destroyAll();
            }
        }
    }

    /**
     * Script constructor initializes often used parameters for subclass.
     */
    public Script() {

        fs = System.getProperty("file.separator");
        if (fs == null || fs.length() == 0)
            System.exit(0);

        ps = System.getProperty("path.separator");
        if (ps == null || ps.length() == 0)
            System.exit(0);

        ecperfHome = System.getProperty("ecperf.home");
        if (ecperfHome == null || ecperfHome.length() == 0)
            System.exit(0);

        if (!ecperfHome.endsWith(fs))
            ecperfHome += fs;

        try {
            env = new Environment();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Method sleep provides accurate sleeping services without interruption.
     * @param millis Milliseconds to sleep
     */
    public void sleep(long millis) {
        long end = System.currentTimeMillis() + millis;
        for (;;)
            try {
                Thread.sleep(end - System.currentTimeMillis());
                break;
            } catch (InterruptedException e) {
            } catch (IllegalArgumentException e) {
                break; // If sleep value goes below 0
            }
    }

    /**
     * Method runScript is the real script content. It is supposed to be
     * implemented by Script subclasses.
     * @exception Exception Any uncaught exception in the script.
     */
    public abstract void runScript() throws Exception;
}
