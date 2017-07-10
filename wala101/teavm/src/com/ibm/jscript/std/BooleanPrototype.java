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

import com.ibm.jscript.InterpretException;
import com.ibm.jscript.JScriptResources;
import com.ibm.jscript.JavaScriptException;
import com.ibm.jscript.engine.IExecutionContext;
import com.ibm.jscript.types.BuiltinFunction;
import com.ibm.jscript.types.FBSObject;
import com.ibm.jscript.types.FBSString;
import com.ibm.jscript.types.FBSUndefined;
import com.ibm.jscript.types.FBSValue;
import com.ibm.jscript.types.FBSValueVector;

/**
 * Boolean object prototype.
 */
public class BooleanPrototype extends AbstractPrototype {

    private static BooleanPrototype booleanPrototype=new BooleanPrototype();

    public static BooleanPrototype getBooleanPrototype(){
        return booleanPrototype;
    }

    public BooleanPrototype(){
        int attrib=FBSObject.P_NODELETE|FBSObject.P_READONLY;
        createProperty("toString",attrib,new BooleanMethod(0)); //$NON-NLS-1$
        createProperty("valueOf",attrib,new BooleanMethod(1)); //$NON-NLS-1$
    }


    ////// Method
    class BooleanMethod extends BuiltinFunction{
        int mIndex=-1;

        public BooleanMethod(int index){
            mIndex=index;
        }

        protected String[] getCallParameters() {
            switch(mIndex) {
                case 0: //toString
                	return new String[] {"():T"}; //$NON-NLS-1$
                case 1: //valueOf
                	return new String[] {"():T"}; //$NON-NLS-1$
            }
            return super.getCallParameters();
        }


        public FBSValue call(FBSValueVector args, FBSObject _this) throws JavaScriptException {
            return FBSUndefined.undefinedValue;
        }

        public FBSValue call(IExecutionContext context, FBSValueVector args, FBSObject _this) throws JavaScriptException {
            switch(mIndex){
            
            	///////////////////////////////////////////////////////////////
            	// Standard ECMA functions
                case 0: {     // toString
                	if(_this instanceof BooleanObject ) {
                		return FBSString.get(_this.booleanValue()?"true":"false"); //$NON-NLS-1$ //$NON-NLS-2$
                	}
                	if(_this==null) {
                		return FBSString.emptyString;
                	}
                	throw new InterpretException(JScriptResources.getString("BooleanPrototype.NotAString.Exception")); //$NON-NLS-1$
                }
                case 1: {     // valueOf
                	if(_this instanceof BooleanObject ) {
                		return ((BooleanObject)_this).getFBSBoolean();
                	}
                	throw new InterpretException(JScriptResources.getString("BooleanPrototype.NotAString.Exception")); //$NON-NLS-1$
                }
            }
            throw new InterpretException(JScriptResources.getString("BooleanPrototype.BoolFuncNotFound.Exception")); //$NON-NLS-1$
        }
    }

}