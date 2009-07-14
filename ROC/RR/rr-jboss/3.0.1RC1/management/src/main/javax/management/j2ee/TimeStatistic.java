package javax.management.j2ee;

/**
 * Represents a standard Time Measurements
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
public interface TimeStatistic
   extends Statistic
{
   // Constants -----------------------------------------------------
   
   public static final String HOUR = "HOUR";
   public static final String MINUTE = "MINUTE";
   public static final String SECOND = "SECOND";
   public static final String MILLISECOND = "MILLISECOND";
   public static final String MICROSECOND = "MICROSECOND";
   public static final String NANOSECOND = "NANOSECOND";
   
   // Public --------------------------------------------------------
   
   /**
   * @return The number of times a time measurements was added
   **/
   public long getCount();
   
   /**
   * @return The minimum time added since start of the measurements
   **/
   public long getMinTime();
   
   /**
   * @return The maximum time added since start of the measurements
   **/
   public long getMaxTime();
   
   /**
   * @return The sum of all the time added to the measurements since
   *         it started
   **/
   public long getTotalTime();
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Static inner classes -------------------------------------------------
}
