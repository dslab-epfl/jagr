/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ha.framework.interfaces;

import java.io.IOException;
import java.util.List;
import java.util.Random;

/**
 * LoadBalancingPolicy implementation that always fully randomly select its target
 * (without basing its decision on any history).
 *
 * @author <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>.
 * @version $Revision: 1.1.1.1 $
 * @see org.jboss.ha.framework.interfaces.LoadBalancePolicy
 */
public class RandomRobin implements LoadBalancePolicy, java.io.Externalizable
{
   // Constants -----------------------------------------------------
   private static final long serialVersionUID = 5738105304997653939L;
   /** This needs to be a class variable or else you end up with multiple
    * Random numbers with the same seed when many clients lookup a proxy.
    */
   public static final Random localRandomizer = new Random (System.currentTimeMillis ());

   // Attributes ----------------------------------------------------

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
      throws IOException
   {       
      // Nothing to serialize
   }

   public void readExternal(final java.io.ObjectInput in)
      throws IOException, ClassNotFoundException
   {
      // nothing to retrieve: we just implement Externalizable to correctly
   }
   // Y overrides ---------------------------------------------------
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}
