package javax.management.j2ee;

/**
 * Represents a standard measurements of the lowest and highest
 * value an attribute has held as well as its current value
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
public interface RangeStatistic
   extends Statistic
{
   // Constants -----------------------------------------------------
   
   // Public --------------------------------------------------------
   
   /**
   * @return The lowest value this attribute has held since the beginning of
   *         the measurements
   **/
   public long getLowWaterMark();
   
   /**
   * @return The highest value this attribute has held since the beginning of
   *         the measurements
   **/
   public long getHighWaterMark();
   
   /**
   * @return The current value of the attribute
   **/
   public long getCurrent();
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Static inner classes -------------------------------------------------
}
