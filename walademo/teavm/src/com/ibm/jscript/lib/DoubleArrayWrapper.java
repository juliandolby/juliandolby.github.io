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

public class DoubleArrayWrapper extends AbstractArrayWrapper {

    double[] values;

    // wrappers must have default constructor to be instantiated thru reflection
    public DoubleArrayWrapper(){
        this(null);
    }

    public DoubleArrayWrapper(double[] o) {
        if (o==null){
            this.values=new double[0];
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
            values[index]=value.doubleValue();
        }
    }

    public FBSValue get(int index){
        if (index>=0 && index<values.length){
            return FBSUtility.wrap(values[index]);
        }
        return FBSUndefined.undefinedValue;
    }

    public boolean isJavaAssignableTo(Class c) {
        if( c.isAssignableFrom(double[].class) ) {
            return true;
        }
        return false;
    }

    public Object toJavaObject(Class _class) throws InterpretException{
        if (_class==null || _class.isAssignableFrom(double[].class) ) {
            return values;
        }
        throw new InterpretException(StringUtil.format("Can't return java object of type '{0}'",_class.getName()));
    }
}