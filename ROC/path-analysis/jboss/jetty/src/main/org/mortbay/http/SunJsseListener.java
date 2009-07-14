// ========================================================================
// Copyright (c) 2000 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: SunJsseListener.java,v 1.1.1.1 2002/11/16 03:16:49 mikechen Exp $
// ========================================================================

package org.mortbay.http;

import com.sun.net.ssl.KeyManager;
import com.sun.net.ssl.KeyManagerFactory;
import com.sun.net.ssl.SSLContext;
import com.sun.net.ssl.TrustManager;
import com.sun.net.ssl.TrustManagerFactory;
import com.sun.net.ssl.internal.ssl.Provider;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.Security;
import javax.net.ssl.SSLServerSocketFactory;
import org.mortbay.util.InetAddrPort;
import org.mortbay.util.Log;
import org.mortbay.util.Password;




/* ------------------------------------------------------------ */
/** SSL Socket Listener for Sun's JSSE.
 *
 * This specialization of JsseListener is an specific listener
 * using the Sun reference implementation.
 *
 * This is heavily based on the work from Court Demas, which in
 * turn is based on the work from Forge Research.
 *
 * @version $Id: SunJsseListener.java,v 1.1.1.1 2002/11/16 03:16:49 mikechen Exp $
 * @author Greg Wilkins (gregw@mortbay.com)
 * @author Court Demas (court@kiwiconsulting.com)
 * @author Forge Research Pty Ltd  ACN 003 491 576
 **/
public class SunJsseListener extends JsseListener
{
    private String _keystore=DEFAULT_KEYSTORE ;
    private transient Password _password;
    private transient Password _keypassword;
    private String _keystore_type = DEFAULT_KEYSTORE_TYPE;
    private String _keystore_provider_name = DEFAULT_KEYSTORE_PROVIDER_NAME;
    private String _keystore_provider_class = DEFAULT_KEYSTORE_PROVIDER_CLASS;

    /* ------------------------------------------------------------ */
    static
    {
        Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
    }

    /* ------------------------------------------------------------ */
    public void setKeystore(String keystore)
    {
        _keystore = keystore;
    }
    
    /* ------------------------------------------------------------ */
    public String getKeystore()
    {
        return _keystore;
    }
    
    /* ------------------------------------------------------------ */
    public void setPassword(String password)
    {
        _password = Password.getPassword(PASSWORD_PROPERTY,password,null);
    }

    /* ------------------------------------------------------------ */
    public void setKeyPassword(String password)
    {
        _keypassword = Password.getPassword(KEYPASSWORD_PROPERTY,password,null);
    }
    
    
    /* ------------------------------------------------------------ */
    public void setKeystoreType(String keystore_type)
    {
        _keystore_type = keystore_type;
    }
    
    /* ------------------------------------------------------------ */
    public String getKeystoreType()
    {
        return _keystore_type;
    }

    /* ------------------------------------------------------------ */
    public void setKeystoreProviderName(String name)
    {
        _keystore_provider_name = name;
    }

    /* ------------------------------------------------------------ */
    public String getKeystoreProviderName()
    {
        return _keystore_provider_name;
    }
    
    /* ------------------------------------------------------------ */
    public String getKeystoreProviderClass()
    {
        return _keystore_provider_class;
    }
    
    /* ------------------------------------------------------------ */
    public void setKeystoreProviderClass(String classname)
    {
        _keystore_provider_class = classname;
    }
    
    /* ------------------------------------------------------------ */
    /** Constructor. 
     * @exception IOException 
     */
    public SunJsseListener()
        throws IOException
    {
        super();
    }

    /* ------------------------------------------------------------ */
    /** Constructor. 
     * @param p_address 
     * @param p_server 
     * @exception IOException 
     */
    public SunJsseListener(InetAddrPort p_address)
        throws IOException
    {
        super( p_address);
    }

    
    /* ------------------------------------------------------------ */
    /* 
     * @return 
     * @exception Exception 
     */
    protected SSLServerSocketFactory createFactory()
        throws Exception
    {
        _keystore = System.getProperty( KEYSTORE_PROPERTY,_keystore);
        
        Log.event(KEYSTORE_PROPERTY+"="+_keystore);

        if (_password==null)
            _password = Password.getPassword(PASSWORD_PROPERTY,null,null);
        Log.event(PASSWORD_PROPERTY+"="+_password.toStarString());
        
        if (_keypassword==null)
            _keypassword = Password.getPassword(KEYPASSWORD_PROPERTY,
                                                null,
                                                _password.toString());
        Log.event(KEYPASSWORD_PROPERTY+"="+_keypassword.toStarString());


        KeyStore ks = null;

        Log.event(KEYSTORE_TYPE_PROPERTY+"="+_keystore_type);
        
        if (_keystore_provider_class != null) {
            // find provider.
            // avoid creating another instance if already installed in Security.
            java.security.Provider[] installed_providers = Security.getProviders();
            java.security.Provider myprovider = null;
            for (int i=0; i < installed_providers.length; i++) {
                if (installed_providers[i].getClass().getName().equals(_keystore_provider_class)) {
                    myprovider = installed_providers[i];
                    break;
                }
            }
            if (myprovider == null) {
                // not installed yet, create instance and add it
                myprovider = (java.security.Provider) Class.forName(_keystore_provider_class).newInstance();
                Security.addProvider(myprovider);
            }
            Log.event(KEYSTORE_PROVIDER_CLASS_PROPERTY+"="+_keystore_provider_class);
            ks = KeyStore.getInstance(_keystore_type,myprovider.getName());
        } else if (_keystore_provider_name != null) {
            Log.event(KEYSTORE_PROVIDER_NAME_PROPERTY+"="+_keystore_provider_name);
            ks = KeyStore.getInstance(_keystore_type,_keystore_provider_name);
        } else {
            ks = KeyStore.getInstance(_keystore_type);
            Log.event(KEYSTORE_PROVIDER_NAME_PROPERTY+"=[DEFAULT]");
        }
        
        ks.load( new FileInputStream( new File( _keystore ) ),
                 _password.toString().toCharArray());
        
        KeyManagerFactory km = KeyManagerFactory.getInstance( "SunX509","SunJSSE"); 
        km.init( ks, _keypassword.toString().toCharArray() );
        KeyManager[] kma = km.getKeyManagers();                        
        
        TrustManagerFactory tm = TrustManagerFactory.getInstance("SunX509","SunJSSE");
        tm.init( ks ); 
        TrustManager[] tma = tm.getTrustManagers();
        
        SSLContext sslc = SSLContext.getInstance( "SSL" ); 
        sslc.init( kma, tma, SecureRandom.getInstance("SHA1PRNG"));
        
        SSLServerSocketFactory ssfc = sslc.getServerSocketFactory();
        Log.event("SSLServerSocketFactory="+ssfc);
        return ssfc;
    }
}



