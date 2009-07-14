/* Copyright  (c) 2002 The Board of Trustees of The Leland Stanford Junior
 * University. All Rights Reserved.
 *
 * See the file LICENSE for information on redistributing this software.
 */

package swig.util;

/**
 * This class contains static method utilities for dealing with
 * Strings.
 *
 * @author <a href="http://www.cs.stanford.edu/~ach">
 *        Andy Huang</a> - ach@cs.stanford.edu 
 **/
public class StringUtil {
    /**
     * Searches for all instances of <code>orig</code> and replaces
     * them with <code>replace</code>.
     **/
    public static String replace(String str, String orig, String replace) {
        Debug.Enter("u", "StringUtil::replace(str, orig, replace)");

        StringBuffer sb = new StringBuffer();

        int i = str.indexOf(orig);
        int doneIndex = 0;

        while (i >= 0) {
            // Append what comes before the index of orig.
            sb.append(str.substring(doneIndex, i));

            // Append the replacement.
            sb.append(replace);

            // Update the doneIndex so that it is refering to the
            // index right after this location of the orig string.
            doneIndex = i + orig.length();

            // Find the next occurence of orig.
            i = str.indexOf(orig, doneIndex);
        }

        // Append the rest.
        sb.append(str.substring(doneIndex, str.length()));

        Debug.Exit("u", "StringUtil::replace(str, orig, replace)");

        // Return the StringBuffer's string.
        return sb.toString();
    }

    /**
     * Replaces characters that are considered 'special' in XML or
     * HTML (e.g., &, <, >, \, ') and replaces them with the escape
     * version (e.g., \\amp, \\lt).
     **/
    public static String escapeXmlize(String orig) {
        Debug.Enter("u", "StringUtil::escapeXmlize(String orig)");

        String newStr;

        newStr = StringUtil.replace(orig, "&", "\\amp");
        newStr = StringUtil.replace(newStr, "<", "\\lt");
        newStr = StringUtil.replace(newStr, ">", "\\gt");
        newStr = StringUtil.replace(newStr, "\"", "\\quot");
        newStr = StringUtil.replace(newStr, "'", "\\apos");

        Debug.Exit("u", "StringUtil::escapeXmlize(String orig)");

        return newStr;
    }

    /**
     * Replaces XML or HTML escape character sequences (e.g., \\amp,
     * \\lt) with the characters they represent (e.g., &, <, >, \, ').
     **/
    public static String escapeDeXmlize(String orig) {
        Debug.Enter("u", "StringUtil::escapeDeXmlize(String orig)");

        String newStr;

        newStr = StringUtil.replace(orig, "\\lt", "<");
        newStr = StringUtil.replace(newStr, "\\gt", ">");
        newStr = StringUtil.replace(newStr, "\\quot", "\"");
        newStr = StringUtil.replace(newStr, "\\apos", "'");
        newStr = StringUtil.replace(newStr, "\\amp", "&");

        Debug.Exit("u", "StringUtil::escapeDeXmlize(String orig)");

        return newStr;
    }

    /**
     * Returns a Class object given the class name.
     **/
    public static Class stringToClass(String classname)
        throws ClassNotFoundException {
        Debug.Enter("u", "StringUtil::stringToClass(" + classname + ")");

        boolean isArray = false;
        boolean isObject = false;

        // Find out whether the classname represents an array. If so, 
        // keep track of that fact and strip off the "[]" suffix.
        if (classname.endsWith("[]")) {
            isArray = true;
            classname = classname.substring(0, classname.length() - 2);
        }

        // Handle primitive types.
        String classCode = null;

        if (classname.compareTo("byte") == 0) {
            classCode = "B";
        }
        else if (classname.compareTo("char") == 0) {
            classCode = "u";
        }
        else if (classname.compareTo("double") == 0) {
            classCode = "D";
        }
        else if (classname.compareTo("float") == 0) {
            classCode = "F";
        }
        else if (classname.compareTo("int") == 0) {
            classCode = "I";
        }
        else if (classname.compareTo("long") == 0) {
            classCode = "J";
        }
        else if (classname.compareTo("short") == 0) {
            classCode = "S";
        }
        else if (classname.compareTo("boolean") == 0) {
            classCode = "Z";
        }
        else {
            classCode = classname;
            isObject = true;
        }

        // Add the array code if needed.
        if (isArray) {
            if (isObject) {
                classCode = "[L" + classCode + ";";
            }
            else {
                classCode = "[" + classCode;
            }
        }

        Debug.Print("u", "using classcode: " + classCode);
        Debug.Exit("u", "StringUtil::stringToClass");

        return Class.forName(classCode);
    }
}