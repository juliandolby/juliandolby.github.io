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
import com.ibm.jscript.std.DateObject;
import com.ibm.jscript.util.DToA;
import com.ibm.jscript.util.StringUtil;

public final class FBSNumber extends FBSValue {

    public static final FBSNumber NaN  		= new FBSNumber(Double.NaN);
    public static final FBSNumber POSITIVE_INFINITY = new FBSNumber(Double.POSITIVE_INFINITY);
    public static final FBSNumber NEGATIVE_INFINITY = new FBSNumber(Double.NEGATIVE_INFINITY);
    public static final FBSNumber MinusOne 	= new FBSNumber(-1);
    public static final FBSNumber Zero 		= new FBSNumber(0);
    public static final FBSNumber One 		= new FBSNumber(1);
    public static final FBSNumber Two 		= new FBSNumber(2);
    public static final FBSNumber Three 	= new FBSNumber(3);
    public static final FBSNumber Four 		= new FBSNumber(4);
    public static final FBSNumber Five 		= new FBSNumber(5);
    public static final FBSNumber Six 		= new FBSNumber(6);
    public static final FBSNumber Seven 	= new FBSNumber(7);
    public static final FBSNumber Eight 	= new FBSNumber(8);
    public static final FBSNumber Nine 		= new FBSNumber(9);
    public static final FBSNumber Ten 		= new FBSNumber(10);

    private double value;

    public static final FBSNumber get( int v ) {
    	switch(v) {
    	    case -1:    return MinusOne;
    		case 0:		return Zero;
    		case 1:		return One;
    		case 2:		return Two;
    		case 3:		return Three;
    		case 4:		return Four;
    		case 5:		return Five;
    		case 6:		return Six;
    		case 7:		return Seven;
    		case 8:		return Eight;
    		case 9:		return Nine;
    		case 10:	return Ten;
    	}
        return new FBSNumber(v);
    }

    public static final FBSNumber get( long v ) {
        return new FBSNumber(v);
    }

    public static final FBSNumber get( double v ) {
        return new FBSNumber(v);
    }

    public static final FBSNumber get( String v ) {
        try {
            double d = Double.parseDouble(v);
            return get(d);
        } catch(Exception e) {}
        return NaN;
    }


    private FBSNumber() {
    }

    private FBSNumber(FBSNumber v) {
        this.value=v.value;
    }

    private FBSNumber(double d) {
        this.value = d;
    }

    public int getType(){
        return NUMBER_TYPE;
    }

    public FBSType getFBSType() {
        return FBSType.numberType;
    }

    public boolean isNumber() {
        return true;
    }

    public boolean isNaN(){
        return Double.isNaN(value);
    }

    public boolean isFinite(){
        return !Double.isInfinite(value);
    }

    public boolean isByte() {
        return value==(double)(byte)value;
    }

    public boolean isShort() {
        return value==(double)(short)value;
    }

    public boolean isInteger() {
        return value==(double)(int)value;
    }

    public boolean isLong() {
        return value==(double)(long)value;
    }

    public boolean isFloat() {
        return value==(double)(float)value;
    }

    public String stringValue() {
        return stringValue(value);
    }

    public static String stringValue(double value) {
        if (value == Double.POSITIVE_INFINITY)
            return "Infinity";
        if (value == Double.NEGATIVE_INFINITY)
            return "-Infinity";
        if (Double.isNaN(value))
            return "NaN";
        if (value == 0.0)
            return "0";

        return DToA.toStandard(value);
    }

    public double numberValue(){
        return value;
    }

    public DateObject dateValueJS() {
        return new DateObject(longValue());
    }

    public FBSNumber incValue(){
        return new FBSNumber(value+1.0);
    }

    public FBSNumber decValue(){
        return new FBSNumber(value-1.0);
    }

    public boolean booleanValue(){
        return (Double.isNaN(value)||value==0)?false:true;
    }

    public FBSValue toFBSPrimitive(){
        return this;
    }

    public FBSBoolean toFBSBoolean(){
        return FBSBoolean.get(booleanValue());
    }

    public FBSNumber toFBSNumber(){
        return this;
    }

    public FBSString toFBSString(){
        return FBSString.get(stringValue());

    }

    public String getTypeAsString(){
        return "number";
    }

    public String toString(){
        return "FBSNumber : "+value;
    }

    public FBSObject toFBSObject(){
        try {
            return (FBSObject)FBSUtility.wrap(new com.ibm.jscript.std.NumberObject(this));
        } catch(Exception e){
            com.ibm.jscript.util.TDiag.exception(e);
        }
        return null;
    }


    public boolean isJavaAssignableTo(Class c) {
        return    isNumberType(c)
               || c==Byte.class
               || c==Short.class
               || c==Integer.class
               || c==Long.class
               || c==Float.class
               || c==Double.class
        	   || c==Object.class;
    }
    private static boolean isNumberType( Class c ) {
        return    c==Byte.TYPE
               || c==Short.TYPE
               || c==Integer.TYPE
               || c==Long.TYPE
               || c==Float.TYPE
               || c==Double.TYPE;
    }
    

    public Object toJavaObject(Class _class) throws InterpretException{
        if( _class==null  || _class==Object.class ) {
            return new Double(doubleValue());
        } else if(_class.isPrimitive()) {
            if(_class==double.class) {
                return new Double(doubleValue());
            } else if(_class==int.class) {
                return new Integer(intValue());
            } else if(_class==long.class) {
                return new Long(longValue());
            } else if(_class==byte.class) {
                return new Byte( (byte)intValue());
            } else if(_class==short.class) {
                return new Short(shortValue());
            } else if(_class==float.class) {
                return new Float( (float)doubleValue());
            }
            // Will fail
        } else if(Number.class.isAssignableFrom(_class)) {
            if(_class==Byte.class){
                return new Byte((byte)intValue());
            } else if (_class==Integer.class) {
                return new Integer(intValue());
            } else if (_class==Short.class) {
                return new Short(shortValue());
            } else if (_class==Long.class) {
                return new Long(longValue());
            } else if (_class==Float.class) {
                return new Float((float)doubleValue());
            } else if (_class==Double.class) {
                return new Double(doubleValue());
            }
            // Will fail
        }
        if (_class==String.class) {
        	return stringValue();
        }
        throw new InterpretException(StringUtil.format("can't convert FBSNumber to {0}",_class.getName()));
    }

    public boolean isJavaNative(){
        return false;
    }
}
