/**
    Copyright (C) 2004 Emre Kiciman and Stanford University

    This file is part of Pinpoint

    Pinpoint is free software; you can distribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation; either version 2.1 of the License, or
    (at your option) any later version.

    Pinpoint is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with Pinpoint; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
**/
package roc.pinpoint.injection;

// marked for release 1.0

import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.util.Set;
import java.util.Map;
import java.util.HashSet;
import java.util.Iterator;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import swig.util.StringHelper;
import swig.util.XMLException;
import swig.util.XMLHelper;

/**
 * a FaultConfig contains a set of fault triggers, and methods to
 * read these fault triggers from a file
 *
 */
public class FaultConfig {

    String name;
    Set faultTriggers; // set of roc.pinpoint.injection.FaultTrigger;

    public FaultConfig() {
        this("Untitled");
    }

    public FaultConfig(String name) {
        this.name = name;
        faultTriggers = new HashSet();
    }

    public static FaultConfig ParseFaultConfig(File configDataFile)
        throws XMLException, IOException {
        FileInputStream fis = new FileInputStream(configDataFile);
        String configData = StringHelper.loadString(fis);

        return ParseFaultConfig(configData);
    }

    public static FaultConfig ParseFaultConfig(String configData)
        throws XMLException {
        try {
            return ParseFaultConfig(
                XMLHelper.GetASCIIDocumentElement(configData));
        }
        catch (IOException e) {
            // This should not happen!
            e.printStackTrace();
            throw new RuntimeException("Assert Failure", e);
        }
    }

    public static FaultConfig ParseFaultConfig(Element configData)
        throws XMLException, IOException {
        FaultConfig ret = new FaultConfig();

        ret.name = XMLHelper.GetChildText(configData, "name");

        NodeList nl =
            XMLHelper.GetChildrenByTagName(configData, "faultTrigger");
        for (int i = 0; i < nl.getLength(); i++) {
            Element eFt = (Element)nl.item(i);
            FaultTrigger ft = FaultTrigger.ParseFaultTrigger(eFt);
            ret.addFaultTrigger(ft);
            System.err.println("faultconfig: read faulttrigger");
        }

        return ret;
    }

    public synchronized FaultTrigger checkFaultTriggers(Map currComponent) {

        Iterator iter = faultTriggers.iterator();
        while (iter.hasNext()) {
            FaultTrigger ft = (FaultTrigger)iter.next();
            if (ft.matches(currComponent)) {
                return ft;
            }
        }
        return null;
    }

    public synchronized void addFaultTrigger(FaultTrigger ft) {
        faultTriggers.add(ft);
    }

    public synchronized void removeAllFaultTriggers() {
        faultTriggers.clear();
    }

}
