/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

// $Id: ObjectNameSerializer.java,v 1.1.1.1 2002/10/03 21:07:02 candea Exp $

package org.jboss.net.jmx.adaptor;

import org.apache.axis.Constants;
import org.apache.axis.encoding.Serializer;
import org.apache.axis.encoding.SerializationContext;
import org.apache.axis.encoding.XMLType;
import org.apache.axis.wsdl.fromJava.Types;

import org.xml.sax.Attributes;
import org.w3c.dom.Element;

import javax.xml.rpc.namespace.QName;

import javax.management.ObjectName;

import java.io.IOException;

/**
 * Serializer specialized to turn JMX-Objectnames into
 * corresponding XML-types.
 * <br>
 * <ul>
 * <li> jung, 10.03.2002: made axis alpha3 compliant. Added wsdl generation. </li>
 * </ul>
 * @created 2. Oktober 2001, 14:01
 * @author <a href="mailto:Christoph.Jung@infor.de">Christoph G. Jung</a>
 * @version $Revision: 1.1.1.1 $
 */

public class ObjectNameSerializer implements Serializer {

   /** this is the fully-qualified type that we serialize into */
   protected QName xmlType;

   // 
   // Constructors
   //

   public ObjectNameSerializer(QName xmlType) {
      this.xmlType = xmlType;
   }

   //
   // Public API
   //

   /** 
    *  turns a JMX objectname into a string-based xml element 
    *  @param name the name of the element that carries our type
    *  @param attributes the attributes of the element that carries our type
    *  @param value the objectname to serialize
    *  @param context the serialization context we live into
    */
   public void serialize(
      QName name,
      Attributes attributes,
      Object value,
      SerializationContext context)
      throws IOException {
      context.startElement(name, attributes);
      context.writeString(((ObjectName) value).getCanonicalName());
      context.endElement();
   }

   /** we use sax approach */
   public String getMechanismType() {
      return Constants.AXIS_SAX;
   }

   /**
    * Return XML schema for the specified type.
    * Our type simply inherits from string.
    */
   public boolean writeSchema(Types types) throws Exception {
      // Emit WSDL for simpleContent
      javax.wsdl.QName qName = types.getWsdlQName(xmlType);
      // ComplexType representation of SimpleType bean class
      Element simpleType = types.createElement("simpleType");
      types.writeSchemaElement(qName, simpleType);
      simpleType.setAttribute("name", qName.getLocalPart());
      Element simpleContent = types.createElement("simpleContent");
      simpleType.appendChild(simpleContent);
      Element extension = types.createElement("extension");
      simpleContent.appendChild(extension);
      extension.setAttribute("base", XMLType.XSD_STRING.toString());
      return true;
   }

}