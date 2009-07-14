import java.util.Vector;

/**
 * @author bling
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ReaderWriter extends Thread {

	Stub _s;
	long statesize;
	int startkey;
	
	Vector _v;

	public void init(Stub s, long statesz, int sk, Vector v) {
		_s = s;
		statesize = statesz;
		startkey = sk;
		_v = v;
	}
	
	public void run() {
		long exp = System.currentTimeMillis() + 500000;
		long l = 1;
		long cumwritetime = 0;
		long cumreadtime = 0;
		String writestring = "S";
		while (writestring.length() < statesize) {
				writestring = writestring + writestring;	
		}
			
		
		int ID = startkey;

		int m;		
		Vector k = new Vector();
		
		long pause = 60000;
		try {synchronized(k) {
			k.wait(4000);
		}}
		catch (Exception e) {}
		
/*		try{
			k = _s.Write(1, "hi", exp);
			_s.read(k);
			System.out.println(this.getName() + " I wrote the initial write");
		}
		catch (SystemOverloadedException e) {
			//e.printStackTrace();
		}
		catch (Stub.InsufficientBricksAvailableException iba) {
			iba.printStackTrace();	
		}
		catch (Exception e) {
			//e.printStackTrace();	
		}
		
		
		
		try {synchronized(k) {
			k.wait(5);
		}}
		catch (Exception e) {}
*/		
		long timezero = System.currentTimeMillis();
		
		int numops = 0;
		int numfailed = 0;
		//System.out.println("Starting reading/writing");
		for (m = 1; m < 200000; m++) {	

			Vector v = null;
			long start;
			long done; 	
			long lastinputtime = 0;
			try {
				start = System.currentTimeMillis();
				v = _s.Write(ID+m, writestring + (1000*ID) + l, System.currentTimeMillis() + 10000);
				pause-=5;
				done = System.currentTimeMillis();
				cumwritetime += (done-start);
				numops++;
			//	System.out.println("Write of " + (ID+m) + " completed in " + (done-start) + "numops " + numops);
			
				// now read it
				start = System.currentTimeMillis();
				String readstr  = null;
				readstr = (String) _s.read(v);
				pause-=5;
				done = System.currentTimeMillis();
				cumreadtime += (done-start);
				numops++;
				//System.out.println("Read completed in " + (done-start) + "numops " + numops);
				
				if (!readstr.equals(writestring + (1000*ID) + l)) {
					System.out.println(ID+"ERROR! " + writestring+ (1000*ID) + l + "what was read is not the same as what was written" + readstr);
				}
				
			}
			catch (Exception ee) {
			    numfailed++;
			    				try {synchronized(k) {
					k.wait(20);
					}}
					catch (Exception eee) {}
				//System.out.println("operation on " + (ID+m) + " failed!");
				//ee.printStackTrace();
				//System.out.println(this.getName() + " caught exception");
				//ee.printStackTrace();	
/*				System.out.println("The system is overloaded, read failed");	
				try {synchronized(k) {
					k.wait(60);
				}}
				catch (Exception eee) {}
				pause = pause*2;*/
			}
			finally {
				//System.out.println("running finally");
				if (lastinputtime < System.currentTimeMillis() - 1000) {
					synchronized(_v) {
						Integer in = (Integer) _v.elementAt(0);	
						in = new Integer(in.intValue() + numops);
						_v.setElementAt(in, 0);

						Integer fail = (Integer) _v.elementAt(1);
						fail = new Integer(fail.intValue() + numfailed);
						_v.setElementAt(fail, 1);
					}				
					lastinputtime = System.currentTimeMillis();	
					numops = 0;
					numfailed = 0;
				}
			}
			



			l++;
		}

		
		Vector v = new Vector();
		v.add(new Long(numops/2));
		v.add(new Long(cumreadtime));
		v.add(new Long(cumwritetime));
		
		synchronized(_v) {
			_v.add(v);
			_v.notifyAll();
		}
				
		
	}
}
