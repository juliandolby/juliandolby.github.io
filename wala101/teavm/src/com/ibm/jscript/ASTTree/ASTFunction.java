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
import java.util.ArrayList;

import com.ibm.jscript.InterpretException;
import com.ibm.jscript.JavaScriptException;
import com.ibm.jscript.engine.IExecutionContext;
import com.ibm.jscript.std.FunctionObject;
import com.ibm.jscript.types.FBSGlobalObject;

public class ASTFunction extends ASTNode{

    public static class Parameter {
        private String     name;
        private String     type;
        private Parameter( String name, String type ) {
            this.name = name;
            this.type = type;
        }
        public String getName() {
            return name;
        }
        public String getType() {
            return type;
        }
    }

    private int modifiers;
    private ArrayList params= new ArrayList();
    private NodeVector nodes= new NodeVector();
    private String funcName;
    private String retType;
    private boolean isExpression = false;

    public ASTFunction() {
    }

    public void setExpression(boolean isExpression) {
	this.isExpression = isExpression;
    }

    public boolean getExpression() {
	return isExpression;
    }

    public int getModifiers() {
        return modifiers;
    }

    public void setModifiers( int modifiers ) {
        this.modifiers = modifiers;
    }

    public void setName(String name){
        funcName=name;
    }

    public String getName(){
        return funcName;
    }

    public void setReturnType(String type){
        retType = type;
    }

    public String getReturnType(){
        return retType;
    }

    public void addParameter(String name, String type){
        params.add(new Parameter(name,type));
    }

    public int getParameterCount(){
        return params.size();
    }

    public Parameter getParameterAt(int index){
        if (index<0 || index>=params.size()) return null;
        return (Parameter)params.get(index);
    }

    public void add(ASTNode node){
        nodes.add(node);
    }

    public int getSlotCount(){
        return nodes.getCount();
    }

    public ASTNode readSlotAt(int index){
        return nodes.get(index);
    }

    public void interpret(ASTNode caller, IExecutionContext context,InterpretResult result) throws JavaScriptException {
        // create a function object which mission is to execute the function body when it is called.
        FunctionObject funcObj= new FunctionObject(modifiers,params,this);
        if(getName()!=null) {
        	context.getVariableObject().put(getName(),funcObj);
        }
        result.setNormal(funcObj);
    }

    public void register(FBSGlobalObject globalObject) throws InterpretException {
        // create a function object which mission is to execute the function body when it is called.
        FunctionObject funcObj= new FunctionObject(modifiers,params,this);
        globalObject.put(getName(),funcObj);
    }

    protected void extraInfo(PrintStream ps,String indent){
        ps.print("name="); //$NON-NLS-1$
        ps.print(funcName);
    }

    public Object visit(ASTNodeVisitor v, Object param) {
	return v.visitFunction(this, param);
    }
}
