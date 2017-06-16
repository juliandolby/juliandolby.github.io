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
import com.ibm.jscript.types.FBSValue;

public class ASTTernaryOp extends ASTNode{
    public static final int CONDITION=0;
    public static final int TRUESTATEMENT=1;
    public static final int FALSESTATEMENT=2;

    private ASTNode conditionNode;
    private ASTNode trueNode;
    private ASTNode falseNode;

    public ASTTernaryOp() {
    }

    public void add(ASTNode cond,ASTNode truestat, ASTNode falsestat){
        conditionNode=cond;
        trueNode=truestat;
        falseNode=falsestat;
    }

    public void addCondition(ASTNode cond){
        conditionNode=cond;
    }

    public void addTrue(ASTNode truestat){
        trueNode=truestat;
    }

    public void addFalse(ASTNode falsestat){
        falseNode=falsestat;
    }




    public int getSlotCount(){
        return 3;
    }

    public ASTNode readSlotAt(int index){
        switch(index){
            case CONDITION:return conditionNode;
            case TRUESTATEMENT:return trueNode;
            case FALSESTATEMENT:return falseNode;
        }
        return null;
    }

    public void interpret(ASTNode caller, IExecutionContext context,InterpretResult result) throws JavaScriptException {
        try {
            conditionNode.interpret(this,context,result);
            FBSValue v=result.getFBSValue();
            boolean b=v.booleanValue();
            if (b){
                trueNode.interpret(this,context,result);
            }else{
                falseNode.interpret(this,context,result);
            }
        } catch( InterpretException ie ) {
            throw ie.fillException(context,this);
        }

    }
    public FBSType getResultType(FBSType.ISymbolCallback symbolCallback) {
        FBSType t1 = trueNode.getResultType(symbolCallback);
        FBSType t2 = falseNode.getResultType(symbolCallback);
        if( t1.equals(t2) ) {
            return t1;
        }
        return FBSType.undefinedType;
    }

    public Object visit(ASTNodeVisitor v, Object param) {
	return v.visitTernaryOp(this, param);
    }
}
