/*
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 *
 * Copyright 1999 by dreamBean Software,
 * All rights reserved.
 */
package org.jnp.interfaces;

import javax.naming.NameParser;
import javax.naming.Name;
import javax.naming.CompoundName;
import javax.naming.NamingException;
import java.util.Properties;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author $Author: mikechen $
 *   @version $Revision: 1.1.1.1 $
 */
public class NamingParser
   implements NameParser, java.io.Serializable
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
    
   // Static --------------------------------------------------------
   static Properties syntax = new Properties();
   static 
   {
       syntax.put("jndi.syntax.direction", "left_to_right");
       syntax.put("jndi.syntax.ignorecase", "false");
       syntax.put("jndi.syntax.separator", "/");
   }
   
   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------

   // NameParser implementation -------------------------------------
   public Name parse(String name) 
   	throws NamingException 
   {
   	return new CompoundName(name, syntax);
   }

   // Y overrides ---------------------------------------------------

   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
    
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}