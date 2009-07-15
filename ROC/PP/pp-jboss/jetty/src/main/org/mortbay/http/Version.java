// ========================================================================
// Copyright (c) 1999-2002 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: Version.java,v 1.1.1.1 2003/03/07 08:26:05 emrek Exp $
// ========================================================================

package org.mortbay.http;
import org.mortbay.util.Code;

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
    
    public static String __Version="Jetty/4.2";
    public static String __VersionImpl=__Version+".x";
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
        if (p!=null && p.getImplementationVersion()!=null)
            __VersionImpl="Jetty/"+p.getImplementationVersion();
        
        if (!__paranoid)
        {
            __VersionDetail=__VersionImpl+" ("+
                System.getProperty("os.name")+" "+
                System.getProperty("os.version")+" "+
                System.getProperty("os.arch")+")";

            __ServletEngine=__VersionImpl+" (Servlet 2.3; JSP 1.2; java "+
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

    public static void main(String[] arg)
    {
        System.out.println("org.mortbay.http.Version="+__Version);
        System.out.println("org.mortbay.http.VersionImpl="+__VersionImpl);
        System.out.println("org.mortbay.http.VersionDetail="+__VersionDetail);
        System.out.println("org.mortbay.http.ServletEngine="+__ServletEngine);
    }
}

