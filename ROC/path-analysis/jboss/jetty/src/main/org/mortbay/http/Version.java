// ========================================================================
// Copyright (c) 1999-2002 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: Version.java,v 1.1.1.1 2002/11/16 03:16:49 mikechen Exp $
// ========================================================================

package org.mortbay.http;
import org.mortbay.util.Code;
import org.mortbay.util.Log;


/* ------------------------------------------------------------ */
/** Jetty version.
 *
 * This class sets the version data returned in the Server and
 * Servlet-Container headers.   If the
 * java.org.mortbay.http.Version.paranoid System property is set to
 * true, then this information is suppressed.
 *
 * @version $Revision: 1.1.1.1 $
 * @author Greg Wilkins (gregw)
 */
public class Version
{
    public static boolean __paranoid = 
        Boolean.getBoolean("org.mortbay.http.Version.paranoid");
    
    public static String __Version="Jetty/4.1";
    public static String __VersionImpl=__Version;
    public static String __VersionDetail="Unknown";
    public static String __ServletEngine="Unknown (Servlet 2.3; JSP 1.2)";

    static
    {
        if (Boolean.getBoolean("java.org.mortbay.http.Version.paranoid"))
        {
            Code.warning("OLD property set. Use org.mortbay.http.Version.paranoid");
            __paranoid=true;
        }
        
        Package p = Version.class.getPackage();
        if (p!=null)
        {
            __Version="Jetty/"+p.getImplementationVersion();
        }
        
        if (!__paranoid)
        {
            __VersionDetail=__Version+" ("+
                System.getProperty("os.name")+" "+
                System.getProperty("os.version")+" "+
                System.getProperty("os.arch")+")";

            __ServletEngine=__Version+" (Servlet 2.3; JSP 1.2; java "+
                System.getProperty("java.version")+")";
        }
    }
    
    public static String __notice = "This application is using software from the "+
        __Version+
        " HTTP server and servlet container.\nJetty is Copyright (c) Mort Bay Consulting Pty. Ltd. (Australia) and others.\nJetty is distributed under an open source license.\nThe license and standard release of Jetty are available from http://jetty.mortbay.org\n";

    static
    {
        // System.err.println(__notice);
    }
    
}

