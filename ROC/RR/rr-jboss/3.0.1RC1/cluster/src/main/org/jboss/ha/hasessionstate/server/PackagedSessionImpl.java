/*
 * JBoss, the OpenSource J2EE WebOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ha.hasessionstate.server;

import org.jboss.ha.hasessionstate.interfaces.PackagedSession;
import java.io.Serializable;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;

/**
 *   Default implementation of PackagedSession
 *
 *   @see PackagedSession, HASessionStateImpl
 *   @author sacha.labourey@cogito-info.ch
 *   @version $Revision: 1.1.1.1 $
 *
 * <p><b>Revisions:</b><br>
 */

public class PackagedSessionImpl implements PackagedSession
{
   
   protected byte[] state = null;
   protected long versionId = 0;
   protected String owner = null;
   protected Serializable key = null;
   protected transient long lastModificationTimeInVM = System.currentTimeMillis ();
   
   public PackagedSessionImpl () { }
   
   public PackagedSessionImpl (Serializable key, byte[] state, String owner)
   {
      this();
      this.key = key;
      this.setState (state);
      this.owner = owner;
   }
   
   public byte[] getState ()
   {
      return this.state;
   }
   
   public boolean setState (byte[] state)
   {
      this.lastModificationTimeInVM = System.currentTimeMillis ();
      if (isStateIdentical (state))
         return true;
      else
      {
         this.state = state;
         this.versionId++;
         return false;
      }
   }
   
   public boolean isStateIdentical (byte[] state)
   {
      return java.util.Arrays.equals (state, this.state);
   }
   
   public void update (PackagedSession clone)
   {
      this.state = (byte[])clone.getState().clone();
      this.versionId = clone.getVersion ();
      this.owner = clone.getOwner ();      
   }
   
   public String getOwner ()
   { return this.owner; }
   public void setOwner (String owner)
   { this.owner = owner; }
   
   public long getVersion ()
   { return this.versionId; }
   
   public Serializable getKey ()
   { return this.key; }
   public void setKey (Serializable key)
   { this.key = key; }

   public long unmodifiedExistenceInVM ()
   {
      return this.lastModificationTimeInVM;
   }
}
