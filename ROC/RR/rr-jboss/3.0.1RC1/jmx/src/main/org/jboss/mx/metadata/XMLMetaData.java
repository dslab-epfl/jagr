/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.mx.metadata;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.DocType;
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

import org.jboss.mx.service.ServiceConstants;

/**
 * Aggregate builder for XML schemas.
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 */
public class XMLMetaData
   implements MetaDataBuilder, ModelMBeanConstants, ServiceConstants
{

   // Attributes ----------------------------------------------------
   private URL url = null;
   private String className = null;

   // Constructors --------------------------------------------------

   public XMLMetaData(String resourceClassName, URL url)
   {
      this.url = url;
      this.className = resourceClassName;
   }

   public XMLMetaData(String resourceClassName, String url) throws MalformedURLException
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

         Document doc = builder.build(url);
         DocType type = doc.getDocType();

         String docURL = type.getSystemID();

         if (docURL == null)
            docURL = type.getPublicID();

         // known document definitions
         if (docURL.endsWith(JBOSSMX_XMBEAN_DTD_1_0))
            return new JBossXMBean10(className, url).build();
         else if (docURL.endsWith(XMBEAN_DTD))
            return new XMBeanMetaData(className, url).build();
         else
            // defaults to the latest JBossMX XMBean schema
            return new JBossXMBean10(className, url).build();
      }
      catch (JDOMException e)
      {
         e.printStackTrace();
         throw new NotCompliantMBeanException("Error parsing the XML file: " + ((e.getCause() == null) ? e.toString() : e.getCause().toString()));
      }
   }

}


