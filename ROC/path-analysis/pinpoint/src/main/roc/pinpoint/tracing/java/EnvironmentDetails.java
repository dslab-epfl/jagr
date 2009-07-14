package roc.pinpoint.tracing.java;

import java.net.InetAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import roc.pinpoint.tracing.Observation;
import roc.pinpoint.tracing.jboss.JMSObservationPublisher;

/**
 * helper class for retrieving relevant details about the operating environment.
 * @author emrek
 *
 */
public class EnvironmentDetails {

    public static final long REPORT_PERIOD = 5000;

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
	    Long id = new Long( new Random().nextLong());
	    detailsCache.put( "detailsId", id );
	    componentid = new HashMap();
	    componentid.put( "mergeWithId", id );
            FillInIPInfo(detailsCache);
            detailsCache = Collections.unmodifiableMap(detailsCache);
        }
        return detailsCache;
    }

        

    public static void ReportEnvironmentDetails() {
	Observation obs = 
	    new Observation( Observation.EVENT_COMPONENT_DETAILS,
			     null,
			     0,
			     GetEnvironmentDetails(),
			     null,
			     null );

	// todo: shouldn't be hard-coding JMSObservationPublisher stuff
	//       into a java-generic package.
	JMSObservationPublisher.GlobalSend( obs );
	reportAfterTime = System.currentTimeMillis() + REPORT_PERIOD;
    }

    public static Map GetDetails() {
	if(( componentid == null ) ||
	   ( reportAfterTime < System.currentTimeMillis() )) {
	    ReportEnvironmentDetails();
	}

	return componentid;
    }

}
