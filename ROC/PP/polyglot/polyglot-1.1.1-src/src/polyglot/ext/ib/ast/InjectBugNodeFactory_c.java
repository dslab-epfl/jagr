package polyglot.ext.ib.ast;

import polyglot.ast.*;
import polyglot.ext.jl.ast.*;
import polyglot.types.Flags;
import polyglot.types.Package;
import polyglot.types.Type;
import polyglot.types.Qualifier;
import polyglot.util.*;
import java.util.*;

/**
 * NodeFactory for ib extension.
 */
public class InjectBugNodeFactory_c extends NodeFactory_c {

    public InjectBugNodeFactory_c() {
	super( new InjectBugExtFactory_c() );
    }

    protected InjectBugNodeFactory_c(ExtFactory extFact ) {
	super(extFact);
    }

    public LocalDecl LocalDecl(Position pos, 
			       Flags flags,
			       TypeNode type, 
			       String name, 
			       Expr init) {
	LocalDecl ret = new LocalDecl_c(pos,flags,type,name,init);
	ret = (LocalDecl)ret.ext(extFactory().extLocalDecl());
	return (LocalDecl)ret.del();
    }

}
