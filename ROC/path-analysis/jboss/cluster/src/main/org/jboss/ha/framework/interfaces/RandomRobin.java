/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ha.framework.interfaces;

import java.util.List;
import java.util.Random;

/**
 * LoadBalancingPolicy implementation that always fully randomly select its target
 * (without basing its decision on any history).
 *
 * @author <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>.
 * @version $Revision: 1.1.1.1 $
 * @see org.jboss.ha.framework.interfaces.LoadBalancePolicy
 *
 * <p><b>Revisions:</b><br>
 * <p><b>2002/08/25: Sacha Labourey</b>
 * <ol>
 *   <li>First Implementation</li>
 * </ol>
 */

public class RandomRobin implements LoadBalancePolicy, java.io.Externalizable
{
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   
   protected transient Random localRandomizer = new Random (System.currentTimeMillis ());
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
       
    // Public --------------------------------------------------------
   
   // LoadBalancePolicy implementation ----------------------------------------------

   public void init (HARMIClient father)
   {
      // do not use the HARMIClient in this policy
   }
   
   public Object chooseTarget (List targets)
   {
      int max = targets.size();

      if (max == 0)
         return null;

      int cursor = localRandomizer.nextInt (max);
      return targets.get(cursor);
   }

   // Externalizable implementation ----------------------------------------------
   
   public void writeExternal(final java.io.ObjectOutput out)
   throws java.io.IOException
   {       
      // Nothing to serialize
   }

   public void readExternal(final java.io.ObjectInput in)
   throws java.io.IOException, ClassNotFoundException
   {
      // nothing to retrieve: we just implement Externalizable to correctly 
      // initialize our object
      //
      localRandomizer = new Random (System.currentTimeMillis ());      
   }
   // Y overrides ---------------------------------------------------
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}
