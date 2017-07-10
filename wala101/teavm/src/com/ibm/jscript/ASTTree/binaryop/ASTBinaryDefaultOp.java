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
package com.ibm.jscript.ASTTree.binaryop;

import com.ibm.jscript.InterpretException;
import com.ibm.jscript.JavaScriptException;
import com.ibm.jscript.ASTTree.ASTBinaryOp;
import com.ibm.jscript.ASTTree.ASTConstants;
import com.ibm.jscript.ASTTree.ASTNode;
import com.ibm.jscript.ASTTree.ASTNodeVisitor;
import com.ibm.jscript.ASTTree.InterpretResult;
import com.ibm.jscript.engine.IExecutionContext;
import com.ibm.jscript.engine.Utility;
import com.ibm.jscript.parser.Token;
import com.ibm.jscript.types.FBSValue;

public class ASTBinaryDefaultOp extends ASTBinaryOp {

    private int op;

    public ASTBinaryDefaultOp(Token t, ASTNode leftNode, ASTNode rightNode) {
        super(leftNode,rightNode);
        this.op=ASTConstants.getConstant(t.image);
    }

    public ASTBinaryDefaultOp(String op, ASTNode leftNode, ASTNode rightNode, Token t) {
    }

    public ASTBinaryDefaultOp(int type) {
        this.op=type;
    }

    public int getOperator(){
        return op;
    }

    public void interpret(ASTNode caller, IExecutionContext context,InterpretResult result) throws JavaScriptException {
        try {
            leftNode.interpret(this,context,result);
            FBSValue v1=result.getFBSValue();

            rightNode.interpret(this,context,result);
            FBSValue v2=result.getFBSValue();

            FBSValue r=Utility.operation(v1,v2,op);
            result.setNormal(r);
        } catch( InterpretException ie ) {
            throw ie.fillException(context,this);
        }
    }

    public Object visit(ASTNodeVisitor v, Object param) {
	return v.visitBinaryDefaultOp(this, param);
    }
}
