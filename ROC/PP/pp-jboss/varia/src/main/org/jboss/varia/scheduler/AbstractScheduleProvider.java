/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.varia.scheduler;

import java.util.Date;

import javax.management.JMException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.jboss.system.ServiceMBeanSupport;

/**
 * Abstract Base Class for Schedule Provider. Any Schedule
 * Provider should extend from this class or must provide
 * the MBean Interface methods.
 *
 * @jmx:mbean name="jboss:service=ScheduleProvider"
 *            extends="org.jboss.system.ServiceMBean"
 *
 * @author <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>
 * @version $Revision: 1.1.1.1 $
 */
public abstract class AbstractScheduleProvider
      extends ServiceMBeanSupport
      implements AbstractScheduleProviderMBean
{

   // -------------------------------------------------------------------------
   // Constants
   // -------------------------------------------------------------------------

   // -------------------------------------------------------------------------
   // Members
   // -------------------------------------------------------------------------

   private ObjectName mScheduleManagerName;

   // -------------------------------------------------------------------------
   // Constructors
   // -------------------------------------------------------------------------

   /**
    * Default (no-args) Constructor
    **/
   public AbstractScheduleProvider()
   {
   }

   // -------------------------------------------------------------------------
   // SchedulerMBean Methods
   // -------------------------------------------------------------------------

   /**
    * Get the Schedule Manager Name
    *
    * @jmx:managed-operation
    */
   public String getScheduleManagerName()
   {
      return mScheduleManagerName.toString();
   }

   /**
    * Set the Schedule Manager Name
    *
    * @jmx:managed-operation
    */
   public void setScheduleManagerName(String pSchedulerManagerName)
         throws MalformedObjectNameException
   {
      mScheduleManagerName = new ObjectName(pSchedulerManagerName);
   }

   /**
    * Add the Schedules to the Schedule Manager.
    *
    * This is a callback method for the Schedule Manager
    * which indicates to the Provider that it can start adding
    * Schedules. Therefore this method should not be called
    * by a client directly.
    *
    * @jmx:managed-operation
    */
   public abstract void startProviding()
         throws Exception;

   /**
    * Stops the Provider from providing and
    * causing him to remove all Schedules
    *
    * This is a callback method for the Schedule Manager
    * which indicates to the Provider that it can start adding
    * Schedules. Therefore this method should not be called
    * by a client directly.
    *
    * @jmx:managed-operation
    */
   public abstract void stopProviding();

   /**
    * Add a single Schedule add the Schedule Manager. This is
    * a convenience method for the providers to add a single
    * schedule add the Schedule Manager.
    *
    * @param pTarget Object Name of the target MBean (receiver
    *                of the time notification)
    * @param pMethodName Name of the Method to be called on the
    *                    target
    * @param pMethodSignature Signature of the Method
    * @param pStart Date when the Schedule has to start
    * @param pPeriod Time between two notifications
    * @param pRepetitions Number of repetitions (-1 for unlimited)
    *
    * @return Identification of the Schedule which is used
    *         to remove it later
    **/
   protected int addSchedule(
         ObjectName pTarget,
         String pMethodName,
         String[] pMethodSignature,
         Date pStart,
         long pPeriod,
         int pRepetitions
         ) throws
         JMException
   {
//AS      log.info( "addScheduler(), start date: " + pStart + ", period: " + pPeriod + ", repetitions: " + pRepetitions );
      return ((Integer) server.invoke(
            mScheduleManagerName,
            "addSchedule",
            new Object[]{
               serviceName,
               pTarget,
               pMethodName,
               pMethodSignature,
               pStart,
               new Long(pPeriod),
               new Integer((int) pRepetitions)
            },
            new String[]{
               ObjectName.class.getName(),
               ObjectName.class.getName(),
               String.class.getName(),
               String[].class.getName(),
               Date.class.getName(),
               Long.TYPE.getName(),
               Integer.TYPE.getName()
            }
      )).intValue();
   }

   /**
    * Remove a Schedule from the Schedule Manager. This
    * is a convenience method for the providers to remove a
    * schedule at the Schedule Manager.
    *
    * @param pID Identification of the Schedule
    **/
   protected void removeSchedule(int pID)
         throws JMException
   {
      server.invoke(
            mScheduleManagerName,
            "removeSchedule",
            new Object[]{new Integer(pID)},
            new String[]{Integer.TYPE.getName()}
      );
   }

   // -------------------------------------------------------------------------
   // ServiceMBean - Methods
   // -------------------------------------------------------------------------

   /**
    * When the Service is started it will register itself at the
    * Schedule Manager which makes it necessary that the Schedule Manager
    * is already running.
    * This allows the Schedule Manager to call {@link #startProviding
    * startProviding()} which is the point to for the Provider to add
    * the Schedules on the Schedule Manager.
    * ATTENTION: If you overwrite this method in a subclass you have
    * to call this method (super.startService())
    **/
   protected void startService()
         throws Exception
   {
//AS      log.info( "startService(), call registerProvider(), service name: " + serviceName );
      server.invoke(
            mScheduleManagerName,
            "registerProvider",
            new Object[]{serviceName.toString()},
            new String[]{String.class.getName()}
      );
   }

   /**
    * When the Service is stopped it will unregister itself at the
    * Schedule Manager.
    * This allws the Schedule Manager to remove the Provider from its
    * list and then call {@link #stopProviding stopProviding()} which
    * is the point for the Provider to remove the Schedules from the
    * Schedule Manager.
    * ATTENTION: If you overwrite this method in a subclass you have
    * to call this method (super.stopService())
    **/
   protected void stopService()
   {
      try
      {
//AS         log.info( "stopService(), call unregisterProvider()" );
         server.invoke(
               mScheduleManagerName,
               "unregisterProvider",
               new Object[]{serviceName.toString()},
               new String[]{String.class.getName()}
         );
      }
      catch (JMException jme)
      {
         log.error("Could not unregister the Provider from the Schedule Manager", jme);
      }
   }
}
