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

public final class FBSBoolean extends FBSValue {

    public static final FBSBoolean TRUE=new FBSBoolean(true);
    public static final FBSBoolean FALSE=new FBSBoolean(false);

    private boolean value;

    public static final FBSBoolean get(boolean b) {
        return b ? TRUE: FALSE;
    }

    public static final FBSBoolean get(String s) {
        return get(StringUtil.equals(s,"true"));
    }

    private FBSBoolean() {
    }

    private FBSBoolean(boolean v) {
        this.value=v;
    }

    public int getType(){
        return BOOLEAN_TYPE;
    }
    
    public boolean isBoolean() {
    	return true;
    }

    public FBSType getFBSType() {
        return FBSType.booleanType;
    }

    public boolean isJavaAssignableTo(Class c) {
        return c==Boolean.TYPE || c==Boolean.class || c==Object.class;
    }

    public String stringValue(){
        return value?"true":"false";
    }

    public double numberValue(){
        return value?1:0;
    }

    public boolean booleanValue(){
        return value;
    }

    public FBSValue toFBSPrimitive(){
        return this;
    }

    public FBSBoolean toFBSBoolean(){
        return new FBSBoolean(value);
    }

    public FBSNumber toFBSNumber(){
        return FBSNumber.get(value?1:0);
    }
    public FBSString toFBSString(){
        return FBSString.get(stringValue());
    }

    public String getTypeAsString(){
        return "boolean";
    }

    public String toString(){
        return "FBSBoolean : "+value;
    }

    public FBSObject toFBSObject(){
        try{
            return (FBSObject)FBSUtility.wrap(new com.ibm.jscript.std.BooleanObject(this));
        }catch(Exception e){
            com.ibm.jscript.util.TDiag.exception(e);
        }
        return null;
    }

    public Object toJavaObject(Class _class) throws InterpretException{
        if (_class==null || _class==Boolean.TYPE || _class==Boolean.class || _class==Object.class){
            return new Boolean(value);
        }
        if (_class==String.class) {
        	return stringValue();
        }
        throw new InterpretException(StringUtil.format("can't convert FBSBoolean to {0}",_class.getName()));
    }

    public boolean isJavaNative(){
        return false;
    }
}
