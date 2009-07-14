/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ha.framework.interfaces;

/**
 * LoadBalancingPolicy implementation that always favor the first available target i.e.
 * no load balancing occurs. This does not mean that fail-over will not occur if the
 * first member in the list dies. In this case, fail-over will occur, and a new target
 * will become the first member and invocation will continously be invoked on the same
 * new target until its death.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>.
 * @author <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>.
 * @version $Revision: 1.1.1.1 $
 * @see LoadBalancePolicy
 *
 * <p><b>Revisions:</b><br>
 */

public class FirstAvailable implements LoadBalancePolicy
{
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
       
    // Public --------------------------------------------------------
   
   public void init (HARMIClient father)
   {
      // do not use the HARMIClient in this policy
   }
   
   public Object chooseTarget (java.util.List targets)
   {
      return targets.get(0);
   }

   // Z implementation ----------------------------------------------
   
   // Y overrides ---------------------------------------------------
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}
