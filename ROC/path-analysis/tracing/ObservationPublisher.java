package tracing;

import java.io.Serializable;
import java.net.*;
import java.util.*;
import javax.jms.*;
import javax.naming.*;

/**
 * A JMS message that contains the basic observations about a request.
 * No complex data structures are used to reduce serialization overhead.   
 *
 * @author  <A HREF="http://www.cs.berkeley.edu/~mikechen/">Mike Chen</A>
 *              (<A HREF="mailto:mikechen@cs.berkeley.edu">mikechen@cs.berkeley.edu</A>)
 * @version $Id: ObservationPublisher.java,v 1.4 2002/12/13 16:30:35 emrek Exp $
 */ 


public class ObservationPublisher {
    static TopicConnection  conn = null;
    static TopicSession session = null;
    static Topic topic = null;
    static TopicPublisher send = null;
    static Object sync = new Object();
    static String hostname = null;

    public ObservationPublisher() {
    }
    
    public static void setupPubSub() throws JMSException, NamingException, UnknownHostException {
	if (hostname == null) {
	    // Setup the pub/sub connection, session

	    String jndiPropsFilename = null;

	    jndiPropsFilename = System.getProperty( "roc.tracing.propsfilename" );
	    if( jndiPropsFilename == null ) 
		jndiPropsFilename = "observation.jndi.properties";

	    try {
		Properties props = new Properties();
		File jndiProps = new File( jndiPropsFilename );
		props.load( new FileInputStream( jndiProps ));

		InitialContext iniCtx = new InitialContext( props );
		Object tmp = iniCtx.lookup("ConnectionFactory");
		TopicConnectionFactory tcf = (TopicConnectionFactory) tmp;
		conn = tcf.createTopicConnection();
		topic = (Topic) iniCtx.lookup("topic/testTopic");
		session = conn.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
		conn.start();
		send = session.createPublisher(topic);
		
	    }
	    catch( FileNotFoundException fnfe ) {
		System.out.println( "Couldn't find properties file: " + jndiPropsFilename );
	    }
	    
	    //// get local host name
	    InetAddress address = InetAddress.getLocalHost();
	    hostname = address.getHostName();
	    /*
	    int dot = hostname.indexOf(".");
	    if (dot != -1) 
		hostname = hostname.substring(0, dot);
	    */
	}
    }
    
    public static void publish(String requestId, int seqNum, String name, long timestamp) throws JMSException, NamingException, UnknownHostException {
	RequestObservation obs = new RequestObservation();
	obs.requestId = requestId;
	obs.seqNum = seqNum;
	obs.name   = name;
	obs.timestamp = timestamp;
	obs.location = hostname;
	publish(obs);
    }

    public static void publish(String requestId, int seqNum, int returnSeqNum, String name, long timestamp, boolean success) throws JMSException, NamingException, UnknownHostException {
	RequestObservation obs = new RequestObservation();
	obs.requestId = requestId;
	obs.seqNum = seqNum;
	obs.returnSeqNum = returnSeqNum;
	obs.name   = name;
	obs.timestamp = timestamp;
	obs.location = hostname;
	obs.latency = System.currentTimeMillis() - timestamp;
	obs.success = success;
	publish(obs);
    }


    public static void publish(Serializable obs) throws JMSException, NamingException, UnknownHostException {
	if (hostname == null) {
	    synchronized (sync) {
		setupPubSub();
	    }
	}
	ObjectMessage message = session.createObjectMessage(obs);
	send.publish(message);
	//System.out.println("publish() sent msg = " + obs);
    }

    public static void stop() throws JMSException {
	System.out.println("stopping");
	send.close();
	conn.stop();
	session.close();
	conn.close();
    }

    public static void main(String args[]) throws Exception {
	System.out.println("Begin , now=" + new Date());
	ObservationPublisher client = new ObservationPublisher();
	client.publish("Hello, it's the ObservationPublisher!");
	client.stop();
	System.out.println("End ObservationPublisher");
	System.exit(0);
    }

}


