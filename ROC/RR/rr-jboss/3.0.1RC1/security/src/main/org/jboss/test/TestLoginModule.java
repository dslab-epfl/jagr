package org.jboss.test;

import java.security.*;
import java.util.*;
import javax.security.auth.*;
import javax.security.auth.callback.*;
import javax.security.auth.login.*;
import javax.security.auth.spi.*;

public class TestLoginModule implements LoginModule
{
        Subject subject;
        String principal;

        public TestLoginModule()
        {
        }

        public void initialize(Subject subject, CallbackHandler handler, Map sharedState, Map options)
        {
            this.subject = subject;
            principal = (String) options.get("principal");
            if( principal == null )
                principal = "guest";
        }

        public boolean login() throws LoginException
        {
            subject.getPrincipals().add(new SimplePrincipal(principal));
            return true;
        }

        public boolean commit() throws LoginException
        {
            return true;
        }

        public boolean abort() throws LoginException
        {
            return true;
        }

        public boolean logout() throws LoginException
        {
            return true;
        }

    public static class SimplePrincipal implements Principal
    {
        String name;
        public SimplePrincipal(String name)
        {
            this.name = name;
        }
        public String getName()
        {
            return name;
        }
    }
}
