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

import com.ibm.jscript.types.FBSBoolean;
import com.ibm.jscript.types.FBSDefaultObject;
import com.ibm.jscript.types.FBSNumber;
import com.ibm.jscript.types.FBSString;
import com.ibm.jscript.types.FBSValue;

/**
 * Abstract object that wraps a primitive type.
 */
public abstract class AbstractPrimitiveObject extends FBSDefaultObject {
	
    public AbstractPrimitiveObject(AbstractPrototype prototype) {
    	super(prototype);
    }
    
    public abstract FBSValue getPrimitiveValue();
    
    public boolean isBoolean() {
    	return getPrimitiveValue().isBoolean();
    }

    public boolean isNumber() {
    	return getPrimitiveValue().isNumber();
    }
    
    public boolean isString() {
    	return getPrimitiveValue().isString();
    }
    
    public boolean booleanValue(){
        return getPrimitiveValue().booleanValue();
    }

    public String stringValue(){
       return getPrimitiveValue().stringValue();
    }

    public double numberValue(){
    	return getPrimitiveValue().numberValue();
    }

    public DateObject dateValueJS(){
    	return getPrimitiveValue().dateValueJS();
    }

    
    public final FBSValue toFBSPrimitive(){
        return getPrimitiveValue();
    }

    public final FBSBoolean toFBSBoolean(){
        return getPrimitiveValue().toFBSBoolean();
    }

    public final FBSNumber toFBSNumber(){
        return getPrimitiveValue().toFBSNumber();
    }

    public final FBSString toFBSString(){
        return getPrimitiveValue().toFBSString();
    }
}