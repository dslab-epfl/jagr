package polyglot.ext.ib.extension;

import polyglot.ast.MethodDecl;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.frontend.Job;
import polyglot.types.Context;
import polyglot.types.TypeSystem;
import polyglot.types.Flags;
import polyglot.ext.ib.PrintUtil;

public class InjectBugMethodDeclExt_c extends InjectBugExt_c {

    public Node rewrite( Job job, TypeSystem ts, NodeFactory nf, Context c ) {
	MethodDecl methoddecl = (MethodDecl)node();
	Node ret;

	Flags flags = methoddecl.flags();

	if(!flags.isSynchronized() ) {
	    // CANNOT INJECT FAULT
	    return methoddecl;
	}

	BugSpotInfo.GetBugSpotInfo(job).add(methoddecl,this.getClass(),0,0,"remove synchronized method modifier" );

	if( !BugSpotInfo.GetBugSpotInfo(job).isActive(methoddecl,this.getClass(),0)) {
	    // DO NOT INJECT FAILURE
	    return methoddecl;
	}

	ret = methoddecl.flags( flags.clearSynchronized() );
	
	PrintUtil.PrintBugInjection( "Remove synchronized method modifier",
				     methoddecl.position(),
				     methoddecl,
				     ret );
	
	return ret;
    }

}
