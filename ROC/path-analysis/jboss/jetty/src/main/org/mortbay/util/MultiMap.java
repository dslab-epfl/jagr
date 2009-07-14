// ========================================================================
// Copyright (c) 1999 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: MultiMap.java,v 1.1.1.1 2002/11/16 03:16:49 mikechen Exp $
// ========================================================================

package org.mortbay.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/* ------------------------------------------------------------ */
/** A multi valued Map.
 * This Map specializes HashMap and provides methods
 * that operate on multi valued items. 
 * <P>
 * Implemented as a map of LazyList values
 *
 * @see LazyList
 * @version $Id: MultiMap.java,v 1.1.1.1 2002/11/16 03:16:49 mikechen Exp $
 * @author Greg Wilkins (gregw)
 */
public class MultiMap extends HashMap
    implements Cloneable
{
    /* ------------------------------------------------------------ */
    /** Constructor. 
     */
    public MultiMap()
    {}
    
    /* ------------------------------------------------------------ */
    /** Constructor. 
     * @param size Capacity of the map
     */
    public MultiMap(int size)
    {
        super(size);
    }
    
    /* ------------------------------------------------------------ */
    /** Constructor. 
     * @param map 
     */
    public MultiMap(Map map)
    {
        super((map.size()*3)/2);
        putAll(map);
    }
    
    /* ------------------------------------------------------------ */
    /** Get multiple values.
     * Single valued entries are converted to singleton lists.
     * @param name The entry key. 
     * @return Unmodifieable List of values.
     */
    public List getValues(Object name)
    {
        return LazyList.getList((LazyList)super.get(name),true);
    }
    
    /* ------------------------------------------------------------ */
    /** Get a value from a multiple value.
     * If the value is not a multivalue, then index 0 retrieves the
     * value or null.
     * @param name The entry key.
     * @param i Index of element to get.
     * @return Unmodifieable List of values.
     */
    public Object getValue(Object name,int i)
    {
        LazyList l=(LazyList)super.get(name);
        if (i==0 && LazyList.size(l)==0)
            return null;
        return LazyList.get(l,i);
    }
    
    
    /* ------------------------------------------------------------ */
    /** Get value as String.
     * Single valued items are converted to a String with the toString()
     * Object method. Multi valued entries are converted to a comma separated
     * List.  No quoting of commas within values is performed.
     * @param name The entry key. 
     * @return String value.
     */
    public String getString(Object name)
    {
        LazyList l=(LazyList)super.get(name);
        switch(LazyList.size(l))
        {
          case 0:
              return null;
          case 1:
              Object o=l.get(0);
              return o==null?null:o.toString();
          default:
              StringBuffer values=new StringBuffer(128);
              synchronized(values)
              {
                  for (int i=0; i<l.size(); i++)              
                  {
                      Object e=l.get(i);
                      if (e!=null)
                      {
                          if (values.length()>0)
                              values.append(',');
                          values.append(e.toString());
                      }
                  }   
                  return values.toString();
              }
        }
    }
    
    /* ------------------------------------------------------------ */
    public Object get(Object name) 
    {
        LazyList l=(LazyList)super.get(name);
        switch(LazyList.size(l))
        {
          case 0:
              return null;
          case 1:
              Object o=l.get(0);
              return o;
          default:
              return LazyList.getList(l,true);
        }
    }
    
    /* ------------------------------------------------------------ */
    /** Put and entry into the map.
     * @param name The entry key. 
     * @param value The entry value.
     * @return The previous value or null.
     */
    public Object put(Object name, Object value) 
    {
        return super.put(name,LazyList.add(null,value));
    }

    /* ------------------------------------------------------------ */
    /** Put multi valued entry.
     * @param name The entry key. 
     * @param value The entry multiple values.
     * @return The previous value or null.
     */
    public Object putValues(Object name, List values) 
    {
        return super.put(name,LazyList.add(null,values));
    }
    
    /* ------------------------------------------------------------ */
    /** Put multi valued entry.
     * @param name The entry key. 
     * @param value The entry multiple values.
     * @return The previous value or null.
     */
    public Object putValues(Object name, String[] values) 
    {
        return putValues(name,LazyList.add(null,Arrays.asList(values)));
    }
    
    
    /* ------------------------------------------------------------ */
    /** Add value to multi valued entry.
     * If the entry is single valued, it is converted to the first
     * value of a multi valued entry.
     * @param name The entry key. 
     * @param value The entry value.
     */
    public void add(Object name, Object value) 
    {
        LazyList lo = (LazyList)super.get(name);
        LazyList ln = LazyList.add(lo,value);
        if (lo!=ln)
            super.put(name,ln);
    }

    /* ------------------------------------------------------------ */
    /** Add values to multi valued entry.
     * If the entry is single valued, it is converted to the first
     * value of a multi valued entry.
     * @param name The entry key. 
     * @param value The entry multiple values.
     */
    public void addValues(Object name, List values) 
    {
        LazyList lo = (LazyList)super.get(name);
        LazyList ln = LazyList.add(lo,values);
        if (lo!=ln)
            super.put(name,ln);
    }
    
    /* ------------------------------------------------------------ */
    /** Add values to multi valued entry.
     * If the entry is single valued, it is converted to the first
     * value of a multi valued entry.
     * @param name The entry key. 
     * @param value The entry multiple values.
     */
    public void addValues(Object name, String[] values) 
    {
        LazyList lo = (LazyList)super.get(name);
        LazyList ln = LazyList.add(lo,Arrays.asList(values));
        if (lo!=ln)
            super.put(name,ln);
    }
    
    /* ------------------------------------------------------------ */
    /** Remove value.
     * @param name The entry key. 
     * @param value The entry value. 
     * @return true if it was removed.
     */
    public boolean removeValue(Object name,Object value)
    {
        LazyList lo = (LazyList)super.get(name);
        LazyList ln=lo;
        int s=LazyList.size(lo);
        if (s>0)
            ln=LazyList.remove(lo,value);
        if (lo!=ln)
            super.put(name,value);
        return LazyList.size(ln)!=s;
    }
    
    /* ------------------------------------------------------------ */
    /** Put all contents of map.
     * @param m Map
     */
    public void putAll(Map m)
    {
        boolean multi = (m instanceof MultiMap);
        Iterator i = m.entrySet().iterator();
        while(i.hasNext())
        {
            Map.Entry entry =
                (Map.Entry)i.next();

            Object value=entry.getValue();
            if (multi)
                super.put(entry.getKey(),value==null?null:((LazyList)value).clone());
            else
                put(entry.getKey(),value);
        }
    }

    /* ------------------------------------------------------------ */
    /** 
     * @return Map of String arrays
     */
    public Map toStringArrayMap()
    {
        HashMap map = new HashMap(size()*3/2);
        
        Iterator i = super.entrySet().iterator();
        while(i.hasNext())
        {
            Map.Entry entry = (Map.Entry)i.next();
            LazyList l = (LazyList)entry.getValue();
            map.put(entry.getKey(),LazyList.toStringArray(l));
        }
        return map;
    }
    
    /* ------------------------------------------------------------ */
    public Object clone()
    {
        return new MultiMap(this);
    }
}
