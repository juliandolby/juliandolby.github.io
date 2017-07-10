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
import com.ibm.jscript.types.FBSGlobalObject;
import com.ibm.jscript.types.FBSObject;

public class ASTImport extends ASTNode {

    private String libName;

    public ASTImport() {
    }

    public ASTImport(String libName) {
        this.libName = libName;
    }

    public int getSlotCount(){
        return 0;
    }

    public ASTNode readSlotAt(int index){
        return null;
    }


    public void interpret(ASTNode caller, IExecutionContext context,InterpretResult result) throws JavaScriptException {
        try {
            // Load the library to the context
            FBSObject g = context.getGlobalObject();
            if( g instanceof FBSGlobalObject ) {
                ((FBSGlobalObject)g).importLibrary(libName);
            }
        } catch( InterpretException ie ) {
            throw ie.fillException(context,this);
        }
    }

    public Object visit(ASTNodeVisitor v, Object param) {
	return v.visitImport(this, param);
    }
}
