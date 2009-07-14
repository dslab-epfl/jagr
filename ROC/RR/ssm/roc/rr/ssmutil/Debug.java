/*
   Debug class
     In order to indicate debug message, 
     set the variable "on" to true  
*/

package roc.rr.ssmutil;

public class Debug{
    private static final boolean on = false;

    static public void println(Object o){
	String s = o.toString();

	if (on){
	    System.out.println(s);
	}
    }
}
