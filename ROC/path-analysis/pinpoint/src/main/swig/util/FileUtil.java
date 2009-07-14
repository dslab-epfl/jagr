/* Copyright  (c) 2002 The Board of Trustees of The Leland Stanford Junior
 * University. All Rights Reserved.
 *
 * See the file LICENSE for information on redistributing this software.
 */

package swig.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * This class contains static method utilities for dealing with files.
 *
 * @author <a href="http://www.cs.stanford.edu/~ach">
 *        Andy Huang</a> - ach@cs.stanford.edu 
 **/
public class FileUtil {
    /**
     * Writes an object to file.
     **/
    public static void makePersist(Object o, String filename)
        throws IOException {
        Debug.Enter("u", "FileUtil::makePersist(object, filename)");

        // Create the filename.
        File f = new File(filename);

        // Write the object to persistent store.
        FileOutputStream fos = new FileOutputStream(f);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(o);
        oos.flush();

        Debug.Exit("u", "FileUtil::makePersist(object, filename)");
    }

    /**
     * Returns an object that has previously been written to file
     * using <code>makePersist</code>.
     **/
    public static Object restoreObject(String filename) throws Exception {
        Debug.Enter("u", "FileUtil::restoreObject(filename)");

        // Deserialize the object.
        FileInputStream fis = new FileInputStream(filename);
        ObjectInputStream ois = new ObjectInputStream(fis);
        Object o = ois.readObject();

        Debug.Exit("u", "FileUtil::restoreObject(filename)");

        return o;
    }

    /**
     * Takes a byte array and writes the bytes to file.  Any original
     * contents of the file are overwritten.
     **/
    public static void writeBytesToFile(byte[] byteArr, String filename)
        throws IOException {
        if (byteArr != null) {
            FileOutputStream fos = new FileOutputStream(filename);
            fos.write(byteArr);
        }
    }

    /**
     * Takes a byte array and appends the bytes to file.
     **/
    public static void appendBytesToFile(byte[] byteArr, String filename)
        throws IOException {
        byte[] origBytes = null;

        File f = new File(filename);

        if (f.exists()) {
            origBytes = readBytesFromFile(filename);
        }

        int newLen = 0;
        int origLen = 0;

        if (origBytes != null) {
            origLen = origBytes.length;
            newLen += origLen;
        }

        if (byteArr != null) {
            newLen += byteArr.length;
        }

        byte[] newBytes = new byte[newLen];
        int i;

        for (i = 0; i < origLen; i++) {
            newBytes[i] = origBytes[i];
        }

        for (i = origLen; i < newLen; i++) {
            newBytes[i] = byteArr[i - origLen];
        }

        writeBytesToFile(newBytes, filename);
    }

    /**
     * Reads bytes from a file and returns them in a byte array.
     **/
    public static byte[] readBytesFromFile(String filename)
        throws IOException {
        File f = new File(filename);
        int len = (int) (f.length());
        byte[] byteArr = new byte[len];

        FileInputStream fis = new FileInputStream(f);
        fis.read(byteArr);

        return byteArr;
    }
}