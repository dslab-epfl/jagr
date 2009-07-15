package polyglot.ext.ib;

import polyglot.util.Position;
import polyglot.ast.Node;


public class PrintUtil {

    public static void PrintBugInjection( String bugname,
				   Position position,
				   Node original,
				   Node modified ) {

	    System.out.println( "INJECTBUG: " +
				bugname + "\n\t" + 
				position.toString() +
				": " + original.toString() +
				" ---> " + modified.toString() );
    }

}
