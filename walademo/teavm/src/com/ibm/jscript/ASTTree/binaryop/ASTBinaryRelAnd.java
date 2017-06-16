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

import java.util.ArrayList;

import com.ibm.jscript.InterpretException;
import com.ibm.jscript.JavaScriptException;
import com.ibm.jscript.ASTTree.ASTBinaryOp;
import com.ibm.jscript.ASTTree.ASTConstants;
import com.ibm.jscript.ASTTree.ASTNode;
import com.ibm.jscript.ASTTree.ASTNodeVisitor;
import com.ibm.jscript.ASTTree.InterpretResult;
import com.ibm.jscript.engine.IExecutionContext;
import com.ibm.jscript.types.FBSBoolean;

public class ASTBinaryRelAnd extends ASTBinaryOp {

    private ASTNode[] optimizedNodes;

    public ASTBinaryRelAnd(ASTNode leftNode, ASTNode rightNode) {
        super(leftNode,rightNode);
    }

    public int getOperator() {
        return ASTConstants.REL_AND;
    }

    public void interpret(ASTNode caller, IExecutionContext context,InterpretResult result) throws JavaScriptException {
        //context.nodeEntered(this);
        try {
            // Optimized evaluation
            if(optimizedNodes!=null ) {
                int count = optimizedNodes.length;
                for( int i=0; i<count; i++ ) {
                    optimizedNodes[i].interpret(this, context, result);
                    boolean r = result.getFBSValue().booleanValue();
                    if( !r ) {
                        result.setNormal(FBSBoolean.FALSE);
                        return;
                    }
                }
                result.setNormal(FBSBoolean.TRUE);
                return;
            }

            leftNode.interpret(this,context,result);
            boolean b1=result.getFBSValue().booleanValue();
            // if operator is && and left node is false
            // don't evaluate the right node, return false
            if (b1==false){
                result.setNormal(FBSBoolean.FALSE);
                return;
            }
            rightNode.interpret(this,context,result);
            boolean b2=result.getFBSValue().booleanValue();

            result.setNormal(FBSBoolean.get(b2));
        } catch( InterpretException ie ) {
            throw ie.fillException(context,this);
        //} finally {
        //    context.nodeLeaved(this);
        }
    }

    public void optimize() {
        ArrayList list = new ArrayList(16);
        listAdd(this,list);
        if( list.size()>2 ) {
            this.optimizedNodes = (ASTNode[])list.toArray(new ASTNode[list.size()]);
            //com.ibm.workplace.designer.util.TDiag.getOutputStream().println( "Add: optimized "+Integer.toString(optimizedNodes.length)+"nodes" );
        }
    }
    private void listAdd(ASTBinaryRelAnd node, ArrayList list) {
        ASTNode n1 = node.getLeftNode();
        if( n1 instanceof ASTBinaryRelAnd ) {
            listAdd((ASTBinaryRelAnd)n1,list);
        } else {
            list.add(n1);
        }
        ASTNode n2 = node.getRightNode();
        list.add(n2);
    }

    public Object visit(ASTNodeVisitor v, Object param) {
	return v.visitBinaryRelAnd(this, param);
    }
}
