/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: Environment.java,v 1.3 2003/03/22 04:55:01 emrek Exp $
 *
 */
package com.sun.ecperf.launcher;

import java.io.*;
import java.util.*;

/**
 * This class creates and provides access to the environment
 * needed to run ECperf from a java program. It requires the
 * system property "ecperf.home" to be set and will read the
 * environment from the env file specified in the config
 * directory.<br>
 * If an environment is needed from the parent shell, it
 * has to be passed into the java command line using
 * -Denvironment.[VAR]=$[VAR].
 * @author Akara Sucharitakul
 * @see Launcher
 */
public class Environment {

    Properties envProp;

    /**
     * Constructor reads the environment from the config directory.
     * @exception IOException ecperf.home not defined or cannot
     *                        read config/env files 
     */
    public Environment() throws IOException {


        /* Find ecperf.home */

        String ecperfHome = System.getProperty("ecperf.home");
        if (ecperfHome == null)
	    throw new IOException("Property ecperf.home not defined");
        String fileSeparator = System.getProperty("file.separator");
        if (fileSeparator == null)
	    throw new IOException("Property file.separator not defined");

        String configPath = ecperfHome + fileSeparator + "config"
                            + fileSeparator;


        /* Get the content of the appsserver file */

	FileInputStream s = new FileInputStream(configPath + "appsserver");
        byte[] b = new byte[s.available()];
	s.read(b);
        for (int i = 0; i < b.length; i++)
            if (Character.isISOControl((char) b[i]))
                b[i] = (byte) ' ';
	String appsServer = new String(b).trim();


        /* Read the appsserver environment it is pointing to */

        BufferedReader reader = new BufferedReader(new FileReader(configPath +
                                                   appsServer + ".env"));
        envProp = new Properties();

        String line = reader.readLine();

        // Populate environment from System property starting with "environment."

        for (Enumeration propNames = System.getProperties().propertyNames();
             propNames.hasMoreElements();) {

            String propName = (String) propNames.nextElement();
            if (propName.startsWith("environment."))
                envProp.setProperty(propName.substring("environment.".length()),
                    System.getProperty(propName));
        }

        // Populate environment from env file

        while (line != null) {
            int mark = line.indexOf('#');
            if (mark >= 0)
                line = line.substring(0, mark).trim();

            mark = line.indexOf('=');

            if (mark > 0) {
                String propName = line.substring(0, mark).trim();
                String propVal  = line.substring(mark + 1).trim();
                set(propName, propVal);
            }
            line = reader.readLine();
        }

    }

    /**
     * Substitutes shell variables with valid values.
     * Currently we only support Unix style shell variables
     * DOS/Win32 style can be added later.
     * This is tricky since we do not have endings, so the
     * current algorithm is rather rudimentary. If needed,
     * we have to come up with some better algorithms.
     * @param propVal The environment string befor substitution
     * @return The substituted environment string
     */
    private String substitute(String propVal) {
        int mark = propVal.indexOf('$');
        if (mark >= 0) {
            StringBuffer propValBuf = new StringBuffer(propVal);
            do {
                String shellVar = null;
                String shellVal = null;
                ++mark;
                if (propVal.charAt(mark) == '{') {
                    /* With enclosing brackets, we just find the next
                     * closing bracket and deal with the whole content.
                     */
                    int endMark = propVal.indexOf('}', mark + 1);
                    if (endMark < 0)
                        return propVal;

                    shellVar = propVal.substring(mark + 1, endMark);
                    shellVal = envProp.getProperty(shellVar);

                    if (shellVal == null)
                        propValBuf.replace(mark - 1, endMark + 1, "");
                    else
                        propValBuf.replace(mark - 1, endMark + 1, shellVal);

                } else {
                    /* Without enclosing brackets we don't know the exact
                     * ending so the code has to try to match the longest
                     * possible match. This code is quite tricky
                     */
                    char[] varBuf = new char[propVal.length() - mark];
                    propVal.getChars(mark, propVal.length(), varBuf, 0);
                    int len = 0;
                    for (; len < varBuf.length; len++)
                        if (!(varBuf[len] >= '0' && varBuf[len] <= '9') &&
                            !(varBuf[len] >= 'a' && varBuf[len] <= 'z') &&
                            !(varBuf[len] >= 'A' && varBuf[len] <= 'Z') &&
                            (varBuf[len] != '_')) {
                            shellVar = new String(varBuf, 0, len);
                            break;
                        }
                    if (shellVar == null)
                        shellVar = new String(varBuf, 0, varBuf.length);
                    int maxVarLen = shellVar.length();
                    do {
                        shellVal = envProp.getProperty(shellVar);
                        if (shellVal != null)
                            break;
                        shellVar = new String(varBuf, 0, --len);
                    } while (len > 0);
                    if (shellVal == null)
                        propValBuf.replace(mark - 1, mark + maxVarLen, "");
                    else
                        propValBuf.replace(mark - 1, mark + shellVar.length(),
                                           shellVal);
                }
                propVal = propValBuf.toString();
                mark = propVal.indexOf('$');
            } while (mark >= 0);
        }
        return propVal;
    }

    /**
     * Provides an array of Strings representing the environment
     * suitable for use in the Launcher.exec(), Launcher.bgExec()
     * and Runtime.exec() calls.
     * @return environment in a String array
     */
    public String[] getList() {
        String[] envList = new String[envProp.size()];
        int i = 0;
        for (Enumeration e = envProp.propertyNames(); e.hasMoreElements();) {
            String envName = (String) e.nextElement();
            envList[i] = envName + '=' + envProp.getProperty(envName);
            i++;
        }
        return envList;
    }

    /**
     * Returns the environment represented by a variable.
     * @param  propName The environment variable
     * @return The value of the variable
     */
    public String get(String propName) {
        return envProp.getProperty(propName);
    }

    /**
     * Returns the environment represented by a variable.
     * @param  propName The environment variable
     * @defaultProp default value if not found
     * @return The value of the variable
     */
    public String get(String propName,  String defaultProp) {
        return envProp.getProperty(propName, defaultProp);
    }

    /**
     * Sets an environment variable.
     * @param propName Variable name
     * @param propValue The variable's value
     */
    public void set(String propName, String propValue) {

        int spcIdx;

        /* Take off any quotes */
        if ((propValue.startsWith("\"") && propValue.endsWith("\"")) ||
            (propValue.startsWith("'")  && propValue.endsWith("'")))
            propValue = propValue.substring(1, propValue.length() - 1);

        /* If not quoted, the value is only to the first space.
         * The rest is ignored.
         */
        else if ((spcIdx = propValue.indexOf(' ')) >= 0)
            propValue = propValue.substring(0, spcIdx);

        /* Substitute the shell variable */
        propValue = substitute(propValue);

        /* Then push the environment */
        envProp.setProperty(propName, propValue);
    }

    public static void main(String[] args) {
        try {
            Environment env = new Environment();
            String[] list = env.getList();
            for (int i = 0; i < list.length; i++)
                System.out.println(list[i]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
