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

import com.ibm.jscript.InterpretException;
import com.ibm.jscript.JavaScriptException;
import com.ibm.jscript.engine.IExecutionContext;
import com.ibm.jscript.types.FBSType;
import com.ibm.jscript.types.FBSUndefined;

public class ASTReturn extends ASTNode{
    private ASTNode expressionNode;
    public ASTReturn() {
    }

    public void add(ASTNode expr){
        expressionNode=expr;
    }

    public int getSlotCount(){
        return expressionNode==null?0:1;
    }
    public ASTNode readSlotAt(int i){
        return expressionNode;
    }

    public void interpret(ASTNode caller, IExecutionContext context,InterpretResult result) throws JavaScriptException {
        try {
            if (expressionNode!=null){
                expressionNode.interpret(this,context,result);
            } else {
                result.setNormal(FBSUndefined.undefinedValue);
            }
            throw new AbruptReturnException(result.getFBSValue());
        } catch( InterpretException ie ) {
            throw ie.fillException(context,this);
        }
    }

    public FBSType getResultType(FBSType.ISymbolCallback symbolCallback) {
        return expressionNode.getResultType(symbolCallback);
    }

    public Object visit(ASTNodeVisitor v, Object param) {
	return v.visitReturn(this, param);
    }
}
