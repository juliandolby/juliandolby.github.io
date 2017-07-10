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

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.ibm.jscript.InterpretException;
import com.ibm.jscript.JScriptResources;
import com.ibm.jscript.JavaScriptException;
import com.ibm.jscript.engine.IExecutionContext;
import com.ibm.jscript.engine.Utility;
import com.ibm.jscript.types.FBSDefaultObject;
import com.ibm.jscript.types.FBSNumber;
import com.ibm.jscript.types.FBSObject;
import com.ibm.jscript.types.FBSReference;
import com.ibm.jscript.types.FBSReferenceByIndex;
import com.ibm.jscript.types.FBSString;
import com.ibm.jscript.types.FBSUndefined;
import com.ibm.jscript.types.FBSUtility;
import com.ibm.jscript.types.FBSValue;
import com.ibm.jscript.types.FBSValueVector;
import com.ibm.jscript.types.IFBSArray;
import com.ibm.jscript.util.StringUtil;

public class ArrayObject extends FBSDefaultObject implements IFBSArray {

    private static final String PROP_LENGTH = "length"; //$NON-NLS-1$

    private static final int INITIAL_SIZE = 16;  // Initial size when not explicitly specified
    private static final int MINDELTA     = 16;  // Min Grow threshold

    
    private static final int INITIAL_THRESHOLD = 10000; // Initial size for HashMap allocation
    private static final int GROW_THRESHOLD    = 10000;


    
    private static final int MAP_LENGTH = 1000;

    
    private static class EntrySlot {
    	int count;
    	MapEntry[] entries;
    	EntrySlot() {
    		entries = new MapEntry[64];
    	}
    	MapEntry addEntry(MapEntry e) {
    		if(entries.length==count) {
    			MapEntry[] me = new MapEntry[entries.length*2];
    			System.arraycopy(entries,0,me,0,entries.length);
    			entries = me;
    		}
			return entries[count++] = e;
    	}
    	MapEntry addEntry(ArrayObject base, int index, FBSValue value, boolean searchExisting) {
    		if(searchExisting) {
	    		MapEntry e = getEntry(index);
	    		if(e!=null) {
	    			e.value = value;
	    		}
    		}
    		if(entries.length==count) {
    			MapEntry[] me = new MapEntry[entries.length*2];
    			System.arraycopy(entries,0,me,0,entries.length);
    			entries = me;
    		}
    		if(count==0 || index>entries[count-1].index) {
    			return entries[count++] = new MapEntry(base,index,value);
    		}
    		int pos = searchEntry(index,false);
    		System.arraycopy(entries,pos,entries,pos+1,count-pos-1);
			count++;
			return entries[pos] = new MapEntry(base,index,value);
    	}
    	void removeEntry(int index) {
    		for( int i=0; i<count; i++ ) {
    			if(entries[i].index==index) {
    				System.arraycopy(entries,i+1,entries,i,entries.length-i-1);
    				count--;
    				return;
    			}
    		}
    	}
    	MapEntry getEntry(int index) {
    		int i = searchEntry(index,true);
    		return i>=0 ? entries[i] : null;
    	}

    	// Dichotomy search
        private int searchEntry(int index, boolean exactSearch) {
            int low = 0;
            int high = count-1;
            while (low <= high) {
                int mid =(low + high)/2;
                int cmp = index-entries[mid].index;
                if (cmp < 0) {
                    low = mid + 1;
                } else if (cmp > 0) {
                    high = mid - 1;
                } else {
                    return mid; // key found
                }
            }
            return exactSearch
                        ? -1
                        : (low<count ? low : count);
        }
    }
    
	private static class MapEntry extends FBSReference {
		
		private ArrayObject base;
		private int index;
		private FBSValue value;
		
		MapEntry(ArrayObject base, int index, FBSValue value) {
			this.base = base;
			this.index = index;
			this.value = value;
		}

		// FBSReference implementation
		public String getPropertyName() {
			return Integer.toString(index);
		}
	    public FBSObject getBase() {
	    	return base;
	    }
	    public FBSValue getValue() throws InterpretException {
	    	return value;
	    }
	    public void putValue(FBSValue value) throws InterpretException {
	    	this.value = value;
	    }
	}
	
	private class ArrayMap {
		
		private EntrySlot[] entries = new EntrySlot[MAP_LENGTH];
		
		ArrayMap() {
			for( int i=0; i<MAP_LENGTH; i++ ) {
				entries[i] = new EntrySlot(); 
			}
		}
		public MapEntry getEntry(int index, boolean create) {
			MapEntry me = null;
			if(index<length) {
				int slot = index % MAP_LENGTH;
				EntrySlot e = entries[slot];
				me = e.getEntry(index);
			}
			if(me==null && create) {
				me = put(index,FBSUndefined.undefinedValue,false);
			}
			return me;
		}
		
		public FBSValue get(int index) {
			MapEntry p = getEntry(index,false);
			return p!=null ? p.value : null;
		}
		
		public MapEntry put( MapEntry entry ) {
			int slot = entry.index % MAP_LENGTH;
			EntrySlot e = entries[slot];
			return entries[slot].addEntry(entry);
		}
		public MapEntry put( int index, FBSValue value, boolean searchExisting ) {
			int slot = index % MAP_LENGTH;
			EntrySlot e = entries[slot];
			return entries[slot].addEntry(ArrayObject.this,index,value,searchExisting);
		}

		public void remove( int index ) {
			int slot = index % MAP_LENGTH;
			EntrySlot e = entries[slot];
			e.removeEntry(index);
		}
	}
    
    
    
    // Indexed array
    private int length;          	// length
    private MapEntry[] values;   	// Indexed values
    private ArrayMap dynamicValues; // Mapped values, for big dynamic arrays (replace with an int/object map?)

    
    public ArrayObject() {
        this(0);
    }

    public ArrayObject(int len) {
        super(ArrayPrototype.getFBSArrayPrototype());

        if( len>0 ) {
        	if( len>INITIAL_THRESHOLD ) {
            	this.dynamicValues = new ArrayMap();
            } else {
        		this.values=new MapEntry[len];
            }
        } else {
            this.values=new MapEntry[INITIAL_SIZE];
        }
        this.length = len;
    }

//    public ArrayObject(FBSValue[] values) throws InterpretException {
//        setPrototype(ArrayPrototype.getFBSArrayPrototype());
//        this.length = values.length;
//        this.values = values;
//    }

    public ArrayObject(String[] args) {
        super(ArrayPrototype.getFBSArrayPrototype());
        int len = args!=null ? args.length : 0;

        if( len>0 ) {
            this.values=new MapEntry[len];
            for(int i=0; i<len; i++) {
                this.values[i] = new MapEntry(this,i,FBSString.get(args[i]));
            }
        } else {
            this.values=new MapEntry[INITIAL_SIZE];
        }
        this.length = len;
    }

    public ArrayObject(int[] args) throws InterpretException{
        super(ArrayPrototype.getFBSArrayPrototype());

        int len = args!=null ? args.length : 0;
        if( len>0 ) {
            this.values=new MapEntry[len];
            for(int i=0; i<len; i++) {
                this.values[i] = new MapEntry(this,i,FBSNumber.get(args[i]));
            }
        } else {
            this.values=new MapEntry[INITIAL_SIZE];
        }
        this.length = len;
    }

    public ArrayObject(FBSValueVector args) throws InterpretException{
        super(ArrayPrototype.getFBSArrayPrototype());

        int len = args!=null ? args.size() : 0;
        switch(len) {
            case 0: {
                this.values=new MapEntry[INITIAL_SIZE];
            } break;
            case 1: {
                FBSValue v=(FBSValue)args.get(0);
                if(v instanceof FBSNumber) {
                	len = v.intValue();
                	if( len<INITIAL_THRESHOLD ) {
                		this.values=new MapEntry[len];
                    } else {
                    	this.dynamicValues = new ArrayMap();
                    }
                    break;
                }
            }
            default: {
                this.values=new MapEntry[len];
                for(int i=0; i<len; i++) {
                    this.values[i] = new MapEntry(this,i,args.get(i));
                }
            }
        }
        this.length = len;
    }

    public FBSObject construct(IExecutionContext context,FBSValueVector args) throws InterpretException {
        return newArray(args);
    }

    public boolean supportConstruct(){
        return true;
    }

    public FBSValue call(IExecutionContext context,FBSValueVector args,FBSObject _this) throws JavaScriptException {
        return newArray(args);
    }
    
    public boolean supportCall() {
        return true;
    }
    
    private ArrayObject newArray(FBSValueVector args) throws InterpretException {
        return new ArrayObject(args);
    }

    public boolean hasIndexedProperty(){
        return true;
    }

    public String getTypeAsString(){
        return "Array"; //$NON-NLS-1$
    }

    public String getName(){
        return "Array"; //$NON-NLS-1$
    }

    public boolean isArray() {
        return true;
    }

    public boolean isJavaArray() {
        return false;
    }

    public boolean isJSArray() {
        return true;
    }

    public boolean isList() {
        return true;
    }

    public boolean hasProperty(String name) {
        if(name.equals(PROP_LENGTH)) {
            return true;
        } else {
            try {
                int index = Integer.parseInt(name);
                if( index>=0 ) {
                    return index<length;
                }
            } catch(Exception e) {}
        }
        return super.hasProperty(name);
    }

    public Iterator getPropertyKeys() {
        // Chain between all the elements and the basic iterator
        return new Iterator() {
            int current = 0;
            Iterator it = ArrayObject.super.getPropertyKeys();
            public boolean hasNext() {
                if( current<ArrayObject.this.length ) {
                    return true;
                }
                return it!=null ? it.hasNext() : false;
            }
            public Object next() {
                if( current<ArrayObject.this.length ) {
                    return Integer.toString(current++);
                }
                return it!=null ? it.next() : null;
            }
            public void remove() {}
        };
    }

    public void put(String name, FBSValue value) throws InterpretException {
        if(name.equals(PROP_LENGTH)) {
            int len = value.intValue();
            setArrayLength(len);
            return;
        } else {
        	if(!name.startsWith("0")) {
	            try {
	                long index = Long.parseLong(name);
	                if( index>=0 && index<Integer.MAX_VALUE) {
	                    setAt((int)index, value);
	                }
	                return;
	            } catch(Exception e) {}
        	}
        }
        super.put(name,value);
    }

    public void put(int index , FBSValue value){
        if( index>=0 ) {
            setAt(index, value);
        }
    }

    public FBSValue get(int index) throws InterpretException {
        if( index>=0 && index<length ) {
        	return __get(index);
        }
        return FBSUndefined.undefinedValue;
    }

    private FBSValue __get(int index) {
    	MapEntry m = __getEntry(index,false);
    	return m!=null ? m.value : FBSUndefined.undefinedValue;
    }

    private MapEntry __getEntry(int index, boolean create) {
    	if( dynamicValues!=null ) {
    		return (MapEntry)dynamicValues.getEntry(index,create);
    	}
    	MapEntry e = values[index];
    	if(e==null && create) { 
    		values[index] = e = new MapEntry(this,index,FBSUndefined.undefinedValue);
    	}
		return e;
    }

    public FBSValue get(String name) throws InterpretException {
        if( name.equals(PROP_LENGTH) ){
            return FBSUtility.wrap(length);
        } else {
            try {
                int index=Integer.parseInt(name);
                if( index>=0 && index<length ) {
                	return __get(index);
                }
            } catch(Exception e) {}
        }
        return super.get(name);
    }

    public FBSReference getPropertyReference(int index) {
    	// If the value exists, or should exists, return it here
    	if(index<length) {
    		return __getEntry(index,true);
    	}
    	// else, it is a simple reference.
    	// the actual reference will be created the assignment will be effective 
    	return new FBSReferenceByIndex(this,index);
    }

    public Object[] getObjects(Class c) throws InterpretException {
        try {
            Object[] objects = (Object[])java.lang.reflect.Array.newInstance(c,length);
            for(int i=0;i<length;i++){
                objects[i]=__get(i).toJavaObject(c);
            }
            return objects;
        } catch( Exception e ) {
            throw new InterpretException( e, StringUtil.format(JScriptResources.getString("ArrayObject.CreatingArrayClass.Exception"), c.getName()) ); //$NON-NLS-1$
        }
    }

    public Object[] getObjects() throws InterpretException {
        try {
            Object[] objects= new Object[length];
            for(int i=0;i<length;i++){
                objects[i]=__get(i).toJavaObject(null);
            }
            return objects;
        } catch( Exception e ) {
            throw new InterpretException( e, JScriptResources.getString("ArrayObject.CreatingObjsArray.Exception") ); //$NON-NLS-1$
        }
    }

    public String[] getStrings(){
        String[] strings= new String[length];
        for(int i=0;i<length;i++){
            strings[i]=__get(i).stringValue();
        }
        return strings;
    }

    public boolean[] getBooleans(){
        boolean[] booleans= new boolean[length];
        for(int i=0;i<length;i++){
            booleans[i]=__get(i).booleanValue();
        }
        return booleans;
    }


    public char[] getChars(){
        char[] chars= new char[length];
        for(int i=0;i<length;i++){
            chars[i]=__get(i).stringValue().charAt(0);
        }
        return chars;
    }


    public byte[] getBytes(){
        byte[] bytes= new byte[length];
        for(int i=0;i<length;i++){
            bytes[i]=(byte)(__get(i).intValue());
        }
        return bytes;
    }

    public short[] getShorts(){
        short[] shorts= new short[length];
        for(int i=0;i<length;i++){
            shorts[i]=(short)(__get(i).intValue());
        }
        return shorts;
    }

    public int[] getIntegers(){
        int[] ints= new int[length];
        for(int i=0;i<length;i++){
            ints[i]=__get(i).intValue();
        }
        return ints;
    }

    public long[] getLongs(){
        long[] longs= new long[length];
        for(int i=0;i<length;i++){
            longs[i]=__get(i).longValue();
        }
        return longs;
    }

    public float[] getFloats(){
        float[] floats= new float[length];
        for(int i=0;i<length;i++){
            floats[i]=(float)(__get(i).doubleValue());
        }
        return floats;
    }

    public double[] getDoubles(){
        double[] doubles= new double[length];
        for(int i=0;i<length;i++){
            doubles[i]=__get(i).doubleValue();
        }
        return doubles;
    }

    public List getList()throws InterpretException{
        ArrayList vec= new ArrayList();
        for(int i=0;i<length;i++){
            vec.add(__get(i).toJavaObject(null));
        }
        return vec;
    }


    public boolean isJavaAssignableTo(Class c) {
        //return c==FBSArray.class;
        return c.isArray() || (c.isInstance(List.class));
    }

    public Object toJavaObject(Class hint) throws InterpretException {
        if (hint!=null) {
            if (hint == ArrayObject.class){
                return this;
            }
            // Convert a javascript array to a java array
            if( hint.isArray() ) {
                hint = hint.getComponentType();
                if( hint.isPrimitive() ) {
                    if (hint.equals(int.class)){
                        return getIntegers();
                    }
                    if (hint.equals(double.class)){
                        return getDoubles();
                    }
                    if (hint.equals(long.class)){
                        return getLongs();
                    }
                    if (hint.equals(boolean.class)){
                        return getBooleans();
                    }
                    if (hint.equals(char.class)){
                        return getChars();
                    }
                    if (hint.equals(byte.class)){
                        return getBytes();
                    }
                    if (hint.equals(short.class)){
                        return getShorts();
                    }
                    if (hint.equals(float.class)){
                        return getFloats();
                    }
                    throw new InterpretException( StringUtil.format(JScriptResources.getString("ArrayObject.UnknownType.Exception"), hint.getName()) ); //$NON-NLS-1$
                }
                return getObjects(hint);
                //throw new InterpretException("Cannot convert a javascript array to a java array of type '{0}'", hint.getName() );
            }
            if( hint.isInstance(List.class) ) {
                //if (hint.equals(Vector.class) || hint.equals(Collection.class) ||
                //hint.equals(List.class)){
                return getList();
            }
            return null;
        }
        // If nothing specified, return a list...
        return getList();
    }

    public String stringValue(){
        StringBuffer sb = new StringBuffer();
        for(int i=0; i<length; i++){
            if(i>0) {
                sb.append(',');
            }
            sb.append(__get(i).stringValue());
        }
        return sb.toString();
    }

    public boolean hasMultipleValues() {
        return true;
    }

    public IValues getValues(){
        return new ArrayValues();
    }


    private class ArrayValues implements FBSValue.IValues{
        int     size;
        int     current;
        private ArrayValues() {
            this.size=length;
            this.current=0;
        }
        public boolean hasNext() {
            return current<size;
        }
        public FBSValue next() {
            if( current<size ) {
                return __get(current++);
            }
            return null;
        }
    }


    // ====================================================================
    // Optimized internal indexed array access

    public int getArrayLength() {
        return length;
    }

    public FBSValue getArrayValue(int index) throws InterpretException {
        return get(index);
    }

    public void setArrayValue(int index, FBSValue value) throws InterpretException {
    	setAt(index,value);
    }

    public void addArrayValue(FBSValue value) throws InterpretException {
        put( length, value );
    }

    public boolean contains( FBSValue v ) {
        int count = length;
        for( int i=0; i<count; i++ ) {
            if( Utility.isEqual(v,__get(i)) ) {
                return true;
            }
        }
        return false;
    }

    private void setArrayLength( int newLength ) {    	
        if( newLength!=length ) {        	
            if( newLength>length ) {
                if( dynamicValues==null ) {
                    if( newLength>values.length ) {
                		if( (newLength-length)>GROW_THRESHOLD ) {
                			this.dynamicValues = new ArrayMap();
                			for( int i=0; i<length; i++ ) {
                				dynamicValues.put(values[i]);
                			}
                			this.values = null;
                		} else {
                        	long nl = (long)newLength+(long)Math.max(MINDELTA,(length*2)/3);
                			MapEntry[] nv = new MapEntry[(int)nl];
                			System.arraycopy(values,0,nv,0,length);
                			this.values = nv;                			
                		}
                    }
                }
            } else {
            	// Remove the values from the array...
                if( dynamicValues!=null ) {
                	// Maybe this is *not* necessary...
                	for( int i=newLength; i<length; i++ ) {
                		dynamicValues.remove(i);
                	}
                } else {
	                for( int i=newLength; i<length; i++ ) {
	                    values[i] = null;
	                }
                }
            }
            this.length = newLength;
        }
    }

    private void setAt( int idx, FBSValue v ) {
    	int oldLength = length; 
        if( idx>=length ) {
        	if(idx!=Integer.MAX_VALUE ) {
        		setArrayLength(idx+1);
        	} else {
        		setArrayLength(Integer.MAX_VALUE);
        	}
        }
        MapEntry e = __getEntry(idx,true);
        e.value = v;
    }



    // ========================================================================
    // Java Object IO.
    // ========================================================================

    public void readExternal( ObjectInput in ) throws IOException {
        super.readExternal(in);
        this.length = in.readInt();
        if( length>INITIAL_THRESHOLD ) {
			this.values = null;
			this.dynamicValues = new ArrayMap();
			for( int i=0; i<length; i++ ) {
				FBSValue v = FBSValue.readValue(in);
				if( !v.isUndefined() ) {
					dynamicValues.put(i, v, false );
				}
			}
        } else {
        	this.values = new MapEntry[length];
			this.dynamicValues = null;
        	for( int i=0; i<length; i++ ) {
        		values[i] = new MapEntry(this,i,FBSValue.readValue(in));
        	}
        }
    }

    public void writeExternal( ObjectOutput out ) throws IOException {
        super.writeExternal(out);
        out.writeInt(length);
        for( int i=0; i<length; i++ ) {
            FBSValue.writeValue(out,__get(i));
        }
    }
}
