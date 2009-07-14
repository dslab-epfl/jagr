/* Copyright  (c) 2002 The Board of Trustees of The Leland Stanford Junior
 * University. All Rights Reserved.
 *
 * See the file LICENSE for information on redistributing this software.
 */

package swig.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CategorizedMap extends HashMap {
    // "this" contains <key, object>
    HashMap categories; // contains <categorykey, Set<key> >
    HashMap reversemap; // contains <key, categorykey>
    HashMap categoryvaluecache; // contains <categorykey, arraylist<object> >

    public CategorizedMap() {
        inithelper();
    }

    public CategorizedMap(int initialCapacity) {
        super(initialCapacity);
        inithelper();
    }

    public CategorizedMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
        inithelper();
    }

    public CategorizedMap(CategorizedMap cmap) {
        super(cmap);
        inithelper();
        categories = new HashMap(cmap.categories);
        reversemap = new HashMap(cmap.reversemap);
    }

    private void inithelper() {
        categories = new HashMap();
        reversemap = new HashMap();
        categoryvaluecache = new HashMap();
    }

    /** extend the standard HashMap functions to do proper
    bookkeeping of the categories **/
    public void clear() {
        categories.clear();
        reversemap.clear();
        categoryvaluecache.clear();
        super.clear();
    }

    public Object clone() {
        return new CategorizedMap(this);
    }

    Object putForCollectionHelper(Object key, Object value) {
        return super.put(key, value);
    }

    public Object put(Object key, Object value) {
        throw new UnsupportedOperationException();
    }

    public void putAll(Map t) {
        throw new UnsupportedOperationException();
    }

    public Object remove(Object key) {
        Object categorykey = reversemap.get(key);
        Set keys = (Set) categories.get(categorykey);
        keys.remove(key);

        if (keys.isEmpty()) {
            categories.remove(categorykey);
        }

        return super.remove(key);
    }

    /** now add new functions to manipulate/query category information **/
    public void clear(Object categorykey) {
        // todo
        throw new UnsupportedOperationException();
    }

    public boolean categoryContainsKey(Object categorykey, Object key) {
        Set keys = (Set) categories.get(categorykey);

        if (keys == null) {
            return false;
        }

        return keys.contains(key);
    }

    /**
     * this is quite an inefficient function.
     */
    public boolean categoryContainsValue(Object categorykey, Object value) {
        List values = categoryValues(categorykey);

        if (values == null) {
            return false;
        }

        return values.contains(value);
    }

    public Object categoryOf(Object key) {
        return reversemap.get(key);
    }

    public Set categoryKeySet(Object categorykey) {
        return Collections.unmodifiableSet((Set) categories.get(categorykey));
    }

    public Object put(Object categorykey, Object key, Object value) {
        Object ret = null;

        if (containsKey(key)) {
            ret = remove(key);
        }

        Object sanity = super.put(key, value);
        Debug.Assert(sanity == null);

        categoryvaluecache.remove(categorykey);
        Set category = getCategorySet(categorykey);
        category.add(key);
        reversemap.put(key, categorykey);

        return ret;
    }

    public void putAll(Object categorykey, Map t) {
        super.putAll(t);
        categoryvaluecache.remove(categorykey);
        Set category = getCategorySet(categorykey);
        Set tkeys = t.keySet();
        category.addAll(tkeys);
        Iterator iter = tkeys.iterator();

        while (iter.hasNext()) {
            reversemap.put(iter.next(), categorykey);
        }
    }

    public int categorySize(Object categorykey) {
        Set keys = (Set) categories.get(categorykey);

        if (keys == null) {
            return 0;
        }

        return keys.size();
    }

    /**
     * returns an unmodifiable collection of values in this category. 
     */
    public List categoryValues(Object categorykey) {
        Set keys = (Set) categories.get(categorykey);

        if (keys == null) {
            return new ArrayList(0);
        }

        List ret = (List) categoryvaluecache.get(categorykey);

        if (ret == null) {
            ret = new ArrayList(keys.size());

            Iterator iter = keys.iterator();

            while (iter.hasNext()) {
                ret.add(get(iter.next()));
            }

            ret = Collections.unmodifiableList(ret);
            categoryvaluecache.put(categorykey, ret);
        }

        return ret;
    }

    /* internal helper functions */
    private Set getCategorySet(Object categorykey) {
        Set ret;
        ret = (Set) categories.get(categorykey);

        if (ret == null) {
            ret = new HashSet();
            categories.put(categorykey, ret);
        }

        return ret;
    }
}