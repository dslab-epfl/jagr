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

public class XMBeanMetaData
   implements MetaDataBuilder
{

   // Constants -----------------------------------------------------
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

   // Attributes ----------------------------------------------------
   private URL url = null;
   private String className = null;

   // Constructors --------------------------------------------------

   public XMBeanMetaData(String resourceClassName, URL url)
   {
      this.url = url;
      this.className = resourceClassName;
   }

   public XMBeanMetaData(String resourceClassName, String url) throws MalformedURLException
   {
      this(resourceClassName, new URL(url));
   }


   // MetaDataBuilder implementation.

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

         builder.setValidation(true);

         Element root = builder.build(url).getRootElement();
         List constructors = root.getChildren("constructor");
         List operations = root.getChildren("operation");
         List attributes = root.getChildren("attribute");
         List notifications = root.getChildren("notifications");
         String description = root.getChildText("description");

         Attribute persistPolicy = root.getAttribute(PERSIST_POLICY);
         Attribute persistPeriod = root.getAttribute(PERSIST_PERIOD);
         Attribute persistLocation = root.getAttribute(PERSIST_LOCATION);
         Attribute persistName = root.getAttribute(PERSIST_NAME);
         Attribute currTimeLimit = root.getAttribute(CURRENCY_TIME_LIMIT);

         Descriptor descr = new DescriptorSupport();
         descr.setField("name", className);
         descr.setField("descriptorType", "mbean");

         if (persistPolicy != null)
            descr.setField(PERSIST_POLICY, persistPolicy.getValue());
         if (persistPeriod != null)
            descr.setField(PERSIST_PERIOD, persistPeriod.getValue());
         if (persistLocation != null)
            descr.setField(PERSIST_LOCATION, persistLocation.getValue());
         if (persistName != null)
            descr.setField(PERSIST_NAME, persistName.getValue());
         if (currTimeLimit != null)
            descr.setField(CURRENCY_TIME_LIMIT, currTimeLimit.getValue());

         ModelMBeanInfo info = buildMBeanMetaData(
            description, constructors, operations,
            attributes, notifications, descr
         );

         return (MBeanInfo) info;
      }
      catch (JDOMException e)
      {
         throw new NotCompliantMBeanException("Error parsing the XML file: " + ((e.getCause() == null) ? e.toString() : e.getCause().toString()));
      }
   }


   // builder methods

   protected ModelMBeanInfo buildMBeanMetaData(String description,
                                               List constructors, List operations, List attributes,
                                               List notifications, Descriptor descr)
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


   protected ModelMBeanConstructorInfo[] buildConstructorInfo(
      List constructors)
   {

      Iterator it = constructors.iterator();
      List infos = new ArrayList();

      while (it.hasNext())
      {
         Element constr = (Element) it.next();
         String name = constr.getChildTextTrim("name");
         String descr = constr.getChildTextTrim("description");
         List params = constr.getChildren("parameter");

         MBeanParameterInfo[] paramInfo =
            buildParameterInfo(params);

         ModelMBeanConstructorInfo info =
            new ModelMBeanConstructorInfo(name, descr, paramInfo);

         infos.add(info);
      }

      return (ModelMBeanConstructorInfo[]) infos.toArray(
         new ModelMBeanConstructorInfo[0]);
   }

   protected ModelMBeanOperationInfo[]
      buildOperationInfo(List operations)
   {

      Iterator it = operations.iterator();
      List infos = new ArrayList();

      while (it.hasNext())
      {
         Element oper = (Element) it.next();
         String name = oper.getChildTextTrim("name");
         String descr = oper.getChildTextTrim("description");
         String type = oper.getChildTextTrim("return-type");
         String impact = oper.getChildTextTrim("impact");
         List params = oper.getChildren("parameter");

         MBeanParameterInfo[] paramInfo =
            buildParameterInfo(params);

         // defaults to ACTION_INFO
         int operImpact = MBeanOperationInfo.ACTION_INFO;

         if (impact != null)
         {
            if (impact.equals("INFO"))
               operImpact = MBeanOperationInfo.INFO;
            else if (impact.equals("ACTION"))
               operImpact = MBeanOperationInfo.ACTION;
            else if (impact.equals("ACTION_INFO"))
               operImpact = MBeanOperationInfo.ACTION_INFO;
         }

         // default return-type is void
         if (type == null)
            type = "void";

         ModelMBeanOperationInfo info = new ModelMBeanOperationInfo(
            name, descr, paramInfo, type, operImpact
         );

         infos.add(info);
      }

      return (ModelMBeanOperationInfo[]) infos.toArray(
         new ModelMBeanOperationInfo[0]);
   }


   protected ModelMBeanNotificationInfo[]
      buildNotificationInfo(List notifications)
   {

      Iterator it = notifications.iterator();
      List infos = new ArrayList();

      while (it.hasNext())
      {
         Element notif = (Element) it.next();
         String name = notif.getChildTextTrim("name");
         String descr = notif.getChildTextTrim("description");
         List notifTypes = notif.getChildren("notification-type");

         Iterator iterator = notifTypes.iterator();
         List types = new ArrayList();

         while (iterator.hasNext())
         {
            Element type = (Element) iterator.next();
            types.add(type.getTextTrim());
         }

         ModelMBeanNotificationInfo info = new ModelMBeanNotificationInfo(
            (String[]) types.toArray(), name, descr
         );

         infos.add(info);
      }

      return (ModelMBeanNotificationInfo[]) infos.toArray(
         new ModelMBeanNotificationInfo[0]
      );
   }

   protected ModelMBeanAttributeInfo[]
      buildAttributeInfo(List attributes)
   {

      Iterator it = attributes.iterator();
      List infos = new ArrayList();

      while (it.hasNext())
      {
         Element attr = (Element) it.next();
         String name = attr.getChildTextTrim("name");
         String description = attr.getChildTextTrim("description");
         String type = attr.getChildTextTrim("type");
         String access = attr.getChildTextTrim("access");

         Attribute persistPolicy = attr.getAttribute(PERSIST_POLICY);
         Attribute persistPeriod = attr.getAttribute(PERSIST_PERIOD);
         Attribute setMethod = attr.getAttribute(SET_METHOD);
         Attribute getMethod = attr.getAttribute(GET_METHOD);
         Attribute currTimeLimit = attr.getAttribute(CURRENCY_TIME_LIMIT);

         Descriptor descr = new DescriptorSupport();
         descr.setField("name", name);
         descr.setField("descriptorType", "attribute");

         if (persistPolicy != null)
            descr.setField(PERSIST_POLICY, persistPolicy.getValue());
         if (persistPeriod != null)
            descr.setField(PERSIST_PERIOD, persistPeriod.getValue());
         if (setMethod != null)
            descr.setField(SET_METHOD, setMethod.getValue());
         if (getMethod != null)
            descr.setField(GET_METHOD, getMethod.getValue());
         if (currTimeLimit != null)
            descr.setField(CURRENCY_TIME_LIMIT, currTimeLimit.getValue());

         // if no method mapping, enable caching automatically
         if (setMethod == null && getMethod == null && currTimeLimit == null) 
            descr.setField(CURRENCY_TIME_LIMIT, "-1");         
            
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



