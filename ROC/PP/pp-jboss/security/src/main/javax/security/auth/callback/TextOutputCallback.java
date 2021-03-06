/*
 * JBoss, the OpenSource WebOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.security.auth.callback;

import java.io.Serializable;

/** The JAAS 1.0 classes for use of the JAAS authentication classes with
 * JDK 1.3. Use JDK 1.4+ to use the JAAS authorization classes provided by
 * the version of JAAS bundled with JDK 1.4+.
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 1.1.1.1 $
 */
public class TextOutputCallback implements Callback, Serializable
{
   public static final int INFORMATION = 0;
   public static final int WARNING = 1;
   public static final int ERROR = 2;

   private int messageType;
   private String message;

   public TextOutputCallback(int messageType, String message)
   {
      this.messageType = messageType;
      this.message = message;
   }

   public int getMessageType()
   {
      return messageType;
   }
   public String getMessage()
   {
      return message;
   }
}
