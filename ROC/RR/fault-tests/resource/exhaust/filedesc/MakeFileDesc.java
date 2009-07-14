import java.io.*;

class MakeFileDesc
{
	public static void main(String args[])
	{
		//String fileNames[] = new String[100];
		//FileDescriptor fd[] = new FileDescriptor[10000];
	        OutputStream os[] = new OutputStream[1000000];
		String dirName = "";
		try
		{
	//	String dirName = "";
		for(int i = 0; i < 1000000 ; i++)
		{	
		
			if( i % 100 == 0)
			{
				dirName = "dir" + i;
				File newDir = new File (dirName);
				newDir.mkdir();
				System.out.println(dirName);
			}
				String fName = dirName + "/file" + i;
				//System.out.println(fName);
				//fileNames[i] = fName;
				//String fileOutName = "fileOup" + i;
				OutputStream fileOutput = new FileOutputStream(fName);
				fileOutput.write(fName.getBytes());
				//fd[i] =  ((FileOutputStream)fileOutput).getFD();
				os[i] = fileOutput;
				//fileOutput.close();
				
				
	
		}
		}
		catch(FileNotFoundException e)
		{
			System.err.println("FileNotFoundException Occurred in MakeFileDesc");
			e.printStackTrace();
		}
		catch(IOException ioe)
		{
			System.err.println("IOException Occurred in MakeFileDesc");		
			ioe.printStackTrace();
		}
	}
}
