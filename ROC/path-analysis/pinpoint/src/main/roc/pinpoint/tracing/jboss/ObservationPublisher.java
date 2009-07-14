package roc.pinpoint.tracing.jboss;

import roc.pinpoint.tracing.*;
import roc.pinpoint.tracing.io.*;

/**
 * A central location for publishing observations collected from JBoss. 
 *
 * @author  <A HREF="http://www.cs.berkeley.edu/~mikechen/">Mike Chen</A>
 *              (<A HREF="mailto:mikechen@cs.berkeley.edu">
 *                mikechen@cs. berkeley.edu</A>)
 */

public class ObservationPublisher {
    static FileSerializer fileout = new FileSerializer();

    public static void publish(Observation obs) {

	if (Config.JMS_REPORTING) {
	    JMSObservationPublisher.GlobalSend( obs );
	}

	if (Config.LOCAL_FILE_REPORTING) {
	    //System.out.println("dumping obs to file");
	    try {
		//System.out.println(">>>> obs.attributes: " + obs.attributes);

		fileout.writeObject(obs);
	    }
	    catch (Exception e) {
		e.printStackTrace();
	    }
	}
    }

}

