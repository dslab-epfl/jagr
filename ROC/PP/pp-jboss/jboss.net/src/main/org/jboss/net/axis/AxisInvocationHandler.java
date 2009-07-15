/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

// $Id: AxisInvocationHandler.java,v 1.1.1.1 2003/03/07 08:26:04 emrek Exp $

package org.jboss.net.axis;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import java.util.Map;
import java.util.HashMap;
import java.net.URL;

import javax.xml.rpc.namespace.QName;

/**
 * An invocation handler that allows typed and persistent client access to
 * remote SOAP/Axis services. Adds method/interface to name resolution
 * to the axis client engine. Unfortunately the AxisClientProxy has a package-protected
 * constructor, otherwise we could inherit from there.
 * <br>
 * <h3>Change notes</h3>
 *   <ul>
 *     <li> cgj, 09.03.02: Adopted axis alpha3 changes. </li>
 *     <li> cgj, 17.12.01: Added overtaking of basic authentication 
 *     data from target url.
 *     <li>
 *   </ul>
 * @created  1. Oktober 2001, 09:29
 * @author <a href="mailto:Christoph.Jung@infor.de">Christoph G. Jung</a>
 * @version $Revision: 1.1.1.1 $
 */

public class AxisInvocationHandler implements InvocationHandler {


	//
	// Attributes
	//
	
	/** mapping of methods to interface names */
	final protected Map methodToInterface;
	/** mapping of methods to method names */
	final protected Map methodToName;
	/** the call object to which we delegate */
	final protected Call call;

	//
	// Constructors
	//
	
	/** 
	 * Creates a new AxisInvocationHandler 
	 * @param call the Axis call object
	 * @param methodMap a map of Java method to service method names
	 * @param interfaceMap a map of Java interface to service names 
	 */
	public AxisInvocationHandler(Call call, Map methodMap, Map interfaceMap) {
		this.call = call;
		this.methodToInterface = interfaceMap;
		this.methodToName = methodMap;
	}

	/** 
	 * Creates a new AxisInvocationHandler 
	 * @param endpoint target address of the service
	 * @param service an Axis service object
	 * @param methodMap a map of Java method to service method names
	 * @param interfaceMap a map of Java interface to service names 
	 */
	public AxisInvocationHandler(
		URL endpoint,
		Service service,
		Map methodMap,
		Map interfaceMap) {
		this(new Call(service), methodMap, interfaceMap);
		call.setTargetEndpointAddress(endpoint);
		setBasicAuthentication(endpoint);
	}

	/** 
	 * Creates a new AxisInvocationHandler 
	 * @param endPoint target url of the web service
	 * @param methodMap a map of Java method to service method names
	 * @param interfaceMap a map of Java interface to service names 
	 * @param maintainSession a flag that indicates whether this handler 
	 * 	should keep a persistent session with the service endpoint
	 */
	public AxisInvocationHandler(
		URL endPoint,
		Map methodMap,
		Map interfaceMap,
		boolean maintainSession) {
		this(endPoint, new Service(), methodMap, interfaceMap);
		call.setMaintainSession(maintainSession);
	}

	/** 
	 * Creates a new AxisInvocationHandler that keeps a persistent session with 
	 * the service endpoint
	 * @param endPoint target url of the web service
	 * @param methodMap a map of Java method to service method names
	 * @param interfaceMap a map of Java interface to service names 
	 */
	public AxisInvocationHandler(URL endPoint, Map methodMap, Map interfaceMap) {
		this(endPoint, methodMap, interfaceMap, true);
	}

	/** 
	 * Creates a new AxisInvocationHandler that keeps a persistent session with 
	 * the service endpoint. The unqualified name of the 
	 * intercepted Java interface will be the used service name.
	 * @param endPoint target url of the web service
	 * @param methodMap a map of Java method to service method names
	 */
	public AxisInvocationHandler(URL endPoint, Map methodMap) {
		this(endPoint, methodMap, new DefaultInterfaceMap());
	}

	/** 
	 * Creates a new AxisInvocationHandler that keeps a persistent session with 
	 * the service endpoint. The unqualified name of the 
	 * intercepted Java interface will be the used service name. 
	 * Intercepted methods are mapped straightforwardly to web service names.
	 * @param endPoint target url of the web service
	 * @param methodMap a map of Java method to service method names
	 */
	public AxisInvocationHandler(URL endPoint) {
		this(endPoint, new DefaultMethodMap());
	}

	//
	// Helpers
	//
	
	/** helper to transfer url authentication information into engine */
	protected void setBasicAuthentication(URL target) {
		String userInfo = target.getUserInfo();
		if (userInfo != null) {
			java.util.StringTokenizer tok = new java.util.StringTokenizer(userInfo, ":");
			if (tok.hasMoreTokens()) {
				call.setUsername(tok.nextToken());
				if (tok.hasMoreTokens()) {
					call.setPassword(tok.nextToken());
				}
			}
		}
	}

	//
	// Invocationhandling API
	//
	
	/** invoke given namespace+method+args */
	public Object invoke(String serviceName, String methodName, Object[] args)
		throws java.rmi.RemoteException {
		return call.invoke(serviceName, methodName, args);
	}

	/** invoke with additional method parameter signature */
	public Object invoke(
		String serviceName,
		String methodName,
		Object[] args,
		Class[] parameters)
		throws java.rmi.RemoteException {
		// classes are normally ignored
		return invoke(serviceName, methodName, args);
	}

	/** generic invocation method */
	public java.lang.Object invoke(
		java.lang.Object target,
		java.lang.reflect.Method method,
		java.lang.Object[] args)
		throws java.lang.Throwable {
		return invoke(
			(String) methodToInterface.get(method),
			(String) methodToName.get(method),
			args,
			method.getParameterTypes());
	}


	//
	// Static API
	//
	
	/** default creation of service */
	public static Object createAxisService(Class _interface, URL endpoint)
	{
		return createAxisService(_interface, new AxisInvocationHandler(endpoint));
	}

	/** default creation of service */
	public static Object createAxisService(
		Class _interface,
		URL endpoint, Service service)
	{
		return createAxisService(
			_interface,
			new AxisInvocationHandler(endpoint, service, new DefaultMethodMap(), new DefaultInterfaceMap()));
	}

	/** default creation of service */
	public static Object createAxisService(
		Class _interface,
		Call call)
	 {
		return createAxisService(
			_interface,
			new AxisInvocationHandler(call, new DefaultMethodMap(), new DefaultInterfaceMap()));
	}

	/** default creation of service */
	public static Object createAxisService(
		Class _interface,
		AxisInvocationHandler handler)
		{
		return Proxy.newProxyInstance(
			_interface.getClassLoader(),
			new Class[] { _interface },
			handler);
	}

	/**
	 * a tiny helper that does some default mapping of methods to interface names
	 */

	public static class DefaultInterfaceMap extends HashMap {

		/** no entries is the default */
		public DefaultInterfaceMap() {
			super(0);
		}

		/** returns default interface if no mapping of method/interface is defined */
		public Object get(Object key) {

			// first try a direct lookup
			Object result = super.get(key);

			if (result == null && key instanceof Method) {
				// if that is unsuccessful, we try to
				// lookup the class/interface itself
				result = super.get(((Method) key).getDeclaringClass());

				// if that is not specified, we simply extract the
				// un-qualified classname
				if (result == null) {
					String sresult = ((Method) key).getDeclaringClass().getName();
					if (sresult.indexOf(".") != -1)
						sresult = sresult.substring(sresult.lastIndexOf(".") + 1);
					result = sresult;
				}
			}

			return result;
		}
	}

	/**
	 * a tiny helper that does some default mapping of methods to method names
	 */

	public static class DefaultMethodMap extends HashMap {

		/** no entries is the default */
		public DefaultMethodMap() {
			super(0);
		}

		/** returns default interface if no mapping is defined */
		public Object get(Object key) {

			Object result = super.get(key);

			if (result == null && key instanceof Method) {
				result = ((Method) key).getName();
			}

			return result;
		}
	}

}