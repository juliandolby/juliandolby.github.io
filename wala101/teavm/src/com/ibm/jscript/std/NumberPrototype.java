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
import com.ibm.jscript.types.FBSNumber;
import com.ibm.jscript.types.FBSObject;
import com.ibm.jscript.types.FBSString;
import com.ibm.jscript.types.FBSUndefined;
import com.ibm.jscript.types.FBSValue;
import com.ibm.jscript.types.FBSValueVector;

/**
 * Date object prototype.
 */
public class NumberPrototype extends AbstractPrototype {

    private static NumberPrototype numberPrototype=new NumberPrototype();

    public static NumberPrototype getNumberPrototype(){
        return numberPrototype;
    }

    public NumberPrototype(){
        int attrib=FBSObject.P_NODELETE|FBSObject.P_READONLY;
        createProperty("toString",attrib,new NumberMethod(0)); //$NON-NLS-1$
        createProperty("valueOf",attrib,new NumberMethod(1)); //$NON-NLS-1$
        createProperty("toLocaleString",attrib,new NumberMethod(2)); //$NON-NLS-1$
        createProperty("toFixed",attrib,new NumberMethod(3)); //$NON-NLS-1$
        createProperty("toExponential",attrib,new NumberMethod(4)); //$NON-NLS-1$
        createProperty("toPrecision",attrib,new NumberMethod(5)); //$NON-NLS-1$

        int attrib2=FBSObject.P_NODELETE|FBSObject.P_READONLY|FBSObject.P_NOENUM;
        createProperty("MAX_VALUE",attrib2,FBSNumber.get(NumberObject.MAX_VALUE)); //$NON-NLS-1$
        createProperty("MIN_VALUE",attrib2,FBSNumber.get(NumberObject.MIN_VALUE)); //$NON-NLS-1$
        createProperty("NaN",attrib2,FBSNumber.NaN); //$NON-NLS-1$
        createProperty("NEGATIVE_INFINITY",attrib2,FBSNumber.NEGATIVE_INFINITY); //$NON-NLS-1$
        createProperty("POSITIVE_INFINITY",attrib2,FBSNumber.POSITIVE_INFINITY); //$NON-NLS-1$
    }

    class NumberMethod extends BuiltinFunction{
        int mIndex=-1;

        public NumberMethod(int index){
            mIndex=index;
        }

        protected String[] getCallParameters() {
            switch(mIndex) {
                case 0: //toString
                	return new String[] {"():T"}; //$NON-NLS-1$
                case 1: //valueOf
                	return new String[] {"():D"}; //$NON-NLS-1$
            	case 2: // toLocaleString(int radix)
                	return new String[] {"(radix:I):T","():T"}; //$NON-NLS-1$ //$NON-NLS-2$
            	case 3: // String toFixed(int precision)
            	case 4: // String toExponential(int precision)
            	case 5: // String toPrecision(int precision)
                	return new String[] {"(precision:I):T","():T"}; //$NON-NLS-1$ //$NON-NLS-2$
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
                	if(_this instanceof NumberObject ) {
                		if(args.getCount()==0) {
                			return FBSString.get(((NumberObject)_this).toString());
                		}
                    	int radix = args.get(0).intValue();
            			return FBSString.get(((NumberObject)_this).toString(radix));
                	}
                	if(_this==null) {
                		return FBSString.emptyString;
                	}
                	throw new InterpretException(JScriptResources.getString("NumberPrototype.NotANumber.Exception")); //$NON-NLS-1$
                }
                case 1: {     // valueOf
                	if(_this instanceof NumberObject ) {
                		return ((NumberObject)_this).getFBSNumber();
                	}
                	throw new InterpretException(JScriptResources.getString("NumberPrototype.NotAString.Exception")); //$NON-NLS-1$
                }
            	case 2: {     // toLocaleString(int radix){
                	if(_this instanceof NumberObject ) {
	            		NumberObject number = (NumberObject)_this;
	                	int radix = args.isUndefined(0) ? 10 : args.get(0).intValue();
	                	return FBSString.get(number.toLocaleString(radix));
                	}
                	if(_this==null) {
                		return FBSString.emptyString;
                	}
                	throw new InterpretException(JScriptResources.getString("NumberPrototype.NotANumber.Exception")); //$NON-NLS-1$
                }
            	case 3: {     // String toFixed(int precision){
            		NumberObject number = (NumberObject)_this;
                	if(args.isUndefined(0)) {
                        return FBSString.get(number.toFixed());
                	}
                	int precision = args.get(0).intValue();
                    return FBSString.get(number.toFixed(precision));
                }
            	case 4: {     // String toExponential(int precision){
            		NumberObject number = (NumberObject)_this;
                	if(args.isUndefined(0)) {
                        return FBSString.get(number.toExponential());
                	}
                	int precision = args.get(0).intValue();
                    return FBSString.get(number.toExponential(precision));
                }
            	case 5: {     // String toPrecision(int precision){
            		NumberObject number = (NumberObject)_this;
                	if(args.isUndefined(0)) {
                        return FBSString.get(number.toPrecision());
                	}
                	int precision = args.get(0).intValue();
                    return FBSString.get(number.toPrecision(precision));
                }
            }
            throw new InterpretException(JScriptResources.getString("NumberPrototype.NumbFuncNotFound.Exception")); //$NON-NLS-1$
        }
    }

}