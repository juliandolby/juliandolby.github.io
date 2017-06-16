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
import com.ibm.jscript.JavaScriptException;
import com.ibm.jscript.engine.IExecutionContext;
import com.ibm.jscript.engine.JSOptions;
import com.ibm.jscript.types.BuiltinFunction;
import com.ibm.jscript.types.FBSNull;
import com.ibm.jscript.types.FBSObject;
import com.ibm.jscript.types.FBSUndefined;
import com.ibm.jscript.types.FBSValue;
import com.ibm.jscript.types.FBSValueVector;


/**
 * This class allows to manipulate standard JavaScript Array objects.
 * @fbscript Array
 */
public class RegExpPrototype extends AbstractPrototype {

    private static RegExpPrototype regExpPrototype=new RegExpPrototype();

    public static RegExpPrototype getRegExpPrototype(){
        return regExpPrototype;
    }

    public RegExpPrototype(){
    	JSOptions options = JSOptions.get();
    	switch(options.getRegExpMode()) {
    	case JSOptions.REGEXP_JDK_14:
    	case JSOptions.REGEXP_JAKARTA_REGEXP:
    		int attrib = FBSObject.P_NODELETE | FBSObject.P_READONLY | FBSObject.P_NOENUM;
			createProperty("toString", attrib, new JavaRegExpMethod(0)); //$NON-NLS-1$
			createProperty("valueOf", attrib, new JavaRegExpMethod(1)); //$NON-NLS-1$
			createProperty("exec", attrib, new JavaRegExpMethod(2)); //$NON-NLS-1$
			createProperty("match", attrib, new JavaRegExpMethod(3)); //$NON-NLS-1$
			createProperty("split", attrib, new JavaRegExpMethod(4)); //$NON-NLS-1$
			createProperty("search", attrib, new JavaRegExpMethod(5)); //$NON-NLS-1$
			createProperty("replace", attrib, new JavaRegExpMethod(6)); //$NON-NLS-1$
			createProperty("compile", attrib, new JavaRegExpMethod(7)); //$NON-NLS-1$
			createProperty("test", attrib, new JavaRegExpMethod(8)); //$NON-NLS-1$
			break;
    	}
    }


    private class JavaRegExpMethod extends BuiltinFunction{
        int mIndex=-1;

        public JavaRegExpMethod(int index){
            mIndex=index;
        }

        protected String[] getCallParameters() {
            switch(mIndex) {
            case 0: //toString
            case 1: //valueOf
            	return new String[] {"():T"}; //$NON-NLS-1$
            case 4: //split
            	return new String[] {"(separator:Tlimit:I):T","(separator:Rlimit:I):T"}; //$NON-NLS-1$ //$NON-NLS-2$
            case 2: //exec
            case 3: //match
            case 5: //search
            case 8: //test
            	return new String[] {"(string:T):T"}; //$NON-NLS-1$
            case 6: //replace
            	return new String[] {"(regExp:RrepStr:T):T", "(regExp:TrepStr:T):T"}; //$NON-NLS-1$ //$NON-NLS-2$
            case 7: //compile
            	return new String[] {"(string:string:T):T"}; //$NON-NLS-1$ //$NON-NLS-2$
            }
            return super.getCallParameters();
        }
        
        public FBSValue call(FBSValueVector args, FBSObject _this) throws JavaScriptException {
            return FBSUndefined.undefinedValue;
        }

        public FBSValue call(IExecutionContext context, FBSValueVector args, FBSObject _this) throws JavaScriptException {

        	if(!(_this instanceof RegExpObject)) {
            	throw new InterpretException("TypeError: Object is not a RegExpObject");
        	}
        	
        	RegExpObject re = (RegExpObject)_this;
        	
            switch(mIndex){
            
            	///////////////////////////////////////////////////////////////
            	// Standard ECMA functions
                case 0:       // toString
                case 1:       // valueOf
                	return re.getFBSString();

                case 2:		  // exec
                case 3:		  // match
                case 4:       // split
                case 5:  	  // search
                case 6:		  // replace
                case 7:		  // compile
                case 8:		  // test
            		String jstring = ""; //$NON-NLS-1$
                    if (args.size()>=1)
                    	jstring = args.get(0).stringValue();
                    if (mIndex == 2 || mIndex == 3) {
						FBSValueVector v = re.exec(jstring);
						if (v == null)
							return FBSNull.nullValue;
						else
                    	return new ArrayObject(re.exec(jstring));
                    }
                    else if (mIndex == 8)
                		return re.test(jstring);
                    else if (mIndex == 4)
                    	return new ArrayObject(re.split(jstring));
                    else if (mIndex == 5)
                    	return re.search(jstring);
                    else if (mIndex == 6)
                    	return re.replace(args.get(0).stringValue(), args.get(1).stringValue());
                    else if (mIndex == 7) {
                    	return re.compile(args.get(0).stringValue(), args.get(1).stringValue());
                    }
            }
            throw new InterpretException("RegExp function not found: index=" + mIndex);
        }
    }
}
