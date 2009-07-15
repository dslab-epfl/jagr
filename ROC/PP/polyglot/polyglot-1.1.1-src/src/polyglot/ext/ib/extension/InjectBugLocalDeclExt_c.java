package polyglot.ext.ib.extension;

import polyglot.ast.Expr;
import polyglot.ast.IntLit;
import polyglot.ast.Lit;
import polyglot.ast.Variable;
import polyglot.ast.LocalDecl;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.types.Context;
import polyglot.frontend.Job;
import polyglot.types.TypeSystem;
import polyglot.ext.ib.PrintUtil;

public class InjectBugLocalDeclExt_c extends InjectBugExt_c {

    // rewrite LocalDecl node to remove the initialization expr;
    public Node rewrite( Job job, TypeSystem ts, NodeFactory nf, Context c ) {
	LocalDecl localdecl = (LocalDecl)node();
	Node ret = localdecl;


	if( localdecl.init() == null && !localdecl.flags().isFinal() ) {
	    // (almost) always make sure that code has a default initializer... this
	    //  saves us a lot of "this var may not have been initialized"
	    //  errors when we compile after injecting a source or dest
	    //  variable change...

	    Expr emptyExpr;

	    if( localdecl.declType().isBoolean() ) {
		emptyExpr = nf.BooleanLit(localdecl.position(),
					  false );
	    }
	    else if( localdecl.declType().isNumeric() ) {
		emptyExpr = nf.IntLit(localdecl.position(),
				      IntLit.INT, 0 );
	    }
	    else {
		emptyExpr = nf.NullLit(localdecl.position());
	    }

	    ret = localdecl.init(emptyExpr);
	    
	    // NO FAULT INJECTION
	    return ret;
	}	
	else {

	    if( !( localdecl.init() instanceof Lit ) &&
		!( localdecl.init() instanceof Variable )) {
		// DO NOT INJECT FAILURE
		// ... this is a bit of a cop-out, but because
		//     this expr might be calling a method that throws
		//     an exception, in which case we might be in a try-catch
		//     block that will no longer compile...
		//     right thing to do is iterate down the expr and see if
		//     there are any calls to methods that throw exceptions,
		//     and only abort failure injection then... but that's
		//     to much work...
		return localdecl;		
	    }

	    BugSpotInfo.GetBugSpotInfo(job).add(localdecl,this.getClass(),0,0,
						"initialization bug" );

	    if( !BugSpotInfo.GetBugSpotInfo(job).isActive(localdecl,this.getClass(),0)) {
		// DONT INJECT FAULT
		return localdecl;
	    }

	    // else INJECT FAULT

	    Expr emptyExpr;

	    if( localdecl.declType().isBoolean() ) {
		emptyExpr = nf.BooleanLit(localdecl.position(),
					  false );
	    }
	    else if( localdecl.declType().isNumeric() ) {
		emptyExpr = nf.IntLit(localdecl.position(),
				      IntLit.INT, 0 );
	    }
	    else {
		emptyExpr = nf.NullLit(localdecl.position());
	    }


	    ret = localdecl.init(BugSpotInfo.createRuntimeBugReport( nf,
								     ts,
								     localdecl.position(),
								     localdecl.declType(),
								     "reportMisassignment",
								     emptyExpr ));

	    PrintUtil.PrintBugInjection( "Initialization bug",
					 localdecl.position(),
					 localdecl,
					 ret );
	    return ret;
	}

    }

}
