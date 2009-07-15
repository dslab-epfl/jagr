package org.jboss.test.jrmp.ejb;

import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.rmi.server.RMIServerSocketFactory;

import org.apache.log4j.Category;

/** The CompressionServerSocketFactory from the RMI custom socket
factory tutorial.

@author Scott.Stark@jboss.org
@version $Revision: 1.1.1.1 $
*/
public class CompressionServerSocketFactory implements RMIServerSocketFactory, Serializable
{
   static Category log = Category.getInstance(CompressionServerSocketFactory.class);

   /**
   * Create a server socket on the specified port (port 0 indicates
   * an anonymous port).
   * @param  port the port number
   * @return the server socket on the specified port
   * @exception IOException if an I/O error occurs during server socket
   * creation
   * @since 1.2
   */
   public ServerSocket createServerSocket(int port) throws IOException
   {
      ServerSocket activeSocket = new CompressionServerSocket(port);
      log.debug("createServerSocket(), port="+port);
      return activeSocket;
   }

   public boolean equals(Object obj)
   {
      boolean equals = obj instanceof CompressionServerSocketFactory;
      log.debug("equals(obj="+obj+") is: "+equals);
      return equals;
   }

   public int hashCode()
   {
      return getClass().getName().hashCode();
   }
}
