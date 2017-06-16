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
import com.ibm.jscript.types.FBSUtility;
import com.ibm.jscript.types.FBSValueVector;

public class ASTArgumentList extends ASTNode{
    private NodeVector nodes= new NodeVector();

    public ASTArgumentList() {
    }

    public int getSlotCount(){
        return nodes.getCount();
    }

    public ASTNode readSlotAt(int index){
        return nodes.get(index);
    }

    public void add(ASTNode node){
        nodes.add(node);
    }

    public void interpret(ASTNode caller, IExecutionContext context,InterpretResult result) throws JavaScriptException {
        //context.nodeEntered(this);
        try {
            FBSValueVector args=new FBSValueVector();
            for(int i=0;i<nodes.getCount();i++){
                ASTNode node=nodes.get(i);
                node.interpret(this,context,result);
                args.add(result.getFBSValue());
            }
            result.setNormal(FBSUtility.wrapAsObject(args));
        } catch( InterpretException ie ) {
            throw ie.fillException(context,this);
        //} finally {
        //    context.nodeLeaved(this);
        }
    }

    public Object visit(ASTNodeVisitor v, Object param) {
	return v.visitArgumentList(this, param);
    }
}
