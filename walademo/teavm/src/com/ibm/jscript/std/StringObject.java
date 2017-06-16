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
import com.ibm.jscript.types.FBSObject;
import com.ibm.jscript.types.FBSString;
import com.ibm.jscript.types.FBSUtility;
import com.ibm.jscript.types.FBSValue;
import com.ibm.jscript.types.FBSValueVector;

/**
 * Javascript String object.
 */
public class StringObject extends AbstractPrimitiveObject {
	
    private static final String PROP_LENGTH = "length"; //$NON-NLS-1$
	
    private FBSString primitive;

    public StringObject(FBSString str) {
        super(StringPrototype.getStringPrototype());
        this.primitive = str;
    }
    ////// Constructor
    //TODO: metadata...
    public boolean supportConstruct(){
        return true;
    }

    public FBSObject construct(FBSValueVector args)throws InterpretException{
        if(args.getCount()==0) {
            return new StringObject(FBSString.emptyString);
        }
        FBSString s = args.get(0).toFBSString();
        return new StringObject(s);
    }

    public FBSValue call(IExecutionContext context,FBSValueVector args,FBSObject _this) throws JavaScriptException {
        if(args.getCount()==0) {
            return FBSString.emptyString;
        }
        return args.get(0).toFBSString();
    }
    
    public boolean supportCall() {
        return true;
    }


    public boolean equals( Object o ) {
    	if( o instanceof FBSValue ) {
    		FBSValue w = (FBSValue)o;
			return primitive.stringValue()==w.stringValue();
    	}
    	if( o instanceof FBSString ) {
    		FBSString w = (FBSString)o;
			return primitive.stringValue()==w.stringValue();
    	}
    	return false;
    }

    public String getTypeAsString(){
        return "String"; //$NON-NLS-1$
    }

    public FBSValue getPrimitiveValue() {
    	return primitive;
    }
    
    public FBSString getFBSString() {
    	return primitive;
    }

    public boolean isJavaAssignableTo(Class c) {
        return c.isAssignableFrom(String.class) || c==StringObject.class;
    }

    public FBSValue get(String name) throws InterpretException {
        if( name.equals(PROP_LENGTH) ){
            return FBSUtility.wrap(primitive.stringValue().length());
        }
        return super.get(name);
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

    public Object toJavaObject(Class c){
        if (c==null || c.isAssignableFrom(String.class) ) {
            return this.primitive.stringValue();
        }
        if( c==StringObject.class ) { // ??? PHIL: I think this is not needed....
            return this;
        }
        throw new RuntimeException("Can't convert "+this.getClass().getName()+" to "+c.getName());
    }

}