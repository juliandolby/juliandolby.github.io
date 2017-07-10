/*
 * IBM Confidential OCO Source Materials
 * 
 * $$FILENAME$$
 * 
 * (C) Copyright IBM Corp. 2004, 2005 The source code for this program is not
 * published or otherwise divested of its trade secrets, irrespective of what
 * has been deposited with the U. S. Copyright Office. All rights reserved.
 */
package com.ibm.jscript.ASTTree;

import com.ibm.jscript.InterpretException;
import com.ibm.jscript.JavaScriptException;
import com.ibm.jscript.engine.IExecutionContext;
import com.ibm.jscript.types.FBSUndefined;
import com.ibm.jscript.types.FBSValue;
import com.ibm.jscript.util.StringUtil;

public class ASTDoWhile extends ASTNode implements ILabelledStatement {
    public static final int STATEMENT = 0;

    public static final int EXPRESSION = 1;

    private ASTNode statementNode;

    private ASTNode expressionNode;

    private String labelName;

    public ASTDoWhile() {
    }

    public void setLabelName(String label) {
        this.labelName = label;
    }

    public void add(ASTNode statement, ASTNode expression) {
        this.statementNode = statement;
        this.expressionNode = expression;
    }

    public void addStatement(ASTNode statement) {
        this.statementNode = statement;
    }

    public void addExpression(ASTNode expression) {
        this.expressionNode = expression;
    }

    public int getSlotCount() {
        return 2;
    }

    public ASTNode readSlotAt(int index) {
        if (index < 0 || index > 1)
            return null;
        return (index == 0) ? statementNode : expressionNode;
    }

    public void interpret(ASTNode caller, IExecutionContext context,
            InterpretResult result) throws JavaScriptException {
        try {
            FBSValue v = FBSUndefined.undefinedValue;
            FBSValue condition = null;
            do {
                // Evaluate the loop content
                try {
                    statementNode.interpret(this, context, result);
                    if (result.getFBSValue() != null) {
                        v = result.getFBSValue();
                    }
                } catch (AbruptBreakException ex) {
                    if (!StringUtil.isEmpty(ex.getLabel())) {
                        if (!StringUtil.equals(ex.getLabel(), labelName)) {
                            throw ex;
                        }
                    }
                    // Break the main loop
                    break;
                } catch (AbruptContinueException ex) {
                    if (!StringUtil.isEmpty(ex.getLabel())) {
                        if (!StringUtil.equals(ex.getLabel(), labelName)) {
                            throw ex;
                        }
                    }
                    // Just continue and do not execute the condition
                    //condition=FBSBoolean.TRUE;
                    //continue;
                }
                // Evaluate the condition
                expressionNode.interpret(this, context, result);
                condition = result.getFBSValue();
            } while (condition.booleanValue());
            result.setNormal(v);
        } catch (InterpretException ie) {
            throw ie.fillException(context, this);
        }
    }

    public Object visit(ASTNodeVisitor v, Object param) {
        return v.visitDoWhile(this, param);
    }

	public String getLabelName() {
		return labelName;
	}
}
