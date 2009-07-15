/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

// $Id: XMLResourceProvider.java,v 1.1.1.1 2003/03/07 08:26:04 emrek Exp $

package org.jboss.net.axis;

import org.apache.axis.AxisEngine;
import org.apache.axis.ConfigurationException;
import org.apache.axis.configuration.FileProvider;
import org.apache.axis.utils.XMLUtils;

import java.net.URL;
import java.io.InputStream;
import java.io.IOException;

/**
 * A <code>FileProvider</code> that sits on a given URL and
 * that hosts classloader-aware deployment information.
 * <br>
 * <h3>Change History</h3>
 * <ul>
 * <li> jung, 09.03.2002: Adopted to axis alpha 3. Mapped to 
 * FileProvider. Created own WsddDocument. </li>
 * </ul>
 * @author <a href="mailto:Christoph.Jung@infor.de">Christoph G. Jung</a>
 * @created 28. September 2001
 * @version $Revision: 1.1.1.1 $
 */

public class XMLResourceProvider extends FileProvider {

   //
   // Attributes
   //

   /** the original resource that we host */
   final protected URL resource;

   //
   // Constructors
   //

   /**
    * construct a new XmlResourceProvider
    * @param resource url pointing to the deployment descriptor
    */
   public XMLResourceProvider(URL resource) {
      super((InputStream) null);
      this.resource = resource;
   }

   //
   // Public API
   //

	/** configures the given AxisEngine with the given descriptor */
   public void configureEngine(AxisEngine engine) throws ConfigurationException {
      try {
         if (myInputStream == null) {
            myInputStream = resource.openStream();
         }

		 deployment=new Deployment(XMLUtils.newDocument(myInputStream).
		 	getDocumentElement());
		 			 
         deployment.configureEngine(engine);
         engine.refreshGlobalOptions();

         myInputStream = null;
      } catch (Exception e) {
         throw new ConfigurationException(e);
      }
   }

	/** returns out special deployment */
	public Deployment getMyDeployment() {
	   return (Deployment) deployment;
	}
	
   /** not supported, yet. Should we use http-push or what? */
   public void writeEngineConfig(AxisEngine engine) {
      // NOOP
   }

}