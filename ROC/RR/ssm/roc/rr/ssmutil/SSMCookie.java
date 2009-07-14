/*
   SSMCookie class 
     This class convert Stub returned SSMCookie (java object) 
       to Http compatible cookie (String) and vice versa.

                                    Jan/07/2004  S.Kawamoto
*/

package roc.rr.ssmutil;
import java.util.Vector;

public class SSMCookie {
    static final String delimiter = "XX";
    static final String intArrayDelimiter = "Y";

    /*  test code 
    public static void main(String[] args) {
	int[] i2= {1,2,3,4,5};
	Long i1 = new Long(12345);
	Long i3 = new Long(678910);
	Integer i4 = new Integer(10);
		
	Vector v = new Vector();
		
	v.addElement(i1);
	v.addElement(i2);
	v.addElement(i3);
	v.addElement(i4);
		
	String s = ssmCookie2HttpCookie(v);
	System.out.println(s);
		
	Vector v2;
	v2 = httpCookie2ssmCookie(s);
	for(int i=0;i<4;i++){
	    System.out.println(v2.elementAt(i));
	}
    }
    */

    public static String ssmCookie2HttpCookie(Vector v){
	String s;

	//
	// format:
	//  LongXXintArrayXXLongXXIntegerXX
	//

	try {
	    s = ((Long)v.elementAt(0)).toString();
	    s += delimiter;
	    s += intArray2String((int[])v.elementAt(1));
	    s += delimiter;
	    s += ((Long)v.elementAt(2)).toString();
	    s += delimiter;
	    s += ((Integer)v.elementAt(3)).toString();
	    s += delimiter;
	} catch (Exception e) {
	    System.out.println("ssmCookie2HttpCookie: Illegal SSM Cookie: "+v);
	    s = null;
	}

	return s;
    }
	
    public static Vector httpCookie2ssmCookie(String s){
	Vector v = new Vector();
	String item;
	int from, to;

	try {
	    from = 0 ;
	    to = s.indexOf(delimiter,from);
	    item = s.substring(from,to);
	    Long l = new Long(item);
	    v.addElement(l);
	
	    from = to+2;
	    to = s.indexOf(delimiter,from);
	    item = s.substring(from,to);
	    int a[] = string2IntArray(item);
	    v.addElement(a);
		
	    from = to+2;
	    to = s.indexOf(delimiter,from);
	    item = s.substring(from,to);
	    l = new Long(item);
	    v.addElement(l);
		
	    from = to+2;
	    to = s.indexOf(delimiter,from);
	    item = s.substring(from,to);
	    Integer i = new Integer(item);
	    v.addElement(i);
	} catch (Exception e){
	    System.out.println("httpCookie2ssmCookie: Illegal SSM cookie string: "+s);
	    v=null;
	}

	return v;
    }
	
	
    static private String intArray2String(int[] ia){
	String s;
		
	s = String.valueOf(ia.length);  // number of elements
		
	for(int i=0;i<ia.length;i++){
	    s += intArrayDelimiter + String.valueOf(ia[i]);
	}
	s+=intArrayDelimiter;
		
	return s;
    }
	
    static private int[] string2IntArray(String s){
	String item;
	int from, to;
	int intArray[];
		
	try {
	    from = 0;
	    to = s.indexOf(intArrayDelimiter);
	    item = s.substring(from,to);
		
	    intArray = new int[Integer.parseInt(item)];
	    
	    for(int i=0;i<intArray.length;i++){
		from = to+1;
		to = s.indexOf(intArrayDelimiter,from);
		item = s.substring(from,to);
		intArray[i]=Integer.parseInt(item);
	    }
	} catch (Exception e){
	    System.out.println("string2IntArray: Illegal int array string: "+s);
	    intArray = null;
	}
	return intArray;
    }	
}
