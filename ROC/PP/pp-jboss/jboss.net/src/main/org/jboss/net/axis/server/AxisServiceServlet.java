/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.net.axis.server;

import org.apache.axis.transport.http.AxisServlet;
import org.apache.axis.server.AxisServer;
import org.apache.axis.AxisEngine;
import org.apache.axis.MessageContext;

import java.util.StringTokenizer;
import java.util.ArrayList;

import java.io.File;

/**
 * An AxisServlet that is able to extract the corresponding AxisEngine 
 * from its installation context and builds the right message contexts for the
 * JBoss classloading and deployment architecture.
 *
 * @author <a href="mailto:Christoph.Jung@infor.de">Christoph G. Jung</a>
 * @created 7. September 2001, 19:17
 * @version $Revision: 1.1.1.1 $
 */

public class AxisServiceServlet extends AxisServlet {

   /** Creates new AxisServlet */
   public AxisServiceServlet() {
   }

   /** override AxisServlet.getEngine() in order to redirect to
    *  the corresponding AxisEngine.
    */
   public AxisServer getEngine() {
      if (getServletContext().getAttribute(Constants.AXIS_ENGINE_ATTRIBUTE) == null) {
         // we need to extract the engine from the 
         // rootcontext
         String installation = getServletContext().getRealPath("");
         if (installation.indexOf(File.separator) != -1)
            installation =
               installation.substring(installation.lastIndexOf(File.separator) + 1);
         // call the static service method to find the installed engine
         getServletContext().setAttribute(
            Constants.AXIS_ENGINE_ATTRIBUTE,
            AxisService.getAxisServer(installation));
      }

      return (AxisServer) getServletContext().getAttribute(Constants.AXIS_ENGINE_ATTRIBUTE);
   }

}