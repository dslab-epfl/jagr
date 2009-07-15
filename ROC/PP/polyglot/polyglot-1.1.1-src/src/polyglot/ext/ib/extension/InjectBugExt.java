package polyglot.ext.ib.extension;

import polyglot.ast.Ext;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.types.TypeSystem;
import polyglot.types.Context;
import polyglot.frontend.Job;

public interface InjectBugExt extends Ext {
    public Node rewrite( Job job, TypeSystem ts, NodeFactory nf, Context c );
}
