package polyglot.ext.ib.extension;

import java.util.Random;

import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ext.jl.ast.Ext_c;
import polyglot.types.TypeSystem;
import polyglot.types.Context;
import polyglot.ext.ib.visit.CountBugSpotsVisitor;
import polyglot.frontend.Job;

public class InjectBugExt_c extends Ext_c implements InjectBugExt {

    protected static Random random = new Random();

    public Node rewrite( Job job, TypeSystem ts, NodeFactory nf, Context c) {
	return node();
    }


     
}
