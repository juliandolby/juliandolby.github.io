/*
 * IBM Confidential 
 * OCO Source Materials 
 * 
 * $$FILENAME$$
 * 
 * (C) Copyright IBM Corp. 2004, 2005
 * The source code for this program is not published or otherwise 
 * divested of its trade secrets, irrespective of what has been 
 * deposited with the U. S. Copyright Office. 
 * All rights reserved.
 */
package com.ibm.jscript.ASTTree;

import java.io.PrintStream;

import com.ibm.jscript.InterpretException;
import com.ibm.jscript.JavaScriptException;
import com.ibm.jscript.engine.IExecutionContext;
import com.ibm.jscript.std.RegExpObject;
import com.ibm.jscript.types.FBSType;
import com.ibm.jscript.types.FBSUndefined;
import com.ibm.jscript.types.FBSValue;

public class ASTRegExp extends ASTNode {

	private String regExp;
	private String flags;

	public ASTRegExp(String regExp, String flags) {
		this.regExp = regExp;
		this.flags = flags;
	}

	public String getTraceString() {
		return getRegExp();
	}

	public String getRegExp() {
		return regExp;
	}

    public String getFlags() {
        return flags;
    }

	public int getSlotCount() {
		return 0;
	}

	public ASTNode readSlotAt(int index) {
		return null;
	}

	public String toString() {
		return getRegExp();
	}

	public void interpret(ASTNode caller, IExecutionContext context, InterpretResult result) throws JavaScriptException {
        RegExpObject re = new RegExpObject(getRegExp(),getFlags());
		result.setNormal(re);
	}

	public FBSType getResultType(FBSType.ISymbolCallback symbolCallback) {
		return FBSType.regExpType;
	}

	public boolean isConstant() {
		return false;
	}

	public FBSValue evaluateConstant() throws InterpretException {
		return FBSUndefined.undefinedValue;
	}

	protected void extraInfo(PrintStream ps, String indent) {
		ps.print("regExp="+getRegExp()+", flags="+getFlags()); //$NON-NLS-1$ //$NON-NLS-2$
	}


    public Object visit(ASTNodeVisitor v, Object param) {
	return v.visitRegExp(this, param);
    }
}