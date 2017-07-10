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
import com.ibm.jscript.types.FBSNull;
import com.ibm.jscript.types.FBSType;
import com.ibm.jscript.types.FBSValue;

public class ASTThis extends ASTNode{

    public ASTThis() {
    }

    public int getSlotCount(){
        return 0;
    }

    /**
     * reads the slot at position 'index'.
     * Value may be null.
     * @return returns the value of a slot.
     */
    public ASTNode readSlotAt(int index){
        return null;
    }


    public void interpret(ASTNode caller, IExecutionContext context,InterpretResult result) throws JavaScriptException {
        //try {
            FBSValue _this = context.getThis();
            if(_this==null) {
                _this = FBSNull.nullValue;
            }
            result.setNormal(_this);
        //} catch( InterpretException ie ) {
        //    throw ie.fillException(context,this);
        //}
    }

    public FBSType getResultType(FBSType.ISymbolCallback symbolCallback) {
        return FBSType.jsObjectType;
    }

    public Object visit(ASTNodeVisitor v, Object param) {
	return v.visitThis(this, param);
    }
}
