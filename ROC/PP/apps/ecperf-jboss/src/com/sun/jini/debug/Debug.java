/*
 * 
 * Copyright 2000 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * The contents of this file are subject to the Sun Community Source License
 * v 3.0/Jini Technology Specific Attachment v 1.0 (the "License"). You may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at http://www.sun.com/jini/ . Software distributed under the
 * License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing rights and limitations under the License.
 * 
 * The Reference Code is Jini Technology Core Platform code, v 1.1. The
 * Developer of the Reference Code is Sun Microsystems, Inc.
 * 
 * Contributor(s): Sun Microsystems, Inc.
 * 
 * The contents of this file comply with the Jini Technology Core Platform
 * Compatibility Kit, v 1.1A.
 * 
 * Tester(s): Sun Microsystems, Inc.
 * 
 * Test Platform(s):
 * 
 * 	Java 2 SDK, Standard Edition, V 1.2.2_006 Solaris
 * 	   Reference Implementation Release
 * 
 * 	Java 2 SDK, Standard Edition, V 1.2.2_05a Solaris
 * 	   Production Release
 * 
 * 	Java 2 SDK, Standard Edition, V 1.2.2_006 Windows 95/98/NT
 * 	   Production Release
 * 	   
 * Version 1.1
 * 
 */
package com.sun.jini.debug;

import java.io.*;
import java.util.*;

/**
 * This class provides a simple object type that supports debug output.  You
 * create a <code>Debug</code> object giving it a property name.  Such
 * a property can cover a number of subsystems.  For example, the property
 * <code>javaSpace.debug</code> might have a number of possible subsystems,
 * such as <code>write</code>, <code>notify</code>, and <code>find</code>.
 * Each such subsystem denotes a set of debug messages that will be printed
 * if debugging for the subsystem is turned on.  In our example, there
 * could be a system-wide class <code>SpaceDebug</code>:
 *
 * <pre>
 *	class SpaceDebug {
 *	    static final Debug debug = new Debug("javaSpace.debug");
 *	};
 *
 *	class SpaceImpl {
 *	    static final PrintWriter writeDebug
 *		= SpaceDebug.debug.getWriter("write");
 *	    static final PrintWriter notifyDebug
 *		= SpaceDebug.debug.getWriter("notify");
 *	    static final PrintWriter findDebug
 *		= SpaceDebug.debug.getWriter("find");
 *
 *	    EntryObj find(EntryObj tmpl, ...) {
 *		if (findDebug != null)
 *		    findDebug.println("find(" + tmpl + ")");
 *		// ...
 *	    }
 *
 *	    // ...
 *	}
 * </pre>
 *
 * The <code>Debug</code> constructor reads the property
 * <code>javaSpace.debug</code>, which might be set as
 *
 * <pre>
 *	java -DjavaSpace.debug=find,notify:nlog SpaceImpl 
 * </pre>
 *
 * When the code in <code>SpaceImpl</code> is initialized, it will
 * go to the <code>Debug</code> object in <code>SpaceDebug.debug</code>
 * to ask for the <code>PrintWriter</code> for debug messages on the
 * subsystems <code>write</code>, <code>notify</code>, and <code>find</code>.
 * Messages should be generated for a subsystem if its <code>PrintWriter</code>
 * reference is not <code>null</code>.
 *
 * <p>
 * The comma-separated list in the property says which subsystems should
 * generate debug output, and to which output stream.  In the example above,
 * the subsystem <code>write</code> will have no debug messages (it is not
 * listed in the property); the subsystem <code>find</code> will have its
 * output go to the default stream; and the subsystem <code>notify</code>
 * will have its messages go to the file named <code>nlog</code>.  The
 * default stream is a <code>PrintWriter</code> wrapped around
 * <code>System.out</code>.  If a subsystem's output descriptor is
 * <code>out</code> or <code>err</code> it's debug output will go to
 * <code>System.out</code> or <code>System.err</code>, respectively.
 * If two or more subsystems have the same path specified for output,
 * they will share the same <code>PrintWriter</code> stream.  All the
 * <code>PrintWriter</code> streams so created have <code>autoFlush</code>
 * <code>true</code>, so newlines flush the output to the file.
 *
 * <p>
 * You can change how the subsystems are mapped to <code>PrintWriter</code>
 * streams by creating your <code>Debug</code> object with a
 * <code>WriterFactory</code> object.  <code>WriterFactory</code> declares
 * a <code>writer</code> method that maps a subsystem name and its output
 * descriptor into a <code>PrintWriter</code> object.  This factory will
 * be used the first time someone asks for the <code>PrintWriter</code>
 * for a particular subsystem -- the results are stored for subsequent
 * calls.
 *
 * @see java.io.PrintWriter
 *
 * @author Ken Arnold
 */
public class Debug {
    /**
     * This interface lets you customize how subsytem names and their
     * output descriptor arguments are mapped into output streams.
     *
     * @see #Debug(java.lang.String,com.sun.jini.debug.Debug.WriterFactory)
     */
    public interface WriterFactory {
	/**
	 * Returns the <code>PrintWriter</code> associated with the given
	 * subsystem, taking into account its output descriptor (the string
	 * after the <code>':'</code>).  If no string was specified, then
	 * <code>arg</code> will be <code>""</code>.
	 *
	 * @see #Debug(java.lang.String,com.sun.jini.debug.Debug.WriterFactory)
	 */
	PrintWriter writer(String subsystem, String arg) throws IOException;
    }

    /** The property name from which the debug specification is read. */
    private String		propertyName;
    /** The value of the property. */
    private String		property;
    /** The known subsystems. */
    private Map 		subsystems;
    /** The factory for streams. */
    private WriterFactory	factory;

    /** The stream for <code>System.out</code>. */
    private final static PrintWriter SYSOUT = new PrintWriter(System.out, true);
    /** The stream for <code>System.err</code>. */
    private final static PrintWriter SYSERR = new PrintWriter(System.err, true);

    /** The <code>WriterFactory</code> implementing the default policy. */
    private final static WriterFactory defaultFactory = new DefaultFactory();

    /**
     * This private class implements the default factory for mapping
     * subsytems and args to streams.
     */
    private static class DefaultFactory implements WriterFactory {
	/** The known files. */
	private Map		files;

	/**
	 * Creates a <code>DefaultFactory</code>, adding the maps for the
	 * names "" (none specified), <code>out</code>, and <code>err</code>.
	 */
	DefaultFactory() {
	    files = new HashMap();
	    files.put("", SYSOUT);
	    files.put("out", SYSOUT);
	    files.put("err", SYSOUT);
	}

	/**
	 * If <code>arg</code> specifies something already known, return it.
	 * This makes sure that if two subsystems specify the same path, they
	 * will get the same stream, instead of two streams to the same file.
	 */
	public synchronized PrintWriter writer(String subsystem, String arg)
	    throws IOException
	{
	    PrintWriter w = (PrintWriter) files.get(arg);
	    if (w == null) {
		w = new PrintWriter(new FileWriter(arg), true);
		files.put(arg, w);
	    }
	    return w;
	}

    };

    /**
     * Creates a new <code>Debug</code> object, initializing its list
     * of subsystems from the given <code>propertyName</code>.  The
     * property is read during construction.
     */
    public Debug(String propertyName) {
	this(propertyName, defaultFactory);
    }

    /**
     * Creates a new <code>Debug</code> object, initializing its list
     * of subsystems from the given <code>propertyName</code> and using
     * the specified <code>factory</code> for mapping subsystem specifications
     * into streams.  The property is read during construction.
     */
    public Debug(String propertyName, WriterFactory factory) {
	this.propertyName = propertyName;
	property = System.getProperty(propertyName, "");
	this.factory = factory;
    }

    /**
     * Return the property name that contains the specifications.
     */
    public String getPropertyName() {
	return propertyName;
    }

    /**
     * Return the writer for the named <code>subsytem</code>.  This is used
     * by the code that wants to know whether to print debug output for the
     * given subsystem.  The subsystem defined here does not include output
     * specifiers (the <code>":file"</code> stuff).
     */
    public PrintWriter getWriter(String subsystem) {
	buildMap();
	Object obj = subsystems.get(subsystem);

	if (obj == null)
	    return null;
	else if (obj instanceof PrintWriter)
	    return (PrintWriter) obj;

	try {
	    PrintWriter out = factory.writer(subsystem, (String) obj);
	    subsystems.put(subsystem, out);
	    return out;
	} catch (IOException e) {
	    e.printStackTrace();
	    System.err.println("debug output for " + subsystem +
		" to System.out");
	    subsystems.put(subsystem, SYSOUT);
	    return SYSOUT;
	}
    }

    // inherit doc comment
    public String toString() {
	return "Debug(" + propertyName + ")->\"" + property + '"';
    }

    /**
     * Build up the map of subsystems specified in the property.  We
     * defer this until needed so that debug variables that are
     * never checked are cheaper.
     */
    private void buildMap() {
	if (subsystems != null)		// already built it
	    return;

	synchronized (this) {
	    if (subsystems != null)	// might have built it while waiting
		return;			// for lock on this

	    subsystems = Collections.synchronizedMap(new HashMap());
	    StringTokenizer s = new StringTokenizer(property, " \t,");
	    while (s.hasMoreTokens()) {
		String str = s.nextToken();
		Object arg;
		int colon = str.indexOf(':');
		if (colon < 0)		// none specified
		    arg = "";
		else if (colon == 0) {	// user input error
		    System.err.println(str +
			" is an illegal debug specifier, ignored (" +
			propertyName + ")");
		    continue;
		}
		else {			// output specification
		    arg = str.substring(colon + 1);
		    str = str.substring(0, colon);
		}
		subsystems.put(str, arg);
	    }
	}
    }
}
