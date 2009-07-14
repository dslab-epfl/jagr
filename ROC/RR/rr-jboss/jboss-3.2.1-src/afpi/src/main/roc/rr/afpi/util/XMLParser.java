/*
 * $Id: XMLParser.java,v 1.14 2004/08/26 17:47:39 skawamo Exp $
 */

package roc.rr.afpi.util;

import java.io.*;
import java.util.*;	
import java.text.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.apache.crimson.tree.*;
import roc.rr.Action;
import roc.rr.afpi.*;

import org.jboss.logging.Logger;

/*
 *  XMLParser: perse XML configuration files and extract data from them
 *
 */

public class XMLParser {

    private static Logger log = Logger.getLogger( XMLParser.class );

    /* static instance for this singleton class */
    private static XMLParser parser = null;

    /* private constructor for this singleton class */
    private XMLParser() {}

    /**
     * getInstance() method for this singleton class
     */
    public static XMLParser getInstance () 
    {
	if ( parser == null ) {
	    parser = new XMLParser();
	}
        return parser;
    }

    /**
     * Parses faultload description.
     *
     *  @param  fileName  XML file describing faultload
     *  @return           ArrayList of FaultInjection instances
     *
     * The faultload description have the following format:
     *
     * <fault-injections>
     *   <two-args-injection-type>
     *     <name>comp-name</name>
     *     <time>time </time>
     *   </injection-type>
     *   ...
     *   <memory-leak>
     *     <name>comp-name</name>
     *     <time>time</time>
     *     <amount>amount of leak memory in byte</amount>
     *   </memory-leak>
     *   ...
     *   <infinite-loop>
     *     <name>comp-name</name>
     *     <time>time</time>
     *     <num-of-iloop>numbe of iloop</num-of-iloop>
     *   </infinite-loop>
     *   ...
     *   <jndi-corruption>
     *     <name>comp-name</name>
     *     <time>time</time>
     *     <corruption-type>NULL/BOGUS/INCREMENT/DECREMENT</corruption-type>
     *   </jndi-corruption>
     *
     *   <data-corruption>
     *     <name>BidPK/BuyNowPK/CategoryPK/IDManagerPK/ItemPK/RegionPK/UserPK/SessionAttribute/SessionStatecomp-name</name>
     *     <time>time</time>
     *     <corruption-type>NULL/BOGUS/INCREMENT/DECREMENT</corruption-type>
     *     <num-of-corruption>number of corruption</num-of-corruption>
     *   </data-corruption>
     *
     * </fault-injections>
     *
     *
     * The <time> tag can be one of:
     *   <time-second>seconds from beginning of campaign</time-second>
     *   <time-date>MM/dd/YY HH:mm:ss</time-date>
     *
     * The <two-args-injection-type> tag can be one of:
     *
     *   <nullmap>          </nullmap>
     *   <exception>        </exception>
     *   <error>            </error>
     *   <microreboot>      </microreboot>  (e.g., "SB_ViewItem")
     *   <full-reboot>      </full-reboot>  (e.g., "rubis")
     *   <cancel>           </cancel>
     *   <unbind-jndi>      </unbind-jndi>   
     *   <deadlock>         </deadlock>
     */
    public ArrayList parseFaultInjection(String fileName) 
	throws Exception 
    {
	ArrayList ret = new ArrayList();

	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	DocumentBuilder        db  = dbf.newDocumentBuilder();
	Document               doc = db.parse( new FileInputStream(fileName) );
	
	// Collects exception-injections
	NodeList exceptions = doc.getElementsByTagName( "exception" );
	for( int i=0 ; i < exceptions.getLength() ; i++ )
	{
	    Node n = exceptions.item( i );
	    Vector v = getContents( n );
	    if(v.size() != 2)
		throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, "you have wrong element or lacking element in <exception> block.");
	    FaultInjection injection = new FaultInjection( Action.INJECT_THROWABLE,
							   (String)v.get(0),
							   (Date)v.get(1) );
	    ret.add(injection);
	}

	// Collects error-injections
	NodeList errors = doc.getElementsByTagName( "error" );
	for( int i=0 ; i < errors.getLength() ; i++ )
	{
	    Node n = errors.item(i);
	    Vector v = getContents(n);
	    if(v.size() != 2)
		throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, "you have wrong element or lacking element in <error> block.");
	    FaultInjection injection = new FaultInjection( Action.INJECT_THROWABLE, 
							   (String)v.get(0), 
							   (Date)v.get(1) );
	    ret.add(injection);
	}

	// Collects memory-leak-injections
	NodeList memory_leaks = doc.getElementsByTagName("memory-leak");
	for( int i=0 ; i < memory_leaks.getLength() ; i++ )
        {
	    Node n = memory_leaks.item(i);
	    Vector v = getContents(n);
	    if(v.size() != 3)
		throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, "you have wrong element or lacking element in <memory-leak> block.");
	    FaultInjection injection = new FaultInjection( Action.INJECT_MEMLEAK, 
							   (String)v.get(0),
							   (Date)v.get(1), ((Integer)v.get(2)).intValue() );
	    ret.add(injection);
	}

	// Collects microreboot requests
	NodeList microreboots = doc.getElementsByTagName("microreboot");
	for( int i=0 ; i < microreboots.getLength() ; i++ )
        {
	    Node n = microreboots.item(i);
	    Vector v = getContents(n);
	    if(v.size() != 2)
		throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, "you have wrong element or lacking element in <microreboot> block.");
	    FaultInjection injection = new FaultInjection( Action.MICROREBOOT, 
							   (String)v.get(0),
							   (Date)v.get(1) );
	    ret.add(injection);
	}

	// Collects full-reboot requests
	NodeList fullReboots = doc.getElementsByTagName("full-reboot");
	for( int i=0 ; i < fullReboots.getLength() ; i++ )
        {
	    Node n = fullReboots.item(i);
	    Vector v = getContents(n);
	    if(v.size() != 2)
		throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, "you have wrong element or lacking element in <full-reboot> block.");
	    FaultInjection injection = new FaultInjection( Action.FULL_REBOOT, 
							   (String)v.get(0),
							   (Date)v.get(1) );
	    ret.add(injection);
	}

	// Collects nullmap requests
	NodeList nullmaps = doc.getElementsByTagName("nullmap");
	for( int i=0 ; i < nullmaps.getLength() ; i++ )
        {
	    Node n = nullmaps.item(i);
	    Vector v = getContents(n);
	    if(v.size() != 2)
		throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, "you have wrong element or lacking element in <nullmap> block.");
	    FaultInjection injection = new FaultInjection( Action.SET_NULL_TXINT, 
							   (String)v.get(0),
							   (Date)v.get(1) );
	    ret.add(injection);
	}

	// Collects deadlock request
	NodeList deadlock = doc.getElementsByTagName( "deadlock" );
	for( int i=0 ; i < deadlock.getLength() ; i++ )
	{
	    Node n = deadlock.item( i );
	    Vector v = getContents( n );
	    if(v.size() != 2)
		throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, "you have wrong element or lacking element in <exception> block.");
	    FaultInjection injection = new FaultInjection( Action.DEADLOCK,
							   (String)v.get(0),
							   (Date)v.get(1) );
	    ret.add(injection);
	}

	// Collects cancel requests
	NodeList cancels = doc.getElementsByTagName("cancel");
	for ( int i=0 ; i < cancels.getLength() ; i++ ) 
	{
	    Node n = cancels.item(i);
	    Vector v = getContents(n);
	    if(v.size() != 2)
		throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, "you have wrong element or lacking element in <cancel> block.");
	    FaultInjection injection = new FaultInjection( Action.NO_ACTION, 
							   (String)v.get(0),
							   (Date)v.get(1) );
	    ret.add(injection);
	}
	
	// Collects unbind-jndi requests
	NodeList unbind = doc.getElementsByTagName( "unbind-jndi" );
	for( int i=0 ; i < unbind.getLength() ; i++ )
	{
	    Node n = unbind.item( i );
	    Vector v = getContents( n );
	    if(v.size() != 2)
		throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, "you have wrong element or lacking element in <exception> block.");
	    FaultInjection injection = new FaultInjection( Action.UNBIND_NAME,
							   (String)v.get(0),
							   (Date)v.get(1) );
	    ret.add(injection);
	}

	// Collects infinite loop
	NodeList iloop = doc.getElementsByTagName( "infinite-loop" );
	for( int i=0 ; i < iloop.getLength() ; i++ )
	{
	    Node n = iloop.item( i );
	    Vector v = getContents( n );
	    if(v.size() != 3)
		throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, "you have wrong element or lacking element in <exception> block.");
	    FaultInjection injection = new FaultInjection( Action.INFINITE_LOOP,
							   (String)v.get(0),
							   (Date)v.get(1),
							   ((Integer)v.get(2)).intValue());

	    ret.add(injection);
	}

	// Collects jndi-corruption
	NodeList jndi = doc.getElementsByTagName("jndi-corruption");
	for( int i=0 ; i < jndi.getLength() ; i++ )
        {
	    Node n = jndi.item(i);
	    Vector v = getContents(n);
	    if(v.size() != 3)
		throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, "you have wrong element or lacking element in <jndi-corruption> block.");
	    FaultInjection injection 
		= new FaultInjection( Action.CORRUPT_JNDI,
				      (String)v.get(0),
				      (Date)v.get(1), 
				      (String)v.get(2),
				      0);
	    ret.add(injection);
	}

	// Collects data-corruption
	NodeList data = doc.getElementsByTagName("data-corruption");
	for( int i=0 ; i < data.getLength() ; i++ )
        {
	    Node n = data.item(i);
	    Vector v = getContents(n);
	    if(v.size() != 4)
		throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, "you have wrong element or lacking element in <jndi-corruption> block.");
	    FaultInjection injection 
		= new FaultInjection( Action.CORRUPT_DATA,
				      (String)v.get(0),
				      (Date)v.get(1), 
				      (String)v.get(2),
				      ((Integer)v.get(3)).intValue());
	    ret.add(injection);
	}

	log.debug("FaultLoad: "+ret);
	return ret;
    }


    private Vector getContents(Node n) throws DOMException, ParseException{

	    String ejbName = "";
	    Date time = new Date();
	    Integer amount = null;
	    String ctype = null;
	    Integer ctime = null;

	    //ejb element
	    Node ejbTagNode = n.getFirstChild().getNextSibling();
	    if(ejbTagNode == null) {
		throw new DOMException(DOMException.SYNTAX_ERR,
				       "Can't parse XML file. element missing.");
	    }
            Node ejbTextNode = ejbTagNode.getFirstChild();
            if ( ejbTagNode.getNodeType() == Node.ELEMENT_NODE
                 && ejbTagNode.getNodeName().trim() == "name"
                 && ejbTextNode.getNodeType() == Node.TEXT_NODE ) {
                ejbName = ejbTextNode.getNodeValue().trim();
            } else {
                throw new DOMException(DOMException.SYNTAX_ERR,
                                       "Can't parse XML file");
            }
                                                                           
            // time element
            Node timeTagNode = ejbTagNode.getNextSibling().getNextSibling();
	    if(timeTagNode == null) {
		throw new DOMException(DOMException.SYNTAX_ERR,
				       "Can't parse XML file. element missing.");
            }
            Node timeTextNode = timeTagNode.getFirstChild();
	    SimpleDateFormat format = new SimpleDateFormat("MM/dd/yy HH:mm:ss");

            if ( timeTagNode.getNodeType() == Node.ELEMENT_NODE
                 && timeTextNode.getNodeType() == Node.TEXT_NODE ) {
		if(timeTagNode.getNodeName().trim() == "time-second"){
		    long t = (new Integer(timeTextNode.getNodeValue().trim()).longValue()*1000 
			      + System.currentTimeMillis());
		    time = new Date(t);
		} else if ( timeTagNode.getNodeName().trim() == "time-date" ) {
		    try{
			time = format.parse(timeTextNode.getNodeValue().trim());
		    } catch (ParseException e){
			throw e;
		    }
		} else {
		    throw new DOMException(DOMException.SYNTAX_ERR,
					   "Can't parse XML file.");
		}
	    } else {
		throw new DOMException(DOMException.SYNTAX_ERR,
				       "Can't parse XML file");
	    }

	    // memory leak element or corruption elements
	    Node leakTagNode = timeTagNode.getNextSibling().getNextSibling();
	    if(leakTagNode != null){
		Node leakTextNode = leakTagNode.getFirstChild();
		
		if ( leakTagNode.getNodeType() == Node.ELEMENT_NODE
		     && leakTextNode.getNodeType() == Node.TEXT_NODE
		     && (leakTagNode.getNodeName().trim() == "amount" ||
			 leakTagNode.getNodeName().trim() == "num-of-iloop"))
		{
		    amount = new Integer(leakTextNode.getNodeValue().trim());
		} else if ( leakTagNode.getNodeType() == Node.ELEMENT_NODE
			    && leakTextNode.getNodeType() == Node.TEXT_NODE
			    && leakTagNode.getNodeName().trim() 
			    == "corruption-type" ) {
		    ctype = leakTextNode.getNodeValue().trim();
		} else {
		    throw new DOMException(DOMException.SYNTAX_ERR,
					   "Can't parse XML file");
		}
	    }

	    // corruption time element
	    Node cTimeTagNode = leakTagNode.getNextSibling().getNextSibling();
	    if(cTimeTagNode != null){
		Node cTimeTextNode = cTimeTagNode.getFirstChild();
		
		if ( cTimeTagNode.getNodeType() == Node.ELEMENT_NODE
		     && cTimeTextNode.getNodeType() == Node.TEXT_NODE
		     && (cTimeTagNode.getNodeName().trim() == "corruption-time"))
		{
		    ctime = new Integer(cTimeTextNode.getNodeValue().trim());
		} else {
		    throw new DOMException(DOMException.SYNTAX_ERR,
					   "Can't parse XML file");
		}
	    }
	    
	    Vector ret = new Vector();
	    ret.add(ejbName);
	    ret.add(time);
	    if(amount != null)
		ret.add(amount);
	    else if ( ctype != null ) {
		ret.add(ctype);
		if (ctime != null) {
		    ret.add(ctime);
		}
	    }
	    
	    return ret;
    }


    /*
     * Read TTL configuration of each jar file and create TTL object.
     *
     * XML File Format:
     *
     * <EJBTTLs>
     *   <EJBTTL>
     *     <jar> jar_file_name.jar </jar>
     *     <TTL-second> second </TTL-second>
     *   </EJBTTL>
     *   ...
     * </EJBTTLs>
     *
     *
     *  @param  fileName  XML file describing TTL
     *  @return           TTL Object
     */
    public static TTL parseTTL(String fileName) 
	throws DOMException, Exception 
    {
	String message = "[Rolling Microrejuvenation]\n";
	TTL ttlMap = new TTL();

	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	DocumentBuilder db = dbf.newDocumentBuilder();
	Document doc = db.parse(new FileInputStream(fileName));
	NodeList lst = doc.getElementsByTagName("EJBTTL");
	for(int i=0; i<lst.getLength(); i++){
	    Node n = lst.item(i);
	    
	    String jarName="";
	    String interval="";
	    
	    // jar element 
	    Node jarTagNode = n.getFirstChild().getNextSibling();
	    Node jarTextNode = jarTagNode.getFirstChild();
	    if ( jarTagNode.getNodeType() == Node.ELEMENT_NODE 
		 && jarTagNode.getNodeName() == "jar" 
		 && jarTextNode.getNodeType() == Node.TEXT_NODE ) {
		jarName = jarTextNode.getNodeValue();
	    } else {
		throw new DOMException(DOMException.SYNTAX_ERR,
				       "Can't parse "+fileName);
	    }

	    // TTL-second element 
	    Node ttlTagNode = jarTagNode.getNextSibling().getNextSibling();
	    Node ttlTextNode = ttlTagNode.getFirstChild();
	    if ( ttlTagNode.getNodeType() == Node.ELEMENT_NODE 
		 && ttlTagNode.getNodeName() == "TTL-second" 
		 && ttlTextNode.getNodeType() == Node.TEXT_NODE ) {
		interval = ttlTextNode.getNodeValue();
	    } else {
		throw new DOMException(DOMException.SYNTAX_ERR,
				       "Can't parse "+fileName);
	    }

	    long ttl = Long.valueOf(interval).longValue();
	    ttlMap.add(jarName, ttl*1000);
	    message += "TTL: "+ttl+" sec / "+jarName+"\n";
	}
	log.debug(message);

	return ttlMap;
    }

}

