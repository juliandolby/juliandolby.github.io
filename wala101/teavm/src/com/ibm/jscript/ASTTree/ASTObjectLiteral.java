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

import java.util.ArrayList;

import com.ibm.jscript.InterpretException;
import com.ibm.jscript.JavaScriptException;
import com.ibm.jscript.engine.IExecutionContext;
import com.ibm.jscript.std.ObjectObject;
import com.ibm.jscript.types.FBSType;

public class ASTObjectLiteral extends ASTNode{
    private ArrayList fieldInitializers = new ArrayList();

    public ASTObjectLiteral() {
    }

    public final static class Initializer {
	final public ASTLiteral field;
	final public ASTNode init;

	Initializer(ASTLiteral field, ASTNode init) {
	    this.field = field;
	    this.init = init;
	}
    };

    public void add(ASTLiteral field, ASTNode initializer){
        fieldInitializers.add(new Initializer(field, initializer));
    }

    public FBSType getResultType(FBSType.ISymbolCallback symbolCallback) {
        return FBSType.jsObjectType;
    }

    public int getSlotCount(){
        return fieldInitializers.size();
    }

    public Initializer readInitAt(int index) {
        return (Initializer)fieldInitializers.get(index);
    }

    public ASTNode readSlotAt(int index){
        return ((Initializer)fieldInitializers.get(index)).init;
    }

    public void interpret(ASTNode caller,IExecutionContext context,InterpretResult result) throws JavaScriptException {
        try {
            ObjectObject obj = new ObjectObject();
            for(int i=0;i<fieldInitializers.size();i++){
                Initializer n=(Initializer)fieldInitializers.get(i);
		ASTLiteral f = n.field;
		ASTNode v = n.init;
		v.interpret(this,context,result);
		obj.put( f.toString(), result.getFBSValue() );
	    }
            result.setNormal( obj );
        } catch( InterpretException ie ) {
            throw ie.fillException(context,this);
        }
    }
    
    public Object visit(ASTNodeVisitor v, Object param) {
	return v.visitObjectLiteral(this, param);
    }
}
