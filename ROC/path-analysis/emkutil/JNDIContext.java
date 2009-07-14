package emkutil;
import java.util.Properties;
import javax.naming.InitialContext;
import javax.naming.Context;
import javax.naming.NamingException;

public class JNDIContext {

 // clustered version
 public static InitialContext getInitialContext()
  throws NamingException {
    Properties p = new Properties();
    p.put(Context.INITIAL_CONTEXT_FACTORY,
          "org.jnp.interfaces.NamingContextFactory");
    p.put(Context.URL_PKG_PREFIXES, "jboss.naming:org.jnp.interfaces");
    p.put(Context.PROVIDER_URL, "localhost:1100"); // HA-JNDI port.
    return new InitialContext( p );
  }

  /*
  // non-clustered version
  public static InitialContext getInitialContext() {
    return new InitialContext();
  }
  */

}
