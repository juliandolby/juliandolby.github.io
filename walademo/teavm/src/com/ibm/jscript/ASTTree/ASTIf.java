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

public class ASTIf extends ASTNode{
    public static final int CONDITION=0;
    public static final int STATEMENT=1;
    public static final int ELSESTATEMENT=2;

    ASTNode conditionNode;
    ASTNode statementNode;
    ASTNode elseStatementNode;

    public ASTIf() {
    }

    public FBSType getResultType(FBSType.ISymbolCallback symbolCallback) {
        FBSType t1 = statementNode.getResultType(symbolCallback);
        if( elseStatementNode!=null ) {
            FBSType t2 = statementNode.getResultType(symbolCallback);
            if( t1.equals(t2) ) {
                return t1;
            }
            return FBSType.undefinedType;
        }
        return t1;
    }

    public void add(ASTNode condition,ASTNode statement, ASTNode elsestatement){
        conditionNode=condition;
        statementNode=statement;
        elseStatementNode=elsestatement;
    }

    public void addCondition(ASTNode condition){
        conditionNode=condition;
    }

    public void addStatement(ASTNode statement){
        statementNode=statement;
    }

    public void addElse(ASTNode elsestatement){
        elseStatementNode=elsestatement;
    }




    public int getSlotCount(){
        return 3;
    }

    public ASTNode readSlotAt(int index){
        switch(index){
            case CONDITION: return conditionNode;
            case STATEMENT: return statementNode;
            case ELSESTATEMENT: return elseStatementNode;
        }
        return null;
    }

    public void interpret(ASTNode caller, IExecutionContext context,InterpretResult result) throws JavaScriptException {
        try {
            conditionNode.interpret(this,context,result);
            boolean istrue=result.getFBSValue().booleanValue();

            if (istrue){
                statementNode.interpret(this,context,result);
            }else{
                if (elseStatementNode!=null){
                    elseStatementNode.interpret(this,context,result);
                } else {
                    result.setNormal(FBSUndefined.undefinedValue);
                }
            }
        } catch( InterpretException ie ) {
            throw ie.fillException(context,this);
        }
    }

    public Object visit(ASTNodeVisitor v, Object param) {
	return v.visitIf(this, param);
    }
}
