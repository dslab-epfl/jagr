package polyglot.ext.ib.extension;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


import polyglot.ast.*;
import polyglot.frontend.Job;
import polyglot.types.*;
import polyglot.ext.ib.PrintUtil;

public class InjectBugBinaryExt_c extends InjectBugExt_c {

    static Map OffByOneRewrites;

    static Map OperatorToPPOp;

    static {
	OffByOneRewrites = new HashMap();
	OffByOneRewrites.put( Binary.GT, Binary.GE );
	OffByOneRewrites.put( Binary.GE, Binary.GT );
	OffByOneRewrites.put( Binary.LT, Binary.LE );
	OffByOneRewrites.put( Binary.LE, Binary.LT );
	OffByOneRewrites.put( Binary.COND_OR, Binary.COND_AND );
	OffByOneRewrites.put( Binary.COND_AND, Binary.COND_OR );

	OperatorToPPOp = new HashMap();
	OperatorToPPOp.put( Binary.GT, new Integer(0));
	OperatorToPPOp.put( Binary.GE, new Integer(1));
	OperatorToPPOp.put( Binary.LE, new Integer(3));
	OperatorToPPOp.put( Binary.LT, new Integer(4));
	OperatorToPPOp.put( Binary.COND_OR, new Integer(6));
	OperatorToPPOp.put( Binary.COND_AND, new Integer(7));

	
    }

    protected Expr rewriteSwitchSourceBug( Job job, Variable v,
					       TypeSystem ts,
					       NodeFactory nf,
					       Context c ) {

	Expr ret = v;

	boolean inStaticMethod = c.currentCode().flags().isStatic();

	Type type = v.type();
	String currname;
	if( v instanceof Field ) {
	    currname = ((Field)v).name();
	} 
	else if( v instanceof Local ) {
	    currname = ((Local)v).name();
	}
	else {
	    // v is an array[], don't worry about name
	    currname = ""; 
	}

	Collection vars = c.getAllVariables();
	ArrayList validReplacements = new ArrayList();

	Iterator iter = vars.iterator();
	while( iter.hasNext() ) {
	    VarInstance vi = (VarInstance)iter.next();
	    if( vi.type().isSubtype( type ) &&
		!currname.equals(vi.name()) &&
		!vi.flags().isFinal() &&
		(!inStaticMethod || vi.flags().isStatic() )) {
		
		validReplacements.add( vi );
	    }
	}
	

	if( validReplacements.size() > 0 ) {

	    BugSpotInfo.GetBugSpotInfo(job).add(v,
						this.getClass(),1,validReplacements.size(),
						"Switch rhs variable" );
	

	    if( BugSpotInfo.GetBugSpotInfo(job).isActive( v,
							  this.getClass(), 0 )) {
		
		int idx = BugSpotInfo.GetBugSpotInfo(job).getActiveBugParam( v,
									    this.getClass(), 0 );
		
		VarInstance vi = (VarInstance)validReplacements.get(idx);

		if( vi instanceof FieldInstance ) {
		    FieldInstance fi = (FieldInstance)vi;
		    Receiver r;
		    if( fi.flags().isStatic() ) {
			r = nf.CanonicalTypeNode(v.position(), fi.container());
		    }
		    else {
			try {
			    ClassType scope = c.findFieldScope(fi.name());
			    if( !ts.equals(scope,c.currentClass() )) {
				r = nf.This(v.position(),
					    nf.CanonicalTypeNode(v.position(),scope));
			    }
			    else {
				r = nf.This(v.position());
			    }
			}
			catch( SemanticException e ) {
			    //e.printStackTrace();
			    // should not happen
			    // DO NOT INJECT BUG
			    return v;
			}
		    }
		    
		    ret = BugSpotInfo.createRuntimeBugReport( nf,
							      ts,
							      v.position(),
							      v.type(),
							      "injectMisreference",
							      v,
							      nf.Field( v.position(), 
									r, fi.name()).fieldInstance(fi).targetImplicit(true));
							      
							      
		}
		else if( vi instanceof LocalInstance ) {
		    LocalInstance li = (LocalInstance)vi;

		    ret = BugSpotInfo.createRuntimeBugReport( nf,
							      ts,
							      v.position(),
							      v.type(),
							      "injectMisreference",
							      v,
							      nf.Local( v.position(), li.name()).localInstance(li));
							      
		}
		else {
		    throw new RuntimeException( "Unexpected type of Variable, neither field nor local!!!" );
		}
		
		PrintUtil.PrintBugInjection( "Switch source variable",
					     v.position(),
					     v,
					     ret );
		return ret;
	    }	
	}

	// DO NOT INJECT FAULT
	return v;
    }

    protected Binary rewriteSwitchSourceBug( Job job, Binary binary, TypeSystem ts,
				       NodeFactory nf, Context c ) {
	Binary ret;

	Expr left = binary.left();
	Expr right = binary.right();

	if( left instanceof Variable ) {
	    left = rewriteSwitchSourceBug( job, (Variable)left,
					   ts, nf, c );
	}

	if( right instanceof Variable ) {
	    right = rewriteSwitchSourceBug( job, (Variable)right,
					    ts,nf,c);
	}

	return nf.Binary( binary.position(), left, binary.operator(), right );
    }


    protected Expr rewriteOffByOneBug( Job job, Binary binary, TypeSystem ts,
				       NodeFactory nf, Context c ) {
	Expr ret;

	if( !OffByOneRewrites.containsKey( binary.operator() )) {
	    return binary;
	}

	BugSpotInfo.GetBugSpotInfo(job).add(binary,this.getClass(),0,0,
					    "off by one bug" );

	if( BugSpotInfo.GetBugSpotInfo(job).isActive(binary,this.getClass(),0)) {
	    // ret = binary.operator((Binary.Operator)OffByOneRewrites.get( binary.operator() ));
	
	    ret = BugSpotInfo.createRuntimeBugReport( nf, ts, binary.position(),
						      null,
						      "injectFaultyBinaryComparison",
						      binary.left(),
						      binary.right(),
						      ((Integer)OperatorToPPOp.get( binary.operator() )).intValue(),
						      ((Integer)OperatorToPPOp.get( OffByOneRewrites.get( binary.operator() ))).intValue() );

	    PrintUtil.PrintBugInjection( "Switch Binary Operator (e.g., off-by-one)",
				     binary.position(),
				     binary,
				     ret );

	    return ret;
	}

	return binary;
    }

    public Node rewrite( Job job, TypeSystem ts, NodeFactory nf, Context c ) {

	Expr binary = (Binary)node();

	binary = rewriteSwitchSourceBug( job, (Binary)binary, ts, nf, c );
	binary = rewriteOffByOneBug( job, (Binary)binary, ts, nf, c);
	
	return binary;	
    }

}
