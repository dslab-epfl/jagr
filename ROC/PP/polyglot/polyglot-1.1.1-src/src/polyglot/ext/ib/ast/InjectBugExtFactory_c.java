package polyglot.ext.ib.ast;

import polyglot.ext.jl.ast.AbstractExtFactory_c;
import polyglot.ast.Ext;
import polyglot.ext.ib.extension.InjectBugExt_c;
import polyglot.ext.ib.extension.InjectBugAssignExt_c;
import polyglot.ext.ib.extension.InjectBugBranchExt_c;
import polyglot.ext.ib.extension.InjectBugBinaryExt_c;
import polyglot.ext.ib.extension.InjectBugFieldDeclExt_c;
import polyglot.ext.ib.extension.InjectBugLocalDeclExt_c;
import polyglot.ext.ib.extension.InjectBugLoopExt_c;
import polyglot.ext.ib.extension.InjectBugMethodDeclExt_c;
import polyglot.ext.ib.extension.InjectBugSynchronizedExt_c;


public class InjectBugExtFactory_c extends AbstractExtFactory_c {

    public InjectBugExtFactory_c() {
    }

    public Ext extNodeImpl() {
	return new InjectBugExt_c();
    }

    public Ext extLocalDeclImpl() {
	return new InjectBugLocalDeclExt_c();
    }

    public Ext extFieldDeclImpl() {
	return new InjectBugFieldDeclExt_c();
    }

    public Ext extLoopImpl() {
	return new InjectBugLoopExt_c();
    }

    public Ext extBranchImpl() {
	return new InjectBugBranchExt_c();
    }

    public Ext extBinaryImpl() {
	return new InjectBugBinaryExt_c();
    }

    public Ext extSynchronizedImpl() {
	return new InjectBugSynchronizedExt_c();
    }

    public Ext extMethodDeclImpl() {
	return new InjectBugMethodDeclExt_c();
    }

    public Ext extAssignImpl() {
	return new InjectBugAssignExt_c();
    }

}
