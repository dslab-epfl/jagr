
/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 *
 */

package org.jboss.tm;

import java.net.InetAddress;
import javax.transaction.xa.Xid;
import java.net.UnknownHostException;


/**
 * XidFactory.java
 *
 *
 * Created: Sat Jun 15 19:01:18 2002
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version
 *
 * @jmx.mbean
 */

public class XidFactory implements XidFactoryMBean
{

   /**
    *  The default value of baseGlobalId is the host name of this host, 
    *  followed by a slash.
    *
    *  This is used for building globally unique transaction identifiers.
    *  It would be safer to use the IP address, but a host name is better
    *  for humans to read and will do for now.
    * This must be set individually if multiple jboss instances are 
    * running on the same machine.
    */
   private String baseGlobalId;

   /**
    *  The next transaction id to use on this host.
    */
   private long globalIdNumber = 0;

   /**
    * The variable <code>pad</code> says whether the byte[] should be their
    * maximum 64 byte length or the minimum.  
    * The max length is required for Oracle..
    *
    */
   private boolean pad = false;

   /**
    * The variable <code>noBranchQualifier</code> is the 0 or 64 byte zero array
    * used for initial xids.
    *
    */
   private byte[] noBranchQualifier = new byte[0];

   public XidFactory() 
   {
      try {
         baseGlobalId = InetAddress.getLocalHost().getHostName() + "/";
         // Ensure room for 14 digits of serial no.
         if (baseGlobalId.length() > Xid.MAXGTRIDSIZE - 15)
            baseGlobalId = baseGlobalId.substring(0, Xid.MAXGTRIDSIZE - 15);
         baseGlobalId = baseGlobalId + "/";
      } catch (UnknownHostException e) {
         baseGlobalId = "localhost/";
      }
   }

   
   
   /**
    * mbean get-set pair for field BaseGlobalId
    * Get the value of BaseGlobalId
    * @return value of BaseGlobalId
    *
    * @jmx:managed-attribute
    */
   public String getBaseGlobalId()
   {
      return baseGlobalId;
   }
   
   
   /**
    * Set the value of BaseGlobalId
    * @param BaseGlobalId  Value to assign to BaseGlobalId
    *
    * @jmx:managed-attribute
    */
   public void setBaseGlobalId(final String baseGlobalId)
   {
      this.baseGlobalId = baseGlobalId;
   }
   

   
   
   /**
    * mbean get-set pair for field globalIdNumber
    * Get the value of globalIdNumber
    * @return value of globalIdNumber
    *
    * @jmx:managed-attribute
    */
   public long getGlobalIdNumber()
   {
      return globalIdNumber;
   }
   
   
   /**
    * Set the value of globalIdNumber
    * @param globalIdNumber  Value to assign to globalIdNumber
    *
    * @jmx:managed-attribute
    */
   public void setGlobalIdNumber(final long globalIdNumber)
   {
      this.globalIdNumber = globalIdNumber;
   }
   
   
   
   
   /**
    * mbean get-set pair for field pad
    * Get the value of pad
    * @return value of pad
    *
    * @jmx:managed-attribute
    */
   public boolean isPad()
   {
      return pad;
   }
   
   
   /**
    * Set the value of pad
    * @param pad  Value to assign to pad
    *
    * @jmx:managed-attribute
    */
   public void setPad(boolean pad)
   {
      this.pad = pad;
      if (pad) 
      {
         noBranchQualifier = new byte[Xid.MAXBQUALSIZE];
      } // end of if ()
      else
      {
         noBranchQualifier = new byte[0];
      } // end of else
   }
   
   
   
   
   /**
    * mbean get-set pair for field instance
    * Get the value of instance
    * @return value of instance
    *
    * @jmx:managed-attribute
    */
   public XidFactoryMBean getInstance()
   {
      return this;
   }
   
   
   /**
    * Describe <code>newXid</code> method here.
    *
    * @return a <code>Xid</code> value
    * @jmx.managed-operation
    */
   public Xid newXid()
   {
      byte[] globalId = (baseGlobalId + Long.toString(getNextId())).getBytes();
      if (pad) 
      {
         byte[] result = new byte[Xid.MAXGTRIDSIZE];
         System.arraycopy(globalId, 0, result, 0, globalId.length);
         globalId = result;
      } // end of if ()
      return new XidImpl(globalId, noBranchQualifier);
   }

   /**
    * Describe <code>newBranch</code> method here.
    *
    * @param xid a <code>Xid</code> value
    * @param branchIdNum a <code>long</code> value
    * @return a <code>Xid</code> value
    * @jmx.managed-operation
    */
   public Xid newBranch(Xid xid, long branchIdNum)
   {
      byte[] branchId = Long.toString(branchIdNum).getBytes();
      if (pad) 
      {
         byte[] result = new byte[Xid.MAXBQUALSIZE];
         System.arraycopy(branchId, 0, result, 0, branchId.length);
         branchId = result;
      } // end of if ()
      return new XidImpl(xid, branchId);
   }

   /**
    * Describe <code>toString</code> method here.
    *
    * @param xid a <code>Xid</code> value
    * @return a <code>String</code> value
    * @jmx.managed-operation
    */
   public String toString(Xid xid)
   {
      if (xid instanceof XidImpl) 
      {
         return XidImpl.toString((XidImpl)xid);
      } // end of if ()
      else
      {
         return xid.toString();
      } // end of else
   }


   private synchronized long getNextId()
   {
      return globalIdNumber++;
   }

   


}// XidFactory
