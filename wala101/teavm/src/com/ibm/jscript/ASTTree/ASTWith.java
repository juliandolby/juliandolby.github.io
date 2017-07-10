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
import com.ibm.jscript.types.FBSObject;

public class ASTWith extends ASTNode{
    public static final int EXPRESSION=0;
    public static final int STATEMENT=1;

    private ASTNode expressionNode;
    private ASTNode statementNode;

    public ASTWith() {
    }

    public void add(ASTNode expr, ASTNode statement){
        expressionNode=expr;
        statementNode=statement;
    }

    public void addExpression(ASTNode expr){
        expressionNode=expr;
    }

    public void addStatement(ASTNode statement){
        statementNode=statement;
    }



    public int getSlotCount(){
        return 2;
    }

    public ASTNode readSlotAt(int index){
        switch(index){
            case EXPRESSION: return expressionNode;
            case STATEMENT: return statementNode;
        }
        return null;
    }

    public void interpret(ASTNode caller, IExecutionContext context,InterpretResult result ) throws JavaScriptException {
        try {
            expressionNode.interpret(this,context,result);
            FBSObject object=result.getFBSValue().toFBSObject();
            context.pushToScope(object);

            // context.addScope(object);
            statementNode.interpret(this,context,result);
            context.popFromScope();
        } catch( InterpretException ie ) {
            throw ie.fillException(context,this);
        }
    }

    public Object visit(ASTNodeVisitor v, Object param) {
	return v.visitWith(this, param);
    }
}
