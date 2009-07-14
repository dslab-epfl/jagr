/*
 * JBoss, the OpenSource J2EE WebOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ha.framework.server;

import java.util.ArrayList;
import java.util.List;
import org.jboss.ha.framework.interfaces.DistributedReplicantManager;
import org.jboss.ha.framework.interfaces.DistributedReplicantManager.ReplicantListener;
import org.jboss.ha.framework.interfaces.HAPartition;
import java.io.Serializable;

/**
 * This class is a holder and manager of replicants.
 * It manages lists of replicated objects and changes the list as the HAPartition
 * notifies it.
 *
 *   @author bill@burkecentral.com
 *   @version $Revision: 1.1.1.1 $
 */
public class HATarget implements ReplicantListener
{
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   
   protected String replicantName;
   protected ArrayList replicants = new ArrayList();
   protected HAPartition partition = null;
   protected org.jboss.logging.Logger log;
   protected int clusterViewId = 0;
   protected Serializable target;
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   public HATarget(HAPartition partition,
      String replicantName,
      Serializable target)
      throws Exception
   {
      this.replicantName = replicantName;
      this.log = org.jboss.logging.Logger.getLogger(this.getClass());
      this.target = target;
      updateHAPartition(partition);
   }
   
   // Public --------------------------------------------------------

   public String toString()
   {
      StringBuffer buffer = new StringBuffer(super.toString());
      buffer.append('{');
      buffer.append("replicantName="+replicantName);
      buffer.append("partition="+partition.getPartitionName());
      buffer.append("clusterViewId="+clusterViewId);
      buffer.append("replicants="+replicants);
      buffer.append('}');
      return buffer.toString();
   }

   public long getCurrentViewId()
   {
      return (long)clusterViewId;
   }

   public void destroy()
   {
      try
      {
         this.cleanExistenceInCurrentHAPartition();
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }
   
   public ArrayList getReplicants()
   {
      return replicants;
   }
   
   public void updateHAPartition(HAPartition partition) throws Exception
   {
      cleanExistenceInCurrentHAPartition();
      
      this.partition = partition;
      DistributedReplicantManager drm = partition.getDistributedReplicantManager();
      drm.registerListener(this.replicantName, this);
      drm.add(this.replicantName, this.target);
   }
   
   // DistributedReplicantManager.ReplicantListener implementation ------------
   
   public void replicantsChanged(String key, List newReplicants, int newReplicantsViewId)
   {
      if (log.isDebugEnabled())
      {
         log.debug("replicantsChanged '" + replicantName + "' to " + newReplicants.size()
            + " (intra-view id: " + newReplicantsViewId + ")");
      }

      synchronized(replicants)
      {
         // client has reference to replicants so it will automatically get
         // updated
         replicants.clear();
         replicants.addAll(newReplicants);
      }
      this.clusterViewId = newReplicantsViewId;
   }
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   protected void cleanExistenceInCurrentHAPartition()
   {
      if (this.partition != null)
      {
         try
         {
            DistributedReplicantManager drm = partition.getDistributedReplicantManager();
            drm.unregisterListener(this.replicantName, this);
            drm.remove(this.replicantName);
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
      }
   }
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
   
}
