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
import com.ibm.jscript.JScriptResources;
import com.ibm.jscript.JavaScriptException;
import com.ibm.jscript.engine.IExecutionContext;
import com.ibm.jscript.types.FBSValue;

public class ASTThrow extends ASTNode{
    ASTNode exprNode;
    public ASTThrow() {
    }

    public void add(ASTNode node){
        exprNode=node;
    }

    public int getSlotCount(){
        return 1;
    }

    public ASTNode readSlotAt(int index){
        return exprNode;
    }

    public void interpret(ASTNode caller, IExecutionContext context,InterpretResult result) throws JavaScriptException {
        try {
            if (exprNode!=null){
                exprNode.interpret(this,context,result);
            }
            FBSValue v=result.getFBSValue();
            throw new InterpretException(v,null,JScriptResources.getString("ASTThrow.JavaScript.Exception")); //$NON-NLS-1$
        } catch( InterpretException ie ) {
            throw ie.fillException(context,this);
        }
    }

    public Object visit(ASTNodeVisitor v, Object param) {
	return v.visitThrow(this, param);
    }
}
