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
package roc.pinpoint.tracing.java;

// marked for release 1.0

import java.net.InetAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import roc.pinpoint.tracing.Observation;
import roc.pinpoint.tracing.GlobalObservationPublisher;
import roc.pinpoint.tracing.SimpleObjectPool;

/**
 *
 * helper class for retrieving relevant details about the operating environment.
 * @author emrek
 *
 */
public class EnvironmentDetails {

    public static final long REPORT_PERIOD = 300000;

    /**
     * names of system properties we want to retrieve
     */
    private static String[] PROPERTYNAMES =
        {
            "java.class.version",
            "java.vm.version",
            "java.vm.vendor",
            "java.specification.version",
            "java.vm.specification.version",
            "java.vm.info",
            "program.name",
            "os.arch",
            "os.name",
            "os.version",
            "jboss.server.name" };

    private static Map detailsCache;
    private static Map componentid;
    private static long reportAfterTime = 0;

    static void FillInIPInfo(Map details) {
        try {
            InetAddress localhost = InetAddress.getLocalHost();
            details.put("ipaddress", localhost.getHostAddress());
            details.put("hostname", localhost.getCanonicalHostName());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * get environment details, such as JVM version, IP address, etc.
     * @return Map map of environment details
     */
    public static Map GetEnvironmentDetails() {
        if (detailsCache == null) {
            detailsCache = new HashMap();
            for (int i = 0; i < PROPERTYNAMES.length; i++) {
                detailsCache.put(
                    PROPERTYNAMES[i],
                    System.getProperty(PROPERTYNAMES[i]));
            }
            Long id = new Long(new Random().nextLong());
            detailsCache.put("detailsId", id);
            componentid = new HashMap();
            componentid.put("mergeWithId", id);
            FillInIPInfo(detailsCache);
            detailsCache = Collections.unmodifiableMap(detailsCache);
        }
        return detailsCache;
    }

    public static void ReportEnvironmentDetails() {
        Observation obs =
            new Observation(
                Observation.EVENT_COMPONENT_DETAILS,
                null,
                0,
                GetEnvironmentDetails(),
                null,
                null);

        GlobalObservationPublisher.Send(obs);
        reportAfterTime = System.currentTimeMillis() + REPORT_PERIOD;
    }

    public static Map GetDetails() {
        if ((componentid == null)
            || (reportAfterTime < System.currentTimeMillis())) {
            ReportEnvironmentDetails();
        }

        return componentid;
    }

    /**
     * helper function for generationg origin information for pinpoint observations
     * and testing for triggering fault injections
     * @param ejbName
     * @param methodName
     * @return
     */
    public static Map GenerateOriginInfo(String type, String name, String methodName) {
        Map ret = (HashMap)SimpleObjectPool.HASHMAP_POOL.get();
	ret.putAll( GetDetails());
	if( name != null ) {
	    ret.put("name", name);
	}
	if( methodName != null ) 
	    ret.put("methodName", methodName);
        ret.put("type", type );
        // todo: add method arguments, and other info, to map
        return ret;
    }

    public static Map GenerateOriginInfo( String ejbName, String methodName ) {
	return GenerateOriginInfo( roc.pinpoint.tracing.TracingHelper.TYPE_EJB, ejbName, methodName );
    }

    public static Map GenerateCompleteOriginInfo(String type, String name, String methodName ) {
	Map ret = GenerateOriginInfo(type,name,methodName);
	ret.putAll( GetEnvironmentDetails() );
	return ret;
    }

    public static Map GenerateCompleteOriginInfo(String ejbName, String methodName ) {
	Map ret = GenerateOriginInfo(roc.pinpoint.tracing.TracingHelper.TYPE_EJB, ejbName,methodName);
	ret.putAll( GetEnvironmentDetails() );
	return ret;
    }



}
