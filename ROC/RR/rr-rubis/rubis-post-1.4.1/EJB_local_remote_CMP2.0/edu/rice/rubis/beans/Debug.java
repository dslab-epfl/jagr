/*
 *  Debug: print debug information if the value of DEBUG variable is true
 *
 */
package edu.rice.rubis.beans;

public class Debug{
    private static final boolean DEBUG = false;

    public static void println(String message){
	if (DEBUG){
	    System.out.println(" DEBUG: "+message);
	}
    }
}
