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

public final class FBSNull extends FBSValue {

    public static FBSNull nullValue = new FBSNull();

    private FBSNull() {
    }

    public int getType(){
        return NULL_TYPE;
    }

    public FBSType getFBSType(){
        // returns an undefined type...
        return FBSType.undefinedType;
    }

    public String stringValue(){
        return "null";
    }

    public double numberValue(){
        return 0;
    }

    public boolean booleanValue(){
        return false;
    }

    public boolean isNull() {
        return true;
    }

    public boolean canFBSObject(){
        return false;
    }

    public FBSValue toFBSPrimitive(){
        return this;
    }

    public FBSBoolean toFBSBoolean(){
        return FBSBoolean.FALSE;
    }

    public FBSNumber toFBSNumber(){
        return FBSNumber.get(numberValue());
    }

    public FBSString toFBSString(){
        return FBSString.get("null");
    }

    public FBSObject toFBSObject() throws InterpretException{
        throw new InterpretException("can't convert Null value to Object ");
    }

    public String getTypeAsString(){
        return "null";
    }

    public String toString(){
        return "FBSNull";
    }


    public boolean isJavaAssignableTo(Class c) {
        if (c.isPrimitive()){
            return false;
        }
        return true;
    }


    public Object toJavaObject(Class _class) throws InterpretException{
        if (_class==null || (!_class.isPrimitive())){
            return null;
        }
        if (_class==String.class) {
        	return stringValue();
        }
        throw new InterpretException(StringUtil.format("can't convert FBSNull to {0}",_class.getName()));
    }

    public boolean isJavaNative(){
        return false;
    }

}
