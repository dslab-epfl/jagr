package javax.management.j2ee;

/**
 * Represents the statistics provided by a stateful session bean
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
public interface StatefulSessionBeanStats
   extends SessionBeanStats
{
   // Constants -----------------------------------------------------
   
   // Public --------------------------------------------------------
   
   /**
   * @return The number of beans that are in the passivated state
   **/
   public RangeStatistic getPassiveCount();
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Static inner classes -------------------------------------------------
}
