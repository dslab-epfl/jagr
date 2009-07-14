import java.util.*;
    public class testLinked {
	

        public static void main( String [ ] args )
        {
        try
	{    
	LinkedList    theList = new LinkedList( );



            for( double i = 0; i < 1000000; i++ )
            {
                theList.add(new Double( i ) );
			if(i % 10000 == 0)
			{
				System.out.println("mod " + i);
				Runtime r = Runtime.getRuntime();
				System.out.println(r.maxMemory());
				System.out.println(r.totalMemory());
				System.out.println(r.freeMemory());


			}
            }

           Iterator itr = theList.iterator();

         /*  while(itr.hasNext())
           {
                Double printInt = (Double) itr.next();
                System.out.println(printInt.doubleValue());
           }
		*/
	}
	catch(Exception e)
	{
		System.out.println("There was some problem");
		e.printStackTrace();
	}
	
        }

    }
