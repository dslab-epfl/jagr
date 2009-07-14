import org.jnp.naming;
import org.jnp.server.Main;
import java.net.URL;
import org.apache.log4j.PropertyConfigurator;

/**
 * A command-line wrapper for the JBoss JNP JNDI server that allows 
 * the JNDI port to be specified.
 *
 * @author Mike Chen <mikechen@cs.berkeley.edu>
 * @version $Id: JnpServer.java,v 1.2 2003/02/09 07:48:12 candea Exp $
 */

public class JnpServer {

public static void usage() {
    System.err.println("usage: java JnpServer <JNDI port. Default is 1099>");
}

public static void main(String[] args) throws Exception {
    if (args.length > 1) {
	usage();
	System.exit(-1);
    }
    
    // Make sure the config file can be found
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    URL url = loader.getResource("log4j.properties");
    if( url == null )
	System.err.println("Failed to find log4j.properties");
    else
	PropertyConfigurator.configure(url);
    // Main server = new Main();
    Main server = new NamingService();
    
    // set the JNDI port
    if (args.length == 2)
    {
	server.setPort(Integer.parseInt(args[0]));
	server.setRmiPort(Integer.parseInt(args[1]));
    }
    server.start();
}
    
}
