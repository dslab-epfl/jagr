/* Copyright  (c) 2002 The Board of Trustees of The Leland Stanford Junior
 * University. All Rights Reserved.
 *
 * See the file LICENSE for information on redistributing this software.
 */

package swig.graph;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import swig.util.CollectionHelper;
import swig.util.Debug;
import swig.util.DeepCopyable;

/**
 *
 * $Id: Graph.java,v 1.1 2003/04/22 20:09:37 emrek Exp $
 *
 * $Log: Graph.java,v $
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
 * Revision 1.2  2002/08/19 06:49:44  emrek
 * Added copyright information to source files
 *
 * Revision 1.1.1.1  2002/07/17 09:07:45  emrek
 *
 *
 * Revision 1.1.1.1  2001/10/17 00:53:49  emrek
 * initial checkin of code that needs a better name than 'u'
 *
 * Revision 1.2  2001/06/22 16:20:09  emrek
 * fixed concurrent modification error (with Iterators) when removing a node
 * from the graph
 *
 * Revision 1.1  2001/05/29 02:43:13  emrek
 * changed file extensions for operator, type, connector, and path
 * descriptions.
 *
 * Revision 1.2  2001/04/25 14:50:16  emrek
 * added a null check to a function
 *
 * Revision 1.1  2001/03/05 06:07:19  emrek
 * added Graph and LabeledGraph data structures
 * updated support for DeepCopy of data structures in CollectionHelper
 *
 *
 */
public class Graph implements Serializable, DeepCopyable {

    int counter = 0;

    HashMap nodes;
    HashMap links;

    public Graph() {
        nodes = new HashMap();
        links = new HashMap();
    }

    public Graph(Graph g) {
        deepCopy(g);
    }

    public void clear() {
        nodes.clear();
        links.clear();
    }

    public boolean containsNodeKey(Object key) {
        Debug.Assert(key != null);
        return nodes.containsKey(key);
    }

    public boolean containsNodeValue(Object value) {
        Debug.Assert(value != null);
        return nodes.containsValue(value);
    }

    public boolean isEmpty() {
        return nodes.isEmpty();
    }

    public int size() {
        return nodes.size();
    }

    public Collection nodeSet() {
        return nodes.values();
    }

    public Collection nodeValueSet() {
        HashSet ret = new HashSet();
        Collection c = nodes.values();
        if (c != null) {
            Iterator iter = c.iterator();
            while (iter.hasNext()) {
                Node n = (Node) iter.next();
                ret.add(n.getValue());
            }
        }
        return ret;
    }

    public Collection linkSet() {
        return links.values();
    }

    public void insertNode(
        Object key,
        Object value,
        Object prevlinkkey,
        Object nextlinkkey,
        Link l) {
        Debug.Assert(key != null);
        Debug.Assert(value != null);
        Debug.Assert(prevlinkkey != null);
        Debug.Assert(nextlinkkey != null);
        Debug.Assert(l != null);

        removeLink(l);
        putNode(key, value);
        putLink(l.prevNodeKey, l.prevNodeLinkKey, key, prevlinkkey);
        putLink(l.nextNodeKey, l.nextNodeLinkKey, key, nextlinkkey);
    }

    public void putNode(Object key, Object value) {
        Debug.Assert(key != null);
        Debug.Assert(value != null);
        Debug.Assert(nodes.get(key) == null);

        Node n = new Node();
        n.key = key;
        n.value = value;
        nodes.put(key, n);
    }

    public void replaceNode(Object key, Object value) {
        Debug.Assert(key != null);
        Debug.Assert(value != null);

        Node n = (Node) nodes.get(key);
        if (n == null) {
            n = new Node();
        }
        n.key = key;
        n.value = value;
        nodes.put(key, n);
    }

    public void removeNode(Object key) {
        Debug.Assert(key != null);
        Node n = (Node) nodes.get(key);

        Iterator iter;

        iter = n.getPrevLinks().iterator();
        while (iter.hasNext()) {
            Object linkkey = iter.next();
            Link l = (Link) links.get(linkkey);
            removeLink(l);
        }

        iter = n.getNextLinks().iterator();
        while (iter.hasNext()) {
            Object linkkey = iter.next();
            Link l = (Link) links.get(linkkey);
            removeLink(l);
        }

        nodes.remove(key);
    }

    public Node getNode(Object key) {
        Debug.Assert(key != null);
        return (Node) nodes.get(key);
    }

    public Object getNodeValue(Object key) {
        Debug.Assert(key != null);
        Node n = (Node) nodes.get(key);
        if (n == null) {
            return null;
        }
        else {
            return n.value;
        }
    }

    public void putLink(
        Object prevNodeKey,
        Object prevNodeLinkKey,
        Object nextNodeKey,
        Object nextNodeLinkKey) {
        Debug.Assert(prevNodeKey != null);
        Debug.Assert(prevNodeLinkKey != null);
        Debug.Assert(nextNodeKey != null);
        Debug.Assert(nextNodeLinkKey != null);

        Link l = new Link();
        l.key = new Integer(counter++);
        l.prevNodeKey = prevNodeKey;
        l.prevNodeLinkKey = prevNodeLinkKey;
        l.nextNodeKey = nextNodeKey;
        l.nextNodeLinkKey = nextNodeLinkKey;

        links.put(l.key, l);

        Node pnode = (Node) nodes.get(prevNodeKey);
        Debug.Assert(pnode != null);
        Node nnode = (Node) nodes.get(nextNodeKey);
        Debug.Assert(nnode != null);

        pnode.nextlinks.put(prevNodeLinkKey, l.key);
        nnode.prevlinks.put(nextNodeLinkKey, l.key);
    }

    public void removeLink(Link l) {
        Debug.Assert(l != null);
        Debug.Assert(links.get(l.key) == l);

        Node pnode = (Node) nodes.get(l.prevNodeKey);
        pnode.nextlinks.remove(l.prevNodeLinkKey);

        Node nnode = (Node) nodes.get(l.nextNodeKey);
        nnode.prevlinks.remove(l.nextNodeLinkKey);

        links.remove(l.key);
    }

    public Link getNextLink(Object nodekey, Object nodelinkkey) {
        Debug.Assert(nodekey != null);
        Debug.Assert(nodelinkkey != null);

        Node n = (Node) nodes.get(nodekey);
        Debug.Assert(n != null);
        Object linkkey = n.nextlinks.get(nodelinkkey);
        return (Link) links.get(linkkey);
    }

    public Link getPrevLink(Object nodekey, Object nodelinkkey) {
        Debug.Assert(nodekey != null);
        Debug.Assert(nodelinkkey != null);

        Node n = (Node) nodes.get(nodekey);
        Debug.Assert(n != null);
        Object linkkey = n.prevlinks.get(nodelinkkey);
        return (Link) links.get(linkkey);
    }

    public Collection getAllNextLinks(Object nodekey) {

        Collection ret = new HashSet();

        Node n = getNode(nodekey);

        Iterator iter = n.getNextLinks().iterator();
        while (iter.hasNext()) {
            Object linkkey = iter.next();
            Link l = (Link) links.get(linkkey);
            ret.add(l);
        }

        return ret;
    }

    public Collection getAllNextLinkValues(Object nodekey) {
        Collection links = getAllNextLinks(nodekey);
        Collection ret = new HashSet(links.size());
        Iterator iter = links.iterator();
        while (iter.hasNext()) {
            Link l = (Link) iter.next();
            ret.add(getNodeValue(l.getNextNodeKey()));
        }
        return ret;
    }

    public Collection getAllPrevLinks(Object nodekey) {

        Collection ret = new HashSet();

        Node n = getNode(nodekey);

        Iterator iter = n.getPrevLinks().iterator();
        while (iter.hasNext()) {
            Object linkkey = iter.next();
            Link l = (Link) links.get(linkkey);
            ret.add(l);
        }

        return ret;
    }

    public Collection getAllPrevLinkValues(Object nodekey) {
        Collection links = getAllPrevLinks(nodekey);
        Collection ret = new HashSet(links.size());
        Iterator iter = links.iterator();
        while (iter.hasNext()) {
            Link l = (Link) iter.next();
            ret.add(getNodeValue(l.getPrevNodeKey()));
        }
        return ret;
    }

    public Node getNextNode(Object nodekey, Object nodelinkkey) {
        Debug.Assert(nodekey != null);
        Debug.Assert(nodelinkkey != null);

        Link l = getNextLink(nodekey, nodelinkkey);
        if (l == null) {
            return null;
        }
        return (Node) nodes.get(l.nextNodeKey);
    }

    public Object getNextNodeValue(Object nodekey, Object nodelinkkey) {
        return getNextNode(nodekey, nodelinkkey).value;
    }

    public Node getPrevNode(Object nodekey, Object nodelinkkey) {
        Debug.Assert(nodekey != null);
        Debug.Assert(nodelinkkey != null);

        Link l = getPrevLink(nodekey, nodelinkkey);
        Debug.Assert(l != null);
        return (Node) nodes.get(l.prevNodeKey);
    }

    public Object getPrevNodeValue(Object nodekey, Object nodelinkkey) {
        return getPrevNode(nodekey, nodelinkkey).value;
    }

    public String toString() {
        String ret = "";
        Iterator iter = nodes.keySet().iterator();
        ret += "\tNODES:\n";
        while (iter.hasNext()) {
            Object key = iter.next();
            ret += "\t\t["
                + key.toString()
                + ", "
                + nodes.get(key).toString()
                + "]\n";
        }

        ret += "\tLINKS:\n";
        iter = links.keySet().iterator();
        while (iter.hasNext()) {
            Object key = iter.next();
            ret += "\t\t["
                + key.toString()
                + ", "
                + links.get(key).toString()
                + "]\n";
        }
        return ret;
    }

    private void deepCopy(Graph g) {
        this.counter = g.counter;
        this.nodes = CollectionHelper.DeepCopyHashMap(g.nodes);
        this.links = CollectionHelper.DeepCopyHashMap(g.links);
    }

    public DeepCopyable deepCopy() {
        Graph g = new Graph(this);
        return g;
    }

}
