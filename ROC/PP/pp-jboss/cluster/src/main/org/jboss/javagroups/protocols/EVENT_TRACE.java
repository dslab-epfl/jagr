/*
 * JBoss, the OpenSource J2EE WebOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.javagroups.protocols;

import org.javagroups.stack.Protocol;
import org.javagroups.Event;
import org.javagroups.Message;
import org.jboss.logging.Logger;

import java.util.Properties;

/** A trival implementation of Protocol that traces all activity through
 * it to its logger. This should be inserted between any two protocols
 * you which to view the events between. Its supports a name property that
 * allows you to insert the element multiple times in a stack to trace
 * multiple protocols. An example config for the ClusterPartition for such
 * a usage is:
 <pre>
   <mbean code="org.jboss.ha.framework.server.ClusterPartition"
         name="jboss:service=JNDITestPartition">
    <!-- -->
    <attribute name="PartitionName">JNDITestPartition</attribute>
    <attribute name="PartitionConfig">
        <Config>
           <TCP start_port="50001" bind_addr="172.17.66.55" />
           <org.jboss.javagroups.protocols.EVENT_TRACE name="TCP-TCPPING-TRACE"
               up_thread="false" down_thread="false" />
           <TCPPING initial_hosts="lamia[50001]"
               port_range="1" timeout="15000"
                up_thread="false" down_thread="false" />
           <MERGE2 min_interval="5000" max_interval="20000" />
           <FD max_tries="4" timeout="15000" />
           <VERIFY_SUSPECT timeout="15000" />
           <pbcast.STABLE desired_avg_gossip="20000" />
           <pbcast.NAKACK gc_lag="50" retransmit_timeout="600,1200,2400,4800" />

           <org.jboss.javagroups.protocols.EVENT_TRACE name="NAKACK-GMS-TRACE"
            up_thread="false" down_thread="false" />
           <pbcast.GMS join_timeout="15000" join_retry_timeout="5000"
              shun="false" print_local_addr="true" />
           <pbcast.STATE_TRANSFER />
        </Config>
     </attribute>
  </mbean>
 </pre>
 * @author Scott.Stark@jboss.org
 * @vesion $Revision: 1.1.1.1 $
 */
public class EVENT_TRACE extends Protocol
{
   private String name = "EVENT_TRACE";
   private Logger log;

   public String getName()
   {
      return name;
   }

   /**
    * @param props
    * @return
    */
   public boolean setProperties(Properties props)
   {
      super.setProperties(props);
      name = props.getProperty("name", name);
      log = Logger.getLogger("org.jboss.javagroups."+name);
      return true;
   }

   public void up(Event event)
   {
      if( log.isTraceEnabled() )
      {
         log.trace("up, event="+event);
         if( event.getType() == Event.MSG )
         {
            Message msg = (Message) event.getArg();
            log.trace("MsgHeader: "+msg.printObjectHeaders());
         }
      }
      // Pass up the protocol stack
      passUp(event);
   }

   public void down(Event event)
   {
      if( log.isTraceEnabled() )
      {
         log.trace("down, event="+event);
         if( event.getType() == Event.MSG )
         {
            Message msg = (Message) event.getArg();
            log.trace("MsgHeader: "+msg.printObjectHeaders());
         }
      }
      // Pass down the protocol stack
      passDown(event);
   }
}

