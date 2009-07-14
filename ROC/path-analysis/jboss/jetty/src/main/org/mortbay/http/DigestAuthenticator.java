// ========================================================================
// Copyright (c) 2002 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: DigestAuthenticator.java,v 1.1.1.1 2002/11/16 03:16:49 mikechen Exp $
// ========================================================================

package org.mortbay.http;

import java.io.IOException;
import org.mortbay.http.SecurityConstraint.Authenticator;
import java.security.Principal;
import org.mortbay.util.Credential;
import org.mortbay.util.Code;
import org.mortbay.util.StringUtil;
import org.mortbay.util.TypeUtil;
import org.mortbay.util.QuotedStringTokenizer;
import java.security.MessageDigest;

/* ------------------------------------------------------------ */
/** DIGEST authentication.
 *
 * @version $id:$
 * @author Greg Wilkins (gregw)
 */
public class DigestAuthenticator implements Authenticator
{
    /* ------------------------------------------------------------ */
    /** 
     * @return UserPrinciple if authenticated or null if not. If
     * Authentication fails, then the authenticator may have committed
     * the response as an auth challenge or redirect.
     * @exception IOException 
     */
    public UserPrincipal authenticated(UserRealm realm,
                                       String pathInContext,
                                       HttpRequest request,
                                       HttpResponse response)
        throws IOException
    {
        
        if (realm==null)
        {
            response.sendError(HttpResponse.__500_Internal_Server_Error,
                               "Realm Not Configured");
            return null;
        }

        // Get the user if we can
        UserPrincipal user=null;
        String credentials = request.getField(HttpFields.__Authorization);
        
        if (credentials!=null )
        {
            Code.debug("Credentials: "+credentials);
            QuotedStringTokenizer tokenizer = new QuotedStringTokenizer(credentials,
                                                                        "=, ",
                                                                        true,
                                                                        false);
            Digest digest=new Digest(request.getMethod());
            String last=null;
            String name=null;
            
            while (tokenizer.hasMoreTokens())
            {
                String tok = tokenizer.nextToken();
                char c=(tok.length()==1)?tok.charAt(0):'\0';

                switch (c)
                {
                  case '=':
                      name=last;
                      last=tok;
                      break;
                  case ',':
                      name=null;
                  case ' ':
                      break;

                  default:
                      last=tok;
                      if (name!=null)
                      {
                          if ("username".equalsIgnoreCase(name))
                              digest.username=tok;
                          else if ("realm".equalsIgnoreCase(name))
                              digest.realm=tok;
                          else if ("nonce".equalsIgnoreCase(name))
                              digest.nonce=tok;
                          else if ("nc".equalsIgnoreCase(name))
                              digest.nc=tok;
                          else if ("cnonce".equalsIgnoreCase(name))
                              digest.cnonce=tok;
                          else if ("qop".equalsIgnoreCase(name))
                              digest.qop=tok;
                          else if ("uri".equalsIgnoreCase(name))
                              digest.uri=tok;
                          else if ("response".equalsIgnoreCase(name))
                              digest.response=tok;
                          break;
                      }
                }
            }            

            user = realm.authenticate(digest.username,digest,request);
            if (user!=null)
            {
                request.setAuthType(SecurityConstraint.__DIGEST_AUTH);
                request.setAuthUser(digest.username);
                request.setUserPrincipal(user);                
            }
            else
                Code.warning("AUTH FAILURE: user "+digest.username);
        }

        // Challenge if we have no user
        if (user==null)
            sendChallenge(realm,request,response);
        
        return user;
    }
    
    /* ------------------------------------------------------------ */
    public String getAuthMethod()
    {
        return SecurityConstraint.__DIGEST_AUTH;
    }
    
    /* ------------------------------------------------------------ */
    public void sendChallenge(UserRealm realm,
                              HttpRequest request,
                              HttpResponse response)
        throws IOException
    {
        response.setField(HttpFields.__WwwAuthenticate,
                          "digest realm=\""+realm.getName()+
                          "\" domain=\""+
                          "/"+ // request.getContextPath()+
                          "\" nonce=\""+
                          Long.toString(request.getTimeStamp(),27)+
                          "\""
                          );
        response.sendError(HttpResponse.__401_Unauthorized);
    }


    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    private static class Digest extends Credential
    {
        private static MessageDigest __md;

        String method=null;
        String username = null;
        String realm = null;
        String nonce = null;
        String nc = null;
        String cnonce = null;
        String qop = null;
        String uri = null;
        String response=null;
        
        /* ------------------------------------------------------------ */
        Digest(String m)
        {
            method=m;
        }
        
        /* ------------------------------------------------------------ */
        public boolean check(Object credentials)
        {
            String password=(credentials instanceof String)
                ?(String)credentials
                :credentials.toString();
            
            try{
                synchronized(DigestAuthenticator.Digest.class)
                {
                    if (__md==null)
                        __md = MessageDigest.getInstance("MD5");
                
                    // calc A1 digest
                    __md.reset();
                    __md.update(username.getBytes(StringUtil.__ISO_8859_1));
                    __md.update((byte)':');
                    __md.update(realm.getBytes(StringUtil.__ISO_8859_1));
                    __md.update((byte)':');
                    __md.update(password.getBytes(StringUtil.__ISO_8859_1));
                    byte[] ha1=__md.digest();
                    
                    // calc A2 digest
                    __md.reset();
                    __md.update(method.getBytes(StringUtil.__ISO_8859_1));
                    __md.update((byte)':');
                    __md.update(uri.getBytes(StringUtil.__ISO_8859_1));
                    byte[] ha2=__md.digest();
                    
                    // calc digest
                    __md.update(TypeUtil.toString(ha1,16).getBytes(StringUtil.__ISO_8859_1));
                    __md.update((byte)':');
                    __md.update(nonce.getBytes(StringUtil.__ISO_8859_1));
                    __md.update((byte)':');
                    __md.update(TypeUtil.toString(ha2,16).getBytes(StringUtil.__ISO_8859_1));
                    byte[] digest=__md.digest();
                    
                    // check digest
                    return (TypeUtil.toString(digest,16).equalsIgnoreCase(response));
                }
            }
            catch (Exception e)
            {Code.warning(e);}

            return false;
        }

        public String toString()
        {
            return username+","+response;
        }
        
    }
}
    
