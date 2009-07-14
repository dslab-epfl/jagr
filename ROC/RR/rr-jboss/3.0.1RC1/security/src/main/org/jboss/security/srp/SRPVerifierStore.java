/*
 * JBoss, the OpenSource Web OS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.security.srp;

import java.io.IOException;
import java.io.Serializable;
import java.security.KeyException;

/** An interface describing the requirements of a password verifier store.
This is an abstraction that allows the <username, verifier, salt> information
needed by the server to be plugged in from various sources. E.g., LDAP
servers, databases, files, etc.

 @author Scott.Stark@jboss.org
 @version $Revision: 1.1.1.1 $
*/
public interface SRPVerifierStore
{
   public static class VerifierInfo implements Serializable
   {
      /** The username the information applies to. Perhaps redundant but it
       makes the object self contained.
       */
      public String username;
      /** The SRP password verifier hash */
      public byte[] verifier;
      /** The random password salt originally used to verify the password */
      public byte[] salt;
      /** The SRP algorithm primitive generator */
      public byte[] g;
      /** The algorithm safe-prime modulus */
      public byte[] N;
   }

    /** Get the indicated user's password verifier information.
     */
    public VerifierInfo getUserVerifier(String username)
      throws KeyException, IOException;
    /** Set the indicated users' password verifier information. This is equivalent
     to changing a user's password and should generally invalidate any existing
     SRP sessions and caches.
     */
    public void setUserVerifier(String username, VerifierInfo info)
      throws IOException;

   /** Verify an optional auxillary challenge sent from the client to the server. The
    * auxChallenge object will have been decrypted if it was sent encrypted from the
    * client. An example of a auxillary challenge would be the validation of a hardware
    * token (SafeWord, SecureID, iButton) that the server validates to further strengthen
    * the SRP password exchange.
    */
   public void verifyUserChallenge(String username, Object auxChallenge)
         throws SecurityException;
}
