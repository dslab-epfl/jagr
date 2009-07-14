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
package com.sun.jini.thread;

import com.sun.jini.debug.Debug;

/**
 * This is a package-accessible class for turning on debug output in
 * the util classes.  The property name is for the debug property is
 * <code>com.sun.jini.thread.debug</code>.
 *
 * @see Debug
 */
class ThreadDebug {
    /**
     * The <code>Debug</code> object for
     * <code>com.sun.jini.thread.debug</code>.
     */
    static final Debug debug = new Debug("com.sun.jini.thread.debug");

    /**
     * A shorthand for invoking <code>getWriter</code> on the <code>debug</code>
     * field.
     */
    static java.io.PrintWriter getWriter(String subsystem) {
	return debug.getWriter(subsystem);
    }
}
