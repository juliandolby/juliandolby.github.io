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
package com.ibm.jscript.std;

import java.util.ArrayList;

import com.ibm.jscript.InterpretException;
import com.ibm.jscript.JavaScriptException;
import com.ibm.jscript.ASTTree.ASTFunction;
import com.ibm.jscript.ASTTree.ASTNode;
import com.ibm.jscript.ASTTree.AbruptReturnException;
import com.ibm.jscript.ASTTree.InterpretResult;
import com.ibm.jscript.engine.FunctionContext;
import com.ibm.jscript.engine.IExecutionContext;
import com.ibm.jscript.types.FBSDefaultObject;
import com.ibm.jscript.types.FBSNumber;
import com.ibm.jscript.types.FBSObject;
import com.ibm.jscript.types.FBSUndefined;
import com.ibm.jscript.types.FBSValue;
import com.ibm.jscript.types.FBSValueVector;

public class FunctionObject extends FBSDefaultObject {

    public static final int MOD_RETLAST    = 0x0001;

    private int modifiers;
    //private FBSDefaultObject argObj;
    private ArrayList formalParams;
//    private String[] formalParamNames;// to preserve the order of parameters
    private ASTFunction functionNode;

    public FunctionObject(int modifiers, ArrayList formalParams, ASTFunction func) {
        this.modifiers = modifiers;
        this.functionNode=func;
        // create the argument object
//        argObj= new FBSDefaultObject();
//        argObj.createProperty("callee",FBSObject.P_NOENUM,this);
        if (formalParams!=null){
            this.formalParams = formalParams;
            createProperty("length",FBSObject.P_NODELETE|FBSObject.P_NOENUM|FBSObject.P_READONLY,FBSNumber.get(formalParams.size())); //$NON-NLS-1$
//            formalParamNames= new String[formalParams.size()];
//            for(int i=0;i<formalParams.size();i++){
//                String argName=((ASTFunction.Parameter)formalParams.get(i)).getName();
//                //argObj.createProperty(argName,FBSObject.P_NOENUM,FBSUndefined.undefinedValue);
//                formalParamNames[i]=argName;
//            }
        } else {
            createProperty("length",FBSObject.P_NODELETE|FBSObject.P_NOENUM|FBSObject.P_READONLY,FBSNumber.Zero); //$NON-NLS-1$
        }
    }

    private FunctionObject(FunctionObject f)  {
    	this.modifiers = f.modifiers;
    	this.functionNode = f.functionNode;
        this.formalParams=f.formalParams;
        FBSValue length = formalParams!=null ? FBSNumber.get(formalParams.size()) : FBSNumber.Zero;
        createProperty("length",FBSObject.P_NODELETE|FBSObject.P_NOENUM|FBSObject.P_READONLY,length); //$NON-NLS-1$
    }
    
    public int getType(){
        return FBSValue.FUNCTION_TYPE;
    }

    public String getTypeAsString(){
        return "function"; //$NON-NLS-1$
    }

    public FunctionObject() {
    }

    public FBSObject construct(IExecutionContext context, FBSValueVector args) throws InterpretException {
    	// PHIL: in order to support the "new func()" syntax, we need to work on a copy of the global object
    	// stored in the context. Else, two calls to the same "new func()" will return the same global object, 
    	// instead of allocating 2 new.
    	FunctionObject func = new FunctionObject(this);
    	FBSValue v = func.executeFunction(context,args,func);
    	if( v instanceof FBSObject ) {
    		return (FBSObject)v;
    	}
    	return func;
    }

    public ASTFunction getFunctionNode() {
    	return functionNode;
    }
    
    public boolean supportConstruct(){
        return true;
    }

    public boolean hasInstance(FBSValue v){
        return false;
    }

    public FBSObject call(FBSValueVector args){
        return null;
    }

    public FBSValue call(IExecutionContext context,FBSValueVector args,FBSObject _this)throws InterpretException{
    	return executeFunction(context,args,_this!=null ? _this : this);
    }
    
    private FBSValue executeFunction(IExecutionContext context,FBSValueVector args,FBSObject _this)throws InterpretException{
        ObjectObject argObj= new ObjectObject();
        argObj.createProperty("callee",FBSObject.P_NOENUM,this); //$NON-NLS-1$

        // Create the activation object
        // This activation object will hold the local variables as weel as the arguments
        FBSActivationObject actObj= new FBSActivationObject();
        actObj.createProperty("arguments",FBSObject.P_NODELETE,argObj); //$NON-NLS-1$
        argObj.createProperty("length",FBSObject.P_NOENUM,FBSNumber.get(args.size())); //$NON-NLS-1$

        // create the function context
        // order of statements is important
        IExecutionContext funcContext= new FunctionContext(context,this,actObj,_this);
        int sz= args.size();
        if(formalParams!=null) {
	        for(int i=0;i<formalParams.size();i++){
	            ASTFunction.Parameter p = (ASTFunction.Parameter)formalParams.get(i);
	            if (i<sz){
	                actObj.put(p.getName(),(FBSValue)args.get(i));
	                argObj.put(String.valueOf(i),(FBSValue)args.get(i));
	            }else{
	                actObj.put(p.getName(),FBSUndefined.undefinedValue);
	                argObj.put(String.valueOf(i),FBSUndefined.undefinedValue);
	            }
	        }
        }

        // Execute the function
        // If a return statement is executed, then a AbruptReturnException is emitted
        try {
            InterpretResult result= new InterpretResult();
            int count = functionNode!=null ? functionNode.getSlotCount() : 0;
            for(int i=0;i<count;i++){
                ASTNode node=functionNode.readSlotAt(i);
                if (node!=null){
                    node.interpret(null, funcContext, result);
                }
            }
            // Return the last evaluated node
            if( (modifiers & MOD_RETLAST)!=0 ) {
                FBSValue v=result.getFBSValue();
                if(v!=null) {
                    return v;
                }
            }
        } catch( AbruptReturnException ex ) {
            return ex.getValue();
        } catch( InterpretException ie ) {
            throw ie;
        } catch( JavaScriptException ie ) {
            // In case of break/continue
            return FBSUndefined.undefinedValue;
        }

        return FBSUndefined.undefinedValue;
    }

    public boolean supportCall(){
        return true;
    }

    public Object toJavaObject(Class _class)throws InterpretException{
        return null;
    }

    // just an identifier of an activation object
    public static class FBSActivationObject extends FBSDefaultObject{
    }
}
