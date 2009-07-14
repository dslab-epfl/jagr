/* Copyright  (c) 2002 The Board of Trustees of The Leland Stanford Junior
 * University. All Rights Reserved.
 *
 * See the file LICENSE for information on redistributing this software.
 */

package swig.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

/**
 *
 * $Id: CollectionHelper.java,v 1.2 2002/12/28 12:27:30 emrek Exp $
 *
 * $Log: CollectionHelper.java,v $
 * Revision 1.2  2002/12/28 12:27:30  emrek
 * no functional changes, just formatting and general cleanup. also did some javadoc'ing of roc.pinpoint.** classes.
 *
 * Revision 1.1  2002/12/17 15:27:43  emrek
 * first commit of new pinpoint tracing and analysis framework
 *
 * Revision 1.3  2002/08/19 06:49:43  emrek
 * Added copyright information to source files
 *
 * Revision 1.2  2002/08/15 22:08:07  emrek
 * formatting changes (only) because of new editor
 *
 * Revision 1.1.1.1  2002/07/17 09:07:47  emrek
 *
 *
 * Revision 1.1.1.1  2001/10/17 00:53:41  emrek
 * initial checkin of code that needs a better name than 'u'
 *
 * Revision 1.6  2001/03/05 06:30:55  emrek
 * small bugfix (GetOneValue should be static)
 *
 * Revision 1.5  2001/03/05 06:07:19  emrek
 * added Graph and LabeledGraph data structures
 * updated support for DeepCopy of data structures in CollectionHelper
 *
 * Revision 1.4  2000/10/17 12:18:34  emrek
 * added support for deepcopying hashsets,
 * added a couple more debug commands (printing exceptions, warnings, etc)
 *
 * Revision 1.3  2000/09/18 03:52:36  emrek
 * added working APC code.  Also refined path descriptions and Holders, and fixed a number of bugs all over the place.
 *
 * Revision 1.2  2000/09/03 22:40:35  emrek
 * first complete checkin of brick, kernel, and admin code.
 *
 * Revision 1.1  2000/08/29 06:39:03  emrek
 * *** empty log message ***
 *
 *
 */
public class CollectionHelper {
    public static HashSet DeepCopyHashSet(HashSet src) {
        return (HashSet) DeepCopy(src, new HashSet(src.size()));
    }

    /**
     * performs a deep-copy of a hashmap
     * It does *NOT* deep-copy the keys
     */
    public static HashMap DeepCopyHashMap(HashMap src) {
        return (HashMap) DeepCopy(src, new HashMap(src.size()));
    }

    public static Collection DeepCopy(Collection src, Collection dest) {
        Iterator iter = src.iterator();

        while (iter.hasNext()) {
            Object o = DeepCopy(iter.next());
            dest.add(o);
        }

        return dest;
    }

    public static Object DeepCopy(Object s) {
        Object ret = null;

        if (s instanceof String) {
            ret = s;
        }
        else if (s instanceof Integer) {
            ret = new Integer(((Integer) s).intValue());
        }
        else if (s instanceof HashMap) {
            ret = DeepCopyHashMap((HashMap) s);
        }
        else if (s instanceof HashSet) {
            ret = DeepCopyHashSet((HashSet) s);
        }
        else {
            ret = ((DeepCopyable) s).deepCopy();
        }

        return ret;
    }

    /**
     * performs a deep-copy of the values of the src map to the dest map. 
     * It *does* *NOT* deep-copy the keys.
     */
    public static Map DeepCopy(Map src, Map dest) {
        Iterator iter = src.keySet().iterator();

        while (iter.hasNext()) {
            Object key = iter.next();
            Object s = src.get(key);
            Object r = DeepCopy(s);
            dest.put(key, r);
        }

        return dest;
    }

    /**
     * performs a deepcopy of the values of a categorized map.
     * It *does* *NOT* deep-copy keys or category-keys
     */
    public static CategorizedMap DeepCopyCategorizedMap(CategorizedMap src) {
        CategorizedMap ret = new CategorizedMap(src.size());

        Iterator iter = src.keySet().iterator();

        while (iter.hasNext()) {
            Object key = iter.next();
            Object o = ((DeepCopyable) src.get(key)).deepCopy();
            ret.putForCollectionHelper(key, o);
        }

        iter = src.categories.keySet().iterator();

        while (iter.hasNext()) {
            Object key = iter.next();
            HashSet s = new HashSet((HashSet) src.categories.get(key));
            ret.categories.put(key, s);
        }

        ret.reversemap = (HashMap) src.reversemap.clone();

        return ret;
    }

    public static Collection GetKeysForValue(Map map, Object value) {
        LinkedList list = new LinkedList();
        Iterator iter = map.keySet().iterator();

        while (iter.hasNext()) {
            Object key = iter.next();

            if (((value != null) && value.equals(map.get(key)))
                || (map.get(key) == null)) {
                list.add(key);
            }
        }

        return list;
    }

    /**
     * returns _one_ value from the collection
     */
    static public Object GetOneValue(Collection c) {
        Iterator i = c.iterator();

        if (i.hasNext()) {
            return i.next();
        }
        else {
            return null;
        }
    }
}