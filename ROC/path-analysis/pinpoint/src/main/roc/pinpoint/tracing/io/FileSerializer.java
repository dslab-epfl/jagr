package roc.pinpoint.tracing.io;

import java.io.*;
import java.util.Date;
import java.util.Calendar;

/**
 * Serialization routines for writing observations to file and reading them back.
 *
 * @author  <A HREF="http://www.cs.berkeley.edu/~mikechen/">Mike Chen</A>
 *              (<A HREF="mailto:mikechen@cs.berkeley.edu">
 *                mikechen@cs. berkeley.edu</A>)
 */

public class FileSerializer {
    FileOutputStream   fout = null;
    ObjectOutputStream oos  = null;

    FileInputStream    fin = null;
    ObjectInputStream  ois = null;

    String fileName = null;

    public FileSerializer() {
	this(true);
    }

    public FileSerializer(boolean createFile) {
	if (createFile) {
	    try {
		createFile();
	    }
	    catch (Exception e) {
		e.printStackTrace();
	    }
	}
    }

    public void createFile() throws IOException {
	Calendar now = Calendar.getInstance();
	String dateStr = "" + now.get(Calendar.YEAR) + "_" + (now.get(Calendar.MONTH)+1) + "_" + now.get(Calendar.DAY_OF_MONTH) + "_" + now.get(Calendar.HOUR_OF_DAY) + "_" + now.get(Calendar.MINUTE);
	fileName = "/tmp/obs_" + dateStr + ".dat";
	fout = new FileOutputStream(fileName);
	oos  = new ObjectOutputStream(fout);
    }


    public synchronized void writeObject(Serializable obj) throws IOException {
	oos.writeObject(obj);
    }

    public void closeFile() throws IOException {
	if (fout != null) {
	    fout.flush();
	    fout.close();
	}

	if (fin != null) {
	    fin.close();
	}
    }


    public String getFileName() {
	return fileName;
    }


    protected void finalize() {
	try {
	    closeFile();
	}
	catch (Exception e) {
	    e.printStackTrace();
	}
    }


    public void openFile(File file) throws IOException {
	fin = new FileInputStream(file);
	ois = new ObjectInputStream(fin);
    }


    public Object readObject() throws IOException, ClassNotFoundException {
	return ois.readObject();
    }


    public static void main(String[] args) {
	try {
	    FileSerializer s = new FileSerializer();
	    s.createFile();
	    s.writeObject(new Date());
	    
	    Thread.sleep(2000);
	    System.out.println("now: " + new Date());
	    s.openFile(new File(s.getFileName()));
	    Date date = (Date)s.readObject();
	    System.out.println("saved: " + date);

	    // EOFException
	    /*
	    date = (Date)s.readObject();
	    System.out.println("saved: " + date);
	    */

	    s.closeFile();
	}
	catch (Exception e) {
	    e.printStackTrace();
	}

	    /*
	long startTime = System.currentTimeMillis();
	for (int i = 0; i < 10; i++) {
	    oos.writeObject(PP_obs);
	}
	long stopTime = System.currentTimeMillis();
	System.out.println("serilization time = " + (stopTime-startTime)/10);
	    */
    }
		
}

