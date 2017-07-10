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

import com.ibm.jscript.JavaScriptException;
import com.ibm.jscript.engine.IExecutionContext;

public class ASTLabel extends ASTNode{
    String label=null;

    public ASTLabel(String label) {
        setLabel(label);
    }

    public void setLabel(String label){
        this.label=label;
    }

    public String getLabel(){
        return label;
    }

    public int getSlotCount(){
        return 0;
    }

    public ASTNode readSlotAt(int i){
        return null;
    }

    public void interpret(ASTNode caller, IExecutionContext context,InterpretResult result) throws JavaScriptException {
//        context.nodeEntered(this);
//        try {
//        } catch( InterpretException ie ) {
//            throw ie.fillException(context,this);
//        } finally {
//            context.nodeLeaved(this);
//        }
    }

    public Object visit(ASTNodeVisitor v, Object param) {
	return v.visitLabel(this, param);
    }
}
