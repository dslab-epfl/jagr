package polyglot.ext.ib.visit;

import polyglot.ext.ib.ast.*;
import polyglot.ext.ib.extension.*;
import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.*;
import polyglot.frontend.*;
import polyglot.visit.*;
import java.util.*;


public class InjectBugVisitor extends AscriptionVisitor {


    public InjectBugVisitor(Job job, TypeSystem ts, NodeFactory nf ) {
	super(job,ts,nf);
    }

    public Node leaveCall( Node old, Node n, NodeVisitor v )
	throws SemanticException {
	n = super.leaveCall(old,n,v);
	if( n.ext() instanceof InjectBugExt ) {
	    Node ret = ((InjectBugExt)n.ext()).rewrite( job(), typeSystem(),nodeFactory(),
						       context() );

	    if( ret instanceof Ambiguous ) {
		System.err.println( "foo" );
		Disamb disamb = nodeFactory().disamb();
		ret = disamb.disambiguate( (Ambiguous)ret, this, ret.position(),
					   null, null );
		System.err.println( "foo2: " + ret.toString() );
	    }

	    return ret;
	}

	return n;
    }

}
