package tracing;

import java.util.*;
import javax.jms.*;
import javax.naming.*;

/**
 * A JMS message that contains the basic observations about a request.
 * No complex data structures are used to reduce serialization overhead.   
 *
 * @author  <A HREF="http://www.cs.berkeley.edu/~mikechen/">Mike Chen</A>
 *              (<A HREF="mailto:mikechen@cs.berkeley.edu">mikechen@cs.berkeley.edu</A>)
 * @version $Id: ObservationListener.java,v 1.4 2002/11/26 00:07:01 mikechen Exp $
 */ 


public class ObservationListener {
    TopicConnection  conn = null;
    TopicSession session = null;
    Topic topic = null;

    public ObservationListener() {
    }
    
    public void setupPubSub() throws JMSException, NamingException {
	InitialContext iniCtx = new InitialContext();
	Object tmp = iniCtx.lookup("ConnectionFactory");
	TopicConnectionFactory tcf = (TopicConnectionFactory) tmp;
	conn = tcf.createTopicConnection();
	topic = (Topic) iniCtx.lookup("topic/testTopic");
	session = conn.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
	conn.start();
    }
    
    public void recvSync() throws JMSException, NamingException
    {
	System.out.println("Begin recvSync");
	// Setup the pub/sub connection, session
	setupPubSub();
	// Wait upto 5 seconds for the message
	//TopicSubscriber recv = session.createDurableSubscriber(topic, "chap6-ex1dtps");

	StateDependencyAnalyzer stateDependency = new StateDependencyAnalyzer();
	DependencyGraphs dependencyGraphs = new DependencyGraphs();

	TopicSubscriber recv = session.createSubscriber(topic);
	int count = 0;
	while (true) {
	    Message msg = recv.receive();
	    System.out.println("DurableTopicRecvClient.recv, msgt="+ ((ObjectMessage)msg).getObject());
	    stateDependency.addObservation((RequestObservation)(((ObjectMessage)msg).getObject()));
	    dependencyGraphs.addObservation((RequestObservation)(((ObjectMessage)msg).getObject()));

	    count++;
	    if (count % 10 == 0) {
		count = 0;
		stateDependency.printCurrentDependency();
		
	    }
	    if (count % 5 == 0) {
		dependencyGraphs.save();
	    }
	}
	/*
	if( msg == null )
	    System.out.println("Timed out waiting for msg");
	else
	    System.out.println("DurableTopicRecvClient.recv, msgt="+msg);
	*/
    }

    public void stop()  throws JMSException {
	conn.stop();
	session.close();
	conn.close();
    }

    public static void main(String args[]) throws Exception {
	System.out.println("Begin , now=" + new Date());
	ObservationListener client = new ObservationListener();
	client.recvSync();
	client.stop();
	System.out.println("End ObservationListener");
	System.exit(0);
    }

}


