package polyglot.ext.ib;

import polyglot.lex.Lexer;
import polyglot.ext.ib.parse.Lexer_c;
import polyglot.ext.ib.parse.Grm;
import polyglot.ext.ib.ast.*;
import polyglot.ext.ib.types.*;
import polyglot.ext.ib.visit.*;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import polyglot.frontend.*;
import polyglot.ext.jl.types.TypeSystem_c;
import polyglot.main.*;

import java.util.*;
import java.io.*;

/**
 * Extension information for ib extension.
 */
public class ExtensionInfo extends polyglot.ext.jl.ExtensionInfo {
    static {
        // force Topics to load
        Topics t = new Topics();
    }

    public String defaultFileExtension() {
        return "ib";
    }

    public String compilerName() {
        return "ibc";
    }

    public Parser parser(Reader reader, FileSource source, ErrorQueue eq) {
        Lexer lexer = new Lexer_c(reader, source.name(), eq);
        Grm grm = new Grm(lexer, ts, nf, eq);
        return new CupParser(grm, source, eq);
    }

    protected NodeFactory createNodeFactory() {
        return new InjectBugNodeFactory_c();
    }

    protected TypeSystem createTypeSystem() {
        return new TypeSystem_c();
    }

    public static final Pass.ID INJECT_BUG = new Pass.ID("inject-bug");
    public static final Pass.ID COUNT_BUG_SPOTS = new Pass.ID("count-bug-spots");

    public List passes(Job job) {

	List newpasses = new ArrayList();
	newpasses.add( new VisitorPass(COUNT_BUG_SPOTS,
				       job, new CountBugSpotsVisitor(job,ts,nf)));
	newpasses.add( new VisitorPass(INJECT_BUG,
				       job, new InjectBugVisitor(job,ts,nf)));

        List passes = super.passes(job);
        beforePass(passes,Pass.PRE_OUTPUT_ALL,
		   newpasses);

        return passes;
    }

}
