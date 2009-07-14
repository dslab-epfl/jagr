/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ha.framework.interfaces;

/**
 * LoadBalancingPolicy implementation that always favor the next available target i.e.
 * load balancing always occurs.
 *
 * @author <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>.
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>.
 * @version $Revision: 1.2 $
 * @see LoadBalancePolicy
 *
 * <p><b>Revisions:</b><br>
 */

import java.util.Random;


public class RoundRobin implements LoadBalancePolicy
{
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   
   /**
    * Index in the list of possible targets on which the previous call has been
    * performed.
    */   
    protected transient int cursorRemote = -1; // ROC PINPOINT EMK: changed so that we can choose a random first target node.
    // protected transient int cursorRemote = 0;

   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
       
    // Public --------------------------------------------------------
   
   public void init (HARMIClient father)
   {
      // do not use the HARMIClient in this policy
   }
   
   public Object chooseTarget(java.util.List targets)
   {
       // BEGIN ROC PINPOINT EMK  ADDED SO THAT RoundRobin works for 
       //      
       if( cursorRemote == - 1 ) {
	   cursorRemote = new Random().nextInt( targets.size() );
       }
       // END ROC PINPOINT EMK 
      cursorRemote = ( (cursorRemote + 1) % targets.size() );
      return targets.get(cursorRemote);
   }

   // Z implementation ----------------------------------------------
   
   // Y overrides ---------------------------------------------------
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}
