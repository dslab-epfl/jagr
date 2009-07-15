package polyglot.ext.ib.extension;

import polyglot.ast.Do;
import polyglot.ast.Expr;
import polyglot.ast.For;
import polyglot.ast.Loop;
import polyglot.ast.While;
import polyglot.ast.Node;
import polyglot.ast.Unary;
import polyglot.ast.NodeFactory;
import polyglot.types.Context;
import polyglot.frontend.Job;
import polyglot.types.TypeSystem;
import polyglot.ext.ib.PrintUtil;

public class InjectBugLoopExt_c extends InjectBugExt_c {

    public Node rewrite( Job job, TypeSystem ts, NodeFactory nf, Context c ) {
	Loop loop = (Loop)node();
	Node ret;

	if(( loop.cond() == null ) || 
	   !((loop instanceof Do) ||
	     ( loop instanceof For )  ||
	     ( loop instanceof While ))) {
	    // CANNOT INJECT FAULT
	    return loop;
	}

	BugSpotInfo.GetBugSpotInfo(job).add(loop,this.getClass(),0,0,
					    "invert loop condition" );

	if( !BugSpotInfo.GetBugSpotInfo(job).isActive(loop,this.getClass(),0)) {
	    // DO NOT INJECT FAULT
	    return loop;
	}
	// else inject fault

	//Expr newExpr = nf.Unary( loop.position(), loop.cond(), Unary.NOT );
	Expr newExpr = BugSpotInfo.createRuntimeBugReport( nf,
							   ts,
							   loop.position(),
							   null,
							   "injectInvertUnaryOp",
							   loop.cond() );
  
	if( loop instanceof Do ) {
	    ret = ((Do)loop).cond( newExpr );
	}
	else if( loop instanceof For ) {
	    ret = ((For)loop).cond( newExpr );

	}
	else if( loop instanceof While ) {
	    ret = ((While)loop).cond( newExpr );
	}
	else {
	    throw new RuntimeException( "THIS IS NOT POSSIBLE, WE CHECKED THAT loop is one of FOR WHILE or DO, but now its: " + loop );
	}

	PrintUtil.PrintBugInjection( "Invert Loop Condition",
				     loop.position(),
				     loop,
				     ret );

	return ret;
    }

}
