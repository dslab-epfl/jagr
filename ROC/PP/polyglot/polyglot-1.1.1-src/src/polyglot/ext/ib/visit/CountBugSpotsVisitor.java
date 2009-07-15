package polyglot.ext.ib.visit;

import polyglot.ext.ib.ast.*;
import polyglot.ext.ib.extension.*;
import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.*;
import polyglot.frontend.*;
import polyglot.visit.*;
import java.util.*;

public class CountBugSpotsVisitor extends AscriptionVisitor {

    List injectablespots;

    public CountBugSpotsVisitor( Job job, TypeSystem ts, NodeFactory nf ) {
	super( job, ts, nf );

	injectablespots = new ArrayList();
	injectablespots.add( Assign.class );
	injectablespots.add( Branch.class );
	injectablespots.add( Binary.class );
	//injectablespots.add( FieldDecl.class );
	injectablespots.add( LocalDecl.class );
	injectablespots.add( Loop.class );
	injectablespots.add( MethodDecl.class );
	injectablespots.add( Synchronized.class );

	BugSpotInfo.CreateBugSpotInfo( job );
    }

    public Node leaveCall( Node old, Node n, NodeVisitor v ) 
	throws SemanticException {

	super.leaveCall(old,n,v);

	return old;
    }

}
