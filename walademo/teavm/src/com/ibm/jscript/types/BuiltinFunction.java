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
package com.ibm.jscript.types;

import com.ibm.jscript.InterpretException;
import com.ibm.jscript.JavaScriptException;
import com.ibm.jscript.engine.IExecutionContext;

public abstract class BuiltinFunction extends FBSDefaultObject{

    private static final String PROP_LENGTH = "length"; //$NON-NLS-1$
	
	//TODO: remove this constructor and add strng typing
    public BuiltinFunction() {
    }

    public int getType(){
        return FBSValue.FUNCTION_TYPE;
    }

    public FBSType getFBSType(){
        return new FBSType.JSBuiltinFunction(this);
    }

    public String getTypeAsString(){
        return "function";
    }

    public FBSObject construct(FBSValueVector args)throws InterpretException{
        throw new InterpretException("Unsupported method for BuiltinFunction");
    }
    public boolean supportConstruct(){
        return false;
    }
    public boolean hasInstance(FBSValue v){
        return false;
    }

    public FBSValue call(IExecutionContext context,FBSValueVector args,FBSObject _this) throws JavaScriptException {
        return call(args,_this);
    }
    public FBSValue call(FBSValueVector args,FBSObject _this)throws JavaScriptException {
        throw new InterpretException( "Function not implemented!" );
    }

    public boolean supportCall(){
        return true;
    }

    public Object toJavaObject(Class _class)throws InterpretException{
        throw new InterpretException("method toJavaObject() is not yet implemented for this object");
    }

    public boolean createProperty(String name , int attribute, FBSValue value ){
        return false;
    }


    public FBSValue get(int index){
        throw new RuntimeException("get(int index) not supported in BuiltinFunction");
    }
    public void put(int index , FBSValue value){
        throw new RuntimeException("put(int index,FBSValue value) not supported in BuiltinFunction");
    }

    public FBSValue get(String name){
    	if(name.equals(PROP_LENGTH)) {
    		// Compute the minimun number of parameters
    		String[] params = getCallParameters();
    		if(params!=null && params.length>0) {
    			int max = Integer.MIN_VALUE;
    			for( int i=0; i<params.length; i++ ) {
    				int np = 0;
    				String s = params[i];
    				int len = s.length();
    				for( int j=0; j<len; j++ ) {
    					char c = s.charAt(j);
    					if(c==')') {
    						break;
    					}
    					if( c==':') {
    						np ++;
    					}
    				}
    				max = Math.max(np,max);
    			}
    			return FBSNumber.get(max);
    		}
    		return FBSNumber.Zero;
    	}
        return FBSUndefined.undefinedValue;
    }

    public void put(String name , FBSValue value) throws InterpretException {
        if( name.equals(PROP_LENGTH) ){
            return;
        }
        super.put(name,value);
    }
    
    public boolean delete(String name){
        if( name.equals(PROP_LENGTH) ){
            return false;
        }
        return super.delete(name);
    }

//    public boolean canPut(String name){
//        return  false;
//    }

    public boolean hasProperty(String name){
    	if(name.equals("length")) {
    		return true;
    	}
        return super.hasProperty(name);
    }

    public FBSValue getDefaultValue(String hint){
        return FBSUndefined.undefinedValue;
    }

    public FBSValue getDefaultValue(int hint){
        return FBSUndefined.undefinedValue;
    }

//    public Iterator getPropertyKeys() {
//        return null;
//    }
}
