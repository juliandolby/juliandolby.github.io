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
import com.ibm.jscript.util.StringUtil;

public final class FBSUndefined extends FBSValue{

    public static FBSUndefined undefinedValue= new FBSUndefined();

    private FBSUndefined() {
    }

    public int getType(){
        return UNDEFINED_TYPE;
    }

    public FBSType getFBSType(){
        return FBSType.undefinedType;
    }

    public String stringValue(){
        return ""; //$NON-NLS-1$
    }

    public double numberValue(){
        return Double.NaN;
    }

    public boolean booleanValue(){
        return false;
    }

    public boolean isUndefined() {
        return true;
    }

    public boolean canFBSObject(){
        return false;
    }

    public FBSValue toFBSPrimitive(){
        return this;
    }

    public FBSBoolean toFBSBoolean(){
        return FBSBoolean.get(booleanValue());
    }

    public FBSNumber toFBSNumber(){
        return FBSNumber.get(numberValue());
    }

    public String getTypeAsString(){
        return "undefined"; //$NON-NLS-1$
    }

    public FBSString toFBSString(){
        return FBSString.get(stringValue());
    }

    public String toString(){
        return "FBSUndefined"; //$NON-NLS-1$
    }

    public FBSObject toFBSObject() throws InterpretException{
        throw new InterpretException("Can't convert Undefined to an Object");
    }

    public boolean isJavaAssignableTo(Class c) {
        if (c==null){
            return true;
        }
        return false;
    }

    public Object toJavaObject(Class _class) throws InterpretException{
        if (_class==null){
            return null;
        }
        if (_class==String.class) {
        	return stringValue();
        }
        throw new InterpretException(StringUtil.format("can't convert FBSUndefined to {0}",_class.getName()));
    }

    public boolean isJavaNative(){
        return false;
    }
}
