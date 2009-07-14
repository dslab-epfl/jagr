import org.w3c.dom.*;

import java.lang.Exception.*;

import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

import java.io.*;

import java.util.*;

public class ConfigParser
{

    List traceInfoList;

    public ConfigParser()
    {
	traceInfoList = new ArrayList();


    }
    
    void loadXMLConfiguration(String configFile, boolean sessionrequired )
    {
	try {
	    File file = new File(configFile);
	    
	    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    dbf.setValidating(false);
	    
	    DocumentBuilder db = null;
	    Document doc = null;
	    
	    try {
		db = dbf.newDocumentBuilder();
	    } catch(ParserConfigurationException e) { e.printStackTrace(); }
	    
	    doc = db.parse(file);
	    
	    Element root = doc.getDocumentElement();



	LoadTraceData(root.getElementsByTagName("trace"));
	LoadSessionData(root.getElementsByTagName("session"));

	}
	catch (SAXException e) {
	    System.err.println("XML parse err:" + e.getMessage());
	}
	catch (IOException e) {
            System.err.println("IO err:" + e.getMessage());
        }    }

    void LoadTraceData(NodeList traces)
    {
	String filename = null, classname = null;

	System.out.println("There are " + traces.getLength() + " traces.\n");
	
	for(int i=0; i < traces.getLength(); i++)
	    {
		NodeList traceElements = traces.item(i).getChildNodes();
		filename = null;
		classname = null;

		System.out.println("Current element has " + traceElements.getLength() + " children.\n");

		for(int j=0; j < traceElements.getLength(); j++)
		    {
			Node elem = traceElements.item(j);
			if(elem.getNodeName().equalsIgnoreCase("filename"))
			    filename = elem.getFirstChild().getNodeValue();
			else if(elem.getNodeName().equalsIgnoreCase("classname"))
			    classname = elem.getFirstChild().getNodeValue();
		    }
		if(filename != null && classname != null)
		    System.out.println("Trace data: " + filename + " " + classname + "\n");

//		traceInfoList.add(filename, new TraceInfo(filename, classname));
	    }
    }

    void LoadSessionData(NodeList sessions)
    {
	String id = null, classname = null;
	Map sessionArgs;
	
	System.out.println("There are " + sessions.getLength() + " sessions.\n");

	for(int i=0; i < sessions.getLength(); i++)
	    {
		NodeList sessionElements = sessions.item(i).getChildNodes();
		id = null;
		classname = null;
		
		System.out.println("Current element has " + sessionElements.getLength() + " children.\n");

		for(int j=0; j < sessionElements.getLength(); j++)
		    {
			Node elem = sessionElements.item(j);
			if(elem.getNodeName().equalsIgnoreCase("id"))
			    id = elem.getFirstChild().getNodeValue();
			else if(elem.getNodeName().equalsIgnoreCase("classname"))
			    classname = elem.getFirstChild().getNodeValue();
		    }
		if(id != null && classname != null)
		    System.out.println("Session data: " + id + " " + classname + "\n");
	    }
    }
    

    public static void main(String [] args) {
	
	ConfigParser parser = new ConfigParser();

	parser.loadXMLConfiguration(args[0], false);
    }
}
