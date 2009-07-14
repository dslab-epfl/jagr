/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.mx.interceptor;

import java.util.HashMap;
import java.util.Date;

import javax.management.Descriptor;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.Attribute;
import javax.management.modelmbean.ModelMBeanInfo;
import javax.management.modelmbean.ModelMBeanInfoSupport;

import org.jboss.mx.modelmbean.ModelMBeanConstants;
import org.jboss.mx.modelmbean.ModelBase;    // should be renamed ModelMBeanInvoker
import org.jboss.mx.server.MBeanInvoker;

/**
 * Quick hack, non mapping/caching attribute interceptor
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @version $Revision: 1.1.1.1 $
 *   
 */
public class MBeanAttributeInterceptor
      extends Interceptor
      implements ModelMBeanConstants
{

   // Attributes ----------------------------------------------------
   private ModelMBeanInfo info  = null;
   private MBeanInvoker invoker = null;
   
   // Constructors --------------------------------------------------
   public MBeanAttributeInterceptor(ModelMBeanInfo info, MBeanInvoker invoker)
   {
      super("MBean Attribute Interceptor");
      this.info = info;
      this.invoker = invoker;
   }

   // Public --------------------------------------------------------
   public Object invoke(Invocation invocation) throws InvocationException
   {
      if (invocation.getInvocationType() == Invocation.OPERATION)
         return getNext().invoke(invocation);

      try
      {
         // name of the attribute
         String attrName = invocation.getName();

         // this is mighty slow as we copy all over, will do for now
         Descriptor d = info.getDescriptor(attrName, ATTRIBUTE_DESCRIPTOR);
                  
         // SetAttribute         
         if (invocation.getImpact() == Invocation.WRITE)
         {
            Object value    = invocation.getArgs() [0];
            Object oldValue = d.getFieldValue(VALUE);
            
            String setMethod = (String)d.getFieldValue(SET_METHOD);
            
            // if setMethod is available, invoke it
            if (setMethod != null)
            {               
               invoker.invoke(setMethod, new Object[] { value }, invocation.getSignature());
            }

            // if caching is not disabled (currencyTimeLimit != 0) update descriptors
            String timeLimit = (String)d.getFieldValue(CURRENCY_TIME_LIMIT);
            long limit = (timeLimit == null) ? 0 : Long.parseLong(timeLimit);
            
            if (limit != 0)
            {
               d.setField(VALUE, value);
               d.setField(LAST_UPDATED_TIME_STAMP, "" + System.currentTimeMillis() / 1000);
               info.setDescriptor(d, ATTRIBUTE_DESCRIPTOR);
            }
            
            // send notification
            ((ModelBase)invoker).sendAttributeChangeNotification(
                  new Attribute(attrName, oldValue),
                  new Attribute(attrName, value)
            );
         }
         
         // GetAttribute
         else
         {   
            String getMethod = (String)d.getFieldValue(GET_METHOD);
            
            // are we mapping to a getter operation ?
            if (getMethod != null)
            {
               // are we caching the attribute value?
               String timeLimit = (String)d.getFieldValue(CURRENCY_TIME_LIMIT);
               long limit = (timeLimit == null) ? 0 : Long.parseLong(timeLimit);

               // if -1 we never invoke the getter
               if (limit == -1)
                  return d.getFieldValue(VALUE);
     
               // if >0 caching is enabled
               if (limit > 0)
               {
                  String timeStamp = (String)d.getFieldValue(LAST_UPDATED_TIME_STAMP);
                  long lastUpdate = (timeStamp == null) ? 0 : Long.parseLong(timeStamp);
              
                  // if the value hasn't gone stale, return from the descriptor
                  if (System.currentTimeMillis() < lastUpdate * 1000 + limit * 1000)
                     return d.getFieldValue(VALUE);
               }
               
               // we got here means either stale value in descriptior, or zero time limit
               Object value = invoker.invoke(getMethod, new Object[0], new String[0]);
               
               // update the descriptor
               d.setField(VALUE, value);
               d.setField(LAST_UPDATED_TIME_STAMP, "" + System.currentTimeMillis() / 1000);
               
               info.setDescriptor(d, ATTRIBUTE_DESCRIPTOR);
            }
         }
         
         return d.getFieldValue(VALUE);
      }
      catch (Throwable t)
      {
         // FIXME: need to check this exception handling
         throw new InvocationException(t);
      }
   }
   
}




