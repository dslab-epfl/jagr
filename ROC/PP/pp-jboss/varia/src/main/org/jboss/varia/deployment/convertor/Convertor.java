/*
* JBoss, the OpenSource EJB server
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.varia.deployment.convertor;

import org.jboss.deployment.DeploymentInfo;

import java.io.File;

/**
 * Defines the methods of a Converter
 *
 * @author <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>
 * @version $Revision: 1.1.1.1 $
 */
public interface Convertor
{
   // Public --------------------------------------------------------
   /**
    * Checks if the a deployment unit can be converted to a JBoss deployment
    * unit by this converter.
    *
    * @param di The deployment info to be converted
    * @param path Path of the extracted deployment
    *
    * @return True if this converter is able to convert
    */
   public boolean accepts( DeploymentInfo di, File path );

   /**
    * Converts the necessary files to make the given deployment deployable
    * into the JBoss
    *
    * @param di Deployment info to be converted
    * @param path Path of the extracted deployment
    **/
   public void convert( DeploymentInfo di, File path )
      throws Exception;
}
