package polyglot.ext.ib.extension;

import polyglot.ast.Expr;
import polyglot.ast.IntLit;
import polyglot.ast.FieldDecl;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.frontend.Job;
import polyglot.types.Context;
import polyglot.types.TypeSystem;
import polyglot.ext.ib.PrintUtil;

public class InjectBugFieldDeclExt_c extends InjectBugExt_c {

    // rewrite FieldDecl node to ensure variables are initialized
    public Node rewrite( Job job, TypeSystem ts, NodeFactory nf, Context c ) {
	FieldDecl fielddecl = (FieldDecl)node();
	Node ret = fielddecl;


	if( fielddecl.init() == null && !fielddecl.flags().isFinal() ) {
	    // (almost) always make sure that decl has a default initializer...
	    //  this saves us a lot of "this var may not have been initialized"
	    //  errors when we compile after injecting a source or dest
	    //  variable change..

	    Expr emptyExpr;

	    if( fielddecl.declType().isBoolean() ) {
		emptyExpr = nf.BooleanLit(fielddecl.position(),
					  false );
	    }
	    else if( fielddecl.declType().isNumeric() ) {
		emptyExpr = nf.IntLit(fielddecl.position(),
				      IntLit.INT, 0 );
	    }
	    else {
		emptyExpr = nf.NullLit(fielddecl.position());
	    }

	    ret = fielddecl.init( emptyExpr );
	}	

	return ret;
    }

}
