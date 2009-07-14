public class SimpleThread extends Thread
{

StartSimpleThread smThread;

public SimpleThread(StartSimpleThread sst)
{
	smThread = sst;
}

public void run()
{
	synchronized (this) {smThread.numLiveThreads++;}
	System.out.println("Create " + smThread.numLiveThreads);
	for(int i = 0;i < 1000; i++)
	{	
		try
		{
		 sleep(100);
		}
		catch(InterruptedException e)
		{
			System.out.println("Sleep distrubed");
			e.printStackTrace();
		}
	}
	synchronized (this) {smThread.numLiveThreads--;}
	System.out.println("Destroy "+ smThread.numLiveThreads);
}

}
