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

import com.ibm.jscript.InterpretException;
import com.ibm.jscript.JavaScriptException;
import com.ibm.jscript.engine.IExecutionContext;
import com.ibm.jscript.types.FBSBoolean;
import com.ibm.jscript.types.FBSObject;
import com.ibm.jscript.types.FBSValue;
import com.ibm.jscript.types.FBSValueVector;

/**
 * This class allows to manipulate standard JavaScript Boolean objects.
 * <br>This class has helpful method to convert a boolean value into a string (true or false)
 * or an integer (0 or 1).
 */
public class BooleanObject extends AbstractPrimitiveObject {
	
    private FBSBoolean primitive;

    public BooleanObject(FBSBoolean value) {
        super(BooleanPrototype.getBooleanPrototype());
        this.primitive = value;
    }
    

    ////// Constructor
    //TODO: metadata...
    public boolean supportConstruct(){
        return true;
    }

    public FBSObject construct(FBSValueVector args)throws InterpretException{
        if(args.getCount()>=1) {
            boolean b = args.get(0).booleanValue();
            return new BooleanObject(FBSBoolean.get(b));
        }
        return new BooleanObject(FBSBoolean.FALSE);
        //throw new InterpretException(JScriptResources.getString("BooleanObject.InvalidBoolArgs.Exception")); //$NON-NLS-1$
    }

    public FBSValue call(IExecutionContext context,FBSValueVector args,FBSObject _this) throws JavaScriptException {
        if(args.getCount()==0) {
            return FBSBoolean.FALSE;
        }
        return args.get(0).toFBSBoolean();
    }
    
    public boolean supportCall() {
        return true;
    }

    
    public boolean equals( Object o ) {
    	if( o instanceof BooleanObject ) {
    		BooleanObject w = (BooleanObject)o;
			return primitive.booleanValue()==w.primitive.booleanValue();
    	}
    	if( o instanceof FBSBoolean ) {
    		FBSBoolean w = (FBSBoolean)o;
			return primitive.booleanValue()==w.booleanValue();
    	}
    	return false;
    }

    public String getTypeAsString(){
        return "Boolean"; //$NON-NLS-1$
    }

    public String getName(){
        return "Boolean"; //$NON-NLS-1$
    }

    public FBSValue getPrimitiveValue() {
    	return primitive;
    }
    
    public FBSBoolean getFBSBoolean() {
    	return primitive;
    }


    public boolean isJavaAssignableTo(Class c) {
        return c.isAssignableFrom(Boolean.class) || c==BooleanObject.class;
    }

    public Object toJavaObject(Class c){
        if (c==null || c.isAssignableFrom(Boolean.class) ) {
            return this.primitive.booleanValue() ? Boolean.TRUE : Boolean.FALSE;
        }
        if( c==BooleanObject.class ) { // ??? PHIL: I think this is not needed....
            return this;
        }
        throw new RuntimeException("Can't convert "+this.getClass().getName()+" to "+c.getName());
    }
}