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

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.ibm.jscript.IValue;
import com.ibm.jscript.InterpretException;
import com.ibm.jscript.JScriptResources;
import com.ibm.jscript.lib.AbstractArrayWrapper;
import com.ibm.jscript.std.ArrayObject;
import com.ibm.jscript.std.DateObject;
import com.ibm.jscript.util.StringUtil;
import com.ibm.jscript.util.TException;

public abstract class FBSValue extends FBSResult implements IValue {

    public static final int UNDEFINED_TYPE= 0;
    public static final int NULL_TYPE     = 1;
    public static final int BOOLEAN_TYPE  = 2;
    public static final int STRING_TYPE   = 3;
    public static final int NUMBER_TYPE   = 4;
    public static final int OBJECT_TYPE   = 5;
    public static final int FUNCTION_TYPE = 6;
    public static final int CUSTOM_TYPE   = 9999;

    abstract public int getType();
    abstract public FBSType getFBSType();
    abstract public String getTypeAsString();
    abstract public String stringValue();
    abstract public double numberValue();
    abstract public boolean isJavaNative();

    public boolean isNumber() {
        return false;
    }

    public boolean isBoolean() {
        return false;
    }

    public boolean isString() {
        return false;
    }

    public boolean isArray() {
        return false;
    }

    public boolean isJavaArray() {
        return false;
    }

    public boolean isJSArray() {
        return false;
    }

    public boolean isObject() {
        return false;
    }

    public boolean isJavaObject() {
        return false;
    }

    public boolean isJSObject() {
        return false;
    }

    public boolean isList() {
        return getArrayLength()>1;
    }

    public boolean isDate() {
        return false;
    }

    public boolean isUndefined() {
        return false;
    }

    public boolean isNull() {
        return false;
    }

    public double doubleValue(){
        return numberValue();
    }

    public int intValue(){
        return (int)numberValue();
    }

    public short shortValue(){
        return (short)numberValue();
    }

    public long longValue(){
        return (long)numberValue();
    }

    public DateObject dateValueJS() {
        return null;
    }

    public java.util.Date dateValue() {
        DateObject dt = dateValueJS();
        if( dt!=null ) {
            return new java.util.Date(dt.getTime());
        }
        return null;
    }

    public String toString() {
        return getTypeAsString()+":"+stringValue(); //$NON-NLS-1$
    }

    // PHIL: For checking the parameters
    // This function return true if the value contained in this object can be assigned to a
    // specific java class. Note that it also includes the primitive types.
    // The primitive types number+string+boolean are *not* automatically converted. This ensure
    // a better handling of method with same number of parameters.
    public boolean isJavaAssignableTo(Class c) {
        return false;
    }

    /**
     * Checks if an FBSValue can be converted to an FBSObject.
     * @return boolean
     */
    public boolean canFBSObject(){
        return true;
    }

    abstract public boolean booleanValue();
    abstract public FBSValue toFBSPrimitive();
    abstract public FBSBoolean toFBSBoolean();
    abstract public FBSNumber toFBSNumber();
    abstract public FBSString toFBSString();
    abstract public FBSObject toFBSObject() throws InterpretException;


    // ========================================================================
    //  Object Accessor
    // ========================================================================

    public boolean hasProperty(String name) throws InterpretException {
        throw new InterpretException( null, StringUtil.format(JScriptResources.getString("FBSValue.NoProperty.Exception"), getTypeAsString()) ); //$NON-NLS-1$
    }
    public IValue getProperty( String name ) throws InterpretException {
        throw new InterpretException( null, StringUtil.format(JScriptResources.getString("FBSValue.NoProperty.Exception"), getTypeAsString()) ); //$NON-NLS-1$
    }
    public void putProperty( String name, IValue value ) throws InterpretException {
        throw new InterpretException( null, StringUtil.format(JScriptResources.getString("FBSValue.NoProperty.Exception"), getTypeAsString()) ); //$NON-NLS-1$
    }


    /**
     * Gets a java object from an FBScript value.<br>
     * The type of the returned object must be <code>_class</code> or one of its<br>
     * subclasses.<br>
     * If the method is not able to meet the required criteria an InterpretException<br>
     * is thrown. In case <code>_class</code> is <b>null</b> the method is free
     * to return an object of any type.<br>
     * @param _class required for the returned java object. may be null.
     * @return Java object representing FBScript value.
     *
     */
    abstract public Object toJavaObject(Class _class) throws InterpretException;

    public Object toJavaObject() throws InterpretException {
        return toJavaObject(null);
    }

    public boolean isPrimitive(){
        return true;
    }

    /**
     *
     */
    public boolean hasMultipleValues(){
        return false;
    }


    // ========================================================================
    //  Array Accessor
    // ========================================================================

    public int getArrayLength() {
        return 1;
    }
    public FBSValue getArrayValue(int index) throws InterpretException {
        return this;
    }
    public void setArrayValue(int index, FBSValue value) throws InterpretException {
        throw new InterpretException( StringUtil.format(JScriptResources.getString("FBSValue.NotAnArray.Exception"), getTypeAsString()) ); //$NON-NLS-1$
    }

    public final IValue getArrayElement( int index ) throws InterpretException {
        return getArrayValue(index);
    }
    public final void setArrayElement( int index, IValue value ) throws InterpretException {
        setArrayValue(index,(FBSValue)value);
    }


    /**
     *
     */
    public FBSValue getLastArrayValue() throws InterpretException {
        int idx = getArrayLength()-1;
        return getArrayValue(idx);
    }

    /**
     *
     */
    public IValues getValues(){
        return new UniqueValue(this);
    }

    /**
     *
     */
    public static interface IValues {
        public boolean hasNext();
        public FBSValue next();
     }

    protected final static class UniqueValue implements IValues {
        FBSValue value;
        protected UniqueValue( FBSValue value ) {
            this.value = value;
        }
        public boolean hasNext() {
            return value!=null;
        }
        public FBSValue next() {
            FBSValue oldValue = value;
            value = null;
            return oldValue;
        }
    }



    // ========================================================================
    // Java Object IO.
    // ========================================================================

    public static FBSValue readValue( ObjectInput in ) throws IOException {
        switch(in.readByte()) {
            case 0: {
                return FBSUndefined.undefinedValue;
            }
            case 1: {
                return FBSNull.nullValue;
            }
            case 2: {
                return FBSBoolean.get(in.readBoolean());
            }
            case 3: {
                return FBSString.get(in.readUTF());
            }
            case 4: {
                return FBSNumber.get(in.readDouble());
            }
            case 5: {
                try {
                    Object obj = in.readObject();
                    return FBSUtility.wrap(obj);
                } catch( InterpretException e ) {
                    IOException ex2 = new IOException( "JavascriptWrapper, Cannot wrap object: "+e.getMessage() );
                    TException.initCause(ex2,e);
                    throw ex2;
                } catch( ClassNotFoundException e ) {
                    IOException ex2 = new IOException( "JavascriptWrapper, class not found: "+e.getMessage() );
                    TException.initCause(ex2,e);
                    throw ex2;
                }
            }
            case 6: {
                try {
                    FBSDefaultObject obj = (FBSDefaultObject)in.readObject();
                    return obj;
                } catch( ClassNotFoundException e ) {
                    IOException ex2 = new IOException( "FBSDefaultObject, class not found: "+e.getMessage() );
                    TException.initCause(ex2,e);
                    throw ex2;
                }
            }
        }
        return null;
    }

    public static void writeValue( ObjectOutput out, FBSValue value ) throws IOException {
        switch(value.getType()) {
            case UNDEFINED_TYPE: {
                out.writeByte(0);
            } break;
            case NULL_TYPE: {
                out.writeByte(1);
            } break;
            case BOOLEAN_TYPE: {
                out.writeByte(2);
                out.writeBoolean(value.booleanValue());
            } break;
            case STRING_TYPE: {
                out.writeByte(3);
                out.writeUTF(value.stringValue());
            } break;
            case NUMBER_TYPE: {
                out.writeByte(4);
                out.writeDouble(value.doubleValue());
            } break;
            case OBJECT_TYPE: {
                if( value instanceof JavaWrapperObject ) {
                    out.writeByte(5);
                    out.writeObject(((JavaWrapperObject)value).getJavaObject());
                } else if( value instanceof FBSDefaultObject ) {
                    out.writeByte(6);
                    out.writeObject((FBSDefaultObject)value);
                } else {
                    throw new IOException( JScriptResources.getString("FBSValue.CannotSerializeObj.Exception") ); //$NON-NLS-1$
                }
            } break;
            case FUNCTION_TYPE:
                throw new IOException( JScriptResources.getString("FBSValue.CannotSerializeFunc.Exception") ); //$NON-NLS-1$
            case CUSTOM_TYPE:
                throw new IOException( JScriptResources.getString("FBSValue.CannotSerializeVal.Exception") ); //$NON-NLS-1$
        }
    }


    // ========================================================================
    // Value dump
    // ========================================================================

    public static void dump( PrintStream ps, FBSValue value ) {
        try {
            HashMap map = new HashMap();
            dump( ps, new HashMap(), new ArrayList(), null, value );
        } catch( InterpretException e ) {
            com.ibm.jscript.util.TDiag.exception(e);
        }
    }

    private static void dump( PrintStream ps, HashMap map, ArrayList hierarchy, String propertyName, FBSValue value ) throws InterpretException {
        // Get the children list
        // Print the indentation
        for( int i=0; i<hierarchy.size(); i++ ) {
ps.print( "    " ); //$NON-NLS-1$
//            if( parent.readSlotAt(parent.getSlotCount()-1)==nodes.get(lv) ) {
//                ps.print( "    " );
//            } else {
//                ps.print( "|   " );
//            }
        }

        hierarchy.add(value);
        try {
            // Print the value itself
            if(hierarchy.size()>1 ) {
                ps.print("+-"); //$NON-NLS-1$
            }
            if( propertyName!=null ) {
                ps.print(propertyName);
                ps.print(": "); //$NON-NLS-1$
            }
            ps.print(value.getTypeAsString());
            String vs = value.stringValue();
            if( !StringUtil.isEmpty(vs) ) {
                ps.print("="); //$NON-NLS-1$
                ps.print(vs);
            }
            ps.print("\n"); //$NON-NLS-1$

            // Display Object's property
            if( value instanceof FBSDefaultObject ) {
                FBSDefaultObject o = (FBSDefaultObject)value;
                for( Iterator it=o.getPropertyKeys(); it.hasNext(); ) {
                    String key = (String)it.next();
                    // Special case for arrays: we display indexed properties within brackets
                    if( value instanceof ArrayObject ) {
                        ArrayObject a = (ArrayObject)value;
                        try {
                            int index=Integer.parseInt(key);
                            dump( ps, map, hierarchy, "["+index+"]", a.getArrayValue(index) ); //$NON-NLS-1$ //$NON-NLS-2$
                            continue;
                        } catch(Exception e) {}
                    }
                    dump( ps, map, hierarchy, key, o.get(key) );
                }
            }
            // Display java arrays
            if( value instanceof AbstractArrayWrapper ) {
                AbstractArrayWrapper a = (AbstractArrayWrapper)value;
                for( int i=0; i<a.getArrayLength(); i++ ) {
                    dump( ps, map, hierarchy, "["+i+"]", a.get(i) ); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
        } finally {
            hierarchy.remove( hierarchy.size()-1 );
        }
    }
}
