package javax.management.j2ee;

/**
 * Represents the statistics provided by a JTA resource
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
public interface JTAStats
   extends Stats
{
   // Constants -----------------------------------------------------
   
   // Public --------------------------------------------------------
   
   /**
   * @return The number of active transactions
   **/
   public CountStatistic getActiveCount();
   
   /**
   * @return The number of committed transactions
   **/
   public CountStatistic getCommitedCount();
   
   /**
   * @return The number of rolled-back transactions
   **/
   public CountStatistic getRolledbackCount();
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Static inner classes -------------------------------------------------
}
