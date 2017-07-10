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
import com.ibm.jscript.engine.Utility;
import com.ibm.jscript.types.FBSType;
import com.ibm.jscript.types.FBSValue;

public abstract class ASTBinaryOp extends ASTNode {

    public static final int LEFT=0;
    public static final int RIGHT=1;

    protected ASTNode leftNode;
    protected ASTNode rightNode;

    public ASTBinaryOp() {
    }

    public ASTBinaryOp(ASTNode leftNode, ASTNode rightNode) {
        this.leftNode = leftNode;
        this.rightNode = rightNode;
    }

    public final ASTNode getLeftNode() {
        return leftNode;
    }

    public final ASTNode getRightNode() {
        return rightNode;
    }

    public void add(ASTNode leftnode, ASTNode rightnode){
        this.leftNode=leftnode;
        this.rightNode=rightnode;
    }

    public void addLeft(ASTNode leftnode){
        this.leftNode=leftnode;
    }

    public void addRight(ASTNode rightnode){
        this.rightNode=rightnode;
    }


    public String getTraceString(){
        return leftNode.getTraceString()+ASTConstants.getToken(getOperator())+rightNode.getTraceString();
    }

    public int getSlotCount(){
        return 2;
    }

    public ASTNode readSlotAt(int index){
        if (index<0 || index>1) return null;
        return (index==0)?leftNode:rightNode;
    }

    protected void extraInfo(PrintStream ps,String indent){
        ps.print("\""); //$NON-NLS-1$
        ps.print(ASTConstants.getToken(this.getOperator()));
        ps.print("\""); //$NON-NLS-1$
    }

    public abstract int getOperator();
    public abstract void interpret(ASTNode caller, IExecutionContext context,InterpretResult result) throws JavaScriptException;

    public FBSType getResultType(FBSType.ISymbolCallback symbolCallback) {
        FBSType type1 = leftNode.getResultType(symbolCallback);
        FBSType type2 = rightNode.getResultType(symbolCallback);
        return Utility.operationType(type1,type2,getOperator());
    }

    public boolean isConstant() {
        return leftNode.isConstant() && rightNode.isConstant();
    }

    public FBSValue evaluateConstant() throws InterpretException {
        FBSValue v1 = leftNode.evaluateConstant();
        FBSValue v2 = rightNode.evaluateConstant();
        return Utility.operation(v1,v2,getOperator());
    }
}
