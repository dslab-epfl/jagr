package org.jboss.admin;

// standard imports
import java.io.IOException;
import javax.naming.InitialContext;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

// JBoss Admin imports
import org.jboss.admin.interfaces.AdminServer;
import org.jboss.admin.interfaces.AdminServerHome;


/**
 * ...
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>
 * @version $Revision: 1.1.1.1 $
 */    
public class AdminLogin implements CallbackHandler {

    char[] password;
    String user;
    
/*
 *************************************************************************
 *
 *      IMPLEMENTS  CALLBACK_HANDLER  INTERFACE
 *
 *************************************************************************
 */     
    public void handle(Callback[] callbacks) 
            throws IOException, UnsupportedCallbackException {
    
        for (int i = 0; i < callbacks.length; ++i) {

            if (callbacks[i] instanceof NameCallback) {
                NameCallback callback = (NameCallback)callbacks[i];
                callback.setName(user);
            }
            
            if (callbacks[i] instanceof PasswordCallback) {
                PasswordCallback callback = (PasswordCallback)callbacks[i];
                callback.setPassword(password);
            }
        }        
    }
    
/*
 *************************************************************************
 *
 *      MAIN METHOD
 *
 *************************************************************************
 */     
    public static void main(String[] args) {
        try {
            AdminLogin login = new AdminLogin();
            login.user = "jboss";
            login.password = "jboss".toCharArray();
        
            LoginContext loginCtx = new LoginContext("Admin Login", login);
            loginCtx.login();
            
            InitialContext initCtx = new InitialContext();
            Object ref = initCtx.lookup("AdminServer");
            
            AdminServerHome home = (AdminServerHome)ref;
            AdminServer server = home.create();
            
            server.isAdminServerEnabled();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    } 
}
