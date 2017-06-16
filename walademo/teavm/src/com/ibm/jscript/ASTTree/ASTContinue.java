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

import com.ibm.jscript.JavaScriptException;
import com.ibm.jscript.engine.IExecutionContext;

public class ASTContinue extends ASTNode{
    String targetLabel;
    public ASTContinue() {
    }

    public ASTContinue(String label) {
        setTargetLabel(label);
    }

    public void setTargetLabel(String label){
        targetLabel=label;
    }

    public String getTargetLabel(){
        return targetLabel;
    }

    public int getSlotCount(){
        return 0;
    }

    public ASTNode readSlotAt(int index){
        return null;
    }

    public void interpret(ASTNode caller, IExecutionContext context,InterpretResult result) throws JavaScriptException {
    	throw new AbruptContinueException(targetLabel);
    }

    protected void extraInfo(PrintStream ps,String indent){
        if (targetLabel!=null){
            ps.print("label="); //$NON-NLS-1$
            ps.print(targetLabel);
        }
    }

    public Object visit(ASTNodeVisitor v, Object param) {
	return v.visitContinue(this, param);
    }

}
