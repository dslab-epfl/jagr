/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.mx.modelmbean;

import java.util.Iterator;

import javax.management.AttributeChangeNotification;
import javax.management.AttributeChangeNotificationFilter;
import javax.management.AttributeList;
import javax.management.Attribute;
import javax.management.Descriptor;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.NotificationBroadcaster;
import javax.management.NotificationBroadcasterSupport;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.JMException;
import javax.management.MBeanException;
import javax.management.ListenerNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.RuntimeOperationsException;

import javax.management.modelmbean.ModelMBean;
import javax.management.modelmbean.ModelMBeanInfo;
import javax.management.modelmbean.ModelMBeanInfoSupport;
import javax.management.modelmbean.ModelMBeanNotificationInfo;
import javax.management.modelmbean.DescriptorSupport;
import javax.management.modelmbean.InvalidTargetObjectTypeException;

import org.jboss.mx.server.MBeanInvoker;
import org.jboss.mx.interceptor.ModelMBeanInterceptor;
import org.jboss.mx.interceptor.PersistenceInterceptor;
import org.jboss.mx.interceptor.MBeanAttributeInterceptor;
import org.jboss.mx.interceptor.ObjectReferenceInterceptor;
import org.jboss.mx.persistence.NullPersistence;
import org.jboss.mx.persistence.PersistenceManager;

/**
 * An abstract base class that can be used to implement different
 * Model MBean implementations.
 *
 * @see javax.management.modelmbean.ModelMBean
 * @see org.jboss.mx.modelmbean.ModelMBeanConstants
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @version $Revision: 1.1.1.1 $
 */
public abstract class ModelBase
   extends MBeanInvoker
   implements ModelMBean, ModelMBeanConstants, MBeanRegistration
{

   // Attributes ----------------------------------------------------
   protected String resourceType              = null;
   protected PersistenceManager persistence   = new NullPersistence();
   protected NotificationBroadcasterSupport notifier = new NotificationBroadcasterSupport();

   protected long notifierSequence     = 1;
   protected long attrNotifierSequence = 1;


   // Constructors --------------------------------------------------
   public ModelBase()
   {}

   public ModelBase(ModelMBeanInfo info) throws MBeanException
   {
      setModelMBeanInfo(info);

      try
      {
         load();
      }
      catch (InstanceNotFoundException e)
      {
         throw new MBeanException(e);
      }
   }

   // Public --------------------------------------------------------

   // verify the resource types supported by the concrete MMBean implementation
   public abstract boolean isSupportedResourceType(String resourceType);


   // ModelMBean implementation -------------------------------------
   public void setModelMBeanInfo(ModelMBeanInfo info)
   throws MBeanException, RuntimeOperationsException
   {
      if (info == null)
         throw new IllegalArgumentException("MBeanInfo cannot be null.");

      this.info = new ModelMBeanInfoSupport(info);
   }

   public void setManagedResource(Object ref, String resourceType)
   throws MBeanException, InstanceNotFoundException, InvalidTargetObjectTypeException
   {
      if (ref == null)
         throw new IllegalArgumentException("Resource reference cannot be null.");

      // check that is a supported resource type (concrete implementations need to implement this)
      if (!isSupportedResourceType(resourceType))
         throw new InvalidTargetObjectTypeException("Unsupported resource type: " + resourceType);

      this.resource = ref;
      this.resourceType = resourceType;
   }

   // ModelMBeanNotificationBroadcaster implementation --------------
   public void addNotificationListener(NotificationListener listener,
                                       NotificationFilter filter,
                                       Object handback)
   {
      notifier.addNotificationListener(listener, filter, handback);
   }

   public void removeNotificationListener(NotificationListener listener)
   throws ListenerNotFoundException
   {
      notifier.removeNotificationListener(listener);
   }

   public void addAttributeChangeNotificationListener(NotificationListener listener,
         String attributeName, Object handback) throws MBeanException
   {
      AttributeChangeNotificationFilter filter = new AttributeChangeNotificationFilter();
      filter.enableAttribute(attributeName);
      notifier.addNotificationListener(listener, filter, handback);
   }

   public void removeAttributeChangeNotificationListener(NotificationListener listener, String attributeName)
   throws MBeanException, ListenerNotFoundException
   {
      notifier.removeNotificationListener(listener);
   }

   public void sendNotification(String message) throws MBeanException
   {
      Notification notif = new Notification(
                              GENERIC_MODELMBEAN_NOTIFICATION, // type
                              this,                            // source
                              ++notifierSequence,              // sequence number
                              message                          // message
                           );

      sendNotification(notif);
   }

   public void sendNotification(Notification notification)
   throws MBeanException
   {
      notifier.sendNotification(notification);
   }

   public void sendAttributeChangeNotification(AttributeChangeNotification notification)
   throws MBeanException
   {
      notifier.sendNotification(notification);
   }

   public void sendAttributeChangeNotification(Attribute oldValue, Attribute newValue)
   throws MBeanException
   {
      String attr = oldValue.getName();
      String type = ((ModelMBeanInfo)info).getAttribute(attr).getType();

      AttributeChangeNotification notif = new AttributeChangeNotification(
                                             this,                          // source
                                             ++attrNotifierSequence,        // seq. #
                                             System.currentTimeMillis(),    // time stamp
                                             "" + attr + " changed from " + oldValue + " to " + newValue,
                                             attr, type,                    // name & type
                                             oldValue.getValue(),
                                             newValue.getValue()            // values
                                          );

      notifier.sendNotification(notif);
   }

   public MBeanNotificationInfo[] getNotificationInfo()
   {
      // FIXME: NYI
      throw new Error("NYI");
   }

   // PersistentMBean implementation --------------------------------
   public void load() throws MBeanException, InstanceNotFoundException
   {
      if (info == null)
         return;

      persistence.load(info);
   }

   public void store() throws MBeanException, InstanceNotFoundException
   {
      persistence.store(info);
   }

   // MBeanRegistration implementation ------------------------------
   public ObjectName preRegister(MBeanServer server, ObjectName name) throws Exception
   {
      // FIXME: stack should be configured via descriptors
      // NOTE: the descriptors are copies, therefore changes to them won't be reflected automatically on the interceptor

      ModelMBeanInfoSupport infoSupport = (ModelMBeanInfoSupport) info;
      this.stack = new PersistenceInterceptor(server, this, infoSupport.getDescriptors(ALL_DESCRIPTORS));
      this.stack.insertLast(new MBeanAttributeInterceptor(infoSupport, this));
      this.stack.insertLast(new ObjectReferenceInterceptor(resource, infoSupport));

      return name;
   }

   public void postRegister(Boolean registrationSuccessful) { }
   public void preDeregister() throws Exception { }
   public void postDeregister() { }


}

