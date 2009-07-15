package javax.management.j2ee;

/**
 * Represents a standard measurements of the upper and lower
 * limits of the value of an attribute
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
public interface BoundaryStatistic
   extends Statistic
{
   // Constants -----------------------------------------------------
   
   // Public --------------------------------------------------------
   
   /**
   * @return The lower limit of the value of this attribute
   **/
   public long getLowerBound();

   /**
   * @return The upper limit of the value of this attribute
   **/
   public long getUpperBound();

   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Static inner classes -------------------------------------------------
}
