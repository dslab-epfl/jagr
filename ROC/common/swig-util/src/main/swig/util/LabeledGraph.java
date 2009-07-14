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
import java.util.Set;

/**
 *
 *
 * $Id: LabeledGraph.java,v 1.1 2003/04/22 20:09:37 emrek Exp $
 *
 * $Log: LabeledGraph.java,v $
 * Revision 1.1  2003/04/22 20:09:37  emrek
 * * populated swig-util directory ROC/common/swig-util
 *
 * Revision 1.1.1.1  2003/03/07 08:12:40  emrek
 * * first checkin of PP to the new ROC/PP/ subdir after reorg
 *
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
 * Revision 1.1.1.1  2002/07/17 09:07:53  emrek
 *
 *
 * Revision 1.1.1.1  2001/10/17 00:53:43  emrek
 * initial checkin of code that needs a better name than 'u'
 *
 * Revision 1.1  2001/03/05 06:07:19  emrek
 * added Graph and LabeledGraph data structures
 * updated support for DeepCopy of data structures in CollectionHelper
 *
 *
 */
public class LabeledGraph extends Graph {
    HashMap labelgroups; // maps labels to nodes
    HashMap nodelabels; // maps nodes to labels

    public LabeledGraph() {
        labelgroups = new HashMap();
        nodelabels = new HashMap();
    }

    protected LabeledGraph(LabeledGraph g) {
        super(g);
        deepCopy(g);
    }

    public void clear() {
        labelgroups.clear();
        nodelabels.clear();
        super.clear();
    }

    public boolean containsLabel(Object label) {
        Debug.Assert(label != null);

        return labelgroups.containsKey(label);
    }

    public boolean LabelGroupContainsNode(Object label, Object key) {
        Debug.Assert(label != null);
        Debug.Assert(key != null);
        Set lg = (Set) labelgroups.get(label);

        if (lg == null) {
            return false;
        }

        return lg.contains(key);
    }

    public Object getNodeLabel(Object key) {
        Debug.Assert(key != null);

        return nodelabels.get(key);
    }

    public Collection nodeSet(Object label) {
        Debug.Assert(label != null);

        return (Set) labelgroups.get(label);
    }

    public Collection nodeValueSet(Object label) {
        Debug.Assert(label != null);
        HashSet ret = new HashSet();
        Collection c = nodeSet(label);

        if (c != null) {
            Iterator iter = c.iterator();

            while (iter.hasNext()) {
                String s = (String) iter.next();
                Node n = (Node) nodes.get(s);

                if (n == null) {
                    Debug.Print(
                        "graph",
                        "key mismatch: "
                            + s
                            + " not found in nodes:\n"
                            + this.toString());
                    Debug.AssertNotReached();
                }

                ret.add(((Node) nodes.get(s)).getValue());
            }
        }

        return ret;
    }

    public void insertNode(
        Object key,
        Object value,
        Object prevlinkkey,
        Object nextlinkkey,
        Link l) {
        throw new UnsupportedOperationException();
    }

    public void insertNode(
        Object label,
        Object key,
        Object value,
        Object prevlinkkey,
        Object nextlinkkey,
        Link l) {
        Debug.Assert(label != null);
        Debug.Assert(key != null);
        Debug.Assert(value != null);
        Debug.Assert(prevlinkkey != null);
        Debug.Assert(nextlinkkey != null);
        Debug.Assert(l != null);

        removeLink(l);
        putNode(label, key, value);
        putLink(l.prevNodeKey, l.prevNodeLinkKey, key, prevlinkkey);
        putLink(l.nextNodeKey, l.nextNodeLinkKey, key, nextlinkkey);
    }

    public void putNode(Object key, Object value) {
        throw new UnsupportedOperationException();
    }

    public void putNode(Object label, Object key, Object value) {
        Debug.Assert(label != null);
        Debug.Assert(key != null);
        Debug.Assert(value != null);

        Set lg = (Set) labelgroups.get(label);

        if (lg == null) {
            lg = new HashSet();
            labelgroups.put(label, lg);
        }

        Debug.Assert(lg.contains(key) == false);
        lg.add(key);
        nodelabels.put(key, label);
        super.putNode(key, value);
    }

    public void removeNode(Object key) {
        Debug.Assert(key != null);

        Object label = nodelabels.get(key);
        Debug.Assert(label != null);

        Set lg = (Set) labelgroups.get(label);
        Debug.Assert(lg != null);
        Debug.Assert(lg.contains(key));

        lg.remove(key);

        if (lg.isEmpty()) {
            labelgroups.remove(label);
        }

        nodelabels.remove(key);

        super.removeNode(key);
    }

    public String toString() {
        String ret = super.toString();
        ret += "\tLABELGROUPS:\n";
        Iterator iter = labelgroups.keySet().iterator();

        while (iter.hasNext()) {
            Object key = iter.next();
            ret
                += ("\t\t["
                    + key.toString()
                    + ", "
                    + labelgroups.get(key).toString()
                    + "]\n");
        }

        ret += "\tNODELABELS:\n";
        iter = nodelabels.keySet().iterator();

        while (iter.hasNext()) {
            Object key = iter.next();
            ret
                += ("\t\t["
                    + key.toString()
                    + ", "
                    + nodelabels.get(key).toString()
                    + "]\n");
        }

        return ret;
    }

    private void deepCopy(LabeledGraph g) {
        this.labelgroups = CollectionHelper.DeepCopyHashMap(g.labelgroups);
        this.nodelabels = CollectionHelper.DeepCopyHashMap(g.nodelabels);
    }

    public DeepCopyable deepCopy() {
        LabeledGraph g = new LabeledGraph(this);

        return g;
    }
}