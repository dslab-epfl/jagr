
/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

// $Id: MBeanProvider.java,v 1.1.1.1 2002/11/16 03:16:50 mikechen Exp $
package org.jboss.net.jmx.server;



// axis package
import org.apache.axis.AxisFault;
import org.apache.axis.Message;
import org.apache.axis.Handler;
import org.apache.axis.MessageContext;
import org.apache.axis.providers.BasicProvider;
import org.apache.axis.handlers.soap.SOAPService;
import org.apache.axis.providers.BasicProvider;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.axis.message.RPCParam;
import org.apache.axis.message.RPCElement;
import org.apache.axis.utils.JavaUtils;
import org.apache.axis.EngineConfiguration;
import org.apache.axis.wsdl.fromJava.Emitter;
import org.apache.axis.encoding.TypeMapping;
import org.apache.axis.handlers.soap.SOAPService;

// Jboss
import org.jboss.net.axis.XMLResourceProvider;

// log4j
import org.apache.log4j.Category;

// sax & jaxrpc
import org.xml.sax.SAXException;
import javax.xml.soap.SOAPException;
import javax.xml.rpc.namespace.QName;

// jmx
import javax.management.MBeanServerFactory;
import javax.management.MBeanServer;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.management.InstanceNotFoundException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.Attribute;
import javax.management.MBeanInfo;
import javax.management.MBeanAttributeInfo;
import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.InvalidAttributeValueException;
import javax.management.IntrospectionException;
import javax.management.ReflectionException;
import javax.management.MBeanOperationInfo;

// W3C
import org.w3c.dom.Document;

// utils
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * Exposes mbeans as targets (pivot-handlers) of web-services. To
 * deploy a particular mbean as a web-service, a deployment descriptor
 * would look like:
 *
 * <wsdd:deployment>
 *  <handler name="MBeanDispatcher" class="org.jboss.net.jmx.MBeanProvider"/>
 *  <wsdd:service name="${ServiceName}" handler="Handler">
 *      <option name="handlerClass" value="org.jboss.net.jmx.server.MBeanProvider"/>
 *      <option name="ObjectName" value="${JMX_ObjectName}"/>
 *  </wsdd:service>
 * </wsdd:deployment>
 *
 * The message format (WSDL generation is to come ...) requires the first
 * parameter being always a String[] of the length of the following
 * parameters that describes the signature of the target method.
 *
 * <br>
 * <h3>Change History</h3>
 * <ul>
 * <li> jung,     03.04.2002: cache some meta-data about mbean. </li>
 * <li> braswell, 03.04.2002: JMX1.1 does not expose attribut accessors as methods anymore. </li>
 * <li> jung,     21.03.2002: made apache axis beta1 compliant. </li>
 * </ul>
 *
 * @created 1. Oktober 2001, 16:38
 * @author <a href="mailto:Christoph.Jung@infor.de">Christoph G. Jung</a>
 * @version $Revision: 1.1.1.1 $
 */

public class MBeanProvider
   extends BasicProvider
{

   /** stores meta-data about mbean */

   protected MBeanInfo info;

   /** stores attribute meta-data */

   protected MBeanAttributeInfo[] attributes;

   /** hashmap from attribute-name to attribute info */

   protected Map attributeMap = new java.util.HashMap();

   /** the server which we are tight to */

   protected MBeanServer server;

   /** the objectName which we are running into */

   protected ObjectName name;

   /** whether this provider has been already initialized */

   protected boolean initialized;

   /** Creates new MBeanProvider */

   private String allowedMethodsOption = "allowedMethods";

   /**
    * Constructor MBeanProvider
    *
    *
    */

   public MBeanProvider ()
   {
   }

   /** initialize the meta-data */

   protected synchronized void initialize (MessageContext msgCtx)
      throws AxisFault
   {
      if (!initialized)
      {
         initialized = true;

         SOAPService service = msgCtx.getService();

         // include the JMX objectname
         String objectName =
            ( String ) service.getOption(Constants.OBJECTNAME_PROPERTY);

         // and the server id (maybe)
         String serverId =
            ( String ) service.getOption(Constants.MBEAN_SERVER_ID_PROPERTY);

         // process server id
         Iterator allServers =
            MBeanServerFactory.findMBeanServer(serverId).iterator();

         if (!allServers.hasNext())
            throw new AxisFault(Constants.NO_MBEAN_SERVER_FOUND);
         else server = ( MBeanServer ) allServers.next();

         // process objectname
         try
         {
            name = new ObjectName(objectName);
         }
         catch (MalformedObjectNameException e)
         {
            throw new AxisFault(Constants.WRONG_OBJECT_NAME, e);
         }

         try
         {
            info = server.getMBeanInfo(name);
         }
         catch (InstanceNotFoundException e)
         {
            throw new AxisFault(Constants.NO_MBEAN_INSTANCE, e);
         }
         catch (IntrospectionException e)
         {
            throw new AxisFault(Constants.INTROSPECTION_EXCEPTION, e);
         }
         catch (ReflectionException e)
         {
            throw new AxisFault(Constants.INTROSPECTION_EXCEPTION, e);
         }

         attributes = info.getAttributes();

         for (int i = 0; i < attributes.length; ++i)
         {
            attributeMap.put(attributes [i].getName(), attributes [i]);
         }
      }
   }

   /**
    * Invoke is called to do the actual work of the Handler object.
    */

   public void invoke (MessageContext msgContext)
      throws AxisFault
   {

      // initialize first
      initialize(msgContext);

      // the options of the service
      String serviceName = msgContext.getTargetService();

      // dissect the message
      Message      reqMsg = msgContext.getRequestMessage();
      SOAPEnvelope reqEnv = ( SOAPEnvelope ) reqMsg.getSOAPEnvelope();
      Message      resMsg = msgContext.getResponseMessage();
      SOAPEnvelope resEnv = (resMsg == null) ? new SOAPEnvelope()
                                             : ( SOAPEnvelope ) resMsg.getSOAPEnvelope();

      // copied code from RobJ, duh?
      if (msgContext.getResponseMessage() == null)
      {
         resMsg = new Message(resEnv);

         msgContext.setResponseMessage(resMsg);
      }

      // navigate the bodies
      Iterator allBodies = reqEnv.getBodyElements().iterator();

      while (allBodies.hasNext())
      {
         Object nextBody = allBodies.next();

         if (nextBody instanceof RPCElement)
         {
            RPCElement body  = ( RPCElement ) nextBody;
            String     mName = body.getMethodName();
            List       args  = null;

            try
            {
               args = body.getParams();
            }
            catch (SAXException e)
            {
               throw new AxisFault(Constants.EXCEPTION_OCCURED, e);
            }

            Object[] arguments;
            String[] classNames;

            // parameter preprocessing
            if (args == null || args.size() == 0)
            {
               arguments  = new Object [0];
               classNames = new String [0];
            }
            else
            {
               arguments = new Object [args.size() - 1];

               RPCParam param = ( RPCParam ) args.get(0);

               try
               {
                  classNames =
                     ( String[] ) JavaUtils.convert(param.getValue(),
                                                    String[].class);

                  for (int count = 0; count < classNames.length; count++)
                  {
                     param = ( RPCParam ) args.get(count + 1);

                     try
                     {
                        arguments [count] = JavaUtils.convert(
                           param.getValue(),
                           msgContext.getClassLoader().loadClass(
                              classNames [count]));
                     }
                     catch (ClassNotFoundException e)
                     {
                        throw new AxisFault(Constants.CLASS_NOT_FOUND, e);
                     }
                  }
               }
               catch (ClassCastException e)
               {
                  throw new AxisFault(Constants.COULD_NOT_CONVERT_PARAMS, e);
               }
            }

            // now do the JMX call
            try
            {
               Object result;

               // now we have to distinguish attribute from
               // "regular" method access, taken over from MBeanProxy
               if (mName.startsWith("get") && arguments.length == 0)
               {
                  String attrName = mName.substring(3);

                  result = server.getAttribute(name, attrName);
               }
               else
                  if (mName.startsWith("is") && arguments.length == 0)
                  {
                     String attrName = mName.substring(2);

                     result = server.getAttribute(name, attrName);
                  }
                  else
                     if (mName.startsWith("set") && arguments.length == 1)
                     {
                        String attrName = mName.substring(3);

                        server.setAttribute(name,
                                            new Attribute(attrName,
                                                          arguments [0]));

                        result = null;
                     }
                     else
                     {
                        result = server.invoke(name, mName, arguments,
                                               classNames);
                     }

               // and encode it back to the response
               RPCElement resBody = new RPCElement(mName + "Response");

               resBody.setPrefix(body.getPrefix());
               resBody.setNamespaceURI(body.getNamespaceURI());

               RPCParam param = new RPCParam(mName + "Result", result);

               resBody.addParam(param);
               resEnv.addBodyElement(resBody);
               resEnv.setEncodingStyle(
                  org.apache.axis.Constants.URI_SOAP_ENC);
            }
            catch (InstanceNotFoundException e)
            {
               throw new AxisFault(Constants.NO_MBEAN_INSTANCE, e);
            }
            catch (AttributeNotFoundException e)
            {
               throw new AxisFault(Constants.NO_SUCH_ATTRIBUTE, e);
            }
            catch (InvalidAttributeValueException e)
            {
               throw new AxisFault(Constants.INVALID_ARGUMENT, e);
            }
            catch (MBeanException e)
            {
               throw new AxisFault(Constants.MBEAN_EXCEPTION, e);
            }
            catch (ReflectionException e)
            {
               throw new AxisFault(Constants.EXCEPTION_OCCURED,
                                   e.getTargetException());
            }
            catch (SOAPException e)
            {
               throw new AxisFault(Constants.EXCEPTION_OCCURED, e);
            }
         }
      }
   }

   public void generateWSDL (MessageContext msgCtx)
      throws AxisFault
   {
      initialize(msgCtx);

      EngineConfiguration engineConfig = msgCtx.getAxisEngine().getConfig();

      if (!(engineConfig instanceof XMLResourceProvider)) return;

      XMLResourceProvider config        =
         ( XMLResourceProvider ) engineConfig;
      ClassLoader         newLoader     =
         config.getMyDeployment().getClassLoader(new QName(null,
            msgCtx.getTargetService()));
      ClassLoader         currentLoader =
         Thread.currentThread().getContextClassLoader();

      try
      {
         Thread.currentThread().setContextClassLoader(newLoader);

         String      serviceName    = msgCtx.getTargetService();
         SOAPService service        = msgCtx.getService();
         String      allowedMethods = getAllowedMethods(service);

         if ((allowedMethods == null) || allowedMethods.equals(""))
            throw new AxisFault(
               "Server.NoMethodConfig",
               JavaUtils.getMessage(
               "noOption00", info.getClassName(), serviceName), null, null);

         // Okay we want to expose everything.  Iterate through all the methods in the
         // MBeanInfo structure and pull out all operations and attribute accessors.
         if (allowedMethods.equals("*"))
         {

            // Get the operations
            StringBuffer         exposedMethods = new StringBuffer();
            MBeanOperationInfo[] mboi           = info.getOperations();
            int                  i              = 0;

            for (i = 0; i < mboi.length; i++)
               exposedMethods.append(mboi [i].getName() + " ");

            // Get the attributes
            MBeanAttributeInfo[] mbai = info.getAttributes();

            for (i = 0; i < mbai.length; i++)
            {
               if (mbai [i].isReadable())
                  exposedMethods.append("get" + mbai [i].getName() + " ");

               if (mbai [i].isWritable())
                  exposedMethods.append("set" + mbai [i].getName() + " ");
            }

            allowedMethods = exposedMethods.toString();
         }

         String  url         = msgCtx.getStrProp(MessageContext.TRANS_URL);
         String  urn         = ( String ) msgCtx.getTargetService();
         String  description = "JMX MBean exposed as a WebService";
         Class   cls         = Class.forName(info.getClassName());
         Emitter emitter     = new Emitter();

         emitter.setMode(service.getStyle());
         emitter.setClsSmart(cls, url);
         emitter.setAllowedMethods(allowedMethods);
         emitter.setIntfNamespace(url);
         emitter.setLocationUrl(url);
         emitter.setTypeMapping(
            ( TypeMapping ) msgCtx.getTypeMappingRegistry().getTypeMapping(
               org.apache.axis.Constants.URI_CURRENT_SOAP_ENC));
         emitter.setDefaultTypeMapping(
            ( TypeMapping ) msgCtx.getTypeMappingRegistry().getDefaultTypeMapping());

         Document doc = emitter.emit(Emitter.MODE_ALL);

         msgCtx.setProperty("WSDL", doc);
      }
      catch (Exception ex)
      {
         ex.printStackTrace();
         Thread.currentThread().setContextClassLoader(currentLoader);

         throw new AxisFault(Constants.COULDNT_GEN_WSDL, ex);
      }

      Thread.currentThread().setContextClassLoader(currentLoader);
   }

   /**
    * ToDo, called when a fault occurs to 'undo' whatever 'invoke' did.
    */

   public void undo (MessageContext msgContext)
   {

      // unbelievable this foresight
   }

   private String getAllowedMethods (Handler service)
   {
      String val = ( String ) service.getOption(allowedMethodsOption);

      if (val == null || val.length() == 0)
      {

         // Try the old option for backwards-compatibility
         val = ( String ) service.getOption("methodName");
      }

      return val;
   }
}



