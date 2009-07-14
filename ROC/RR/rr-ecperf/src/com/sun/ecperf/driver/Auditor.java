/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: Auditor.java,v 1.1.1.1 2003/04/25 08:04:22 mdelgado Exp $
 *
 */
package com.sun.ecperf.driver;

import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;
import java.net.*;
import java.io.*;
import java.util.*;
import javax.naming.*;
import javax.ejb.*;
import javax.rmi.*;

import com.sun.ecperf.orders.orderauditses.ejb.*;
import com.sun.ecperf.mfg.mfgauditses.ejb.*;
import com.sun.ecperf.supplier.supplierauditses.ejb.*;
import com.sun.ecperf.corp.corpauditses.ejb.*;

public class Auditor {

	Properties runProps = null;
        RunInfo runInfo = null;

        MfgAuditSes mfgAudit = null;
        OrderAuditSes orderAudit = null;
        CorpAuditSes corpAudit = null;
        SupplierAuditSes suppAudit = null;

        private int initCorpCustCnt = 0;
        private int initPOCount = 0;
        private int initPOLineCount = 0;
	/**
	 * Constructor
	 * 
	 */
    public Auditor(Properties props, RunInfo info) {
		runProps = props;
                runInfo = info;
    }

    private void getAuditBeans() throws RemoteException {

        // Get an initial context
	Context context;

	try {
            context = new InitialContext();
	} catch (NamingException ne) {
            throw new RemoteException("InitialContext failed. " + ne);
        }

        try {
            // Create audit objects
            String mfgaudithome, ordsaudithome, suppaudithome, corpaudithome;

            // The homePrefix will have the trailing '/'
            String prefix = runProps.getProperty("homePrefix");
            if (prefix != null) {
                  mfgaudithome = prefix + "MfgAuditSes";
                  ordsaudithome = prefix + "OrderAuditSes";
                  suppaudithome = prefix + "SupplierAuditSes";
                  corpaudithome = prefix + "CorpAuditSes";
            }
            else {
                  mfgaudithome = "MfgAuditSes";
                  ordsaudithome = "OrderAuditSes";
                  suppaudithome = "SupplierAuditSes";
                  corpaudithome = "CorpAuditSes";
            }

            if(runInfo.runOrderEntry == 1) {
                OrderAuditSesHome orderAuditSesHome =
                        (OrderAuditSesHome) PortableRemoteObject.narrow
                        (context.lookup(ordsaudithome), OrderAuditSesHome.class);

                orderAudit = orderAuditSesHome.create();

                CorpAuditSesHome corpAuditSesHome =
                        (CorpAuditSesHome) PortableRemoteObject.narrow
                        (context.lookup(corpaudithome), CorpAuditSesHome.class);

                corpAudit = corpAuditSesHome.create();
            }

            if(runInfo.runMfg == 1) {
                MfgAuditSesHome mfgAuditSesHome =
                              (MfgAuditSesHome) PortableRemoteObject.narrow
                              (context.lookup(mfgaudithome), MfgAuditSesHome.class);
                mfgAudit = mfgAuditSesHome.create();

                SupplierAuditSesHome suppAuditSesHome =
                              (SupplierAuditSesHome) PortableRemoteObject.narrow
                              (context.lookup(suppaudithome), SupplierAuditSesHome.class);
                suppAudit = suppAuditSesHome.create();
            }
        } catch (NamingException e) {
            throw new RemoteException("Failure looking up home " + e);
        } catch (Exception ex) {
            throw new RemoteException("Failure creating Audit bean " + ex);
        }
    }


    public void validateInitialValues() throws RemoteException {

        // Get the Bean refs
        getAuditBeans(); 

        // Validate initial Mfg DB State
        try {
            if((runInfo.runOrderEntry == 1) &&
               ((!orderAudit.validateInitialValues(runInfo.txRate)) ||
                (!corpAudit.validateInitialValues(runInfo.txRate))))
                        throw new RemoteException("Invalid initial Order DB State");
            else
                this.collectInitialValues();
            

            if((runInfo.runMfg == 1) &&
               ((!mfgAudit.validateInitialValues(runInfo.txRate)) ||
                 (!suppAudit.validateInitialValues(runInfo.txRate))))
                        throw new RemoteException("Invalid initial Mfg DB State");
            else
                this.collectInitialValues();

        } catch (Exception ex) {
            throw new RemoteException("Failure in calling validateInitialValues() " + ex);
        }
    }

    public void validateReport(ECperfReport report) throws RemoteException {
        String summary = runProps.getProperty("runOutputDir") + 
                         System.getProperty("file.separator") +
                         "Audit.report";

        PrintStream ps = null;

        try {
            ps = new PrintStream(new FileOutputStream(summary));
            ps.println();
            ps.println("\t\t\tECperf Audit Report");
            ps.println("\t\t\tVersion : " + ECperfReport.version);
            ps.println();

        } catch (IOException ie) {
           ie.printStackTrace();
        }

         long startTime = runInfo.start + runInfo.rampUp;
         long endTime = startTime + runInfo.stdyState;
         ps.println("Study State Started at : " + new Date(startTime));
         ps.println("Study State Ended at : " + new Date(endTime));

         try {
            if(runInfo.runOrderEntry == 1)  {

                ps.println("Orders Domain Transactions");
                ps.println();
                // We need to take into consideration the Orders which failed 
                // credit check  & cancelled orders when checking data base
                int newOrdCount = report.ordsReport.sumNewoCount - 
                                    (report.ordsReport.sumNewoBadCredit + 
                                    report.ordsReport.sumCancelOrdCnt);
                int newOrdDbCnt = orderAudit.getOrderCount(startTime, endTime);
 
                ps.println("New Order Transaction validation");
                // ps.println("Condition : New Order TxCount ~ New Order DB Count < 5%"); 
                ps.println("Condition : New Order TxCount <= New Order DB Count"); 
                ps.println("New Order Tx Count " + newOrdCount);
                ps.println("New Order DB Count " + newOrdDbCnt);
                ps.print("Orders Transaction validation ");
                /***************
                if((newOrdCount !=  0) && ((newOrdCount < newOrdDbCnt) || 
                                          ((double)Math.abs(newOrdCount - newOrdDbCnt) / newOrdCount < 0.05)))
                ****************/
                if((newOrdCount !=  0) && (newOrdCount <= newOrdDbCnt))
                    ps.println("PASSED");
                else
                    ps.println("FAILED");
                ps.println();

                ps.println("Corp Domain Transactions");
                ps.println();
                int corpCustCnt = corpAudit.getCustomerCount();
                ps.println("Corp Customer Transaction validation");
                ps.println("Condition : Final Corp Customer Count >= Initial Count");
                ps.println("Initial Corp Customer  Count = " + initCorpCustCnt);
                ps.println("Final Corp Customer  Count  = " + corpCustCnt);
                ps.print("Corp Customer Transaction validation ");
                if(corpCustCnt >= initCorpCustCnt)
                    ps.println("PASSED");
                else
                    ps.println("FAILED");

                // remove the beans
                orderAudit.remove();
                corpAudit.remove();
            }
            else
                ps.println("Orders Transactions were not performed");

            ps.println();

            if(runInfo.runMfg == 1) {
                ps.println("Mfg Domain Transactions");
                ps.println();
                int woTxCount = report.mfgReport.workOrderCnt;
                int woDbCount = mfgAudit.getWorkOrderCount(startTime, endTime);
                ps.println("Work Order Transaction validation");
                // ps.println("Condition : Work Order TxCount ~ Work Order DB Count < 5%"); 
                ps.println("Condition : New Work Order TxCount <= New Work Order DB Count"); 
                ps.println("Work Order Tx Count " + woTxCount);
                ps.println("Work Order DB Count " + woDbCount);
                ps.print("Work Order Transaction validation ");
                /********************
                if((woTxCount !=  0) && ((woTxCount < woDbCount) || 
                                         ((double)Math.abs(woTxCount - woDbCount) / woTxCount < 0.05)))
                *******************/
                if((woTxCount !=  0) && (woTxCount <= woDbCount)) 
                    ps.println("PASSED");
                else
                    ps.println("FAILED");
                ps.println();

                ps.println("Suppier Domain Transactions");
                // Get the # req. rcvd  by Emulator & Delivery Servlets
                int[] servletTx = suppAudit.getServletTx();
                ps.println();
                ps.println("Purchase Order (PO) Transaction validation");
                int newPOCount = suppAudit.getPOCount() - initPOCount;
                // ps.println("Condition : Emulator TxCount ~ New PO DB Count < 5%"); 
                ps.println("Condition : PO DB Count <= Emulator TxCount"); 
                ps.println("Emulator Tx Count  = " + servletTx[0]);
                ps.println("PO DB Count  = " + newPOCount);
                ps.print("PO Transaction validation ");
                /************
                if((servletTx[0] > 0) && ((double)Math.abs(servletTx[0] - newPOCount) / servletTx[0] < 0.05)) 
                ************/
                if((newPOCount != 0) && (newPOCount <= servletTx[0])) 
                    ps.println("PASSED");
                else
                    ps.println("FAILED");
                ps.println();

                ps.println("Purchase Order Line (POLine) Transaction validation");
                int newPOLineCount = suppAudit.getPOLineCount() - initPOLineCount;
                ps.println("Condition : New POLine DB Count >= Delivery Servlet Tx Count"); 
                ps.println("Delivery Servlet Tx Count  = " + servletTx[1]);
                ps.println("New POLine DB Count  = " + newPOLineCount);
                ps.print("POLine Transaction validation ");
                if((servletTx[1] > 0) && (newPOLineCount >= servletTx[1]))
                    ps.println("PASSED");
                else
                    ps.println("FAILED");

                ps.println();

                mfgAudit.remove();
                suppAudit.remove();
            }
            else
                ps.println("Manufacturing Transactions were not performed");

        } catch (Exception ex) {
            throw new RemoteException("Failure in validateReport : " + ex);
        }
    }
    private void collectInitialValues() {
        try {
            if(runInfo.runOrderEntry == 1)  {
                initCorpCustCnt = corpAudit.getCustomerCount();
            }

            if(runInfo.runMfg == 1) {
                initPOCount = suppAudit.getPOCount();
                initPOLineCount = suppAudit.getPOLineCount();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
