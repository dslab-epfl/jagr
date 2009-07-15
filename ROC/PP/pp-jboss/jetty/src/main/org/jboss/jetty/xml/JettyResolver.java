/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */

//------------------------------------------------------------------------------

package org.jboss.jetty.xml;

//------------------------------------------------------------------------------

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import org.jboss.logging.Logger;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

//------------------------------------------------------------------------------

// I could use a Hashtable instead of HashMap, but the current usage
// of this class (populate/write and then use/read) makes this
// unecessary. If at a later date usage changes this should be
// revisited.

public class JettyResolver
  implements EntityResolver
{
  protected Logger _log = Logger.getLogger(JettyResolver.class);
  protected HashMap  _map=new HashMap();

  public
    JettyResolver()
  {
    // nothing
  }

  public InputSource
    resolveEntity (String publicId, String systemId)
  {
    if (_log.isDebugEnabled())
      _log.debug("resolving "+publicId+" : "+systemId);

    URL url=(URL)_map.get(publicId);

    if (url==null)
    {
      _log.warn("no resolution for "+publicId+" - are you using a supported JSDK version?");
    }
    else
    {
      if (_log.isDebugEnabled())
	_log.debug("resolved "+publicId+" : "+url);
      try
      {
	InputSource is=new InputSource(url.openConnection().getInputStream());
	return is;
      }
      catch (IOException e)
      {
	_log.error("bad resolution "+publicId+" : "+url, e);
      }
    }

    return null;
  }

  public void
    put(String key, URL val)
  {
    _map.put(key, val);
  }
}
