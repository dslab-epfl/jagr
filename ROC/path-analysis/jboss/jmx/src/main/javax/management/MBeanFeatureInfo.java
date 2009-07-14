/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.management;

import java.io.Serializable;

/**
 * General information for MBean descriptor objects.
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 *
 * @version $Revision: 1.1.1.1 $
 */
public class MBeanFeatureInfo 
   implements Serializable
{

   // Attributes ----------------------------------------------------
   
   /**
    * Name of the MBean feature.
    */
   protected String name = null;
   
   /**
    * Human readable description string of the MBean feature.
    */
   protected String description = null;

   // Constructors --------------------------------------------------
   
   /**
    * Constructs an MBean feature info object.
    *
    * @param   name name of the MBean feature
    * @param   description human readable description string of the feature
    */
   public MBeanFeatureInfo(String name, String description)
   {
      this.name = name;
      this.description = description;
   }

   // Public --------------------------------------------------------
   
   /**
    * Returns the name of the MBean feature.
    *
    * @return  name string
    */
   public String getName()
   {
      return name;
   }

   /** 
    * Returns the description of the MBean feature.
    *
    * @return  a human readable description string
    */
   public String getDescription()
   {
      return description;
   }
   
}
