package org.jboss.deployment;

import java.util.Hashtable;
import org.jboss.deployment.DeploymentInfo;
import javax.management.ObjectName;

/**
 * Global repository of <componentName, deployment> mappings, shared
 * across the whole system.  For each deployed component, map gives
 * corresponding DeploymentInfo object.  Currently we only track EJBs
 * (added/removed from within EjbModule.java).  Only works within
 * local JVM.
 *
 * @author George Candea
 * @version $Revision: 1.3 $
 *
 */

public class ComponentMap
{
    private static Hashtable components = new Hashtable();  // this is synchronized
    
    /**
     * Add a new component to the map.
     *
     * @param compName Name of component (e.g., EJB name)
     * @param jndiName JNDI name of component
     * @param di       DeploymentInfo for this component
     **/
    public static void add( String compName, String jndiName, DeploymentInfo di, ObjectName containerName )
    {
	Object[] elem = { (Object) di, (Object) jndiName, (Object) containerName };
	components.put( compName, elem );
    }
    
    /**
     * Remove a component from the map.
     *
     * @param compName Name of component (e.g., EJB name)
     **/
    public static Object remove( String compName )
    {
	return components.remove( compName );
    }

    /**
     * Retrieve the deployment info of a component.
     *
     * @param compName Name of component (e.g., EJB name)
     **/
    public static DeploymentInfo getDeploymentInfo( String compName )
    {
	Object[] elem = (Object[]) components.get( compName );
	if( elem == null )
	    return null;
	else
	    return (DeploymentInfo) elem[0];
    }

    /**
     * Retrieve the JNDI name of a component.
     *
     * @param compName Component name (e.g., EJB name)
     **/
    public static String getJndiName( String compName )
    {
	Object[] elem = (Object[]) components.get( compName );
	if( elem == null )
	    return null;
	else
	    return (String) elem[1];
    }

    /**
     * Retrieve the container name of a component.
     *
     * @param compName Name of component (e.g., EJB name)
     **/
    public static ObjectName getContainerName( String compName )
    {
	Object[] elem = (Object[]) components.get( compName );
	if( elem == null )
	    return null;
	else
	    return (ObjectName) elem[2];
    }

    /**
     * Retrieve a copy of the component map.
     *
     * @param compName Name of component (e.g., EJB name)
     **/
    public static Hashtable map()
    {
	return (Hashtable) components.clone();
    }
}


