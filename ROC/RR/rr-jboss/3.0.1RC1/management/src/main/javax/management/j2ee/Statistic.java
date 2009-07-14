package javax.management.j2ee;

/**
 * Base Model for a Statistic Information.
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
public interface Statistic
{
   // Constants -----------------------------------------------------
   
   // Public --------------------------------------------------------
   
   /**
   * @return Name of the Statistics
   **/
   public String getName();
   
   /**
   * @return Unit of Measurement. For TimeStatistics valid values are "HOUR",
   *         "MINUTE", "SECOND", "MILLISECOND", "MICROSECOND", "NANOSECOND"
   **/
   public String getUnit();
   
   /**
   * @return A human-readable description
   **/
   public String getDescription();
   
   /**
    * @return The time the first measurment was taken represented as a long, whose
    *         value is the number of milliseconds since January 1, 1970, 00:00:00.
    **/
   public long getStartTime();
   
   /**
    * @return The time the most recent measurment was taken represented as a long,
    *         whose value is the number of milliseconds since January 1, 1970, 00:00:00.
    **/
   public long getLastSampleTime();
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Static inner classes -------------------------------------------------
}
