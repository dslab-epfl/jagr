//
// $Id: ExceptionInjector.java,v 1.8 2003/04/08 16:30:34 steveyz Exp $
//

package org.jboss.RR;

import org.jboss.ejb.Container;
import java.util.*;
import java.io.*;
import java.lang.*;
import java.lang.reflect.*;
import org.apache.log4j.Category;

public class ExceptionInjector
{
   private final static String FILENAME = "/tmp/injected_exceptions";
    
   private HashSet ignoreNames = null;  // EJB names to be ignored
   private Hashtable containers = null; // deployed EJB containers
   private LinkedList remainingMethods = null; // methods to be fault-injected
   private LinkedList remainingExceptions = null; // exceptions to be injected
   private LinkedList additionalExceptions; // non-app-declared exceptions to inject
   private boolean loadFromFile = false;  // load faults from file or not ?
   private Category log;  // log4j output
   private boolean done = false;  // let FaultInjector know if all exceptions have been used up


   /**
    * Constructs a fault injector specific for Java exceptions.
    * Initializes the logging system for this class, the set of EJBs
    * names to ignore during reflection, and a list of
    * non-application-declared exceptions to inject.
    *
    * @param  
    * @return
    */
   public ExceptionInjector() 
      { 
	 //
	 // Initialize logging system
	 //
	 log = Category.getInstance("org.jboss.RR.ExceptionInjector");
	
	 //
	 // Initialize the set of EJB names we choose to ignore
	 //
	 ignoreNames = new HashSet();
	 ignoreNames.add("ejbActivate");
	 ignoreNames.add("ejbLoad");
	 ignoreNames.add("ejbPassivate");
	 ignoreNames.add("ejbRemove");
	 ignoreNames.add("ejbCreate");
	 ignoreNames.add("ejbPostCreate");
	 ignoreNames.add("ejbFindByPrimaryKey");
	 ignoreNames.add("getHandle");
	 ignoreNames.add("getEJBHome");
	 ignoreNames.add("toString");
	 ignoreNames.add("hashCode");
	 ignoreNames.add("ejbStore");
	 ignoreNames.add("setEntityContext");
	 ignoreNames.add("unsetEntityContext");
	 ignoreNames.add("getClass");
	 ignoreNames.add("equals");
	 ignoreNames.add("wait");
	 ignoreNames.add("notifyAll");
	 ignoreNames.add("notify");
	 ignoreNames.add("getPrimaryKey");
	 ignoreNames.add("remove");

	 //
	 // Initialize the list of additional exceptions we inject.
	 // In addition to the ones shown here, may want to consider
	 // java.lang.RuntimeException, java.lang.VirtualMachineError,
	 // and java.lang.ThreadDeath.  NullPointerException is a
	 // RuntimeException that is sometimes caught by code, hence
	 // we should inject it separately from RuntimeException.
	 // VirtualMachineError is the superclass for InternalError,
	 // OutOfMemoryError, StackOverflowError, and UnknownError.
	 //
	 additionalExceptions = new LinkedList();
	 additionalExceptions.add("java.lang.NullPointerException");
	 additionalExceptions.add("java.lang.OutOfMemoryError");
      }


   /**
    * Allow the FaultInjector to test when the set of exceptions to
    * inject has been exhausted.
    *
    * @param
    * @return      <i>true</i>, if no more exceptions, <i>false</i> otherwise
    */
   public boolean isDone()
      {
	 return done;
      }
   
   
   /**
    * Return number of exceptions left to be injected.
    *
    * @param
    * @return      number of exceptions
    */
   public int exceptionsLeft()
      {
	 return remainingExceptions.size();
      }
   


   /**
    * This method is used to report a new EJB container to the
    * ExceptionInjector.  It will use reflection to extract the EJB's
    * declared exceptions and adds them, along with a few extra
    * exceptions, to the list of injectable exceptions.
    *
    * @param   cont the Container being reported
    * @return  
    */
   public void newContainer( Container cont )
      {
	 if ( remainingExceptions == null ) 
            remainingExceptions = new LinkedList();

	 if ( containers == null ) 
            containers = new Hashtable();

	 if ( remainingMethods == null ) 
            remainingMethods = new LinkedList();

	 String ejbName = cont.getBeanMetaData().getEjbName();

	 //
	 // We ignore EJBs that don't belong to the app per se
	 //
	 if ( ejbName.equals("MEJB") || ejbName.equals("jmx/ejb/Adaptor") ) 
            return;

         containers.put(ejbName, cont); // place in hash map

	 if( !loadFromFile )
	 {      
	    Class ejbClass = cont.getBeanClass();
	    Method[] methods = ejbClass.getMethods();

	    //
	    // Extract method and exception name.  Ignore methods that would
	    // never get invoked during AFPI.
	    //
	    for ( int i=0; i < methods.length; i++ )
	    {
	       if( !ignoreNames.contains(methods[i].getName()) )
	       {
		  String mName = ejbName + "_" + methods[i].getName();
		  if ( !remainingMethods.contains(mName) )
		  {
		     remainingMethods.add(mName);
		  }

		  //
		  // Add all app-level exceptions
		  //
		  Class[] exceptionTypes = methods[i].getExceptionTypes();
		  for(int j=0; j < exceptionTypes.length; j++)
		  {  
		     String sHash = conHash(ejbName, methods[i].getName(), 
					    exceptionTypes[j].getName());
		     if ( !remainingExceptions.contains(sHash) )
		     {
			remainingExceptions.add(sHash);
		     }
		  } 

		  //
		  // Now put in the additional exceptions
		  //
		  for (Iterator exIter = additionalExceptions.iterator() ;
		       exIter.hasNext() ; )
		  {
		     String exName = (String) exIter.next();
		     String sHash = conHash(ejbName, methods[i].getName(), exName);
		     if ( !remainingExceptions.contains(sHash) )
		     {
			remainingExceptions.add(sHash);
		     }
		  }
	       }
	    }
	 }
      }



   /**
    * Injects the first exception on the list of remaining exceptions.
    *
    * @param
    * @return  
    */
   public void doInjection()
      {
	 if ( containers == null ) 
	 {
	    log.info("CONTAINERS ARE NULL"); 
	    return;
	 }

	 // if have reached end of list, load list from file.
	 if ( remainingExceptions.isEmpty() ) 
	 { 
	    load();
	    // Set done to true signalling that the test is over
	    // PKEYANI : this only works for the sosp experiment
	    // where we are using the variable done to test if 
	    // the set of experiments is done for keeping track
	    // of time
	    done = true;
	 }
	
	 String sMap = (String)(remainingExceptions.getFirst());
	 int index = sMap.indexOf("_");
	 String sCon = sMap.substring(0, index);
	 Container con = (Container)containers.get(sCon);
         
	 if ( con==null ) 
	 {
	    log.info("No Container found: " + sCon);
	    return;
	 }
	 else
	 {   
	    int secondIndex = sMap.indexOf("_", index+1);
	    String met = sMap.substring(index+1, secondIndex);
	    
	    String exceptionS = sMap.substring(secondIndex+1, sMap.length());
	    log.info("Scheduling exception <" + exceptionS + "> into " + sCon + ":" + met + "()");
	    int result = -1;
	    try
	    {
	       result = con.injectFault(met, Class.forName(exceptionS));
	    }
	    catch ( java.lang.ClassNotFoundException e )
	    {
	       result = -1;
	    }
	    if ( result != 1 ) 
	    {
	       log.info("FAILURE TO INJECT FAULT");
	       return;  
	    }
	    else
	    {
	       remainingExceptions.removeFirst();
	    }
	 }
      }
   


   /**
    * Saves exception list to a predetermined file (currently
    * /tmp/injected_exceptions).
    *
    * @param
    * @return 
    */
   public void save()
      {
	 save(FILENAME);
      }
  

   /**
    * Saves exception list to a file containing each
    * (EJB,method,exception) tuple on one line.
    *
    * @param  filename Name of the file to save to
    * @return 
    */
   public void save( String filename )
      {
	 try {
	    FileWriter fw = new FileWriter(new File(filename));

	    for( int i = 0; i < remainingExceptions.size(); i++ )
	    {
	       fw.write(remainingExceptions.get(i).toString() + "\n");
	    }

	    fw.close();
	 }
	 catch (Exception e) 
	 {
	    e.printStackTrace();
	 }  
      }



   /**
    * Loads exception list from a predetermined file (currently
    * /tmp/injected_exceptions).
    *
    * @param
    * @return 
    */
   public void load()
      {
	 load(FILENAME);
      }



   /**
    * Loads exception list from a file containing each
    * (EJB,method,exception) tuple on one line.
    *
    * @param  filename Name of the file to load from
    * @return 
    */
   public void load( String filename )
      {
	 try {
	    RandomAccessFile raf = new RandomAccessFile(filename, "r");
	    remainingExceptions  = new LinkedList();

	    for (String line=raf.readLine() ; line != null ; line=raf.readLine())
	    {
	       remainingExceptions.add(line);
	    }
	    raf.close();
	 }
	 catch (FileNotFoundException e) 
	 {
	    loadFromFile = false;
	 }
	 catch (Exception f)
	 {
	    f.printStackTrace();
	 }	
      }
                      
   /**
    * Private method to concatenate containers, methods and exceptions
    * in a uniform way.
    *
    * @param   conName EJB container name
    *          methodName name of method
    *          exceptionName name of exception
    * @return  the concatenation of the three parameters
    */
   private String conHash(String conName, String methodName, String exceptionName)
      {
	 return conName + "_" + methodName + "_" + exceptionName;
      }
}
