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
import com.ibm.jscript.engine.Utility;
import com.ibm.jscript.types.FBSUndefined;
import com.ibm.jscript.types.FBSValue;

public class ASTSwitch extends ASTNode{

    private ASTNode expressionNode;
    private NodeVector nodes = new NodeVector();
    private int defaultCaseIndex=-1;

    public ASTSwitch() {
    }

    public void addExpression(ASTNode expr){
        expressionNode=expr;
    }

    public void addCase(ASTNode node){
        nodes.add(node);
        ASTCase caseNode=(ASTCase)node;
        if (caseNode.isDefault()){
            defaultCaseIndex=nodes.getCount()-1;
        }
    }

    public int getSlotCount(){
        return nodes.getCount()+1;
    }

    public ASTNode readSlotAt(int index){
        if (index==0) return expressionNode;
        return nodes.get(index-1);
    }

    public void interpret(ASTNode caller, IExecutionContext context,InterpretResult result ) throws JavaScriptException {
        try {
            expressionNode.interpret(this,context,result);
            FBSValue value=result.getFBSValue();

            // Find the node that matches the value
            int matchIndex = -1;
            int notEqOp=ASTConstants.getConstant("!=="); //$NON-NLS-1$
            for(int i=0;i<nodes.size();i++) {
                if(i!=defaultCaseIndex) {
                    ASTCase caseNode= (ASTCase)nodes.get(i);
                    caseNode.getExpression().interpret(this, context, result);
                    if( !Utility.operation(value, result.getFBSValue(), notEqOp).booleanValue() ) {
                        matchIndex = i;
                        break;
                    }
                }
            }

            // Get the default index if none matches
            if( matchIndex<0 ) {
                matchIndex = defaultCaseIndex;
            }

            // And execute all the statements from that node
            if( matchIndex>=0 ) {
                for( int i=matchIndex; i<nodes.size(); i++ ) {
                    ASTCase caseNode= (ASTCase)nodes.get(i);
                    try {
                        caseNode.interpretStatementList(this, context, result);
                    } catch(AbruptBreakException ex) {
                        result.setNormal(result.getFBSValue());
                        return;
                    }
                }
            }

            // No result, undefined...
            result.setNormal(FBSUndefined.undefinedValue);
        } catch( InterpretException ie ) {
            throw ie.fillException(context,this);
        }
    }

    public Object visit(ASTNodeVisitor v, Object param) {
	return v.visitSwitch(this, param);
    }
}
