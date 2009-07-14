/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.jdbc;

import java.io.File;
import java.io.IOException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import javax.management.*;

import org.jboss.system.ServiceMBeanSupport;
import org.jboss.system.server.ServerConfigLocator;

import org.jboss.logging.Logger;

// our patched HSQLDB Server class
import org.hsqldb.Embedded_Server;
import org.hsqldb.util.Embedded_DatabaseManager;

/**
 * Integration with <a href="http://sourceforge.net/projects/hsqldb">Hypersonic SQL</a> (c).
 * 
 * <p>Starts a "patched" HSQLDB 1.61 Hypersonic database in-VM.
 * 
 * @jmx:mbean name="jboss:service=Hypersonic"
 *            extends="org.jboss.system.ServiceMBean"
 * 
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:Scott_Stark@displayscape.com">Scott Stark</a>.
 * @author <a href="mailto:pf@iprobot.com">Peter Fagerlund</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @version $Revision: 1.1.1.1 $
 */
public class HypersonicDatabase 
   extends ServiceMBeanSupport 
   implements HypersonicDatabaseMBean, MBeanRegistration 
{
   /** HSQLDB patched server class. */
   org.hsqldb.Embedded_Server embeddedDBServer;

   /** Full path to db/hypersonic. */
   File dbPath;

   /** Database name will be appended to <em>jbossHome/db/hypersonic</em>. */
   String name = "default";

   /** Default port. */
   int port = 1476;

   /** Default silent. */
   boolean silent = true;

   /** Default trace. */
   boolean trace = false;

   public HypersonicDatabase() {
      // empty
   }

   /**
    * @jmx:managed-attribute
    */
   public void setDatabase(final String name) {
      this.name = name;
   }

   /**
    * @jmx:managed-attribute
    */
   public String getDatabase() {
      return name;
   }

   /**
    * @jmx:managed-attribute
    */
   public void setPort(final int port) {
      this.port = port;
   }

   /**
    * @jmx:managed-attribute
    */
   public int getPort() {
      return port;
   }

   /**
    * @jmx:managed-attribute
    */
   public void setSilent(final boolean silent) {
      this.silent = silent;
   }

   /**
    * @jmx:managed-attribute
    */
   public boolean getSilent() {
      return silent;
   }

   /**
    * @jmx:managed-attribute
    */
   public void setTrace(final boolean trace) {
      this.trace = trace;
   }

   /**
    * @jmx:managed-attribute
    */
   public boolean getTrace() {
      return trace;
   }

   /**
    * @jmx:managed-attribute
    */
   public String getDatabasePath() {
      return dbPath.toString();
   }

   protected ObjectName getObjectName(MBeanServer server, ObjectName name)
      throws MalformedObjectNameException 
   {
      return name == null ? OBJECT_NAME : name;
   }

   /** 
    * start of DatabaseManager accesible from the localhost:8082
    *
    * @jmx:managed-operation
    */
   public void startDatabaseManager() {
      // Start DBManager in new thread
      new Thread() {
	 public void run() {
	    try {
	       String[] args = { Integer.toString(port) };
	       org.hsqldb.util.Embedded_DatabaseManager.main(args);
	    } 
	    catch (Exception e) { 
	       log.error("Failed to start database manager", e);
	    }
	 }
      }.start();
   }

   protected void startService() throws Exception {
      // Get the server data directory
      File dataDir = ServerConfigLocator.locate().getServerDataDir();

      // Get DB directory
      dbPath = new File(dataDir, "hypersonic");
      if (!dbPath.exists()) {
         dbPath.mkdirs();
      }
      if (!dbPath.isDirectory()) {
         throw new IOException("Failed to create directory: " + dbPath);
      }
      
      final File prefix = new File(dbPath, name);
      
      // Start DB in new thread, or else it will block us
      new Thread("hypersonic-" + name) {
	 public void run() {
	    try {
	       // Create startup arguments
	       String[] args = new String[] {
		  "-database", prefix.toString(),
		  "-port",     String.valueOf(port), 
		  "-silent",   String.valueOf(silent), 
		  "-trace",    String.valueOf(trace),
	       };
	       
	       // Start server
	       embeddedDBServer.main(args);
	    } 
	    catch (Exception e) { 
	       log.error("Failed to start database", e);
	    }
	 }
      }.start();
   }

   /**
    * We now close the connection clean by calling the
    * serverSocket throught jdbc. The MBeanServer calls this 
    * method at closing time ... this gives the db
    * a chance to write out its memory cashe ...
    *
    * @author Peter Fagerlund pf@iprobot.com 
    */
   protected void stopService() throws Exception {
      Connection connection;
      Statement statement;
      String cmd = "SHUTDOWN";
      String jdbcDriver = "org.hsqldb.jdbcDriver";
      String dbStrVersion_1_6 = "jdbc:hsqldb:hsql://localhost:" + port;
      String user = "sa";
      String password = "";

      try {
	 new org.hsqldb.jdbcDriver();
	 Class.forName(jdbcDriver).newInstance();
	 connection = DriverManager.getConnection(dbStrVersion_1_6, 
						  user, 
						  password);
	 statement = connection.createStatement();
	 statement.executeQuery(cmd);
	 log.info("Database closed clean");
      }
      finally {
         embeddedDBServer = null;
      }
   }
}
