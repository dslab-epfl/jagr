public class StartSimpleThread
{
	
int numLiveThreads;

public static void main(String args[])	
{
	StartSimpleThread mainThread = new StartSimpleThread();
	for(int i = 0; i < 10; i++)
	{
		SimpleThread st = new SimpleThread(mainThread);
		st.start();
	}
	while (mainThread.numLiveThreads > 0)
	{
	}
	System.out.println("DOne");
}

}
