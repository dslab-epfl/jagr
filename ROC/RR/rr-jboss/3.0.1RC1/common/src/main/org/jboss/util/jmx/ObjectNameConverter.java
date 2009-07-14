/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.util.jmx;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * Converts forbidden characters in the value of an object name
 * to valid characters and back.
 * <br>
 * Character Conversion Table: (based on RFC 1738 style escapes<br>
 * '%'  =>  '%25'  <br>
 * '*'  =>  '%2a'  <br>
 * ','  =>  '%2c'  <br>
 * ':'  =>  '%3a'  <br>
 * '?'  =>  '%3f'  <br>
 * '='  =>  '%3d'  <br>
 * <br>Thanx to William Hoyle for mention this
 *
 * @author <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>
 * @author <a href="mailto:william.hoyle@jungledrum.co.nz">William Hoyle</a>
 * @version $Revision: 1.1.1.1 $
 */
public class ObjectNameConverter
{
   /**
    * Parses the given Object Name String representation and
    * replaces any invalid characters in property values with
    * valid characters.
    *
    * @param pObjectName String representing an Object Name
    *
    * @return Created Object Name with the given and parse string
    *         representation
    *
    * @throws MalformedObjectNameException If the given Object Name
    *         is not correct
    **/
   public static ObjectName convert( String pObjectName )
      throws MalformedObjectNameException
   {
      if( pObjectName == null ) {
         throw new MalformedObjectNameException( "null name" );
      }
      
      // REVIEW, is the following a hack?: It is in the spec for patterns
      if( pObjectName.length() == 0 ) {
         pObjectName = "*:*";
      }
      
      int lIndex = pObjectName.indexOf( ":" );
      if( lIndex < 0 ) {
         throw new MalformedObjectNameException( "missing domain" );
      }
      String lDomain = pObjectName.substring( 0, lIndex );
      if( ( lIndex + 1 ) < pObjectName.length() ) {
         Hashtable lProperties = initProperties( pObjectName.substring( lIndex + 1 ) );
         return new ObjectName( lDomain, lProperties );
      } else {
         throw new MalformedObjectNameException( "properties missing" );
      }
   }
   
   /**
    * Check the values of the properties and convert invalid characters
    *
    * @param pDomainName Name of the Domain
    * @param pProperites Hashtable containing the properties of the Object Name
    *
    * @return Created Object Name with the converted values
    *
    * @throws MalformedObjectNameException If the given Object Name
    *         is not correct
    **/
   public static ObjectName convert( String pDomainName, Hashtable pProperties )
      throws MalformedObjectNameException
   {
      if( pDomainName == null ) {
         throw new MalformedObjectNameException( "missing domain" );
      }
      if( pProperties == null || pProperties.size() == 0 ) {
         throw new MalformedObjectNameException(" null or empty properties" );
      }
      
      Hashtable lProperties = initProperties( pProperties, false );
      return new ObjectName( pDomainName, lProperties );
   }
   
   /**
    * Takes the properties from the given Object Name and convert
    * special characters back
    *
    * @param pObjectName Given Object Name
    *
    * @return Hashtable with the back converted properties in it
    **/
   public static Hashtable getProperties( ObjectName pObjectName )
   {
      return reverseProperties( pObjectName.getKeyPropertyList() );
   }
   
   /**
    * Takes the properties from the given Object Name and convert
    * special characters back
    *
    * @param pObjectName Given Object Name
    *
    * @return String with the original Object Name String representation
    **/
   public static String getString( ObjectName pObjectName )
   {
      return pObjectName.getDomain() + ":" + reverseString( pObjectName.getKeyPropertyList() );
   }
   
   private static String convertCharacters( String pValue, boolean pEncrypt ) {
      String lReturn = pValue;
      if( pEncrypt ) {
         int lIndex = lReturn.indexOf( "%" );
         while( lIndex >= 0 ) {
            lReturn = ( lIndex > 0 ? lReturn.substring( 0, lIndex ) : "" ) +
                      "%25" +
                      ( ( lIndex + 1 ) < lReturn.length() ? lReturn.substring( lIndex + 1 ) : "" );
            lIndex = lReturn.indexOf( "%", lIndex + 2 );
         }
         lIndex = lReturn.indexOf( "*" );
         while( lIndex >= 0 ) {
            lReturn = ( lIndex > 0 ? lReturn.substring( 0, lIndex ) : "" ) +
                      "%2a" +
                      ( ( lIndex + 1 ) < lReturn.length() ? lReturn.substring( lIndex + 1 ) : "" );
            lIndex = lReturn.indexOf( "*" );
         }
         lIndex = lReturn.indexOf( ":" );
         while( lIndex >= 0 ) {
            lReturn = ( lIndex > 0 ? lReturn.substring( 0, lIndex ) : "" ) +
                      "%3a" +
                      ( ( lIndex + 1 ) < lReturn.length() ? lReturn.substring( lIndex + 1 ) : "" );
            lIndex = lReturn.indexOf( ":" );
         }
         lIndex = lReturn.indexOf( "?" );
         while( lIndex >= 0 ) {
            lReturn = ( lIndex > 0 ? lReturn.substring( 0, lIndex ) : "" ) +
                      "%3f" +
                      ( ( lIndex + 1 ) < lReturn.length() ? lReturn.substring( lIndex + 1 ) : "" );
            lIndex = lReturn.indexOf( "?" );
         }
         lIndex = lReturn.indexOf( "=" );
         while( lIndex >= 0 ) {
            lReturn = ( lIndex > 0 ? lReturn.substring( 0, lIndex ) : "" ) +
                      "%3d" +
                      ( ( lIndex + 1 ) < lReturn.length() ? lReturn.substring( lIndex + 1 ) : "" );
            lIndex = lReturn.indexOf( "=" );
         }
         lIndex = lReturn.indexOf( "," );
         while( lIndex >= 0 ) {
            lReturn = ( lIndex > 0 ? lReturn.substring( 0, lIndex ) : "" ) +
                      "%2c" +
                      ( ( lIndex + 1 ) < lReturn.length() ? lReturn.substring( lIndex + 1 ) : "" );
            lIndex = lReturn.indexOf( "," );
         }
      } else {
         int lIndex = lReturn.indexOf( "%2a" );
         while( lIndex >= 0 ) {
            lReturn = ( lIndex > 0 ? lReturn.substring( 0, lIndex ) : "" ) +
                      "*" +
                      ( ( lIndex + 3 ) < lReturn.length() ? lReturn.substring( lIndex + 3 ) : "" );
            lIndex = lReturn.indexOf( "%2a" );
         }
         lIndex = lReturn.indexOf( "%3a" );
         while( lIndex >= 0 ) {
            lReturn = ( lIndex > 0 ? lReturn.substring( 0, lIndex ) : "" ) +
                      ":" +
                      ( ( lIndex + 3 ) < lReturn.length() ? lReturn.substring( lIndex + 3 ) : "" );
            lIndex = lReturn.indexOf( "%3a" );
         }
         lIndex = lReturn.indexOf( "%3f" );
         while( lIndex >= 0 ) {
            lReturn = ( lIndex > 0 ? lReturn.substring( 0, lIndex ) : "" ) +
                      "?" +
                      ( ( lIndex + 3 ) < lReturn.length() ? lReturn.substring( lIndex + 3 ) : "" );
            lIndex = lReturn.indexOf( "%3f" );
         }
         lIndex = lReturn.indexOf( "%3d" );
         while( lIndex >= 0 ) {
            lReturn = ( lIndex > 0 ? lReturn.substring( 0, lIndex ) : "" ) +
                      "=" +
                      ( ( lIndex + 3 ) < lReturn.length() ? lReturn.substring( lIndex + 3 ) : "" );
            lIndex = lReturn.indexOf( "%3d" );
         }
         lIndex = lReturn.indexOf( "%2c" );
         while( lIndex >= 0 ) {
            lReturn = ( lIndex > 0 ? lReturn.substring( 0, lIndex ) : "" ) +
                      "," +
                      ( ( lIndex + 3 ) < lReturn.length() ? lReturn.substring( lIndex + 3 ) : "" );
            lIndex = lReturn.indexOf( "%2c" );
         }
         lIndex = lReturn.indexOf( "%25" );
         while( lIndex >= 0 ) {
            lReturn = ( lIndex > 0 ? lReturn.substring( 0, lIndex ) : "" ) +
                      "%" +
                      ( ( lIndex + 3 ) < lReturn.length() ? lReturn.substring( lIndex + 3 ) : "" );
            lIndex = lReturn.indexOf( "%25" );
         }
      }
      return lReturn;
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
   private static Hashtable initProperties(String properties) throws MalformedObjectNameException
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
      boolean lPattern = false;
      while (tokenizer.hasMoreTokens())
      {
         String chunk = tokenizer.nextToken();
         
         if (chunk.equals("*"))
         {
            lPattern = true;
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
      
      return initProperties( ptable, lPattern );
   }

   /**
    * validates incoming properties hashtable
    *
    * builds canonical string
    *
    * precomputes the hashcode
    *
    * ATTENTION: Originally taken from the JBossMQ ObjectName Implementation
    * but adjusted to our need here.
    */
   private static Hashtable initProperties(Hashtable properties, boolean pPattern ) throws MalformedObjectNameException
   {
      if (null == properties || (!pPattern && properties.size() < 1))
      {
         throw new MalformedObjectNameException("null or empty properties");
      }
      
      Iterator it = properties.keySet().iterator();
      Hashtable lReturn = new Hashtable( properties.size() );
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
         
         // Search for invalid characters and replace them
         String lConverted = convertCharacters( val, true );
         if( isIllegalKeyOrValue( key ) )
         {
            throw new MalformedObjectNameException("malformed key: " + key);
         }
         lReturn.put( key, lConverted );
      }
      return lReturn;
   }
   
   /**
    * returns true if the key or value string is zero length or contains illegal characters
    *
    * ATTENTION: Taken from the JBossMQ ObjectName Implementation
    */
   private static boolean isIllegalKeyOrValue(String keyOrValue)
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
   
   private static Hashtable reverseProperties( Hashtable pProperties ) {
      Hashtable lReturn = new Hashtable( pProperties.size() );
      Iterator i = pProperties.keySet().iterator();
      while( i.hasNext() ) {
         String lKey = (String) i.next();
         String lValue = (String) pProperties.get( lKey );
         String lConverted = convertCharacters( lValue, false );
         lReturn.put( lKey, lConverted );
      }
      return lReturn;
   }
   
   private static String reverseString( Hashtable pProperties ) {
      StringBuffer lReturn = new StringBuffer();
      Iterator i = pProperties.keySet().iterator();
      while( i.hasNext() ) {
         String lKey = (String) i.next();
         String lValue = (String) pProperties.get( lKey );
         String lConverted = convertCharacters( lValue, false );
         if( lReturn.length() > 0 ) {
            lReturn.append( "," );
         }
         lReturn.append( lKey );
         lReturn.append( "=" );
         lReturn.append( lConverted );
      }
      return lReturn.toString();
   }
}
