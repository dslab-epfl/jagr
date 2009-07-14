//
// $Id: FailureMonitor.java,v 1.9 2003/05/01 03:13:13 candea Exp $
//

package org.jboss.RR;

import java.io.*;
import javax.management.ObjectName;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.ejb.Container;

/**
 *
 * @jmx:mbean name="jboss:type=FailureMonitor"
 *            extends="org.jboss.system.ServiceMBean"
 *            
 */
public class FailureMonitor 
   extends ServiceMBeanSupport
   implements FailureMonitorMBean
{
   private FMap fmap;
   
   //
   // Provided for JMX compliance.
   //
   
   public FailureMonitor()    {}


   /**
    * @jmx:managed-operation
    */
   public void newContainer ( Container cont )
   {
      String ejbName = cont.getBeanMetaData().getEjbName();
      fmap.addEjb(ejbName);
   }

   //=========================================================================
   // The ReportFailure method is called to notify the FailureMonitor that the
   // EJB housed in container 'failedNode' failed while invoking the bean
   // housed in container 'sourceNode'.

    // 
   

    //public void reportFailure ( Container srcCont, Container dstCont )
      //
      // Record an edge in the f-map from sourceNode to failedNode
      //
       //String srcName = srcCont.getBeanMetaData().getEjbName();
       //String dstName = dstCont.getBeanMetaData().getEjbName();
    //fmap.addEdge(srcCont, dstCont);
    //}

   /**
    * @jmx:managed-operation
    */
    public void reportFailure ( String caller, String callerMethod, String callee, String calleeMethod, Throwable exception )
   {
      //
      // Record an edge in the f-map from sourceNode to failedNode
      //
       //String srcName = srcCont.getBeanMetaData().getEjbName();
       //String dstName = dstCont.getBeanMetaData().getEjbName();
      // System.out.println("FAILURE MONITOR RECEIVED NOTIFICATION OF FAILURE BETWEEN: " + callee + " AND " + caller);
        int idx1 = 1 + callee.lastIndexOf(".");
        int idx2 = 1 + caller.lastIndexOf(".");
        if (idx1 <= 0) idx1 = 1;
        if (idx2 <= 0) idx2 = 1;
        fmap.addEdge(callee.substring(idx1), caller.substring(idx2));
   }    


   //=========================================================================
   // The ReportFailure method is called to notify the FailureMonitor that the
   // EJB housed in container 'failedNode' has failed "out of the blue"
   // without any apparent root cause.  This could be due to an injected fault
   // or due to a some problem in the bean itself.  Depending on our current
   // operating mode, this may are may not get reported to the Oracle.
   
   /**
    * @jmx:managed-operation
    */
   public void reportFailure ( Container failedNode )    {}    
    

   //=========================================================================
   // Start the FailureMonitor; right now, all this does is instantiate the
   // f-map.

   /**
    * @jmx:managed-operation
    */
   public void startService()
   {
      String filename = "/tmp/f-map.xml"; // FIXME: this is a temp hack
      fmap = new FMap();
      log.info("Creating failure propagation map...");

      // The f-map is maintained persistent across reboots by storing it in a
      // file /tmp/f-map.xml.  When the FailureMonitor service starts up, it
      // searches for this file; if it's not there, it infers that a new FI
      // experiment is being started; if there, file is parsed into the f-map.

      try 
      {
         fmap.loadFMap(new File(filename));
      }
      catch(IOException e)
      {
         log.warn("Didn't find file, starting a new map");
      }
   }


   /**
    * @jmx:managed-operation
    */
   public void loadNewFmap ( String filename )
       throws IOException
   {
       fmap = new FMap();
       fmap.loadFMap(new File(filename));
   }

   //=========================================================================
   // Produce a graphical representation of the current f-map.
   
   /**
    * @jmx:managed-operation
    */
   public void drawFMap()
   {
       fmap.draw();
   }

   
   //=========================================================================
   // Produce an ASCII version of the current f-map.

   /**
    * @jmx:managed-operation
    */
   public void printFMap()
   {
      fmap.print();
   }


   //=========================================================================
   // Save the current f-map to a file, in XML format.
   
   /**
    * @jmx:managed-operation
    */
   public void saveFMap( String filename )
   {
      fmap.saveFMapToXML(new File(filename));
   }


   /**
    * @jmx:managed-operation
    */
   public void stopService ()
   {
      saveFMap("/tmp/f-map.xml");
   }

}


