/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.mx.metadata;

import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import javax.management.Descriptor;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.NotCompliantMBeanException;
import javax.management.modelmbean.DescriptorSupport;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanConstructorInfo;
import javax.management.modelmbean.ModelMBeanInfo;
import javax.management.modelmbean.ModelMBeanInfoSupport;
import javax.management.modelmbean.ModelMBeanNotificationInfo;
import javax.management.modelmbean.ModelMBeanOperationInfo;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jboss.mx.modelmbean.ModelMBeanConstants;

/**
 */
public class JBossXMBean10
   implements MetaDataBuilder, ModelMBeanConstants
{

   // Constants -----------------------------------------------------
   /*
   private final static String GET_METHOD = "getMethod";
   private final static String SET_METHOD = "setMethod";
   private final static String PERSIST_POLICY = "persistPolicy";
   private final static String PERSIST_PERIOD = "persistPeriod";
   private final static String PERSIST_NAME = "persistName";
   private final static String PERSIST_LOCATION = "persistLocation";
   private final static String CURRENCY_TIME_LIMIT = "currencyTimeLimit";
   private final static String ON_UPDATE = "OnUpdate";
   private final static String NO_MORE_OFTEN_THAN = "NoMoreOftenThan";
   private final static String NEVER = "Never";
   private final static String ON_TIMER = "OnTimer";
   private final static String[] PERSIST_POLICY_LIST = {NEVER, ON_UPDATE, NO_MORE_OFTEN_THAN, ON_TIMER};
   private final static String STATE_ACTION_ON_UPDATE = "state-action-on-update";
   private final static String KEEP_RUNNING = "KEEP_RUNNING";
   private final static String RESTART = "RESTART";
   private final static String REINSTANTIATE = "REINSTANTIATE";
   private final static String[] STATE_ACTION_ON_UPDATE_LIST = {KEEP_RUNNING, RESTART, REINSTANTIATE};
   private final static String DESCRIPTOR = "descriptor";
   private final static String GETMETHOD = "getMethod";
   private final static String SETMETHOD = "setMethod";


   private final static String DESCRIPTOR = "descriptor";
   private final static String ACTION = "ACTION";
   private final static String ACTION_INFO = "ACTION_INFO";
   private final static String INFO = "INFO";
*/

   // Attributes ----------------------------------------------------
   private URL url = null;
   private String className = null;

   // Constructors --------------------------------------------------

   public JBossXMBean10(String resourceClassName, URL url)
   {
      this.url = url;
      this.className = resourceClassName;
   }

   public JBossXMBean10(String resourceClassName, String url) throws MalformedURLException
   {
      this(resourceClassName, new URL(url));
   }


   // MetaDataBuilder implementation --------------------------------

   public MBeanInfo build() throws NotCompliantMBeanException
   {
      try
      {
         // FIXME FIXME FIXME
         // the SAX driver is hard coded here due to conflicts with the JAXP/Crimson
         // version in the testsuite build system and the JAXP/crimson versions required
         // by the JDOM Beta 7 implementation (JAXP 1.1 & crimson 1.1)
         //
         // the driver class (and validation) should be configurable by the client
         //
         SAXBuilder builder = new SAXBuilder("org.apache.crimson.parser.XMLReaderImpl");

         builder.setValidation(false);

         Element root = builder.build(url).getRootElement();

         String description = root.getChildText("description");

         if (className == null) 
         {
            className = root.getChildText("class");
         } // end of if ()
         List constructors = root.getChildren("constructor");
         List operations = root.getChildren("operation");
         List attributes = root.getChildren("attribute");
         List notifications = root.getChildren("notifications");

         Descriptor descr = getDescriptor(root, className, "mbean");


         ModelMBeanInfo info = buildMBeanMetaData(
            description, constructors, operations,
            attributes, notifications, descr
         );

         return (MBeanInfo) info;
      }
      catch (JDOMException e)
      {
         e.printStackTrace();
         throw new NotCompliantMBeanException("Error parsing the XML file: " + ((e.getCause() == null) ? e.toString() : e.getCause().toString()));
      }
   }

   protected Descriptor getDescriptor(final Element parent, final String infoName, final String type) throws NotCompliantMBeanException
   {
      Descriptor descr = new DescriptorSupport();
      descr.setField("name", infoName);
      descr.setField("descriptorType", type);

      Element descriptors = parent.getChild("descriptors");
      if (descriptors == null) 
      {
         return descr;
      } // end of if ()
      for (Iterator i = descriptors.getChildren().iterator(); i.hasNext();)
      {
         Element descriptor = (Element)i.next();
         String name = descriptor.getName();
         if (name.equals("persistence")) 
         {
            Attribute persistPolicy = descriptor.getAttribute(PERSIST_POLICY);
            Attribute persistPeriod = descriptor.getAttribute(PERSIST_PERIOD);
            Attribute persistLocation = descriptor.getAttribute(PERSIST_LOCATION);
            Attribute persistName = descriptor.getAttribute(PERSIST_NAME);
            if (persistPolicy != null)
            {
               String value = persistPolicy.getValue();
               validate(value, PERSIST_POLICY_LIST);
               descr.setField(PERSIST_POLICY, value);
            }
            if (persistPeriod != null)
            {
               descr.setField(PERSIST_PERIOD, persistPeriod.getValue());
            }
            if (persistLocation != null)
            {
               descr.setField(PERSIST_LOCATION, persistLocation.getValue());
            }
            if (persistName != null)
            {
               descr.setField(PERSIST_NAME, persistName.getValue());
            }
         }
         else if (name.equals(CURRENCY_TIME_LIMIT))
         {
            descr.setField(CURRENCY_TIME_LIMIT, descriptor.getAttributeValue("value"));
         } // end of else
         else if (name.equals(STATE_ACTION_ON_UPDATE))
         {
            String value = descriptor.getAttributeValue("value");
            validate(value, STATE_ACTION_ON_UPDATE_LIST);
            descr.setField(STATE_ACTION_ON_UPDATE, value);
         } // end of else
         else if (name.equals(DESCRIPTOR))
         {
            descr.setField(descriptor.getAttributeValue("name"), descriptor.getAttributeValue("value"));
         } // end of else
      } // end of for ()
      return descr;
   }

   private void validate(String value, String[] valid) throws NotCompliantMBeanException
   {
      for (int i = 0; i< valid.length; i++)
      {
         if (valid[i].equalsIgnoreCase(value)) 
         {
            return;
         } // end of if ()
      } // end of for ()
      throw new NotCompliantMBeanException("Unknown descriptor value: " + value);      
   }


   // builder methods

   protected ModelMBeanInfo buildMBeanMetaData(String description,
                                               List constructors, List operations, List attributes,
                                               List notifications, Descriptor descr)
      throws NotCompliantMBeanException
   {

      ModelMBeanOperationInfo[] operInfo =
         buildOperationInfo(operations);
      ModelMBeanAttributeInfo[] attrInfo =
         buildAttributeInfo(attributes);
      ModelMBeanConstructorInfo[] constrInfo =
         buildConstructorInfo(constructors);
      ModelMBeanNotificationInfo[] notifInfo =
         buildNotificationInfo(notifications);

      ModelMBeanInfo info = new ModelMBeanInfoSupport(
         className, description, attrInfo, constrInfo,
         operInfo, notifInfo, descr
      );

      return info;
   }


   protected ModelMBeanConstructorInfo[] buildConstructorInfo(List constructors)
      throws NotCompliantMBeanException
   {

      List infos = new ArrayList();

      for (Iterator it = constructors.iterator(); it.hasNext();)
      {
         Element constr = (Element) it.next();
         String name = constr.getChildTextTrim("name");
         String description = constr.getChildTextTrim("description");
         List params = constr.getChildren("parameter");

         MBeanParameterInfo[] paramInfo =
            buildParameterInfo(params);

         Descriptor descr = getDescriptor(constr, name, CONSTRUCTOR_DESCRIPTOR);

         ModelMBeanConstructorInfo info =
            new ModelMBeanConstructorInfo(name, description, paramInfo, descr);

         infos.add(info);
      }

      return (ModelMBeanConstructorInfo[]) infos.toArray(
         new ModelMBeanConstructorInfo[0]);
   }

   protected ModelMBeanOperationInfo[] buildOperationInfo(List operations)
      throws NotCompliantMBeanException
   {
      List infos = new ArrayList();

      for (Iterator it = operations.iterator(); it.hasNext(); )
      {
         Element oper = (Element) it.next();
         String name = oper.getChildTextTrim("name");
         String description = oper.getChildTextTrim("description");
         String type = oper.getChildTextTrim("return-type");
         String impact = oper.getAttributeValue("impact");
         List params = oper.getChildren("parameter");

         MBeanParameterInfo[] paramInfo =
            buildParameterInfo(params);

         Descriptor descr = getDescriptor(oper, name, OPERATION_DESCRIPTOR);

         // defaults to ACTION_INFO
         int operImpact = MBeanOperationInfo.ACTION_INFO;

         if (impact != null)
         {
            if (impact.equals(INFO))
               operImpact = MBeanOperationInfo.INFO;
            else if (impact.equals(ACTION))
               operImpact = MBeanOperationInfo.ACTION;
            else if (impact.equals(ACTION_INFO))
               operImpact = MBeanOperationInfo.ACTION_INFO;
         }

         // default return-type is void
         if (type == null)
            type = "void";

         ModelMBeanOperationInfo info = new ModelMBeanOperationInfo(
            name, description, paramInfo, type, operImpact, descr);

         infos.add(info);
      }

      return (ModelMBeanOperationInfo[]) infos.toArray(
         new ModelMBeanOperationInfo[0]);
   }


   protected ModelMBeanNotificationInfo[] buildNotificationInfo(List notifications)
      throws NotCompliantMBeanException
   {

      List infos = new ArrayList();

      for (Iterator it = notifications.iterator(); it.hasNext();)
      {
         Element notif = (Element) it.next();
         String name = notif.getChildTextTrim("name");
         String description = notif.getChildTextTrim("description");
         List notifTypes = notif.getChildren("notification-type");
         Descriptor descr = getDescriptor(notif, name, NOTIFICATION_DESCRIPTOR);

         List types = new ArrayList();

         for (Iterator iterator = notifTypes.iterator(); iterator.hasNext();)
         {
            Element type = (Element) iterator.next();
            types.add(type.getTextTrim());
         }

         ModelMBeanNotificationInfo info = new ModelMBeanNotificationInfo(
            (String[]) types.toArray(), name, description, descr);

         infos.add(info);
      }

      return (ModelMBeanNotificationInfo[]) infos.toArray(
         new ModelMBeanNotificationInfo[0]
      );
   }

   protected ModelMBeanAttributeInfo[] buildAttributeInfo(List attributes)
      throws NotCompliantMBeanException
   {

      List infos = new ArrayList();

      for (Iterator it = attributes.iterator(); it.hasNext();)
      {
         Element attr = (Element) it.next();
         String name = attr.getChildTextTrim("name");
         String description = attr.getChildTextTrim("description");
         String type = attr.getChildTextTrim("type");
         String access = attr.getAttributeValue("access");
         String getMethod = attr.getAttributeValue("getMethod");
         String setMethod = attr.getAttributeValue("setMethod");
         Descriptor descr = getDescriptor(attr, name, ATTRIBUTE_DESCRIPTOR);
         if (getMethod != null) 
         {
            descr.setField(GET_METHOD, getMethod);
         } // end of if ()
         
         if (setMethod != null) 
         {
            descr.setField(SET_METHOD, setMethod);
         } // end of if ()
         

         // defaults read-write
         boolean isReadable = true;
         boolean isWritable = true;

         if (access.equalsIgnoreCase("read-only"))
            isWritable = false;

         else if (access.equalsIgnoreCase("write-only"))
            isReadable = false;


         ModelMBeanAttributeInfo info = new ModelMBeanAttributeInfo(
            name, type, description, isReadable, isWritable, false, descr
         );


         infos.add(info);
      }

      return (ModelMBeanAttributeInfo[]) infos.toArray(
         new ModelMBeanAttributeInfo[0]
      );
   }


   protected MBeanParameterInfo[] buildParameterInfo(List parameters)
   {

      Iterator it = parameters.iterator();
      List infos = new ArrayList();

      while (it.hasNext())
      {
         Element param = (Element) it.next();
         String name = param.getChildTextTrim("name");
         String type = param.getChildTextTrim("type");
         String descr = param.getChildTextTrim("description");

         MBeanParameterInfo info = new MBeanParameterInfo(name, type, descr);

         infos.add(info);
      }

      return (MBeanParameterInfo[]) infos.toArray(new MBeanParameterInfo[0]);
   }

}


