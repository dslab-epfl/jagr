package polyglot.ext.ib.extension;

import polyglot.ast.Branch;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.frontend.Job;
import polyglot.types.TypeSystem;
import polyglot.types.Context;
import polyglot.ext.ib.PrintUtil;

public class InjectBugBranchExt_c extends InjectBugExt_c {

    public Node rewrite( Job job, TypeSystem ts, NodeFactory nf, Context c ) {
	Branch branch = (Branch)node();
	Node ret;

	BugSpotInfo.GetBugSpotInfo(job).add(branch,this.getClass(),0,0,
					    "remove branch" );

	if( BugSpotInfo.GetBugSpotInfo(job).isActive(branch,this.getClass(),0)) {
	    // ret = nf.Empty( branch.position() );
	    ret = BugSpotInfo.createRuntimeBugReport( nf,
						      ts,
						      branch.position(),
						      "reportBranchError" );

	    PrintUtil.PrintBugInjection( "Remove Branch",
					 branch.position(),
					 branch,
					 ret );
	    return ret;
	}

	return branch;
    }

}
