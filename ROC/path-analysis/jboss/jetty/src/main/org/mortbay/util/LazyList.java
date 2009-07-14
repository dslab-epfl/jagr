// ========================================================================
// Copyright (c) 1999 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: LazyList.java,v 1.1.1.1 2002/11/16 03:16:49 mikechen Exp $
// ========================================================================

package org.mortbay.util;
import java.io.Serializable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/* ------------------------------------------------------------ */
/** Lazy List creation.
 * A List helper class that attempts to avoid unneccessary List
 * creation.   If a method needs to create a List to return, but it is
 * expected that this will either be empty or frequently contain a
 * single item, then using LazyList will avoid additional object
 * creations by using Collections.EMPTY_LIST or
 * Collections.singletonList where possible.
 *
 * <p><h4>Usage</h4>
 * <pre>
 *   LazyList lazylist =null;
 *   while(loopCondition)
 *   {
 *     Object item = getItem();
 *     if (item.isToBeAdded())
 *         lazylist = LazyList.add(lazylist,item);
 *   }
 *   return LazyList.getList(lazylist);
 * </pre>
 *
 * An ArrayList of default size is used as the initial LazyList.
 *
 * @see java.util.List
 * @version $Revision: 1.1.1.1 $
 * @author Greg Wilkins (gregw)
 */
public class LazyList extends AbstractList
    implements Cloneable, Serializable
{
    private static final String[] __EMTPY_STRING_ARRAY = new String[0];
    
    private Object _first;
    private List _list;

    /* ------------------------------------------------------------ */
    private LazyList(Object first)
    {
        _first=first;
    }
    
    /* ------------------------------------------------------------ */
    private LazyList(List list)
    {
        _list=list;
    }
    
    /* ------------------------------------------------------------ */
    /** Add an item to a LazyList 
     * @param list The list to add to or null if none yet created.
     * @param item The item to add.
     * @return The lazylist created or added to.
     */
    public static LazyList add(LazyList list, Object item)
    {
        if (list==null)
            return new LazyList(item);

        if (list._list!=null)
        {
            list._list.add(item);
            return list;
        }

        list._list=new ArrayList();
        list._list.add(list._first);
        list._list.add(item);
        list._first=null;
        
        return list;    
    }
    
    /* ------------------------------------------------------------ */
    /** Add an item to a LazyList 
     * @param list The list to add to or null if none yet created.
     * @param item The item to add.
     * @return The lazylist created or added to.
     */
    public static LazyList add(LazyList list, Collection collection)
    {
        Iterator i=collection.iterator();
        while(i.hasNext())
            list=LazyList.add(list,i.next());
        return list;
    }

    /* ------------------------------------------------------------ */
    /** Add an item to a LazyList 
     * @param list The list to add to or null if none yet created.
     * @param initialSize A size to use when creating the real list
     * @param item The item to add.
     * @return The lazylist created or added to.
     */
    public static LazyList add(LazyList list, int initialSize, Object item)
    {
        if (list==null)
            return new LazyList(item);

        if (list._list!=null)
        {
            list._list.add(item);
            return list;
        }

        list._list=new ArrayList(initialSize);
        list._list.add(list._first);
        list._list.add(item);
        return list;    
    }

    /* ------------------------------------------------------------ */
    public static LazyList remove(LazyList list, Object o)
    {
        if (list==null)
            return null;

        if (list._first!=null && list._first.equals(o))
            return null;

        list._list.remove(o);
        if (list._list.size()==1)
        {
            list._first=list._list.get(0);
            list._list=null;
        }
        return list;
    }
    
    
    /* ------------------------------------------------------------ */
    /** Get the real List from a LazyList.
     * 
     * @param list A LazyList returned from LazyList.add(Object)
     * @return The List of added items, which may be an EMPTY_LIST
     * or a SingletonList.
     */
    public static List getList(LazyList list)
    {
        return getList(list,false);
    }
    

    /* ------------------------------------------------------------ */
    /** Get the real List from a LazyList.
     * 
     * @param list A LazyList returned from LazyList.add(Object) or null
     * @param nullForEmpty If true, null is returned instead of an
     * empty list.
     * @return The List of added items, which may be null, an EMPTY_LIST
     * or a SingletonList.
     */
    public static List getList(LazyList list, boolean nullForEmpty)
    {
        if (list==null)
            return nullForEmpty?null:Collections.EMPTY_LIST;
        if (list._list==null)
            return list;
        return list._list;
    }
    
    /* ------------------------------------------------------------ */
    public static String[] toStringArray(LazyList list)
    {
        if (list==null)
            return __EMTPY_STRING_ARRAY;
        if (list._list!=null)
        {
            String[] a = new String[list._list.size()];
            for (int i=list._list.size();i-->0;)
            {
                Object o=list._list.get(i);
                if (o!=null)
                    a[i]=o.toString();
            }
            return a;
        }
        
        return new String[] {list._first==null?null:list._first.toString()};
    }


    /* ------------------------------------------------------------ */
    /** The size of a lazy List 
     * @param list  A LazyList returned from LazyList.add(Object) or null
     * @return the size of the list.
     */
    public static int size(LazyList list)
    {
        if (list==null)
            return 0;
        if (list._list==null)
            return 1;
        return list._list.size();
    }
    
    /* ------------------------------------------------------------ */
    /** Get item from the list 
     * @param list  A LazyList returned from LazyList.add(Object) or null
     * @param int i index
     * @return the item from the list.
     */
    public static Object get(LazyList list, int i)
    {
        if (list==null)
            throw new IndexOutOfBoundsException();
        
        if (list._list==null)
        {
            if (i==0)
                return list._first;
            throw new IndexOutOfBoundsException();
        }
            
        return list._list.get(i);
    }

    /* ------------------------------------------------------------ */
    public static LazyList clone(LazyList list)
    {
        if (list==null)
            return null;
        return (LazyList) list.clone();
    }
    
    /* ------------------------------------------------------------ */
    public Object get(int i)
    {
        if (_list!=null)
            return _list.get(i);
        if (i!=0)
            throw new IndexOutOfBoundsException("index "+i);
        return _first;
    }

    /* ------------------------------------------------------------ */
    public int size()
    {
        if (_list!=null)
            return _list.size();
        return 1;
    }
    
    /* ------------------------------------------------------------ */
    public ListIterator listIterator()
    {
        if (_list!=null)
            return _list.listIterator();
        return new SIterator();
    }
    
    /* ------------------------------------------------------------ */
    public ListIterator listIterator(int i)
    {
        if (_list!=null)
            return _list.listIterator(i);
        return new SIterator(i);
    }
    
    /* ------------------------------------------------------------ */
    public Iterator iterator()
    {
        if (_list!=null)
            return _list.iterator();
        return new SIterator();
    }

    /* ------------------------------------------------------------ */
    public Object clone()
    {
        if (_list!=null)
            return new LazyList(new ArrayList(_list));
        return new LazyList(_first);
    }
    
    /* ------------------------------------------------------------ */
    private class SIterator implements ListIterator
    {
        int i;
        
        SIterator(){i=0;}
        
        SIterator(int i)
        {
            if (i<0||i>1)
                throw new IndexOutOfBoundsException("index "+i);
            this.i=i;
        }
        
        public void add(Object o){throw new UnsupportedOperationException("LazyList.add()");}
        public boolean hasNext() {return i==0;}
        public boolean hasPrevious() {return i==1;}
        public Object next() {
            if (i!=0) throw new NoSuchElementException();
            if (_list!=null) throw new ConcurrentModificationException();
            i++;
            return _first;
        }
        public int nextIndex() {return i;}
        public Object previous() {
            if (i!=1) throw new NoSuchElementException();i--;
            if (_list!=null) throw new ConcurrentModificationException();
            return _first;
        }
        public int previousIndex() {return i-1;}
        public void remove(){throw new UnsupportedOperationException("LazyList.remove()");}
        public void set(Object o){throw new UnsupportedOperationException("LazyList.add()");}
    }


    /* ------------------------------------------------------------ */
    public String toString()
    {
        if (_list==null)
            return "["+_first+"]";
        return _list.toString();
    }

    /* ------------------------------------------------------------ */
    public static String toString(LazyList list)
    {
        if (list==null)
            return "[]";
        return list.toString();
    }

    
}

