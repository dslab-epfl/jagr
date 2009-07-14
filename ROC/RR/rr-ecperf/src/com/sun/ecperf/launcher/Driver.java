/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: Driver.java,v 1.1.1.1 2003/04/25 08:04:22 mdelgado Exp $
 *
 */
package com.sun.ecperf.launcher;

import java.io.*;
import java.util.*;
import java.net.*;

/**
 * Script to drive the benchmark.
 * @author Akara Sucharitakul
 */
public class Driver extends Script {

    String driverHost;
    Properties runProps;

    /**
     * Method contains all the scripts to drive the benchmark.
     */
    public void runScript() throws Exception {

        if (args.length > 0)
            driverHost = args[1];

        if (driverHost == null)
            driverHost = System.getProperty("node.name");

        System.out.println("Driver Host: " + driverHost);

        // Get the JAVA_HOME
        String javaHome = env.get("JAVA_HOME");
        if (javaHome == null) {
            System.err.println("JAVA_HOME not set");
            System.exit(1);
        }
        if (!javaHome.endsWith(fs))
            javaHome += fs;

        String configDir = ecperfHome + "config" + fs;

        // Set the JAVA parameter
        String javaCmd = env.get("JAVA");
        if (javaCmd == null) {
            javaCmd = javaHome + "bin" + fs + "java";
            env.set("JAVA", javaCmd);
        }

        // Set the CLASSPATH
        env.set("CLASSPATH", ecperfHome + "jars" + fs + "driver.jar" + 
		ps + env.get("CLASSPATH"));

        // Check BINDWAIT
        int bindWait = 5000;
        String bindWaitStr = env.get("BINDWAIT");
        if (bindWaitStr != null)
            bindWait = Integer.parseInt(bindWaitStr) * 1000;
        else
           env.set("BINDWAIT", "5");

        // Read run.properties
        runProps = new Properties();
        runProps.load(new FileInputStream(configDir + "run.properties"));
      
        // Check dumpStats
        boolean dumpStats = true;
        if ("0".equals(runProps.getProperty("dumpStats").trim()))
            dumpStats = false;

        /* showChart specifies if the chart should be launched.
         * It will only be launched if dumpStats is true as well.
         */
        int showChartInt = Integer.parseInt(runProps.getProperty("showChart",
                            "-1").trim());

        /* Derive the showChart value:
         * 0 = false
         * 1 = true only if dumpStats is true
         * otherwise showChart equals dumpStats
         */
        boolean showChart = dumpStats;

        if (showChartInt == 0)
            showChart = false;
        else if (showChartInt > 0 && dumpStats)
            showChart = true;
            
        // Prepare the environment for the run
        String driverPolicy = configDir + fs + "security" + fs +
                              "driver.policy";
        String driverPackage = "com.sun.ecperf.driver.";
        String[] environment = env.getList();

        ArrayList cmd = new ArrayList(8);

        // rmi registry
        // Bug Id: 4487500 Fix
        String rmiCmd = javaHome + "bin" + fs + "rmiregistry";
        cmd.add(runProps.getProperty("rmiCommand", rmiCmd));
        Launcher rmiReg = new Launcher(cmd, environment);

        // controller
        cmd.clear();
        StringTokenizer st = new StringTokenizer(javaCmd);
        int cmdLen = 0;
        for (; st.hasMoreTokens(); cmdLen++)
            cmd.add(st.nextToken());
        cmd.add("-Djava.security.policy=" + driverPolicy);
        cmd.add(driverPackage + "ControllerImpl");
        Launcher controller = new Launcher(cmd, environment);
        controller.matchOut("Binding controller to /");

        // Modified by Ramesh. App server context will be 
        // located using -D option set in the appserver.env file.

        // orders agent
        cmd.set(cmdLen + 1, driverPackage + "OrdersAgent");
        cmd.add(configDir + "agent.properties");
        cmd.add("O1");
        cmd.add(driverHost);
        Launcher ordersAgent = new Launcher(cmd, environment);

        // mfg agent
        cmd.set(cmdLen + 1, driverPackage + "MfgAgent");
        cmd.set(cmdLen + 3, "M1");
        Launcher mfgAgent = new Launcher(cmd, environment);

        // lo agent
        cmd.set(cmdLen + 1, driverPackage + "LargeOLAgent");
        cmd.set(cmdLen + 3, "L1");
        Launcher loAgent = new Launcher(cmd, environment);

        // charts
        cmd.set(cmdLen, "-DstreamChart.propertyFile=" + configDir +
                   "charts.properties");
        cmd.set(cmdLen + 1, "-classpath");
        cmd.set(cmdLen + 2, ecperfHome + "jars" + fs +
                   "charts.jar" + ps + ecperfHome + "jars" +
                   fs + "jcchart450K.jar");

        cmd.set(cmdLen + 3, "com.sun.ecperf.charts.StreamChart");
        for (int i = cmd.size() - 1; i > cmdLen + 3; i--)
            cmd.remove(i);

        Launcher chart = new Launcher(cmd, environment);

        cmd.set(cmdLen, driverPackage + "Driver");
        cmd.set(cmdLen + 1, configDir + "run.properties");
        for (int i = cmd.size() - 1; i > cmdLen + 1; i--)
            cmd.remove(i);

        Launcher driver = new Launcher(cmd, environment);
        driver.matchOut("Starting StatsWriter");

        // fetch the runID from the sequence file
        String runID = getRunID();

        // Call switchLog() and getLog only if runMfg 
        int runMfg = Integer.parseInt(runProps.getProperty("runMfg",
                     "-1").trim());

        /**************************************************************
         Stat Collection code. It will invoke ECstat.sh script in 
         ECPERF_HOME/bin directory. and pass the following args
         1. numberOfSeconds to start studyState
         2. studyState duration in seconds
         3. interval for each measurement
         4. number of measurement to be done
         5. output dir to write the stat logs
        **************************************************************/
        Process stat = null;

        if(Integer.parseInt(runProps.getProperty("startECstat", "0")) == 1) {
            String cmdString = ecperfHome + "bin" + fs + "ECstat.sh  " +
                (Integer.parseInt(runProps.getProperty("triggerTime")) +
                Integer.parseInt(runProps.getProperty("rampUp"))) + " " +
                runProps.getProperty("stdyState") + " " +
                runProps.getProperty("interval", "30") + " " +
                (Integer.parseInt(runProps.getProperty("stdyState")) /
                Integer.parseInt(runProps.getProperty("interval", "30"))) + " " +
                runProps.getProperty("outDir") + fs + runID;

            System.out.println(cmdString);

            stat = Runtime.getRuntime().exec(cmdString);
        }

        // Now, start the run!
        rmiReg.bgExec();
        controller.bgExec();
        controller.waitMatch();
        ordersAgent.bgExec();
        mfgAgent.bgExec();
        loAgent.bgExec();

        sleep(bindWait);

        if(runMfg > 0)
            switchLog();

        driver.bgExec();
        driver.waitMatch();

        if (showChart)
            chart.bgExec();

        driver.waitFor();

        if(stat != null)
            stat.waitFor();

        // Ending the run

        if(runMfg > 0)
            getLog(runID);

        if (showChart && chart.isRunning()) {
            System.out.println("Please close Chart windows to exit driver");
            chart.waitFor();
        }

        /* We do not have to destroy all the processes. This will be done
         * automatically by the Script framework.
         */
    }

    /**
     * Switches the servlet logfile.
     * @exception IOException if request fails
     */
    void switchLog() throws IOException {

        String okMsg = "200 OK";
        URL[] url = new URL[2];

        // URL PREFIX is added for ECPERF and EMULATOR. By Default it is just a "/" 
        // It is an optional field so that other app server env files need not be 
        // updated with this field if it is not using any special URL PREFIX
        // RFE 4491953
        url[0] = new URL(new StringBuffer().append("http://" + env.get("EMULATOR_HOST")) 
                                           .append(':' + env.get("EMULATOR_PORT")) 
                                           .append(env.get("EMULATOR_PREFIX", "/")) 
                                           .append("Emulator/EmulatorServlet?cmd=switchlog")
                                           .toString());

        url[1] = new URL(new StringBuffer().append("http://" + env.get("ECPERF_HOST") + ':')
                                   .append(env.get("ECPERF_PORT")) 
                                   .append(env.get("ECPERF_PREFIX", "/")) 
                         .append("Supplier/DeliveryServlet?cmd=switchlog").toString());

        for (int i = 0; i < url.length; i++ ) {
            HttpURLConnection conn = (HttpURLConnection) url[i].openConnection();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));

            boolean ok = false;
            String r = null;

            for (;;) {
                r = reader.readLine();
                if (r == null)
                    break;
                if (r.indexOf(okMsg) != -1)
                    ok = true;
            }
            reader.close();
            if (!ok) {
                throw new IOException("Unsuccessful switchlog");
            }
        }
    }


    /**
     * Gets the servlet log for both the emulator and delivery
     * servlet.
     * @param runID The id of the run received from getRunID
     * @exception IOException If downloading the logs fail
     * @see getRunID
     */
    void getLog(String runID) throws IOException {

        URL[] url = new URL[2];

        url[0] = new URL(new StringBuffer().append("http://" + env.get("EMULATOR_HOST")) 
                                           .append(':' + env.get("EMULATOR_PORT")) 
                                           .append(env.get("EMULATOR_PREFIX", "/")) 
                                           .append("Emulator/EmulatorServlet?cmd=getlog")
                                           .toString());

        url[1] = new URL(new StringBuffer().append("http://" + env.get("ECPERF_HOST") + ':')
                                   .append(env.get("ECPERF_PORT")) 
                                   .append(env.get("ECPERF_PREFIX", "/")) 
                         .append("Supplier/DeliveryServlet?cmd=getlog").toString());


        String homeDir = System.getProperty("user.home");
        String outDir = runProps.getProperty("outDir");
        if (outDir == null)
                outDir = homeDir + fs + "output";

        FileOutputStream[] outStream = new FileOutputStream[2];
        outStream[0] = new FileOutputStream(outDir + fs + runID + fs + "emulator.err");
        outStream[1] = new FileOutputStream(outDir + fs + runID + fs + "delivery.err");

        for (int i = 0; i < url.length; i++ ) {
            HttpURLConnection conn = (HttpURLConnection) url[i].openConnection();
            StreamConnector s = new StreamConnector(conn.getInputStream(), outStream[i]);
            s.run();
        }
    }

    /*
     * This method retrieves the ID for the current run, by looking
     * in the ecperf.seq file in the user's home directory.
     * It increments the sequence file.
     */
    private String getRunID() throws IOException {
        String runId = null;
        File seqFile = new File(System.getProperty("user.home"), "ecperf.seq");
        if (seqFile.exists()) {
            BufferedReader bufIn = new BufferedReader(
                                   new FileReader(seqFile));
            runId = bufIn.readLine();
            bufIn.close();
            runId = String.valueOf(Integer.parseInt(runId));
        }
        else {
            runId = "1";
        }
        return runId;
    }
}
