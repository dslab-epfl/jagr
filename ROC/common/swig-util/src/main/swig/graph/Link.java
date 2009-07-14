/* Copyright  (c) 2002 The Board of Trustees of The Leland Stanford Junior
 * University. All Rights Reserved.
 *
 * See the file LICENSE for information on redistributing this software.
 */

package swig.graph;

import swig.util.CollectionHelper;
import swig.util.DeepCopyable;

/**
 *
 * $Id: Link.java,v 1.1 2003/04/22 20:09:37 emrek Exp $
 *
 * $Log: Link.java,v $
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
public class Link implements DeepCopyable {
    Object key;
    Object prevNodeKey;
    Object prevNodeLinkKey;
    Object nextNodeKey;
    Object nextNodeLinkKey;

    public Object getKey() {
        return key;
    }

    public Object getPrevNodeKey() {
        return prevNodeKey;
    }

    public Object getPrevNodeLinkKey() {
        return prevNodeLinkKey;
    }

    public Object getNextNodeKey() {
        return nextNodeKey;
    }

    public Object getNextNodeLinkKey() {
        return nextNodeLinkKey;
    }

    public String toString() {
        String ret = "";
        ret += "{Link: "
            + key.toString()
            + ","
            + prevNodeKey.toString()
            + ", "
            + prevNodeLinkKey.toString()
            + ", "
            + nextNodeKey.toString()
            + ", "
            + nextNodeLinkKey.toString()
            + "}";
        return ret;
    }

    public DeepCopyable deepCopy() {
        Link l = new Link();
        l.key = CollectionHelper.DeepCopy(this.key);
        l.prevNodeKey = CollectionHelper.DeepCopy(this.prevNodeKey);
        l.prevNodeLinkKey = CollectionHelper.DeepCopy(this.prevNodeLinkKey);
        l.nextNodeKey = CollectionHelper.DeepCopy(this.nextNodeKey);
        l.nextNodeLinkKey = CollectionHelper.DeepCopy(this.nextNodeLinkKey);
        return l;
    }
}
