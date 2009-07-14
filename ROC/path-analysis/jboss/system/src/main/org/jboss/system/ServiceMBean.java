/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.system;

/**
 * An interface describing a JBoss service MBean.
 *
 * @see Service
 * @see ServiceMBeanSupport
 * 
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:Scott_Stark@displayscape.com">Scott Stark</a>
 * @author <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>
 * @version $Revision: 1.1.1.1 $
 *   
 * <p><b>Revisions:</b>
 *
 * <p><b>20011202 Andreas Schaefer:</b>
 * <ul>
 * <li> Added new state FAILED which the Service goes to when starting
 *      or stopping fails.
 * </ul>
 */
public interface ServiceMBean
   extends Service
{
   // Constants -----------------------------------------------------
   
   String[] states = {
      "Stopped", "Stopping", "Starting", "Started", "Failed", "Destroyed"
   };

   int STOPPED  = 0;
   int STOPPING = 1;
   int STARTING = 2;
   int STARTED  = 3;
   int FAILED  = 4;
   int DESTROYED = 5;

   // Public --------------------------------------------------------
   
   String getName();
   int getState();
   String getStateString();
}
