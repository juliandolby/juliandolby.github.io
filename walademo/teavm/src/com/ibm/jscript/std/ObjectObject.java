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

import com.ibm.jscript.InterpretException;
import com.ibm.jscript.JavaScriptException;
import com.ibm.jscript.engine.IExecutionContext;
import com.ibm.jscript.types.FBSDefaultObject;
import com.ibm.jscript.types.FBSObject;
import com.ibm.jscript.types.FBSValue;
import com.ibm.jscript.types.FBSValueVector;

public class ObjectObject extends FBSDefaultObject{

    public ObjectObject() {
        super(ObjectPrototype.getObjectPrototype());
    }

    public FBSObject construct(FBSValueVector args)throws InterpretException{
        if(args.getCount()==1) {
            FBSValue v = args.get(0);
            if(v.isObject()) {
                return v.toFBSObject();
            }
            if(v.isString() || v.isBoolean() || v.isNumber()) {
                return v.toFBSObject();
            }
        }
        return new ObjectObject();
    }

    public boolean supportConstruct(){
        return true;
    }
    
    public FBSValue call(IExecutionContext context,FBSValueVector args,FBSObject _this) throws JavaScriptException {
        if(args.getCount()==1) {
            FBSValue v = args.get(0);
            if(!v.isNull() && !v.isUndefined() ) {
                return v.toFBSObject();
            }
        }
        return new ObjectObject();
    }
    
    public boolean supportCall() {
        return true;
    }

    public boolean equals( Object o ) {
    	return this==o;
    }

    public String getTypeAsString(){
        return "Object"; //$NON-NLS-1$
    }

    public String getName(){
        return "Object"; //$NON-NLS-1$
    }
    

    // ========================================================================
    // Java Object IO.
    // ========================================================================

    public void readExternal( ObjectInput in ) throws IOException {
        super.readExternal(in);
    }

    public void writeExternal( ObjectOutput out ) throws IOException {
        super.writeExternal(out);
    }
}