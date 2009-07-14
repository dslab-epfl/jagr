/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ha.framework.server;

import java.util.Collection;

import javax.management.ObjectName;

import org.jboss.util.jmx.ObjectNameFactory;

import org.jboss.ha.framework.interfaces.HAPartition;

/** 
 *   Management Bean for Cluster HAPartitions.  It will start a JavaGroups
 *   channel and initialize the ReplicantManager and DistributedStateService.
 *
 *   @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>.
 *   @author <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>.
 *   @version $Revision: 1.1.1.1 $
 *
 * <p><b>Revisions:</b><br>
 */

public interface ClusterPartitionMBean
   extends org.jboss.system.ServiceMBean
{
   ObjectName OBJECT_NAME = ObjectNameFactory.create("jboss:service=ClusterPartition");

   /**
    * Name of the partition being built. All nodes/services belonging to 
    * a partition with the same name are clustered together.
    */
   String getPartitionName();
   void setPartitionName(String newName);

   /**
    * Get JavaGroups property string a la JDBC
    * see <a href="http://www.javagroups.com/">JavaGroups web site for more information</a>
    */
   String getPartitionProperties(); // i.e. JavaGroups properties
   void setPartitionProperties(String newProps);

   /**
    * Determine if deadlock detection is enabled
    */
   boolean getDeadlockDetection();
   void setDeadlockDetection(boolean doit);

   // Access to the underlying HAPartition without going through JNDI
   //  
   HAPartition getHAPartition ();

}
