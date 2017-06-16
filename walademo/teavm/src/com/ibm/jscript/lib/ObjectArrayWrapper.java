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

import com.ibm.jscript.InterpretException;
import com.ibm.jscript.types.FBSUndefined;
import com.ibm.jscript.types.FBSUtility;
import com.ibm.jscript.types.FBSValue;
import com.ibm.jscript.util.StringUtil;

public class ObjectArrayWrapper extends AbstractArrayWrapper {
    Object[] values;

    // wrappers must have default constructor to be instantiated thru reflection
    public ObjectArrayWrapper(){
        this(null);
    }

    public ObjectArrayWrapper(Object[] o) {
        if (o==null){
            this.values=new Object[0];
        } else {
            this.values=o;
        }
    }

    public int getArrayLength() {
        return values.length;
    }

    public Object getJavaObject(){
        return values;
    }

    public void put(int index , FBSValue value){
        if (index>=0 && index<values.length){
            try {
                values[index]=value.toJavaObject(null);
            } catch( InterpretException e ) {}
        }
    }

    public FBSValue get(int index){
        if (index>=0 && index<values.length){
            try {
                return FBSUtility.wrapAsObjectWithNull(values[index]);
            } catch( InterpretException e ) {}
        }
        return FBSUndefined.undefinedValue;
    }

    public boolean isJavaAssignableTo(Class c) {
        if(values!=null) {
            return c.isAssignableFrom(values.getClass());
        }
        if( c.isAssignableFrom(Object[].class) ) {
            return true;
        }
        return false;
    }

    public Object toJavaObject(Class _class) throws InterpretException{
        if (_class==null || (values!=null && _class.isAssignableFrom(values.getClass())) ) {
            return values;
        }
        throw new InterpretException(StringUtil.format("Can't return java object of type '{0}'",_class.getName()));
    }
}
