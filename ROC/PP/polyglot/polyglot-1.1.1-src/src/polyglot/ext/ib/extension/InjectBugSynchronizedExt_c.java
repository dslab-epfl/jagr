package polyglot.ext.ib.extension;

import polyglot.ast.Synchronized;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.frontend.Job;
import polyglot.types.TypeSystem;
import polyglot.types.Context;
import polyglot.ext.ib.PrintUtil;

public class InjectBugSynchronizedExt_c extends InjectBugExt_c {

    public Node rewrite( Job job, TypeSystem ts, NodeFactory nf, Context c ) {
	Synchronized synchronizedd = (Synchronized)node();
	Node ret;

	BugSpotInfo.GetBugSpotInfo(job).add(synchronizedd,this.getClass(),0,0,
					    "remove synchronized statement" );
	
	if( !BugSpotInfo.GetBugSpotInfo(job).isActive(synchronizedd,this.getClass(),0)) {
	    // DO NOT INJECT FAILURE
	    return synchronizedd;
	}

	ret = synchronizedd.body();

	PrintUtil.PrintBugInjection( "Remove synchronized statement",
				     synchronizedd.position(),
				     synchronizedd,
				     ret );
	
	return ret;
    }

}
