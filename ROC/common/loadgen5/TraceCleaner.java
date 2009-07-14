import java.util.*;
import java.io.*;

public class TraceCleaner extends Thread
{
   public static final String TEMP_FILE = "temp.trc";
    
    boolean InPost = false;
    boolean DataNext = false;
    String outputfile;
    TraceProxy proxy;

    public void run()
    {
	System.err.println("Ending trace");
	proxy.setDone( true );
	while(proxy.isRecording())
	    {
		try{
		    this.sleep(500);
		} catch (Exception e) {};
	    }
	proxy.closeWriter();
	System.err.println("Cleaning trace file.");
	cleanTrace();
	File f = new File(TEMP_FILE);
	f.delete();
    }

    public TraceCleaner(String outputfile, TraceProxy proxy)
    {    
	this.outputfile = outputfile;
	this.proxy = proxy;
    }

    synchronized void cleanTrace()
    {
	try {
	    BufferedReader traceReader =
		new BufferedReader(new FileReader(TEMP_FILE));
	    PrintWriter writer = new PrintWriter(new FileWriter(outputfile, true));	
	String line = null;

	do {
	    line = traceReader.readLine();
	    if(line != null) {
		line = ParseLine(line);
		if(!DataNext)
		    {
			writer.print(line + "\n");
			writer.flush();
		    }
	    }
	} while(line != null);

	writer.close();
	traceReader.close();

	} catch(Exception e) {};
    }

    private String ParseLine(String line)
    {
	if (DataNext)
	    {
		line = "LG-POSTDATA " + line;
		DataNext = false;
		InPost = false;
	    }
	else if (line.startsWith("POST")) 
	    InPost = true;
	else if (line.equals("") && InPost)
	    DataNext = true;
	
	return line;
    }
}
