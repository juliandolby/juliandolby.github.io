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

public class ASTFor extends ASTNode implements ILabelledStatement {
    public static final int INIT=0;
    public static final int CONDITION=1;
    public static final int UPDATE=2;
    public static final int STATEMENT=3;

    ASTNode initNode;
    ASTNode conditionNode;
    ASTNode updateNode;
    ASTNode statementNode;
    String labelName;

    public ASTFor() {
    }
    
	public void setLabelName(String label) {
		this.labelName = label;
	}

    public void add(ASTNode init,ASTNode condition,ASTNode update,ASTNode statement){
        initNode=init;
        conditionNode=condition;
        updateNode=update;
        statementNode=statement;
    }

    public void addInit(ASTNode init){
        initNode=init;
    }

    public void addCondition(ASTNode condition){
        conditionNode=condition;
    }

    public void addUpdate(ASTNode update){
        updateNode=update;
    }

    public void addStatement(ASTNode statement){
        statementNode=statement;
    }

    public int getSlotCount(){
        return 4;
    }

    public ASTNode readSlotAt(int index){
        switch(index){
            case INIT: return initNode;
            case CONDITION: return conditionNode;
            case UPDATE: return updateNode;
            case STATEMENT: return statementNode;
        }
        return null;
    }

    public void interpret(ASTNode caller, IExecutionContext context,InterpretResult result) throws JavaScriptException {
        try {
            // Execute the initialization
            if (initNode!=null){
                initNode.interpret(this,context,result);
            }
            FBSValue v=FBSUndefined.undefinedValue;
            for(;;){
                // Evaluate the condition
                if (conditionNode!=null ){
                    conditionNode.interpret(this,context,result);
                    if (!result.getFBSValue().booleanValue()) {
                        break;
                    }
                }

                // Evaluate the statement within the loop
                try {
                    statementNode.interpret(this, context, result);
                    // Store the resulting value
                    FBSValue res = result.getFBSValue(); 
                    if (res!=null) { 
                    	v=res;
                    }
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
                    // Still execute because the updateNode should be executed
                    //continue;
                }

                // And evaluate the third part (update)
                if (updateNode!=null){
                    updateNode.interpret(this,context,result);
                }
            }
            result.setNormal(v);
        } catch( InterpretException ie ) {
            throw ie.fillException(context,this);
        }
    }

    public Object visit(ASTNodeVisitor v, Object param) {
	return v.visitFor(this, param);
    }

	public String getLabelName() {
		return labelName;
	}
}
