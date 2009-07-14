package roc.rr;

import java.io.Serializable;
import java.util.*;
import java.net.*;

import javax.management.*;

/**
 *
 * A recursively rebootable component.
 *      
 *   @author  candea@cs.stanford.edu
 *   @version $Revision: 1.5 $
 *
 **/

public class Component
    implements Serializable 
{
    /*---------------------------------------------------------------------------
     * FIELDS
     *---------------------------------------------------------------------------*/

    /* Reference to our MBean server */
    MBeanServer server;

    /**
     * Canonical name of this component (unique across system).
     **/
    public String UID;
    private URL    url;

    /**
     * Shorthand name of this component.
     **/
    public String name;

    /**
     * Free-form string representing the type of this component.
     **/
    public String type;

    /**
     * Free-form string representing the persistence type used by this component.
     **/
    public String persistence;

    /**
     * The UID of the parent of this component (null if no parent).
     **/
    public String parentUID=null;
    
    /**
     * Synchronized list of this component's exported methods.
     **/
    private List methods;

    /**
     * Inter-method separator for string representation of a component.
     **/
    private static String SEPARATOR = " / ";


    /*---------------------------------------------------------------------------
     * PUBLIC METHODS
     *---------------------------------------------------------------------------*/

    /** 
     * Constructor for given Component name and a list of method names.
     *
     * @param  UID  name of the Component to construct
     * @param  cMethods
     * @param  type
     * @param  parent
     *
     **/

    public Component( String UID, String name, List cMethods, String type, String parent, MBeanServer server ) 
    {
	this.server = server;
	this.UID = filter(UID);

	if ( name != null ) {
	    this.name = name;
	} else {
	    this.name = this.UID.replaceAll( ".*,name=", "" ).replaceAll( ",.*", "");
	}

	try {
	    this.url = new URL( this.UID );
	}
	catch ( java.net.MalformedURLException murle ) {
	    this.url = null;
	}

	this.type = type;
	this.parentUID = parent;

	methods = Collections.synchronizedList(cMethods);
    }

    public Component( String UID, String name, List cMethods, String type, String parent) {
	this( UID, name, cMethods, type, parent, (MBeanServer)null );
    }

    public Component( String UID, String type, String parent, MBeanServer server ) {
	this( UID, null, new LinkedList(), type, parent, server );
    }

    public Component( String UID, String type, String parent ) {
	this( UID, type, parent, (MBeanServer)null );
    }

    public Component( String UID, String name, String type, String parent, MBeanServer server ) {
	this( UID, name, new LinkedList(), type, parent, server );
    }

    public Component( String UID, String name, String type, String parent ) {
	this( UID,  name, type, parent, (MBeanServer)null );
    }


    /** 
     * Filters escape characters out of component names.  This is
     * important if we want to do searches on component names and
     * match them up, because in some cases escape characters are
     * used, while in others they're not.
     *
     * @param  name  component name to filter
     *
     **/
    private String filter( String name )
    {
	name = name.replaceAll("%3a", ":");
	name = name.replaceAll("%3d", "=");
	name = name.replaceAll("%2c", ",");
	return name;
    }


    /** 
     * Setter for the component type.
     *
     * @param  cType  free-form type of Component
     *
     **/
    public void setType( String cType )
    {
	type = cType;
    }


    /** 
     * Setter for the persistence type.
     *
     * @param  cPersistence  free-form type of persistence
     *
     **/
    public void setPersistence( String cPersistence )
    {
	persistence = cPersistence;
    }

    public void setUrl( URL url )
    {
	this.url = url;
    }

    public URL getUrl()
    {
	return this.url;
    }

    /** 
     * Adds a method to the component.
     *
     * @param  mSig  signature of the method to be added
     *
     **/
    public void addMethod( String mName )
    {
	methods.add((Object) mName);
    }


    public ListIterator getMethods ()
    {
	return methods.listIterator();
    }

    /** 
     * Generates a string representation of the component.
     *
     **/
    public synchronized String toString ()
    {
	return UID + " " + toDB();
    }

    /** 
     * Generates a string representation of the component to put in a
     * database column.
     *
     **/
    public synchronized String toDB ()
    {
	String ret = "[type=" + type + ", persistence=" + persistence + "]";

	for (ListIterator it = methods.listIterator() ; it.hasNext() ; ) {
	    ret += SEPARATOR + (String)it.next();
	}

	return ret;
    }


    /** 
     * Obtains a Component object from its string-based
     * representation; assumes representation was generated by
     * toString() method, although extra whitespace on the ends is
     * fine.
     *
     * @param  rep  string representing the desired object
     *
     **/
    /*
    static public Component fromString( String rep )
    {
	String namePlusMethods[] = rep.trim().split(SEPARATOR, 0);

	Component comp = new Component( namePlusMethods[0], "generated", null );

	for (int i=1 ; i < namePlusMethods.length ; i++ ) {
	    comp.addMethod( namePlusMethods[i] );
	}

	return comp;
    }
    */


    /** 
     * Generates an HTML representation of the component.
     *
     **/
    public String toHtml()
    {
	String ret = "<B>" + name + "</B>";

	if ( type!=null  ||  persistence!=null ) 
	{
	    ret += "(";
	    if ( type!= null) {
		ret += type;
	    }
	    if ( persistence != null ) {
		ret += " " + persistence;
	    }
	    ret += ")";
	}

	ret += ":";
	for (ListIterator it = methods.listIterator() ; it.hasNext() ; ) {
	    ret += " " + (String)it.next();
	}

	return ret;
    }

    /** 
     * Microreboot this component.
     *
     **/
    public void reboot()
    {
	System.out.println("############################## Microrebooting " + url);

	ObjectName deployerSvc;
	try 
	{
	    deployerSvc = new ObjectName("jboss.system:service=MainDeployer");
	    server.invoke(deployerSvc, "redeploy", 
			  new Object[] { url }, new String[] { "java.net.URL" });
	} 
	catch ( Exception e ) 
	{
	    e.printStackTrace();
	}

	System.out.println("############################## DONE rebooting " + url);
    }
}



