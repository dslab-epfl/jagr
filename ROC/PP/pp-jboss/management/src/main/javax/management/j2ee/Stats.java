package javax.management.j2ee;

/**
 * Represents specific performance data attributes for each
 * of the specific managed object types.
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
public interface Stats
{
   // Constants -----------------------------------------------------
   
   // Public --------------------------------------------------------
   
   /**
    * @return The list of names of attributes for the specific Stats submodel
    *         that this object supports. Attributes named in the list match
    *         the attributes that will return a Statistics object of the
    *         appropriate type.
    **/
   public String[] getStatisticNames();

   /**
    * @return The list of Statistics objects supported by this Stats object
    **/
   public Statistic[] getStatistics();

   /**
    * Delivers a Statistic by its given name
    *
    * @param pName Name of the Statistic to look up.
    *
    * @return A Statistic if the given name is found otherwise null
    **/
   public Statistic getStatistic( String pName );

   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Static inner classes -------------------------------------------------
}
