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
import com.ibm.jscript.engine.JSOptions;
import com.ibm.jscript.lib.BooleanArrayWrapper;
import com.ibm.jscript.lib.ByteArrayWrapper;
import com.ibm.jscript.lib.CharArrayWrapper;
import com.ibm.jscript.lib.DoubleArrayWrapper;
import com.ibm.jscript.lib.FloatArrayWrapper;
import com.ibm.jscript.lib.IntArrayWrapper;
import com.ibm.jscript.lib.LongArrayWrapper;
import com.ibm.jscript.lib.ObjectArrayWrapper;
import com.ibm.jscript.lib.ShortArrayWrapper;
import com.ibm.jscript.std.DateObject;
import com.ibm.jscript.std.StringObject;
import com.ibm.jscript.util.StringUtil;

public final class FBSUtility {

    // =======================================================================
    // Basic java primitive type wrappers
    // =======================================================================

    public static FBSValue wrap(char c){
        return FBSString.get(c);
    }

    public static FBSValue wrap(byte b){
        return FBSNumber.get((int)b);
    }

    public static FBSValue wrap(short s){
        return FBSNumber.get((int)s);
    }

    public static FBSValue wrap(int i){
        return FBSNumber.get(i);
    }

    public static FBSValue wrap(long l){
        return FBSNumber.get(l);
    }

    public static FBSValue wrap(float f){
        return FBSNumber.get((double)f);
    }

    public static FBSValue wrap(double d){
        return FBSNumber.get(d);
    }

    public static FBSValue wrap(boolean b){
        return b ? FBSBoolean.TRUE : FBSBoolean.FALSE;
    }


    // =======================================================================
    // Basic java primitive array wrappers
    // =======================================================================

    public static FBSObject wrap(boolean[] array){
        return new BooleanArrayWrapper(array);
    }

    public static FBSObject wrap(char[] array){
        return new CharArrayWrapper(array);
    }

    public static FBSObject wrap(byte[] array){
        return new ByteArrayWrapper(array);
    }

    public static FBSObject wrap(short[] array){
        return new ShortArrayWrapper(array);
    }

    public static FBSObject wrap(int[] array){
        return new IntArrayWrapper(array);
    }

    public static FBSObject wrap(long[] array){
        return new LongArrayWrapper(array);
    }

    public static FBSObject wrap(float[] array){
        return new FloatArrayWrapper(array);
    }

    public static FBSObject wrap(double[] array){
        return new DoubleArrayWrapper(array);
    }

    public static FBSObject wrap(Object[] array){
        return new ObjectArrayWrapper(array);
    }

    public static FBSObject wrapArray(Object array) throws InterpretException {
        if(array.getClass().isArray()) {
            Class c=array.getClass().getComponentType();
            if(c.isPrimitive()) {
                if(array instanceof char[]) {
                    return wrap( (char[])array);
                } else if(array instanceof byte[]) {
                    return wrap( (byte[])array);
                } else if(array instanceof short[]) {
                    return wrap( (short[])array);
                } else if(array instanceof int[]) {
                    return wrap( (int[])array);
                } else if(array instanceof long[]) {
                    return wrap( (long[])array);
                } else if(array instanceof float[]) {
                    return wrap( (float[])array);
                } else if(array instanceof double[]) {
                    return wrap( (double[])array);
                } else if(array instanceof boolean[]) {
                    return wrap( (boolean[])array);
                } else {
                    // Should fail....
                }
            }
            return wrap( (Object[])array);
        }
        throw new InterpretException( "Object is not a java array" );
    }


    // =======================================================================
    // Javascript primitive wrappers
    // =======================================================================

    public static FBSValue wrap(String s){
        return FBSString.get(s);
    }

    public static FBSValue wrap(java.util.Date value){
        if (value==null){
            return FBSNull.nullValue;
        }
        try{
            DateObject dobj = new DateObject(value);
            return wrap(dobj);
        }catch(Exception e){
        }
        return FBSUndefined.undefinedValue;
    }

//    public static FBSValue wrap(java.sql.Timestamp value){
//        if (value==null){
//            return FBSNull.nullValue;
//        }
//        try{
//            return wrap(new DateObject(value!=null ? new java.util.Date(value.getTime()) : (java.util.Date)null));
//        }catch(Exception e){
//        }
//        return FBSUndefined.undefinedValue;
//    }
//
//    public static FBSValue wrap(java.sql.Time value){
//        if (value==null){
//            return FBSNull.nullValue;
//        }
//        try{
//            return wrap(new DateObject(value!=null ? new java.util.Date(value.getTime()) : (java.util.Date)null));
//        }catch(Exception e){
//        }
//        return FBSUndefined.undefinedValue;
//
//    }
//
//    public static FBSValue wrap(java.sql.Date value){
//        if (value==null){
//            return FBSNull.nullValue;
//        }
//        try{
//            return wrap(new DateObject(value!=null ? new java.util.Date(value.getTime()) : (java.util.Date)null));
//        }catch(Exception e){
//        }
//        return FBSUndefined.undefinedValue;
//
//    }


    // =======================================================================
    // Object wrappers
    // =======================================================================

    public static FBSValue wrap(Object o) throws InterpretException {
        // Null object wraps to null...
        if (o==null) {
            return FBSNull.nullValue;
        }
        Class _class = o.getClass();

        // If a Java function already returns a FBSValue, do not convert it
        if( o instanceof FBSValue ) {
            return (FBSValue)o;
        }
        
        // Custom wrapper
        JavaWrapperObject ja = JSOptions.get().wrapObject(o);
        if(ja!=null) {
        	return ja;
        }
        
        // Std types
        if( _class==String.class ) {
            return wrap( (String)o );
        }
        if( o instanceof Number ) {
            // Standard Java numbers are converted to FBSNUmber
            if(    _class==Byte.class
                || _class==Integer.class
                || _class==Short.class
                || _class==Long.class
                || _class==Float.class
                || _class==Double.class) {
                return wrap( ((Number)o).doubleValue() );
            }
            // Other are left as java objects (like BigDecimal, BigInteger...)
            return new JavaAccessObject(o.getClass(),o);
        }
        if( o instanceof java.util.Date ) {
            return wrap( (java.util.Date)o );
        }

        return wrapAsObject(o);
    }
    public static final FBSValue wrapAsObjectWithNull( Object o ) throws InterpretException {
        if (o==null) {
            return FBSNull.nullValue;
        }
        return wrapAsObject(o);
    }
    public static final FBSObject wrapAsObject( Object o ) throws InterpretException {
        Class _class = o.getClass();

        // If a Java function already returns a FBSValue, do not convert it
        if( o instanceof FBSObject ) {
            return (FBSObject)o;
        }
        
        if (o instanceof String) {
            return new StringObject(FBSString.get(o.toString()));
        }
        
        // Custom wrapper
        JavaWrapperObject ja = JSOptions.get().wrapObject(o);
        if(ja!=null) {
        	return ja;
        }

        // Arrays
        if( _class.isArray() ) {
            return wrapArray(o);
        }

        // Search for a registered wrapper
        if(JSOptions.get().useGeneratedWrappers()) {
        	Class wrapper=searchWrapper(_class);
        	if(wrapper!=null) {
		        try {
		            GeneratedWrapperObject jo=(GeneratedWrapperObject)wrapper.newInstance();
		            jo.setJavaObject(o);
		            return jo;
		        } catch(Exception e) {
		            throw new InterpretException(e,StringUtil.format("Unable to wrap java object of type {0}",o.getClass().getName()));
		        }
        	}
        }
        
        // Standard introspection based wrapper
        return new JavaAccessObject(_class,o);
    }

    public static final Class searchWrapper(Class c){
        while( c!=null && c!=Object.class ) {
        	Class wrapper=Registry.getWrapper(c);
        	if(wrapper!=null) {
        		return wrapper;
        	}
        	c = c.getSuperclass();
        }
        return null;
    }
}
