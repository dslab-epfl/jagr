/* Copyright  (c) 2002 The Board of Trustees of The Leland Stanford Junior
 * University. All Rights Reserved.
 *
 * See the file LICENSE for information on redistributing this software.
 */

package swig.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * The <code>DynamicHtml</code> class makes it easier to produce *
 * dynamic Web pages (in HTML or XML format). The templates used by
 * this object look just like a normal HTML/XML file, except they may
 * contain keywords and function calls denoted by "&lt%___&gt."  
 *
 * <ul> 
 *
 * <li>To include a method, include <code>&lt%methodname&gt</code> in
 * the template. Note that methods must return a String object (unless
 * they are used in the for loop initilization (described next). 

 * <p>
 * <li>For loops can be specified using the
 * <code>&lt%foreach&gt</code> tag. Immediately following this tag,
 * there must be a method call <code>&lt%methodname&gt</code> for a
 * method that returns an array of String objects (String[]). Within
 * the loop, <code>&lt%i&gt</code> is used to denote the loop
 * variable, which is set to the array elements in order. Here is an
 * example of a loop:
 *
 * <pre>
 * <%foreach> <%listElements>
 * Element: <%i>
 * <%endfor>
 * </pre>
 *
 * <li>The DynamicHtml package also has special method calls that are
 * useful to many ADS services. These are denoted by
 * <code>&lt%DynamicHtml::methodname&gt</code>. One example is the
 * get[servicetype]Url, which returns the hostname and port of a
 * certain class of services (e.g., control, services, accesspt). This
 * prevents hardcoding of hostnames and machines in the
 * templates. Some example calls to this method are:
 *
 * <pre>
 * <%getControlUrl>
 * <%getServicesUrl>
 * </pre>
 *
 * @author <a href="http://www-cs-students.stanford.edu/~ach">
 *        Andy Huang</a> - ach@cs.stanford.edu
 **/
public class DynamicHtml {
    private Hashtable args;

    public DynamicHtml() {
        args = new Hashtable();
    }

    public DynamicHtml(Hashtable args) {
        setArgs(args);
    }

    public void setArgs(Hashtable args) {
        this.args = args;
    }

    /**
     * The <code>parse</code> method parses an HTML/XML file template,
     * interprets certain keywords described below, and performs
     * function calls where specified. 
     *
     * @param caller The object on which methods specified in the
     * template are invoked.
     * @param htmlTempalte The HTML/XML template that is parsed.
     *
     * @return A String with the HTML/XML template with all keyword
     * and method call returns substituted in place of the
     * <code>&lt%___&gt</code>.
     **/
    public static String parse(Object caller, String htmlTemplate)
        throws
            NoSuchMethodException,
            IllegalAccessException,
            InvocationTargetException {
        Debug.Enter("d", "DynamicHtml::parse(caller, htmlTemplate)");

        DynamicHtml dh = new DynamicHtml();
        String retStr =
            dh.parse(caller, htmlTemplate, "", null, null, null, null);

        Debug.Exit("d", "DynamicHtml::parse(caller, htmlTemplate)");

        return retStr;
    }

    public String parse(
        Object caller,
        String htmlTemplate,
        String forLoopArg,
        Object[] actuals,
        Class[] paramTypes,
        String alt,
        String methodStr)
        throws
            NoSuchMethodException,
            IllegalAccessException,
            InvocationTargetException {
        Debug.Enter("d", "DynamicHtml::parse");

        String htmlStr;
        htmlStr =
            parseRecursive(
                caller,
                htmlTemplate,
                0,
                forLoopArg,
                actuals,
                paramTypes,
                alt,
                methodStr);

        Debug.Exit("d", "DynamicHtml::parse");

        return htmlStr;
    }

    private String parseRecursive(
        Object caller,
        String htmlTemplate,
        int doneIndex,
        String forLoopArg,
        Object[] actuals,
        Class[] paramTypes,
        String alt,
        String methodStr)
        throws
            NoSuchMethodException,
            IllegalAccessException,
            InvocationTargetException {
        if (htmlTemplate == null) {
            return "";
        }

        Debug.Enter("d", "DynamicHtml::parseRecursive");
        Debug.Print("d", "At entrance, doneIndex = " + doneIndex);

        // The StringBuffer to which the resulting HTML will be appended.
        StringBuffer sb = new StringBuffer();

        try {
            Class c = caller.getClass();
            String methodName;
            String dynamicStr;
            Method m;

            // Find the start of the next dynamic HTML "variable" to
            // substitute.
            int i = htmlTemplate.indexOf("<%", doneIndex);

            if (i == -1) {
                // Copy the rest of the html template to the string buffer.
                sb.append(
                    htmlTemplate.substring(doneIndex, htmlTemplate.length()));
                Debug.Exit("d", "DynamicHtml::parseRecursive");

                return sb.toString();
            }

            String not_variable = htmlTemplate.substring(doneIndex, i);
            Debug.Print("d", "DynamicHtml copying:" + not_variable + "\n");
            Debug.Print("d", "Variable found at index " + i);

            // Append from the doneIndex to the index of the next variable.
            sb.append(not_variable);

            // Find out the method name or keyword specified.
            int j = htmlTemplate.indexOf('>', i);
            methodName = htmlTemplate.substring(i + 2, j);
            Debug.Print("d", "Method name = " + methodName);

            // Update the doneIndex.
            doneIndex = j + 1;
            Debug.Print("d", "Updating doneIndex to " + doneIndex);

            // Handle the special keywords.
            if (methodName.equals("foreach")) {
                Debug.Print("d", "Handling for body");

                // Find the methodName.
                i = htmlTemplate.indexOf("<%", doneIndex);
                j = htmlTemplate.indexOf('>', i);
                methodName = htmlTemplate.substring(i + 2, j);
                doneIndex = j + 1;

                // Find the end of the foreach and update the doneIndex.
                i = indexOfEndtag(htmlTemplate, "foreach", doneIndex);
                j = htmlTemplate.indexOf('>', i);
                String forBody = htmlTemplate.substring(doneIndex, i);
                Debug.Print("d", "For body: " + forBody);
                doneIndex = j + 1;

                // Check the actuals to make sure something is set from
                // param.  If not, we have to set args.
                actuals = checkActuals(actuals);
                paramTypes = checkParamTypes(paramTypes);

                // Call the method specified.
                Debug.Print("d", "Calling method: " + methodName);
                m = c.getMethod(methodName, paramTypes);
                String[] forLoopArr = (String[]) m.invoke(caller, actuals);
                Debug.Print("d", "Clearing actuals and paramTypes arrays");
                actuals = null;
                paramTypes = null;

                // For each string in the returned string array, 
                // call "parse" on the for loop body.
                if (forLoopArr == null) {
                    if (alt != null) {
                        sb.append(alt);
                    }
                    else {
                        sb.append("none");
                    }
                }
                else {
                    int size = forLoopArr.length;
                    Debug.Print("d", "number of strings: " + size);

                    for (int n = 0; n < size; n++) {
                        dynamicStr =
                            parse(
                                caller,
                                forBody,
                                forLoopArr[n],
                                actuals,
                                paramTypes,
                                alt,
                                methodStr);
                        sb.append(dynamicStr);
                    }
                }
            }
            else if (methodName.equals("if")) {
                Debug.Print("d", "Handling if body");

                // Find the methodName.
                i = htmlTemplate.indexOf("<%", doneIndex);
                j = htmlTemplate.indexOf('>', i);
                methodName = htmlTemplate.substring(i + 2, j);
                doneIndex = j + 1;

                // Check the actuals to make sure something is set from
                // param.  If not, we have to set args.
                actuals = checkActuals(actuals);
                paramTypes = checkParamTypes(paramTypes);

                // Call the method specified.
                Debug.Print("d", "Calling method: " + methodName);
                m = c.getMethod(methodName, paramTypes);
                Boolean methodRet = (Boolean) m.invoke(caller, actuals);

                String ifBody = null;
                String elseTag = "<%else>";
                String endifTag = "<%endif>";

                i = htmlTemplate.indexOf(elseTag, doneIndex);

                // Check to see whether there is an else tag for this if
                // statement.
                boolean elseExists = true;

                if (i < 0) {
                    elseExists = false;
                    i = htmlTemplate.indexOf(endifTag, doneIndex);
                    j = i;
                }
                else {
                    j = htmlTemplate.indexOf(endifTag, i);
                }

                if (methodRet.booleanValue()) {
                    // If the method returned true, than parse the
                    // contents before the else tag.
                    Debug.Print("d", "method returned true");
                    ifBody = htmlTemplate.substring(doneIndex, i);
                }
                else if (elseExists) {
                    // Otherwise take the contents from the else tag to
                    // the endif tag.
                    Debug.Print("d", "method returned false");
                    ifBody = htmlTemplate.substring(i + elseTag.length(), j);
                }

                Debug.Print("d", "if body: " + ifBody);

                if (ifBody != null) {
                    dynamicStr =
                        parse(
                            caller,
                            ifBody,
                            forLoopArg,
                            null,
                            null,
                            null,
                            methodStr);
                    Debug.Print("d", "Parsed ifBody: " + dynamicStr);
                    sb.append(dynamicStr);
                }

                Debug.Print("d", "Clearing actuals and paramTypes arrays");
                actuals = null;
                paramTypes = null;

                doneIndex = j + endifTag.length();
            }
            else if (methodName.equals("method")) {
                Debug.Print("d", "Handling method body");

                // Find the methodName.
                i = htmlTemplate.indexOf("<%", doneIndex);
                j = htmlTemplate.indexOf('>', i);
                methodName = htmlTemplate.substring(i + 2, j);
                doneIndex = j + 1;

                // Find the end of the method and update the doneIndex.
                i = indexOfEndtag(htmlTemplate, "method", doneIndex);
                j = htmlTemplate.indexOf('>', i);
                String methodBody = htmlTemplate.substring(doneIndex, i);
                Debug.Print("d", "Method body: " + methodBody);
                doneIndex = j + 1;

                // Check the actuals to make sure something is set from
                // param.  If not, we have to set args.
                actuals = checkActuals(actuals);
                paramTypes = checkParamTypes(paramTypes);

                // Call the method specified.
                Debug.Print("d", "Calling method: " + methodName);
                m = c.getMethod(methodName, paramTypes);
                String newMethodStr = (String) m.invoke(caller, actuals);
                Debug.Print("d", "Clearing actuals and paramTypes arrays");
                actuals = null;
                paramTypes = null;

                // For each string in the returned string array, 
                // call "parse" on the for loop body.
                if (newMethodStr == null) {
                    if (alt != null) {
                        sb.append(alt);
                    }
                    else {
                        sb.append("none");
                    }
                }
                else {
                    dynamicStr =
                        parse(
                            caller,
                            methodBody,
                            forLoopArg,
                            actuals,
                            paramTypes,
                            alt,
                            newMethodStr);
                    sb.append(dynamicStr);
                }
            }
            else if (methodName.equals("i")) {
                Debug.Print("d", "Loop variable substitution " + forLoopArg);

                sb.append(forLoopArg);
            }
            else if (methodName.equals("m")) {
                Debug.Print("d", "Method variable substitution " + methodStr);

                sb.append(methodStr);
            }
            else if (methodName.startsWith("verbatim")) {
                Debug.Print("d", "Handling verbatim");

                // Find the end of the ver section and update the doneIndex.
                i = indexOfEndtag(htmlTemplate, "verbatim", doneIndex);
                j = htmlTemplate.indexOf('>', i);
                String verBody = htmlTemplate.substring(doneIndex, i);
                Debug.Print("d", "Ver body: " + verBody);
                doneIndex = j + 1;

                // Replace characters that must be escped for XML.
                if (methodName.endsWith("xml")) {
                    verBody = StringUtil.escapeXmlize(verBody);
                    Debug.Print("d", "Ver body w/ xml replacement: " + verBody);
                }

                sb.append(verBody);
            }
            else if (methodName.equals("alt")) {
                Debug.Print("d", "Handling alt");

                // Find the end of the alt section and update the doneIndex.
                i = indexOfEndtag(htmlTemplate, "alt", doneIndex);
                j = htmlTemplate.indexOf('>', i);
                String altBody = htmlTemplate.substring(doneIndex, i);
                Debug.Print("d", "Alt body: " + altBody);
                doneIndex = j + 1;

                alt =
                    parse(
                        caller,
                        altBody,
                        forLoopArg,
                        null,
                        null,
                        null,
                        methodStr);
                Debug.Print("d", "Parsed alt body: " + alt);
            }
            else if (methodName.equals(",")) {
                sb.append("<%,>");
            }
            else if (methodName.equals("param")) {
                Debug.Print("d", "Handling parameters");

                // Find the end of the parameter list and update the doneIndex.
                i = indexOfEndtag(htmlTemplate, "param", doneIndex);
                j = htmlTemplate.indexOf('>', i);
                String paramBody = htmlTemplate.substring(doneIndex, i);
                Debug.Print("d", "Param body: " + paramBody);
                doneIndex = j + 1;

                paramBody =
                    parse(
                        caller,
                        paramBody,
                        forLoopArg,
                        null,
                        null,
                        alt,
                        methodStr);
                Debug.Print("d", "Parsed param body: " + paramBody);

                // Create a list of actuals by parsing the parameter list
                // String.
                ArrayList actualsList = new ArrayList();
                int startIndex = 0;
                int commaIndex;
                int endIndex;
                String actualStr;
                String commaStr = "<%,>";

                do {
                    commaIndex = paramBody.indexOf(commaStr, startIndex);
                    endIndex = commaIndex;

                    if (endIndex == -1) {
                        // To the end, minus the last ')'.
                        endIndex = paramBody.length();
                    }

                    // Parse the paramBody String to get the next actual.
                    Debug.Print(
                        "d",
                        "handling parameter at index: "
                            + startIndex
                            + " to "
                            + endIndex);
                    actualStr = paramBody.substring(startIndex, endIndex);
                    Debug.Print("d", "next parameter: " + actualStr);

                    // Put the actual into the list.
                    actualsList.add(actualStr);

                    // Find the location of the next actual.
                    startIndex = endIndex + commaStr.length();
                }
                while (commaIndex != -1);

                // Convert the actuals list to an array and create a type
                // array of the same size filled with String classes.
                // (Note: Add one for the DynamicHtml args).
                Debug.Print("d", "Setting actuals and paramTypes arrays");
                int numActuals = actualsList.size() + 1;
                actuals =
                    (Object[]) actualsList.toArray(new Object[numActuals]);
                actuals[numActuals - 1] = args;

                paramTypes = new Class[numActuals];
                int p;

                try {
                    for (p = 0; p < (numActuals - 1); p++) {
                        paramTypes[p] =
                            StringUtil.stringToClass("java.lang.String");
                    }

                    paramTypes[p] =
                        StringUtil.stringToClass("java.util.Hashtable");
                }
                catch (Exception e) {
                    System.err.println(
                        "failed to convert string to class: " + e);
                    e.printStackTrace();

                    return null;
                }
            }
            else if (methodName.startsWith("DynamicHtml::")) {
                int colonLocation = methodName.indexOf("::");
                methodName =
                    methodName.substring(
                        colonLocation + 2,
                        methodName.length());
                Debug.Print("d", "Special DynamicHtml call to: " + methodName);
                handleSpecialMethod(
                    caller,
                    forLoopArg,
                    actuals,
                    paramTypes,
                    alt,
                    methodStr,
                    methodName,
                    sb);
            }
            else {
                // Check the actuals to make sure something is set from
                // param.  If not, we have to set args.
                actuals = checkActuals(actuals);
                paramTypes = checkParamTypes(paramTypes);

                // Call the method specified.
                Debug.Print("d", "Calling method: " + methodName);
                m = c.getMethod(methodName, paramTypes);
                dynamicStr = (String) m.invoke(caller, actuals);
                Debug.Print("d", "Clearing actuals and paramTypes arrays");
                actuals = null;
                paramTypes = null;
                Debug.Print("d", "String returned: " + dynamicStr);

                // Append the string returned by the method unless it is 
                // null, in which case we append the String in alt.
                if ((dynamicStr != null) && !dynamicStr.equals("")) {
                    sb.append(dynamicStr);
                }
                else if (alt != null) {
                    sb.append(alt);
                    alt = null;
                }
            }

            sb.append(
                parseRecursive(
                    caller,
                    htmlTemplate,
                    doneIndex,
                    forLoopArg,
                    actuals,
                    paramTypes,
                    alt,
                    methodStr));
        }
        catch (java.lang.reflect.InvocationTargetException e) {
            System.err.println(
                "InvocationTargetException caught:" + e.getTargetException());
        }
        catch (Exception obe) {
            System.err.println("DynamicHtml error: " + obe);
            System.err.println("doneIndex: " + doneIndex);
            System.err.println("forLoopArg: " + forLoopArg);
            System.err.println("methodStr: " + methodStr);
            System.err.println("num params: " + paramTypes.length);
            System.err.println(
                "done htmlTemplate: " + htmlTemplate.substring(0, doneIndex));
            obe.printStackTrace();
        }

        Debug.Exit("d", "DynamicHtml::parseRecursive");

        return sb.toString();
    }

    protected void handleSpecialMethod(
        Object caller,
        String forLoopArg,
        Object[] actuals,
        Class[] paramTypes,
        String alt,
        String methodStr,
        String methodName,
        StringBuffer sb)
        throws
            NoSuchMethodException,
            IllegalAccessException,
            InvocationTargetException {
        Debug.Enter("d", "DynamicHtml::handleSpecialMethod(methodName)");
        Debug.Print("d", "methodName: " + methodName);

        if (methodName.startsWith("insertFile")) {
            // Find the filename parameter.
            int startParam = methodName.indexOf("(") + 1;
            int len = methodName.length();
            String filename = methodName.substring(startParam, len - 1);

            // Get the HTML string and put it through the parser.
            String htmlStr = getFile(caller, filename);
            String dynamicStr =
                parse(
                    caller,
                    htmlStr,
                    forLoopArg,
                    actuals,
                    paramTypes,
                    alt,
                    methodStr);
            sb.append(dynamicStr);
        }
        else if (methodName.startsWith("lookup")) {
            // Find the filename parameter.
            int startParam = methodName.indexOf("(") + 1;
            int len = methodName.length();
            String hashKey = methodName.substring(startParam, len - 1);

            // Get the HTML string and put it through the parser.
            String htmlStr = (String) args.get(hashKey);
            String dynamicStr =
                parse(
                    caller,
                    htmlStr,
                    forLoopArg,
                    actuals,
                    paramTypes,
                    alt,
                    methodStr);
            sb.append(dynamicStr);
        }

        Debug.Exit("d", "DynamicHtml::handleSpecialMethod(methodName)");
    }

    private Object[] checkActuals(Object[] actuals) {
        if (actuals == null) {
            Debug.Print("d", "handling effectively-null param method");
            actuals = new Object[1];
            actuals[0] = args;
        }

        return actuals;
    }

    private Class[] checkParamTypes(Class[] paramTypes) {
        if (paramTypes == null) {
            Debug.Print("d", "handling effectively-null param method");
            paramTypes = new Class[1];

            try {
                paramTypes[0] = StringUtil.stringToClass("java.util.Hashtable");
            }
            catch (ClassNotFoundException cnfe) {
                System.err.println("bug in DynamicHtml call to stringtoclass");
                System.exit(-1);
            }
        }

        return paramTypes;
    }

    private int indexOfEndtag(
        String htmlTemplate,
        String tagType,
        int doneIndex) {
        Debug.Enter("d", "indexOfEndtag(html, tagtype, doneindex)");

        int retIndex;

        String startTag = "<%" + tagType;
        String endTag = "<%end" + tagType;

        int endTagIndex = doneIndex;
        int startTagIndex = doneIndex;

        int i = 0;
        int j = 0;

        while (j <= i) {
            // Find the endtag.
            i = htmlTemplate.indexOf(endTag, endTagIndex);

            // Check that there is not a nested tag.
            j = htmlTemplate.indexOf(startTag, startTagIndex);

            if (j == -1) {
                break;
            }

            endTagIndex = i + endTag.length() + 1;
            startTagIndex = j + startTag.length() + 1;
        }

        retIndex = i;

        Debug.Exit("d", "indexOfEndtag(html, tagtype, doneindex)");

        return retIndex;
    }

    private String getFile(Object caller, String filename) {
        Debug.Enter("d", "getFile()");

        DynamicHtmlClientIF c = (DynamicHtmlClientIF) caller;
        String htmlDir = c.getHtmlDir();
        String path = htmlDir + filename;
        Debug.Print("d", "getting file: " + path);

        String htmlStr = null;

        try {
            byte[] fileBytes = FileUtil.readBytesFromFile(path);
            htmlStr = new String(fileBytes);
        }
        catch (Exception e) {
            System.err.println("File read failed: " + e);
            e.printStackTrace();

            return null;
        }

        Debug.Exit("d", "getFile()");

        return htmlStr;
    }

    //  	// Handle any method parameters.
    //  	// !! For now, we assume all parameters are Strings.
    //  	String[] actuals = null;
    //  	Class[] paramTypes = null;
    //  	int paramIndex = methodName.indexOf('(');
    //  	if (paramIndex != -1) {
    //  	    // If there are nested '<' and '>', the methodName was not
    //  	    // fully captured.  Extend the methodName past the ')'.
    //  	    // !! Right now, cannot handle nested methods that take
    //  	    // parameters.
    //  	    j = htmlTemplate.indexOf(')', paramIndex) + 1;
    //  	    methodName = htmlTemplate.substring(i + 2, j);
    //  	    Debug.Print("d", "parsing parameters for method: " + methodName);
    //  	    // Create a list of actuals by parsing the parameter list
    //  	    // String: (x, y, z).  Note that there may be function
    //  	    // calls within the parameter list.
    //  	    ArrayList actualsList = new ArrayList();
    //  	    int startIndex = methodName.indexOf('(') + 1;
    //  	    int commaIndex = methodName.indexOf(',');
    //  	    int endIndex;
    //  	    String actualStr;
    //  	    do {
    //  		endIndex = commaIndex;
    //  		if (endIndex == -1) {
    //  		    // To the end, minus the last ')'.
    //  		    endIndex = methodName.length() - 1;
    //  		}
    //  		// Parse the methodName String to get the next actual.
    //  		Debug.Print("d", "handling parameter at index: "
    //  			    + startIndex + " to " + endIndex);
    //  		actualStr = methodName.substring(startIndex, endIndex);
    //  		actualStr = parseRecursive(caller, actualStr, 0, forLoopArg);
    //  		Debug.Print("d", "next parameter: " + actualStr);
    //  		// Put the actual into the list.
    //  		actualsList.add(actualStr);
    //  		// Find the location of the next actual.
    //  		startIndex = endIndex + 1;
    //  		commaIndex = methodName.indexOf(',', startIndex);
    //  	    } while (commaIndex != -1);
    //  	    // Convert the actuals list to an array and create a type
    //  	    // array of the same size filled with String classes.
    //  	    int numActuals = actualsList.size();
    //  	    actuals = (String[])actualsList.toArray(new String[numActuals]);
    //  	    paramTypes = new Class[numActuals];
    //  	    int p;
    //  	    try {
    //  		for (p = 0; p < numActuals; p++) {
    //  		    paramTypes[p] = Common.stringToClass("java.lang.String");
    //  		}
    //  	    }
    //  	    catch (Exception e) {
    //  		System.err.println("failed to convert string to class: " + e);
    //  		return null;
    //  	    }
    //  	    // Strip off the parameter Strings from the methodName.
    //  	    methodName = methodName.substring(0, methodName.indexOf('('));
    //  	}
}