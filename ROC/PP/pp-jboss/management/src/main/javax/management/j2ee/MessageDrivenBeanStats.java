package javax.management.j2ee;

/**
 * Represents a deployed Connector (RAR) Module
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
/**
* Represents the statistics provide by a message driven
* bean
*
* @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
* @author <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>
**/
public interface MessageDrivenBeanStats
   extends EJBStats
{
   // Constants -----------------------------------------------------
   
   // Public --------------------------------------------------------
   
   /**
   * @return The number of messages received
   **/
   public CountStatistic getMessageCount();
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Static inner classes -------------------------------------------------
}
