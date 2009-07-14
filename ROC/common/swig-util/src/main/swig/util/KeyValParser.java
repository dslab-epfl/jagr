/* Copyright  (c) 2002 The Board of Trustees of The Leland Stanford Junior
 * University. All Rights Reserved.
 *
 * See the file LICENSE for information on redistributing this software.
 */

package swig.util;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * This class provides a simple way to deal with Strings of the form
 * <code>key [separator] value</code>.  Using this class, key/value
 * pairs can be stored, added, and output to String.
 *
 * @author <a href="http://www.cs.stanford.edu/~ach">
 *        Andy Huang</a> - ach@cs.stanford.edu 
 **/
public class KeyValParser extends Hashtable {
    private String keyValueSeparator;
    private String pairSeparator;

    // -=-=- Constructors =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    public KeyValParser() {
        // Call the parent constructor.
        super();

        Debug.Enter("u", "KeyValParser default constructor");

        Debug.Exit("u", "KeyValParser default constructor");
    }

    /**
     * @param keyValueSeparator The String that separates keys and
     * values (e.g., in URL data, the keyValueSeparator is "=").
     * @param pairSeparator The String that separates (key, value)
     * pairs (e.g., in URL data, the pairSeparator is "&").
     **/
    public KeyValParser(String keyValueSeparator, String pairSeparator) {
        // Call the default constructor.
        this();

        Debug.Enter("u", "KeyValParser separator constructor");
        setKeyValueSeparator(keyValueSeparator);
        setPairSeparator(pairSeparator);
        Debug.Exit("u", "KeyValParser separator constructor");
    }

    // -=-=- General Public Methods =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=

    /**
     * Adds a key/value pair to this object.
     **/
    public void put(String key, String value) {
        super.put(key, value);
    }

    //      public void parseReverse(String s)
    //      {
    //  	Debug.Enter("u", "KeyValParser::parseReverse()");
    //  	parseHelper(s, true);
    //  	Debug.Exit("u", "KeyValParser::parseReverse()");
    //      }

    /**
     * Parses the given String and adds all key/value pairs to this
     * object.  
     **/
    public void parse(String s) {
        Debug.Enter("u", "KeyValParser::parse()");
        parseHelper(s, null, null);
        Debug.Exit("u", "KeyValParser::parse()");
    }

    /**
     * Allows special handling during parsing.  For each key/value
     * pair found, the KeyValParser will call the given caller's
     * <code>hashHandler</code> method.  One example of a use of
     * custom parsing is adding key/value pairs when the String is of
     * the form "[value]:[pair]."
     *
     * @param s The String to be parsed.
     * @param caller The {@link KeyValParserClientIF} object to be
     * called for each key/value pair; this object is responsible for
     * adding the key/value pair to this object.  
     **/
    public void parseCustom(String s, KeyValParserClientIF caller) {
        Debug.Enter("u", "KeyValParser::parseCustom(str, caller)");
        parseCustom(s, caller, "");
        Debug.Exit("u", "KeyValParser::parseCustom(str, caller)");
    }

    /**
     * @param flag The flag to be passed to the caller each time its
     * <code>hashHandler</code> method is called.  
     **/
    public void parseCustom(
        String s,
        KeyValParserClientIF caller,
        String flag) {
        Debug.Enter("u", "KeyValParser::parseCustom(str, caller, flag)");
        parseHelper(s, caller, flag);
        Debug.Exit("u", "KeyValParser::parseCustom(str, caller, flag)");
    }

    private void parseHelper(
        String s,
        KeyValParserClientIF caller,
        String flag) {
        Debug.Print("u", "parsing string: " + s);

        int pairStart = 0;
        int midIndex = s.indexOf(keyValueSeparator);
        int pairEnd;

        String key;
        String value;

        while (midIndex != -1) {
            Debug.Print("u", "pair separator: " + midIndex);
            key = s.substring(pairStart, midIndex);

            pairEnd = s.indexOf(pairSeparator, pairStart);
            Debug.Print("u", "pair end: " + pairEnd);

            if (pairEnd == -1) {
                pairEnd = s.length();
            }

            value = s.substring(midIndex + keyValueSeparator.length(), pairEnd);

            // Add the pair to the hash table: normally if caller is
            // null, otherwise, call the custom handler.
            if (caller == null) {
                put(key, value);
                Debug.Print("u", "added " + key + " / " + value + " pair");
            }
            else {
                caller.hashHandler(key, value, this, flag);
            }

            // Update the indices to the next pair.
            pairStart = pairEnd + pairSeparator.length();
            midIndex = s.indexOf(keyValueSeparator, pairStart);
        }
    }

    /**
     * Creates a String containing all key/value pairs in this object
     * using the <code>keyValSeparator</code> and
     * <code>pairSeparator</code> to separate the keys, values, and
     * key/value pairs.  
     **/
    public String toString() {
        Debug.Enter("u", "KeyValParser::toString()");

        StringBuffer strBuf = new StringBuffer();
        Enumeration allNames = keys();
        String name;
        String value;

        // Add the name/value pairs.
        while (allNames.hasMoreElements()) {
            name = (String) allNames.nextElement();
            value = (String) get(name);

            strBuf.append(name);
            strBuf.append(keyValueSeparator);
            strBuf.append(value);

            if (allNames.hasMoreElements()) {
                strBuf.append(pairSeparator);
            }
        }

        Debug.Exit("u", "KeyValParser::toString()");

        return strBuf.toString();
    }

    // -=-=- Set and Get Methods -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    public void setKeyValueSeparator(String keyValueSeparator) {
        this.keyValueSeparator = keyValueSeparator;
    }

    public void setPairSeparator(String pairSeparator) {
        this.pairSeparator = pairSeparator;
    }

    public String getKeyValueSeparator() {
        return keyValueSeparator;
    }

    public String getPairSeparator() {
        return pairSeparator;
    }

    /**
     * Returns the value corresponding to the given key.  
     **/
    public String get(String key) {
        return (String) super.get(key);
    }
}