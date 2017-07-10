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
import com.ibm.jscript.types.FBSUndefined;
import com.ibm.jscript.types.FBSValue;
import com.ibm.jscript.util.StringUtil;

public class ASTWhile extends ASTNode implements ILabelledStatement {
    public static final int CONDITION=0;
    public static final int STATEMENT=1;

    private ASTNode conditionNode;
    private ASTNode statementNode;
    private String labelName;

    public ASTWhile() {
    }
    
	public void setLabelName(String label) {
		this.labelName = label;
	}

    public void add(ASTNode cond, ASTNode statement){
        conditionNode=cond;
        statementNode=statement;
    }
    public void addCondition(ASTNode cond){
        conditionNode=cond;
    }

    public void addStatement(ASTNode statement){
        statementNode=statement;
    }


    public int getSlotCount(){
        return 2;
    }

    public ASTNode readSlotAt(int index){
        switch(index){
            case CONDITION: return conditionNode;
            case STATEMENT: return statementNode;
        }
        return null;
    }

    public void interpret(ASTNode caller, IExecutionContext context,InterpretResult result ) throws JavaScriptException {
        try {
            FBSValue v=FBSUndefined.undefinedValue;
            while(true) {
                // Evaluate the condition and stop the loop if false
                conditionNode.interpret(this,context,result);
                if(!result.getFBSValue().booleanValue()) {
                    break;
                }

                // Evaluate the statement
                try {
                    statementNode.interpret(this, context, result);
                    if (result.getFBSValue()!=null) v=result.getFBSValue();
                } catch( AbruptBreakException ex ) {
                	if(!StringUtil.isEmpty(ex.getLabel())) {
                		if(!StringUtil.equals(ex.getLabel(),labelName)) {
                			throw ex;
                		}
                	}
                    // Break the main loop
                    break;
                } catch( AbruptContinueException ex ) {
                	if(!StringUtil.isEmpty(ex.getLabel())) {
                		if(!StringUtil.equals(ex.getLabel(),labelName)) {
                			throw ex;
                		}
                	}
                    // Continue the main loop
                    continue;
                }
            }
            result.setNormal(v);
        } catch( InterpretException ie ) {
            throw ie.fillException(context,this);
        }
    }

    public Object visit(ASTNodeVisitor v, Object param) {
	return v.visitWhile(this, param);
    }

	public String getLabelName() {
		return labelName;
	}
}
