package javax.management.j2ee;

import javax.management.ObjectName;

/**
 * Represents the statistics provided by a JCA connection
 *
 * @author <a href="mailto:marc@jboss.org">Marc Fleury</a>
 * @author <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>
 * @version $Revision: 1.1.1.1 $
 *   
 * <p><b>Revisions:</b>
 *
 * <p><b>200112009 Andreas Schaefer:</b>
 * <ul>
 * <li> Adjustment to the new JBoss guide lines and also adjustments
 *      to the latest JSR-77 specification
 * </ul>
 **/
public interface JCAConnectionStats
   extends Stats
{
   // Constants -----------------------------------------------------
   
   // Public --------------------------------------------------------
   
   /**
    * @return Object Name of the Connection Factory
    **/
   ObjectName getConnectionFactory();
   
   /**
    * @return object Name of the Managed Connection Factory
    **/
   Object getManagedConnectionFactory();
   
   /**
   * @return The time spent waiting for a conection to be available
   **/
   public TimeStatistic getWaitTime();
   
   /**
   * @return The time spent using a connection
   **/
   public TimeStatistic getUseTime();
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Static inner classes -------------------------------------------------
}
