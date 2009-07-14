/* Copyright  (c) 2002 The Board of Trustees of The Leland Stanford Junior
 * University. All Rights Reserved.
 *
 * See the file LICENSE for information on redistributing this software.
 */

package swig.util;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.Vector;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *  XMLHelper functions
 *
 *  $Id: XMLHelper.java,v 1.2 2003/10/16 23:19:29 emrek Exp $
 *
 *  $Log: XMLHelper.java,v $
 *  Revision 1.2  2003/10/16 23:19:29  emrek
 *  * commented out a system.println("") that was printing out some
 *    status messages
 *
 *  Revision 1.1  2003/04/22 20:09:37  emrek
 *  * populated swig-util directory ROC/common/swig-util
 *
 *  Revision 1.1.1.1  2003/03/07 08:12:40  emrek
 *  * first checkin of PP to the new ROC/PP/ subdir after reorg
 *
 *  Revision 1.2  2002/12/28 12:27:30  emrek
 *  no functional changes, just formatting and general cleanup. also did some javadoc'ing of roc.pinpoint.** classes.
 *
 *  Revision 1.1  2002/12/17 15:27:43  emrek
 *  first commit of new pinpoint tracing and analysis framework
 *
 *  Revision 1.4  2002/10/29 17:31:17  emrek
 *  * many major performance improvements
 *
 *  Revision 1.3  2002/08/19 06:49:43  emrek
 *  Added copyright information to source files
 *
 *  Revision 1.2  2002/08/15 22:08:07  emrek
 *  formatting changes (only) because of new editor
 *
 *  Revision 1.1.1.1  2002/07/17 09:07:55  emrek
 *
 *
 *  Revision 1.1.1.1  2001/10/17 00:53:42  emrek
 *  initial checkin of code that needs a better name than 'u'
 *
 *  Revision 1.5  2001/04/25 14:51:04  emrek
 *  fixed XMLHelper.getText() so it skips over comments
 *
 *  Revision 1.4  2000/10/08 04:07:40  emrek
 *  adding FileFinder, to support java-classpath style searching for files
 *
 *  Revision 1.3  2000/09/18 03:52:36  emrek
 *  added working APC code.  Also refined path descriptions and Holders, and fixed a number of bugs all over the place.
 *
 *  Revision 1.2  2000/09/13 02:21:01  emrek
 *  iSpaceBridge service to connect iSpace services and Paths is complete and seems to work well enough.  See paths.ispacefe.bridge.test.Client for a test client.
 *  I still need to add automatic transformations (APC) to the bridge IF.
 *
 *  Revision 1.1  2000/03/07 13:10:13  emrek
 *  moved xmlhelper classes from emrek/ to swig/
 *
 *  Revision 1.2  2000/02/27 05:30:30  emrek
 *  debugging
 *
 *  Revision 1.1  1999/12/25 07:53:35  emrek
 *  bare-bones checkin
 *
 *
 */
public class XMLHelper {
    // static NonValidatingDOMParser parser = new NonValidatingDOMParser();

    static DOMParser parser;

    static {
        parser = new DOMParser();
        try {
            parser.setFeature(
                "http://apache.org/xml/features/dom/defer-node-expansion",
                false);
            parser.setFeature("http://xml.org/sax/features/validation", false);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized static Document GetDocument(InputSource is)
        throws XMLException, IOException {
//        System.err.println(
//            "XMLHelper.GetDocument: called " + count++ +" times");

        try {
            /**
             * // code for IBM's XML4J Parser
             * NonValidatingDOMParser parser = new NonValidatingDOMParser();
             * parser.parse( is );
             * return( parser.getDocument() );
             **/

            // code for Apache's xerces parser
            //            DOMParser parser = new DOMParser();

            //			new Throwable().printStackTrace();
            parser.reset();
            parser.parse(is);

            return parser.getDocument();

        }
        catch (SAXException e) {
            throw new XMLException("", e);
        }
    }

    static int count = 0;

    public static Document GetDocument(URL url)
        throws IOException, XMLException {
        InputSource is = new InputSource(url.openStream());

        return GetDocument(is);
    }

    public static Document GetASCIIDocument(String xml)
        throws XMLException, IOException {

        xml = "<?xml version=\"1.0\" encoding=\"US-ASCII\"?>" + xml;

        InputSource is = new InputSource(new StringReader(xml));

        return GetDocument(is);
    }

    public static Document GetDocument(String xml)
        throws XMLException, IOException {
        InputSource is = new InputSource(new StringReader(xml));

        return GetDocument(is);
    }

    public static Element GetDocumentElement(URL url)
        throws IOException, XMLException {
        return GetDocument(url).getDocumentElement();
    }

    public static Element GetDocumentElement(InputSource is)
        throws IOException, XMLException {
        return GetDocument(is).getDocumentElement();
    }

    public static Element GetASCIIDocumentElement(String xml)
        throws IOException, XMLException {
        return GetASCIIDocument(xml).getDocumentElement();
    }

    public static Element GetDocumentElement(String xml)
        throws IOException, XMLException {
        return GetDocument(xml).getDocumentElement();
    }

    public static Element GetPartialDocumentElement(String xml, String elname)
        throws IOException, XMLException {
        String begintag = "<" + elname + ">";
        String endtag = "</" + elname + ">";

        int idxbegin = xml.indexOf(begintag);

        if (idxbegin == -1) {
            return null;
        }

        int idxend = xml.indexOf(endtag);
        Debug.Assert(idxend > (idxbegin + begintag.length()));

        String partial = xml.substring(idxbegin, idxend + endtag.length());

        return GetDocumentElement(partial);
    }

    public static Element GetChildElement(Element el, String tagname)
        throws XMLException {
        // todo -- getchildrenbytagname is already iterating through
        //         all children of el.  perhaps we should just do that
        //         directly? and return the first 'correct' child we
        //         encounter?  this screws up our assert about the
        //         #of children though.
        NodeList childlist = GetChildrenByTagName(el, tagname);

        if (childlist.getLength() > 1) {
            Debug.Print(
                "xml",
                "element has more than one child with name "
                    + tagname
                    + ":\n"
                    + XMLHelper.ToString(el));
            throw new XMLException(
                "element has more than one child with name "
                    + tagname
                    + "\n"
                    + XMLHelper.ToString(el));
        }

        if (childlist.getLength() == 0) {
            return null;
        }
        else {
            return (Element) childlist.item(0);
        }
    }

    public static String GetText(Element el) {
        StringBuffer str = new StringBuffer("");
        NodeList nodelist = el.getChildNodes();
        int length = nodelist.getLength();

        for (int i = 0; i < length; i++) {
            Node n = nodelist.item(i);

            if ((n instanceof CharacterData) && !(n instanceof Comment)) {
                str.append(((CharacterData) n).getData());
            }
        }

        return str.toString();
    }

    public static String GetChildText(Element el, String childtag)
        throws XMLException {
        Element child = GetChildElement(el, childtag);

        if (child != null) {
            return GetText(child);
        }
        else {
            return null;
        }
    }

    public static Element GetOnlyChild(Element el) {
        NodeList nl = el.getChildNodes();
        int length = nl.getLength();

        for (int i = 0; i < length; i++) {
            Node n = nl.item(i);

            if (n instanceof Element) {
                return (Element) n;
            }
        }

        return null;
    }

    public static NodeList GetChildrenByTagName(Element el, String tagname) {
        Vector v = new Vector();
        NodeList nl = el.getChildNodes();
        int length = nl.getLength();

        for (int i = 0; i < length; i++) {
            Node n = nl.item(i);

            if (n instanceof Element) {
                if (((Element) n).getTagName().equals(tagname)) {
                    v.addElement(n);
                }
            }
        }

        return new MyNodeList(v);
    }

    public static String ToString(Document d) {
        return ToString(d.getDocumentElement());
    }

    public static String ToString(Node n) {
        StringBuffer ret = new StringBuffer();

        if (n instanceof Text) {
            ret.append(((Text) n).getData());
        }
        else if (n instanceof Element) {
            StringBuffer str_attr = new StringBuffer();
            NamedNodeMap attrs = n.getAttributes();

            for (int i = 0; i < attrs.getLength(); i++) {
                Node a = (Attr) attrs.item(i);
                str_attr.append(" ");
                str_attr.append(ToString(a));
            }

            ret.append("<");
            ret.append(((Element) n).getTagName());
            ret.append(str_attr);
            ret.append(">");

            NodeList children = n.getChildNodes();

            for (int i = 0; i < children.getLength(); i++) {
                ret.append(ToString(children.item(i)));
            }

            ret.append("</");
            ret.append(((Element) n).getTagName());
            ret.append(">");
        }
        else if (n instanceof Attr) {
            Attr a = (Attr) n;
            ret.append(a.getName());
            ret.append("=\"");
            ret.append(a.getValue());
            ret.append("\"");
        }
        else if (n instanceof Comment) {
            Comment c = (Comment) n;
            ret.append("<!--");
            ret.append(c.getData());
            ret.append("-->");
        }
        else if (n instanceof CDATASection) {
            CDATASection c = (CDATASection) n;
            ret.append("<![CDATA[");
            ret.append(c.getData());
            ret.append("]]>");
        }
        else {
            Debug.Assert(
                false,
                "Unrecognized XML Node: " + n.getClass().getName());
        }

        return ret.toString();
    }

    public static String ToDocument(Element el) {
        return "<?xml version=\"1.0\"?>" + ToString(el);
    }
}

class MyNodeList implements NodeList {
    Vector v;

    MyNodeList(Vector v) {
        this.v = v;
    }

    public Node item(int index) {
        return (Node) v.elementAt(index);
    }

    public int getLength() {
        return v.size();
    }
}
