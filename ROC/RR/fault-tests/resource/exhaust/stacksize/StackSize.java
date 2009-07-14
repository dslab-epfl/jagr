public class StackSize
{

public void recursiveCall()
{
int i = 0;
double j = 1;
long k = 2;

recursiveCall();	

}
public static void main(String args[])
{
	StackSize ss = new StackSize();
	ss.recursiveCall();
	System.out.println("Never to get printed");
}
}
