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

public class ASTExpression extends ASTNode{
    private NodeVector nodes= new NodeVector();

    public ASTExpression() {
    }

    public void add(ASTNode node){
        nodes.add(node);
    }

    public int getSlotCount(){
        return nodes.getCount();
    }

    public ASTNode readSlotAt(int index){
        return nodes.get(index);
    }

    public String getTraceString(){
        StringBuffer bufstr= new StringBuffer();
        for(int i=0;i<getSlotCount();i++){
            ASTNode n=readSlotAt(i);
            if (n!=null){
                bufstr.append(n.getTraceString());
            }
        }
        return bufstr.toString();
    }


    public void interpret(ASTNode caller, IExecutionContext context,InterpretResult result) throws JavaScriptException {
        try {
            int count = getSlotCount();
            for(int i=0;i<count;i++){
                ASTNode n=readSlotAt(i);
                if (n!=null){
                    n.interpret(this,context,result);
                }
            }
        } catch( InterpretException ie ) {
            throw ie.fillException(context,this);
        }
    }

    public Object visit(ASTNodeVisitor v, Object param) {
	return v.visitExpression(this, param);
    }
}
