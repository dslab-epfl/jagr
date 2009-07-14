import java.io.*;

class DataToFill
{
 public static void main(String args[])
 {
  String str = "Let us all write in so many loops of data"
                  + " that the hard disk is filled with junk"
                  + " and may the JVM throw an exception.\n\n";
  byte buff[]=str.getBytes();
try
{	
  OutputStream fileOutput=new FileOutputStream("file.txt");
 // for(int i=0;i<10;i++)
	while(true)
	{
  fileOutput.write(buff);
	}
 // fileOutput.close();
}
catch(Exception e)
{
System.out.println("Too big a file");
e.printStackTrace();
}
 }
}
