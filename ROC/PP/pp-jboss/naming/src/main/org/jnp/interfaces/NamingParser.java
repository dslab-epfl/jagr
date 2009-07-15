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
 *   @author $Author: emrek $
 *   @author $Author: emrek $
 *   @version $Revision: 1.1.1.1 $
 */
public class NamingParser
   implements NameParser, java.io.Serializable
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
    
   // Static --------------------------------------------------------
   /** The unsynchronized syntax properties
    */
   static Properties syntax = new FastNamingProperties();

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
