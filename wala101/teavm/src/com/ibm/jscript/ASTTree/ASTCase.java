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

import java.io.PrintStream;

import com.ibm.jscript.InterpretException;
import com.ibm.jscript.JavaScriptException;
import com.ibm.jscript.engine.IExecutionContext;

public class ASTCase extends ASTNode {
    boolean _default=false;
    NodeVector nodes = new NodeVector();
    ASTNode expression;

    public ASTCase() {
    }

    public ASTCase(boolean isdefault) {
        setDefault(isdefault);
    }

    public boolean isDefault(){
        return _default;
    }

    public void setDefault(boolean _default){
        this._default=_default;
    }

    public int getSlotCount(){
        return nodes.getCount();
    }

    public ASTNode readSlotAt(int index){
        return nodes.get(index);
    }

    public ASTNode getExpression(){
        return expression;
    }

    public void setExpression(ASTNode expression){
        this.expression=expression;
    }


    public void add(ASTNode node){
        nodes.add(node);
    }


    public void interpret(ASTNode caller, IExecutionContext context,InterpretResult result ) throws JavaScriptException {
        try {
            throw new InterpretException("Interpret must not be called on ASTCase");
        } catch( InterpretException ie ) {
            throw ie.fillException(context,this);
        }
    }

    public void interpretStatementList(ASTNode caller, IExecutionContext context,InterpretResult result ) throws JavaScriptException{
        try {
            int count = nodes.getCount();
            for(int i=0; i<count; i++) {
                nodes.get(i).interpret(this, context, result);
            }
        } catch( InterpretException ie ) {
            throw ie.fillException(context,this);
        }

    }

    protected void extraInfo(PrintStream ps,String indent){
        ps.print("expr=");
        ps.print( expression!=null? expression.toString(): "<empty>" );
    }

    public Object visit(ASTNodeVisitor v, Object param) {
	return v.visitCase(this, param);
    }
}
