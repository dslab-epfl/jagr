/* $Id */

package rr;

import java.util.*;
import java.sql.*;
import javax.management.ObjectName;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.deployment.*;

/** MBean for the AFPI fault injection service.
 *      
 *   @author  candea@cs.stanford.edu
 *   @version $Revision: 1.11 $
 *
 * @jmx:mbean extends="org.jboss.system.ServiceMBean"
 **/

public class FaultInjectionService
   extends ServiceMBeanSupport
   implements rr.FaultInjectionServiceMBean
{
    /**
     * Schedules a given fault for injection in the given method of
     * the given MBean.
     *
     * @param  cName  Name of MBean
     * @param  mName  Name of MBean's method
     * @param  fName  Fault name (most often an exception name)
     *
     * @jmx:managed-operation
     **/
    public String scheduleMBeanFault( String cName, String mName, String fName ) 
	throws Exception
    {
	MBeanInterceptor.scheduleFault(cName, mName, fName);
	return "Scheduled <b>" + fName + "</b> failure of <b>" + cName + "</b> on calls to <b>" + mName + "</b>";
    }


    /**
     * Schedules a given fault for injection whenever any method of
     * the given MBean is invoked.
     *
     * @param  cName  Name of MBean
     * @param  fName  Fault name (most often an exception name)
     *
     * @jmx:managed-operation
     **/
    public String scheduleMBeanFault( String cName, String fName ) 
	throws Exception
    {
	MBeanInterceptor.scheduleFault(cName, fName);
	return "Scheduled total <b>" + fName + "</b> failure of component <b>" + cName + "</b>";
    }


    /**
     * Schedules a given fault for injection whenever any method of
     * the given EJB is invoked.
     *
     * @param  cName  Name of MBean
     * @param  fName  Fault name (most often an exception name)
     *
     * @jmx:managed-operation
     **/
    public String scheduleEJBFault( String cName, String fName ) 
	throws Exception
    {
	EJBInterceptor.scheduleFault(cName, fName);
	return "Scheduled total <b>" + fName + "</b> failure of component <b>" + cName + "</b>";
    }

    /**
     * @jmx:managed-operation
     **/
    public LinkedList currentApps()
    {
	String query = "select short from components where canonical like '%j2eeType=J2EEApplication%'" +
                       "or canonical like '%J2EEApplication=null%j2eeType=EJBModule%'";

	ResultSet rs = DBUtil.executeQuery( query );

	LinkedList apps = new LinkedList();

	try {
	    while ( rs.next() ) 
	    {
		String compShortName = rs.getString(1);
		if ( !compShortName.endsWith( ".sar" ) ) /* not interested in system archives */
		{
		    apps.add( compShortName );
		}
	    }
	}
	catch ( SQLException sqle ) {
	    sqle.printStackTrace();
	    return null;
	}

	return apps;
    }

    /**
     * Starts an application-wide injection test.
     *
     * @jmx:managed-operation
     **/
    public String testApplication ( String appName ) 
    {
	/* Get all of the app's components */

	/* Systematically schedule each component to fail (one at a time) */

//	 select short from components where canonical like '%j2eeType=J2EEApplication%' or canonical like '%J2EEApplication=null%j2eeType=EJBModule%'

	return "";
    }

    /**
     * Cancels a scheduled fault in MBeans.
     *
     * @jmx:managed-operation
     **/
    public String cancelMBeanFault () 
    {
	return MBeanInterceptor.cancelFault();
    }


    /**
     * @jmx:managed-operation
     */
    public String listComponentsInHtml () 
    {
	return "Not implemented";
	// return convertComponentsToHtml( MBeanInterceptor.getMBeans() );
    }
    

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

	if ( toReboot==null ) {
	    return "FAILED: No package matches string " + appName;
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

    private static String convertComponentsToHtml( LinkedList comps )
    {
	String ret="";

	for (ListIterator i=comps.listIterator() ; i.hasNext() ; ) 
	{
	    Component comp = (Component) i.next();
	    ret += comp.toHtml() + "<BR>";
	}

	return ret;
    }


//      /**
//       * @jmx:managed-operation
//       */

//      public String showInjectionConsole() 
//      {
//  	String ret="<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0>";

//  	for (Enumeration ce = components.elements(); ce.hasMoreElements(); ) 
//  	{
//  	    Component comp = (Component) ce.nextElement();

//  	    ret += "<TR><TD><BR><FONT SIZE=+1><B>" + comp.shortName + "</B></FONT></TD></TR>";

//  	    for (ListIterator it = comp.getMethods() ; it.hasNext() ; ) 
//  	    {
//  		MethodSignature mSig = (MethodSignature) it.next();

//  		ret += "<TR><TD>\n";
//  		ret += "\n<FORM METHOD=\"POST\" ACTION=\"http://localhost:8080/jmx-console/HtmlAdaptor\">\n";
//  		ret += "<INPUT TYPE=\"HIDDEN\" NAME=\"action\" VALUE=\"invokeOp\">\n";
//  		ret += "<INPUT TYPE=\"HIDDEN\" NAME=\"name\" VALUE=\"RR:service=FaultInjectionService\">\n";
//  		// FIXME: the following is a hardcoded index; we should define this in ROCConfig.java
//  		ret += "<INPUT TYPE=\"HIDDEN\" NAME=\"methodIndex\" VALUE=\"0\">\n"; 
//  		ret += "<INPUT TYPE=\"HIDDEN\" NAME=\"arg\" VALUE=\"" + comp.shortName + "\">\n";
//  		ret += "<INPUT TYPE=\"HIDDEN\" NAME=\"arg\" VALUE=\"" + mSig.name + "\">";

//  		ret += "&nbsp;&nbsp;&nbsp;&nbsp;" + mSig.name + "()&nbsp;";

//  		ListIterator faultsI = mSig.faults.listIterator();
		
//  		// Generate a pull-down list if we have user-defined exceptions
//  		if ( faultsI.hasNext() ) 
//  		{
//  		    ret += "<SELECT NAME=\"arg\" SIZE=\"1\">\n";
//  		    while ( faultsI.hasNext() ) 
//  		    {
//  			String fName = (String) faultsI.next();
//  			ret += "<OPTION VALUE=\"" + fName + "\">" + fName + "\n";
//  		    }
//  		    ret += "<OPTION VALUE=\"java.lang.Exception\">java.lang.Exception";
//  		    ret += "</SELECT>";
//  		} 
//  		else {
//  		    ret += "<INPUT TYPE=\"HIDDEN\" NAME=\"arg\" VALUE=\"java.lang.Exception\">";
//  		    ret += " [java.lang.Exception] ";
//  		}

//  		// Submit button
//  		ret += "<INPUT TYPE=\"SUBMIT\" VALUE=\"Schedule\">\n</FORM>\n";
//  		ret += "</TD></TR>\n";
//  	    }
//  	}

//  	ret += "</TABLE>";
//  	return ret;
//      }

}
