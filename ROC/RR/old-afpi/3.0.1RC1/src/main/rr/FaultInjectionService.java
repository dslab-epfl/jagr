/* $Id */

package rr;

import java.util.*;
import javax.management.ObjectName;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.deployment.*;

/** MBean for the AFPI fault injection service.
 *      
 *   @author  candea@cs.stanford.edu
 *   @version $Revision: 1.9 $
 */

/**
 * @jmx:mbean extends="org.jboss.system.ServiceMBean"
 */
public class FaultInjectionService
   extends ServiceMBeanSupport
   implements FaultInjectionServiceMBean
{
    //---------------------------------------------------------------------------
    // FIELDS
    //---------------------------------------------------------------------------
    private Hashtable components;
    private boolean   experimentInProgress = false; // true if we're currently running an injection exp
    private String    failed = "<b>FAILED</b>: ";

    //---------------------------------------------------------------------------
    // PUBLIC METHODS
    //---------------------------------------------------------------------------
    /**
     * @jmx:managed-operation
     */
    public String scheduleFault( String cName, String mName, String fName ) 
	throws Exception
    {
	Component comp = (Component) components.get(cName); 
	return comp.injector.scheduleFault(mName,fName);
    }

    /*---------------------------------------------------------------------------*
     *                                                                           *
     *---------------------------------------------------------------------------*/

    /**
     * @jmx:managed-operation
     */
    public String showKnownComponents() 
    {
	String ret = "";
	
	for (Enumeration e = components.elements(); e.hasMoreElements(); ) {
	    ret += ((Component) e.nextElement()).toString();
	}
 
	return ret;
    }
    
    /*---------------------------------------------------------------------------*
     *                                                                           *
     *---------------------------------------------------------------------------*/

    /**
     * @jmx:managed-operation
     */

    public String showInjectionConsole() 
    {
	String ret="<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0>";

	for (Enumeration ce = components.elements(); ce.hasMoreElements(); ) 
	{
	    Component comp = (Component) ce.nextElement();

	    ret += "<TR><TD><BR><FONT SIZE=+1><B>" + comp.name + "</B></FONT></TD></TR>";

	    for (Enumeration me = comp.methods.elements() ; me.hasMoreElements() ; ) 
	    {
		MethodSignature mSig = (MethodSignature) me.nextElement();

		ret += "<TR><TD>\n";
		ret += "\n<FORM METHOD=\"POST\" ACTION=\"http://localhost:8080/jmx-console/HtmlAdaptor\">\n";
		ret += "<INPUT TYPE=\"HIDDEN\" NAME=\"action\" VALUE=\"invokeOp\">\n";
		ret += "<INPUT TYPE=\"HIDDEN\" NAME=\"name\" VALUE=\"RR:service=FaultInjectionService\">\n";
		// FIXME: the following is a hardcoded index; we should define this in ROCConfig.java
		ret += "<INPUT TYPE=\"HIDDEN\" NAME=\"methodIndex\" VALUE=\"0\">\n"; 
		ret += "<INPUT TYPE=\"HIDDEN\" NAME=\"arg\" VALUE=\"" + comp.name + "\">\n";
		ret += "<INPUT TYPE=\"HIDDEN\" NAME=\"arg\" VALUE=\"" + mSig.name + "\">";

		ret += "&nbsp;&nbsp;&nbsp;&nbsp;" + mSig.name + "()&nbsp;";

		ListIterator faultsI = mSig.faults.listIterator();
		
		// Generate a pull-down list if we have user-defined exceptions
		if ( faultsI.hasNext() ) 
		{
		    ret += "<SELECT NAME=\"arg\" SIZE=\"1\">\n";
		    while ( faultsI.hasNext() ) 
		    {
			String fName = (String) faultsI.next();
			ret += "<OPTION VALUE=\"" + fName + "\">" + fName + "\n";
		    }
		    ret += "<OPTION VALUE=\"java.lang.Exception\">java.lang.Exception";
		    ret += "</SELECT>";
		} 
		else {
		    ret += "<INPUT TYPE=\"HIDDEN\" NAME=\"arg\" VALUE=\"java.lang.Exception\">";
		    ret += " [java.lang.Exception] ";
		}

		// Submit button
		ret += "<INPUT TYPE=\"SUBMIT\" VALUE=\"Schedule\">\n</FORM>\n";
		ret += "</TD></TR>\n";
	    }
	}

	ret += "</TABLE>";
	return ret;
    }

    /*---------------------------------------------------------------------------*
     *                                                                           *
     *---------------------------------------------------------------------------*/

    /**
     * @jmx:managed-operation
     */
    public String fullReboot( String appName ) 
    {
	Collection packs;
	ObjectName deployerSvc;

	//
	// Get all deployed packages
	//
	try 
	{
	    deployerSvc = new ObjectName("jboss.system:service=MainDeployer");
	    packs = (Collection) server.invoke(deployerSvc, "listDeployed", null, null);
	} 
	catch ( Exception e ) 
	{
	    e.printStackTrace();
	    return "FAILED: See server.log for stack trace details...";
	}

	//
	// Search for the package whose name contains the given substring
	//
	DeploymentInfo toReboot=null;
	for (Iterator i = packs.iterator() ; i.hasNext() ; ) 
	{
	    DeploymentInfo di = (DeploymentInfo) i.next();
	    if ( di.shortName.indexOf(appName) >= 0 ) 
	    {
		if ( toReboot != null )
		    return "FAILED: More than one package matches string " + appName;
		toReboot = di;
	    }
	}

	//
	// Undeploy the package and then deploy it again
	//
	try 
	{
	    server.invoke(deployerSvc, "undeploy", 
			  new Object[] { toReboot.url }, new String[] { "java.net.URL" });
	    server.invoke(deployerSvc, "deploy", 
			  new Object[] { toReboot.url }, new String[] { "java.net.URL" });
	} 
	catch (Exception e) 
	{
	    e.printStackTrace();
	    return "FAILED during reboot: Please see server.log for stack trace details...";
	}

	return "Successfully rebooted <B>" + toReboot.url + "</B>";
    }

    /*---------------------------------------------------------------------------*
     *                                                                           *
     *---------------------------------------------------------------------------*/

    /**
     * @jmx:managed-operation
     */
    public void addNewComponent( rr.Component component ) 
    {
	// We only accept a new component if our FI experiment is not in progress
	if (! experimentInProgress) {
	    try {
		components.put((Object)component.name, (Object)component);
	    }
	    catch (NullPointerException npe) {
		npe.printStackTrace();
	    }
	}
    }


    /*---------------------------------------------------------------------------*
     *                                                                           *
     *---------------------------------------------------------------------------*/

    /**
     * @jmx:managed-operation
     */
    public void reportThrowable( String cName, String mName, Throwable thrown )
    {
	System.out.println("##### RECV'D NOTIFICATION FROM comp=" + cName + " meth=" + mName);
	thrown.printStackTrace();
    }

    
    public void create() throws Exception
    {
	components = new Hashtable();
    }
   
    public void start() throws Exception
    {
    }
   
    public void stop()
    {
    }
   
    public void destroy()
    {
    }
}
