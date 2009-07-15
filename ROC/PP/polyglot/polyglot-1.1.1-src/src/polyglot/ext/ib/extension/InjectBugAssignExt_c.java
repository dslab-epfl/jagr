package polyglot.ext.ib.extension;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import polyglot.ast.*;
import polyglot.frontend.Job;
import polyglot.types.*;
import polyglot.ext.ib.PrintUtil;

public class InjectBugAssignExt_c extends InjectBugExt_c {

    public Node rewrite( Job job, TypeSystem ts, NodeFactory nf, Context c ) {
	Assign assign = (Assign)node();
	Node ret;

	Type rhType = assign.right().type();

	if( rhType == null ) {
	    // CANNOT INJECT FAILURE HERE
	    return assign;
	}

	String currname;
	if( assign.left() instanceof Field ) {
	    currname = ((Field)assign.left()).name();
	} 
	else if( assign.left() instanceof Local ) {
	    currname = ((Local)assign.left()).name();
	}
	else {
	    // assign.left() is an array[], don't worry about name
	    currname = ""; 
	}


	Collection vars = c.getAllVariables();
	ArrayList validReplacements = new ArrayList();

	Iterator iter = vars.iterator();
	while( iter.hasNext() ) {
	    VarInstance vi = (VarInstance)iter.next();

	    if( vi == null )
		continue;

	    if( rhType.isSubtype( vi.type() ) &&
		!currname.equals(vi.name()) &&
		!vi.flags().isFinal() ) {
		
		validReplacements.add( vi );
	    }
	}

	if( validReplacements.size() > 0 ) {

	    BugSpotInfo.GetBugSpotInfo(job).add(assign,
						this.getClass(),
						0, validReplacements.size(),
						"Switch lhs of assignment" );
	    

	    if( BugSpotInfo.GetBugSpotInfo(job).isActive( assign, 
							  this.getClass(), 0 )) {

		int idx = 
		    BugSpotInfo.GetBugSpotInfo(job).getActiveBugParam( assign,
								 this.getClass(),0 );

		VarInstance vi = (VarInstance)validReplacements.get(idx);
		
		if( vi instanceof FieldInstance ) {
		    FieldInstance fi = (FieldInstance)vi;
		    Receiver r;
		    if( fi.flags().isStatic() ) {
			r = nf.CanonicalTypeNode(assign.position(), fi.container());
		    }
		    else {
			try {
			    ClassType scope = c.findFieldScope(fi.name());
			    if( !ts.equals(scope,c.currentClass() )) {
				r = nf.This(assign.position(),
					    nf.CanonicalTypeNode(assign.position(),scope));
			    }
			    else {
				r = nf.This(assign.position());
			    }
			}
			catch( SemanticException e ) {
			    // e.printStackTrace();
			    // should not happen -- if it does, we give up on the bug injection
			    return assign;
			}
		    }
		    
		    Field lhs = nf.Field( assign.position(),
					  r, fi.name()).fieldInstance(fi).targetImplicit(true);
		    
		    ret = nf.FieldAssign( assign.position(), 
					  lhs, assign.operator(),
					  BugSpotInfo.createRuntimeBugReport( nf,
									      ts,
									      assign.position(),
									      rhType,
									      "reportMisassignment",
									      assign.right())
					  );
		}
		else if( vi instanceof LocalInstance ) {
		    LocalInstance li = (LocalInstance)vi;
		    Local lhs = nf.Local( assign.position(),
					  li.name()).localInstance(li);
		    ret = nf.LocalAssign( assign.position(),
					  lhs, assign.operator(),
					  BugSpotInfo.createRuntimeBugReport( nf,
									      ts,
									      assign.position(),
									      rhType,
									      "reportMisassignment",
									      assign.right())
					  );
		}
		else {
		    throw new RuntimeException( "Unexpected type of Variable, neither field nor local!!!" );
		}
		
		PrintUtil.PrintBugInjection( "Switch lhs of assignment",
					     assign.position(),
					     assign,
					     ret );
		return ret;
	    }
	}

	// DO NOT INJECT FAULT
	return assign;
    }


}
