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
package com.ibm.jscript.lib;

import java.util.Arrays;
import java.util.Iterator;

import com.ibm.jscript.InterpretException;
import com.ibm.jscript.JavaScriptException;
import com.ibm.jscript.engine.IExecutionContext;
import com.ibm.jscript.types.BuiltinFunction;
import com.ibm.jscript.types.FBSObject;
import com.ibm.jscript.types.FBSUndefined;
import com.ibm.jscript.types.FBSUtility;
import com.ibm.jscript.types.FBSValue;
import com.ibm.jscript.types.FBSValueVector;
import com.ibm.jscript.types.IFBSArray;
import com.ibm.jscript.types.JavaWrapperObject;
import com.ibm.jscript.util.StringUtil;

public abstract class AbstractArrayWrapper extends JavaWrapperObject implements IFBSArray {

    private static final String PROP_LENGTH = "length";
    private static final String PROP_SORT = "sort";
    private static final String PROP_JOIN = "join";

    // wrappers must have default constructor to be instantiated thru reflection
    protected AbstractArrayWrapper(){
    }

    public String stringValue(){
		return join(",");
    }
	
	public String join(String separator) {
        StringBuffer sb = new StringBuffer();
        for(int i=0; i<getArrayLength(); i++){
            if(i>0) {
                sb.append(separator);
            }
            sb.append(get(i).stringValue());
        }
        return sb.toString();
    }
    
    public String toString() {
    	return stringValue();
    }

    public boolean isArray() {
        return true;
    }

    public boolean isJavaArray() {
        return true;
    }

    public boolean isJSArray() {
        return false;
    }

    public abstract int getArrayLength();
    public FBSValue getArrayValue(int index) throws InterpretException {
        return get(index);
    }
    public void setArrayValue(int index, FBSValue value) throws InterpretException {
        put( index, value );
    }

    public abstract void put(int index, FBSValue value);
    public abstract FBSValue get(int index);
    public abstract Object toJavaObject(Class _class) throws InterpretException;

    public String getTypeAsString() {
        Object array = getJavaObject();
        if( array!=null ) {
            return array.getClass().getName();
        }
        return "null"; // Should not ho there...
    }

    public final boolean canPut(String name){
        return true;
    }

    public final boolean hasProperty(String name){
        if(name.equals(PROP_LENGTH)) {
            return true;
        } else if(name.equals(PROP_SORT) || name.equals(PROP_JOIN)) {
        	return true;
        } else {
            try {
                int index = Integer.parseInt(name);
                if( index>=0 ) {
                    return index<getArrayLength();
                }
            } catch(Exception e) {}
        }
        return false;
    }

    public final Iterator getPropertyKeys() {
        // Chain between all the elements and the basic iterator
        return new Iterator() {
            int current = 0;
            int arrayLength = AbstractArrayWrapper.this.getArrayLength();
            public boolean hasNext() {
                if( current<arrayLength ) {
                    return true;
                }
                return false;
            }
            public Object next() {
                if( current<arrayLength ) {
                    return Integer.toString(current++);
                }
                return null;
            }
            public void remove() {}
        };
    }

    public final void put(String name, FBSValue value) throws InterpretException {
        if(name.equals(PROP_LENGTH)) {
            throw new InterpretException( "Cannot change the length of a java array. Use a javascript Array instead" );
        } else if(name.equals(PROP_SORT)) {
            throw new InterpretException( "Cannot change the sort method of a java array." );
        } else if(name.equals(PROP_JOIN)) {
            throw new InterpretException( "Cannot change the join method of a java array." );
        } else {
            try {
                int index = Integer.parseInt(name);
                if( index>=0 ) {
                    put( index, value );
                    return;
                }
            } catch(Exception e) {}
        }
        throw new InterpretException( StringUtil.format("Cannot set property '{0}' to a java array", name) );
    }

    public final FBSValue get(String name){
        if( name.equals(PROP_LENGTH) ){
            return FBSUtility.wrap(getArrayLength());
        } else if(name.equals(PROP_SORT)) {
        	return METHOD_SORT;
        } else if(name.equals(PROP_JOIN)) {
        	return METHOD_JOIN;
        } else {
            try {
                int index=Integer.parseInt(name);
                return get(index);
            } catch(Exception e) {}
        }
        return FBSUndefined.undefinedValue;
    }

    public boolean hasIndexedProperty(){
        return true;
    }

    public final FBSValue getDefaultValue(String hint){
        return FBSUndefined.undefinedValue;
    }

    public final FBSValue getDefaultValue(int hint){
        return FBSUndefined.undefinedValue;
    }

    public final boolean hasMultipleValues() {
        return true;
    }

    public final FBSValue.IValues getValues(){
        return new Values();
    }

    public final void sort() throws InterpretException {
    	Object arr = getJavaObject();
    	if (arr instanceof int[])
    		Arrays.sort((int[])arr);
    	else if (arr instanceof long[])
    		Arrays.sort((long[])arr);
    	else if (arr instanceof short[])
    		Arrays.sort((short[])arr);
    	else if (arr instanceof double[])
    		Arrays.sort((double[])arr);
    	else if (arr instanceof float[])
    		Arrays.sort((float[])arr);
    	else if (arr instanceof char[])
    		Arrays.sort((char[])arr);
    	else if (arr instanceof byte[])
    		Arrays.sort((byte[])arr);
    	else if (arr instanceof Object[])
    		Arrays.sort((Object[])arr);
    	else
            throw new InterpretException( "Cannot sort java array of type " + arr.getClass().getName());
    }

    private class Values implements FBSValue.IValues{
        int     current;
        private Values() {
            this.current=0;
        }
        public boolean hasNext() {
            return current<getArrayLength();
        }
        public FBSValue next() {
            if( current<getArrayLength() ) {
                return get(current++);
            }
            return null;
        }
    }

    // Builin function
    private static class ArrayMethod extends BuiltinFunction{
        int mIndex=-1;

        public ArrayMethod(int index){
            mIndex=index;
        }

        protected String[] getCallParameters() {
            switch(mIndex) {
                case 0: //sort()
                	return new String[] {"():T"}; //$NON-NLS-1$
            }
            return super.getCallParameters();
        }


        public FBSValue call(IExecutionContext context, FBSValueVector args, FBSObject _this) throws JavaScriptException {
            switch(mIndex){
                case 0: {     // sort
                	if(_this instanceof AbstractArrayWrapper ) {
                		((AbstractArrayWrapper)_this).sort();
                		return FBSUndefined.undefinedValue;
                	}
                    throw new InterpretException("Cannot call sort() on a non java array object");
                }
                case 1: {	// join
                	if(_this instanceof AbstractArrayWrapper ) {
						String sep = ",";
						if (args.getCount() == 1)
							sep = args.get(0).stringValue();
                		return FBSUtility.wrap(((AbstractArrayWrapper)_this).join(sep));
                	}
                    throw new InterpretException("Cannot call join() on a non java array object");
                }
            }
            throw new InterpretException("Array function not found");
        }
    }
    private static ArrayMethod METHOD_SORT = new ArrayMethod(0);
    private static ArrayMethod METHOD_JOIN = new ArrayMethod(1);
    
}
