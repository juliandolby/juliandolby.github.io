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
import com.ibm.jscript.types.FBSType;
import com.ibm.jscript.types.FBSUndefined;
import com.ibm.jscript.util.StringUtil;

public class ASTBlock extends ASTNode implements ILabelledStatement {
    
    private NodeVector nodes = new NodeVector();
    private String labelName;

    public ASTBlock() {
    }

    public void setLabelName(String label) {
        this.labelName = label;
    }

    public void add(ASTNode node) {
        nodes.add(node);
    }

    public int getSlotCount() {
        return nodes.getCount();
    }

    public ASTNode readSlotAt(int index) {
        return nodes.get(index);
    }

    public FBSType getResultType(FBSType.ISymbolCallback symbolCallback) {
        if (nodes.size() > 0) {
            FBSType t = null;
            for (int i = 0; i < nodes.size(); i++) {
                if (t == null) {
                    t = ((ASTNode) nodes.get(i)).getResultType(symbolCallback);
                } else {
                    FBSType t2 = ((ASTNode) nodes.get(i))
                            .getResultType(symbolCallback);
                    if (!t.equals(t2)) {
                        return FBSType.undefinedType;
                    }
                }
            }
            return t;
        }
        return FBSType.undefinedType;
    }

    public void interpret(ASTNode caller, IExecutionContext context,
            InterpretResult result) throws JavaScriptException {
        try {
            if (nodes.getCount() == 0) {
                result.setNormal(FBSUndefined.undefinedValue);
            } else {
                for (int i = 0; i < nodes.getCount(); i++) {
                    ASTNode node = nodes.get(i);
                    node.interpret(this, context, result);
                }
            }
        } catch( AbruptBreakException ex ) {
            // Break this block...
            if(!StringUtil.isEmpty(ex.getLabel())) {
                if(!StringUtil.equals(ex.getLabel(),labelName)) {
                    throw ex;
                }
            } else {
                throw ex;
            }
        } catch (InterpretException ie) {
            throw ie.fillException(context, this);
        }
    }

    public Object visit(ASTNodeVisitor v, Object param) {
        return v.visitBlock(this, param);
    }
}
