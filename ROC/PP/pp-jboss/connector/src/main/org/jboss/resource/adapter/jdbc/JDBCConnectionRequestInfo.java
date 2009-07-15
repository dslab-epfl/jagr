/*
 * Licensed under the X license (see http://www.x.org/terms.htm)
 */
package org.jboss.resource.adapter.jdbc;

import javax.resource.spi.ConnectionRequestInfo;

/**
 * JDBC parameters for creating connections.
 *
 * @author    Aaron Mulder <ammulder@alumni.princeton.edu>
 * @version   $Revision: 1.1.1.1 $
 */
public class JDBCConnectionRequestInfo implements ConnectionRequestInfo
{
   /**
    * Description of the Field
    */
   public String user;
   /**
    * Description of the Field
    */
   public String password;

   /**
    * Constructor for the JDBCConnectionRequestInfo object
    */
   public JDBCConnectionRequestInfo()
   {
   }

   /**
    * Constructor for the JDBCConnectionRequestInfo object
    *
    * @param user      Description of Parameter
    * @param password  Description of Parameter
    */
   public JDBCConnectionRequestInfo(String user, String password)
   {
      this.user = user;
      this.password = password;
   }
}
