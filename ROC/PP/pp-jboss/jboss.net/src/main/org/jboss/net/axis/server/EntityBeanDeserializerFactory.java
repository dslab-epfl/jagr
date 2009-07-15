/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

// $Id: EntityBeanDeserializerFactory.java,v 1.1.1.1 2003/03/07 08:26:04 emrek Exp $

package org.jboss.net.axis.server;

import org.jboss.net.axis.ParameterizableDeserializerFactory;

import javax.xml.rpc.namespace.QName;
import java.util.Hashtable;

/**
 * Factory for server-side Entity Bean Deserialization. 
 * <br>
 * <h3>Change History</h3>
 * <ul>
 * <li> jung, 06.04.2002: Added parameter table for additional
 * deserializer options. </li>
 * </ul>
 * @created 21.03.2002
 * @author <a href="mailto:Christoph.Jung@infor.de">Christoph G. Jung</a>
 * @version $Revision: 1.1.1.1 $
 */

public class EntityBeanDeserializerFactory extends ParameterizableDeserializerFactory {
	
	//
	// Constructors
	//
	
	/** the usual constructor used by axis */
	public EntityBeanDeserializerFactory(Class javaType, QName xmlType) {
	   this(javaType,xmlType,new Hashtable(0));
	}
	
	/** the extended constructor that is parameterized */
	public EntityBeanDeserializerFactory(Class javaType, QName xmlType, Hashtable options) {
	   super(EntityBeanDeserializer.class,false,javaType,xmlType,options);
	}
		   
}