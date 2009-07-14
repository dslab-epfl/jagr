/* Copyright  (c) 2002 The Board of Trustees of The Leland Stanford Junior
 * University. All Rights Reserved.
 *
 * See the file LICENSE for information on redistributing this software.
 */

package swig.graph;

import java.util.Collection;
import java.util.HashMap;

import swig.util.CollectionHelper;
import swig.util.DeepCopyable;

/**
 *
 * $Id: Node.java,v 1.2 2002/12/28 12:27:30 emrek Exp $
 *
 * $Log: Node.java,v $
 * Revision 1.2  2002/12/28 12:27:30  emrek
 * no functional changes, just formatting and general cleanup. also did some javadoc'ing of roc.pinpoint.** classes.
 *
 * Revision 1.1  2002/12/17 15:27:43  emrek
 * first commit of new pinpoint tracing and analysis framework
 *
 * Revision 1.2  2002/08/19 06:49:44  emrek
 * Added copyright information to source files
 *
 * Revision 1.1.1.1  2002/07/17 09:07:47  emrek
 *
 *
 * Revision 1.1.1.1  2001/10/17 00:53:49  emrek
 * initial checkin of code that needs a better name than 'u'
 *
 * Revision 1.1  2001/05/29 02:43:13  emrek
 * changed file extensions for operator, type, connector, and path
 * descriptions.
 *
 *
 */
public class Node implements DeepCopyable {
    Object key;
    Object value;
    HashMap prevlinks;
    HashMap nextlinks;

    Node() {
        key = null;
        value = null;
        prevlinks = new HashMap(1);
        nextlinks = new HashMap(1);
    }

    public Object getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }

    public Collection getPrevLinks() {
        return prevlinks.values();
    }

    public Collection getNextLinks() {
        return nextlinks.values();
    }

    public Object getPrevLink(Object portkey) {
        return prevlinks.get(portkey);
    }

    public Object getNextLink(Object portkey) {
        return nextlinks.get(portkey);
    }

    public String toString() {
        String ret = "";
        ret += "{Node: " + key.toString() + "," + value.toString() + "}";
        return ret;
    }

    public DeepCopyable deepCopy() {
        Node ret = new Node();
        ret.key = CollectionHelper.DeepCopy(this.key);
        ret.value = CollectionHelper.DeepCopy(this.value);
        ret.prevlinks = CollectionHelper.DeepCopyHashMap(prevlinks);
        ret.nextlinks = CollectionHelper.DeepCopyHashMap(nextlinks);
        return ret;
    }
}
