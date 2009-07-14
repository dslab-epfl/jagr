/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.management;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * Object name represents the MBean reference.
 *
 * @see javax.management.MBeanServer
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @author  <a href="mailto:trevor@protocool.com">Trevor Squires</a>.
 * @version $Revision: 1.1.1.1 $
 *
 * <p><b>Revisions:</b>
 * <p><b>20020521 Adrian Brock:</b>
 * <ul>
 * <li>Allow *,* in the hashtable properties to signify a property pattern
 * </ul>
 *
 */
public class ObjectName implements java.io.Serializable
{

   // Attributes ----------------------------------------------------
   private boolean hasPattern = false;
   private boolean hasPropertyPattern = false;
   private Hashtable propertiesHash = null;

   private String domain = null;
   private String kProps = null;
   private String ckProps = null;

   private int hash;

   // Constructors --------------------------------------------------
   public ObjectName(String name) throws MalformedObjectNameException
   {
      if (name == null)
         throw new MalformedObjectNameException("null name");

      // REVIEW, is the following a hack?: It is in the spec for patterns
      if (name.length() == 0)
         name = "*:*";

      int domainSep = name.indexOf(':');

      if (-1 == domainSep)
         throw new MalformedObjectNameException("missing domain");

      initDomain(name.substring(0, domainSep));
      initProperties(name.substring(domainSep + 1));
   }

   public ObjectName(String domain, String key, String value)
      throws MalformedObjectNameException
   {
      initDomain(domain);

      if (null == key || null == value)
      {
         throw new MalformedObjectNameException("properties key or value cannot be null");
      }

      Hashtable ptable = new Hashtable();
      ptable.put(key, value);

      initProperties(ptable);

      this.kProps = key + "=" + value;
   }

   public ObjectName(String domain, Hashtable table) throws MalformedObjectNameException
   {
      if (null == table || table.size() < 1)
      {
         throw new MalformedObjectNameException("null or empty properties");
      }

      initDomain(domain);
      initProperties((Hashtable) table.clone());

      this.kProps = ckProps;
   }

   // Public ------------------------------------------------------
   public boolean equals(Object object)
   {
      if (object == this)
      {
         return true;
      }

      if (object instanceof ObjectName)
      {
         ObjectName oname = (ObjectName) object;
         return (oname.hash == hash && domain.equals(oname.domain) &&
            ckProps.equals(oname.ckProps));
      }

      return false;
   }

   public int hashCode()
   {
      return hash;
   }

   public String toString()
   {
      return this.domain + ":" + kProps;
   }

   public boolean isPattern()
   {
      return hasPattern;
   }

   public String getCanonicalName()
   {
      return this.domain + ":" + ckProps;
   }

   public String getDomain()
   {
      return domain;
   }

   public String getKeyProperty(String property)
   {
      return (String) propertiesHash.get(property);
   }

   public Hashtable getKeyPropertyList()
   {
      return (Hashtable) propertiesHash.clone();
   }

   public String getKeyPropertyListString()
   {
      return kProps;
   }

   public String getCanonicalKeyPropertyListString()
   {
      return ckProps;
   }

   public boolean isPropertyPattern()
   {
      // FIXME: patterns not implemented
      return hasPropertyPattern;
   }

   // Private -----------------------------------------------------

   /**
    * checks for domain patterns and illegal characters
    */
   private void initDomain(String dstring) throws MalformedObjectNameException
   {
      if (null == dstring)
      {
         throw new MalformedObjectNameException("null domain");
      }

      if (isIllegalDomain(dstring))
      {
         throw new MalformedObjectNameException("domain contains illegal characters");
      }

      if (dstring.indexOf('*') > -1 || dstring.indexOf('?') > -1)
      {
         this.hasPattern = true;
      }

      this.domain = dstring;
   }

   /**
    * takes the properties string and breaks it up into key/value pairs for
    * insertion into a newly created hashtable.
    *
    * minimal validation is performed so that it doesn't blow up when
    * constructing the kvp strings.
    *
    * checks for duplicate keys
    *
    * detects property patterns
    *
    */
   private void initProperties(String properties) throws MalformedObjectNameException
   {
      if (null == properties || properties.length() < 1)
      {
         throw new MalformedObjectNameException("null or empty properties");
      }

      // The StringTokenizer below hides malformations such as ',,' in the
      // properties string or ',' as the first or last character.
      // Rather than asking for tokens and building a state machine I'll
      // just manually check for those 3 scenarios.

      if (properties.startsWith(",") || properties.endsWith(",") || properties.indexOf(",,") != -1)
      {
         throw new MalformedObjectNameException("empty key/value pair in properties string");
      }

      Hashtable ptable = new Hashtable();

      StringTokenizer tokenizer = new StringTokenizer(properties, ",");
      while (tokenizer.hasMoreTokens())
      {
         String chunk = tokenizer.nextToken();

         if (chunk.equals("*"))
         {
            this.hasPropertyPattern = true;
            this.hasPattern = true;
            continue;
         }

         int keylen = chunk.length();
         int eqpos = chunk.indexOf('=');

         // test below: as in '=value' or 'key=' so that our substrings don't blow up
         if (eqpos < 1 || (keylen == eqpos + 1))
         {
            throw new MalformedObjectNameException("malformed key/value pair: " + chunk);
         }

         String key = chunk.substring(0, eqpos);
         if (ptable.containsKey(key))
         {
            throw new MalformedObjectNameException("duplicate key: " + key);
         }

         ptable.put(key, chunk.substring(eqpos + 1, keylen));
      }

      initProperties(ptable);
      this.kProps = properties;
   }

   /**
    * validates incoming properties hashtable
    *
    * builds canonical string
    *
    * precomputes the hashcode
    */
   private void initProperties(Hashtable properties) throws MalformedObjectNameException
   {
      if (null == properties || (!this.hasPropertyPattern && properties.size() < 1))
      {
         throw new MalformedObjectNameException("null or empty properties");
      }

      Iterator it = properties.keySet().iterator();
      ArrayList list = new ArrayList();

      while (it.hasNext())
      {
         String key = null;
         try
         {
            key = (String) it.next();
         }
         catch (ClassCastException e)
         {
            throw new MalformedObjectNameException("key is not a string");
         }

         String val = null;
         try
         {
            val = (String) properties.get(key);
         }
         catch (ClassCastException e)
         {
            throw new MalformedObjectNameException("value is not a string");
         }

         if (key.equals("*") && val.equals("*"))
         {
            it.remove();
            this.hasPropertyPattern = true;
            this.hasPattern = true;
            continue;
         }
         if (isIllegalKeyOrValue(key) || isIllegalKeyOrValue(val))
         {
            throw new MalformedObjectNameException("malformed key/value pair: " + key + "=" + val);
         }
         list.add(new String(key + "=" + val));
      }

      Collections.sort(list);
      StringBuffer strBuffer = new StringBuffer();

      it = list.iterator();
      while (it.hasNext())
      {
         strBuffer.append(it.next());
         if (it.hasNext())
         {
            strBuffer.append(',');
         }
      }

      if (this.hasPropertyPattern)
      {
         if (properties.size() > 0)
         {
            strBuffer.append(",*");
         }
         else
         {
            strBuffer.append("*");
         }
      }

      this.propertiesHash = properties;
      this.ckProps = strBuffer.toString();
      this.hash = getCanonicalName().hashCode();
   }

   /**
    * returns true if the key or value string is zero length or contains illegal characters
    */
   private boolean isIllegalKeyOrValue(String keyOrValue)
   {
      char[] chars = keyOrValue.toCharArray();

      if (chars.length == 0)
      {
         return true;
      }

      for (int i = 0; i < chars.length; i++)
      {
         switch (chars[i])
         {
            case ':':
            case ',':
            case '=':
            case '*':
            case '?':
               return true;
         }
      }

      return false;
   }

   /**
    * returns true if the domain contains illegal characters
    */
   private boolean isIllegalDomain(String dom)
   {
      char[] chars = dom.toCharArray();

      for (int i = 0; i < chars.length; i++)
      {
         switch (chars[i])
         {
            case ':':
            case ',':
            case '=':
               return true;
         }
      }

      return false;
   }
}
