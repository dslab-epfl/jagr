/* Copyright  (c) 2002 The Board of Trustees of The Leland Stanford Junior
 * University. All Rights Reserved.
 *
 * See the file LICENSE for information on redistributing this software.
 */

package swig.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class XMLStructs {

    public static String MapToXML(Map map, String elname) {
        StringBuffer ret = new StringBuffer();
        Iterator iter = map.keySet().iterator();
        while (iter.hasNext()) {
            String k = (String) iter.next();
            Object o = map.get(k);
            if (o instanceof String) {
                String v = (String) o;
                ret.append('<');
                ret.append(elname);
                ret.append(" key=\"");
                ret.append(k);
                ret.append("\">");
                ret.append(v);
                ret.append("</");
                ret.append(elname);
                ret.append('>');
            }
            else if (o instanceof List) {
                List l = (List) o;
                Iterator iter2 = l.iterator();
                while (iter2.hasNext()) {
                    String v = (String) iter2.next();
                    ret.append('<');
                    ret.append(elname);
                    ret.append(" key=\"");
                    ret.append(k);
                    ret.append("\">");
                    ret.append(v);
                    ret.append("</");
                    ret.append(elname);
                    ret.append('>');
                }
            }

        }
        return ret.toString();
    }

    public static Map ParseMap(Element m, String elname) {
        Map ret = new HashMap();

        if (m != null) {
            NodeList nl = XMLHelper.GetChildrenByTagName(m, elname);
            for (int i = 0; i < nl.getLength(); i++) {
                Element e = (Element) nl.item(i);
                String k = e.getAttribute("key");
                String v = XMLHelper.GetText(e);

                if (ret.containsKey(k)) {
                    Object o = ret.get(k);
                    if (o instanceof List) {
                        ((List) o).add(v);
                    }
                    else {
                        String s = (String) o;
                        List l = new ArrayList(2);
                        l.add(s);
                        l.add(v);
                        ret.put(k, l);
                    }
                }
                else {
                    ret.put(k, v);
                }
            }
        }

        return ret;
    }

    public static String CollectionToXML(Collection l, String elname) {
        StringBuffer ret = new StringBuffer();
        Iterator iter = l.iterator();
        while (iter.hasNext()) {
            String s = (String) iter.next();
            ret.append('<');
            ret.append(elname);
            ret.append('>');
            ret.append(s);
            ret.append("</");
            ret.append(elname);
            ret.append('>');
        }
        return ret.toString();
    }

    public static List ParseList(Element l, String elname) {
        List ret = new LinkedList();

        if (l != null) {
            NodeList nl = XMLHelper.GetChildrenByTagName(l, elname);
            for (int i = 0; i < nl.getLength(); i++) {
                Element e = (Element) nl.item(i);
                String s = XMLHelper.GetText(e);
                ret.add(s);
            }
        }

        return ret;
    }

    public static Set ParseSet(Element l, String elname) {
        Set ret = new HashSet();

        if (l != null) {
            NodeList nl = XMLHelper.GetChildrenByTagName(l, elname);
            for (int i = 0; i < nl.getLength(); i++) {
                Element e = (Element) nl.item(i);
                String s = XMLHelper.GetText(e);
                ret.add(s);
            }
        }

        return ret;
    }

    public static String ArrayToXML(String[] sarr, String elname) {
        return CollectionToXML(Arrays.asList(sarr), elname);
    }

    public static String[] ParseArray(Element l, String elname) {
        List list = ParseList(l, elname);
        String[] ret = (String[]) list.toArray(new String[list.size()]);
        return ret;
    }

}
