/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss;

import java.net.Authenticator;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.PasswordAuthentication;
import java.net.URLConnection;

/**
 * Provides an OS-independent way of shutting down JBoss.  This
 * works by accessing the JMX server and giving it the shutdown
 * command.  The host to the JMX server can be passed in as well
 * as the port number.  If neither is supplied, the defaults of
 * <tt>localhost</tt> and <tt>8080</tt> are used.
 *
 * <h3>Usage:</h3>
 * <pre>
 * java org.jboss.Shutdown [host [port]] [-u username] [-p password] [-c shutdown_command]
 * </pre>
 *
 * @author <a href="mailto:dewayne@dmsoft.com">Dewayne McNair</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author Scott.Stark@jboss.org
 * @version $Revision: 1.1.1.1 $
 */
public class Shutdown
{
   private static final String COMMAND =
      "/jmx-console/HtmlAdaptor?action=invokeOpByName&name=jboss.system%3Atype%3DServer&methodName=shutdown";

   /**
    * Parse the command line and shutdown the remote server.
    *
    * @param  args       Command line arguments.
    * @throws Exception  Invalid port number.
    */
   public static void main(final String args[]) throws Exception
   {
      String host = "localhost";
      String username = null;
      String password = null;
      String command = COMMAND;
      int port = 8080;

      if( args.length > 0 )
      {
         if( args[0].startsWith("-h") || args[0].startsWith("-?") )
         {
            System.out.println("Usage: Shutdown [host [port]] [-u username] "
                  + "[-p password] [-c shutdown_command]");
            System.exit(0);
         }
         for(int a = 0; a < args.length; a ++)
         {
            if( args[a].startsWith("-u"))
               username = args[++ a];
            else if( args[a].startsWith("-p"))
               password = args[++ a];
            else if( args[a].startsWith("-c"))
               command = URLEncoder.encode(args[++ a]);
            else if( a == 0 )
               host = args[0];
            else if( a == 1 )
               port = Integer.parseInt(args[1]);
         }
      }

      shutdown(host, port, username, password, command);
   }

   /**
    * Connect to the JBoss servers HTML JMX adapter and invoke the
    * shutdown service.
    *
    * @param host The hostname of the JMX server.
    * @param port The port of the JMX server.
    *
    */
   public static void shutdown(final String host, final int port,
      final String username, final String password, final String command)
   {
      try
      {
         System.out.println("Shutting down server "+host+":"+port);
         Authenticator.setDefault(new PasswordAuthenticator(username, password));
         URL url = new URL("http", host, port, command);
         System.out.println(url);
         URLConnection conn = url.openConnection();
         conn.getContent();
      }
      catch(ProtocolException e)
      {
         e.printStackTrace();
      }
      catch(Exception ignore)
      {
         // Since the web container is shuting down an error is thrown
      }
      System.out.println("Shutdown complete");
   }

   static class PasswordAuthenticator extends Authenticator
   {
      private String username;
      private char[] password;

      PasswordAuthenticator(String username, String password)
      {
         this.username = username;
         this.password = password.toCharArray();
      }
      protected PasswordAuthentication getPasswordAuthentication()
      {
         PasswordAuthentication auth = new PasswordAuthentication(username, password);
         return auth;
      }
   }
}
