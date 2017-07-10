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

/**
 * 
 */
import com.ibm.jscript.InterpretException;
import com.ibm.jscript.JScriptResources;
import com.ibm.jscript.JavaScriptException;
import com.ibm.jscript.engine.IExecutionContext;
import com.ibm.jscript.engine.JSOptions;
import com.ibm.jscript.types.BuiltinFunction;
import com.ibm.jscript.types.FBSBoolean;
import com.ibm.jscript.types.FBSObject;
import com.ibm.jscript.types.FBSString;
import com.ibm.jscript.types.FBSUndefined;
import com.ibm.jscript.types.FBSValue;
import com.ibm.jscript.types.FBSValueVector;

public class ObjectPrototype extends AbstractPrototype {

    private static final ObjectPrototype objectPrototype= new ObjectPrototype();

    public static ObjectPrototype getObjectPrototype(){
        return objectPrototype;
    }

    public ObjectPrototype() {
    	super(null);
        int attrib=FBSObject.P_NODELETE|FBSObject.P_READONLY;
        createProperty("toString",attrib,new ObjectMethod(0)); //$NON-NLS-1$
        createProperty("toLocaleString",attrib,new ObjectMethod(1)); //$NON-NLS-1$
        createProperty("valueOf",attrib,new ObjectMethod(2)); //$NON-NLS-1$
        createProperty("hasOwnProperty",attrib,new ObjectMethod(3)); //$NON-NLS-1$

        if(JSOptions.get().hasObjectPrototypeExtensions()) {
            createProperty("equals",attrib,new ObjectMethod(4)); //$NON-NLS-1$
        }
    }


    class ObjectMethod extends BuiltinFunction{
        int mIndex=-1;

        public ObjectMethod(int index){
            mIndex=index;
        }

        protected String[] getCallParameters() {
            switch(mIndex) {
                case 0: //toString
                case 1: //toLocaleString
                	return new String[] {"():T"}; //$NON-NLS-1$
                case 2: //valueOf
                	return new String[] {"():T"}; //$NON-NLS-1$
                case 3: //hasOwnProperty
                	return new String[] {"(property:T):Z"}; //$NON-NLS-1$
                	
                // Extension
                case 4: //hasOwnProperty
                	return new String[] {"(object:W):Z"}; //$NON-NLS-1$
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
                    return FBSString.get("[object "+_this.getTypeAsString()+"]"); //$NON-NLS-1$ //$NON-NLS-2$
                }
                case 1: {     // toLocaleString
                    return FBSString.get("[object "+_this.getTypeAsString()+"]"); //$NON-NLS-1$ //$NON-NLS-2$
                }
                case 2: {     // valueOf
                    return _this;
                }
                case 3: {     // hasOwnProperty
                    if(args.getCount()>=1) {
                    	FBSValue v = args.get(0);
                    	return FBSBoolean.get(_this.hasProperty(v.stringValue()));
                    }
                    return FBSBoolean.FALSE;
                }

                
                ///////////////////////////////////////////////////////////////
            	// Extensions
                case 4: {     // equals
                    if(args.getCount()>=1) {
                    	FBSObject v = args.get(0).toFBSObject();
                    	return FBSBoolean.get(_this.equals(v));
                    }
                    return FBSBoolean.FALSE;
                }
            }
            throw new InterpretException(JScriptResources.getString("ObjectPrototype.StringFuncNotFound.Exception")); //$NON-NLS-1$
        }
    }
    
/////////////////////////////////////////////////////////
/// Dummy fucntions for the javaDoc.
/////////////////////////////////////////////////////////
    /**
     * Returns a string representation of the object. In general, the
     * <code>toString</code> method returns a string that
     * "textually represents" this object. The result should
     * be a concise but informative representation that is easy for a
     * person to read.
     * It is recommended that all subclasses override this method.
     * <p>
     * The <code>toString</code> method for class <code>Object</code>
     * returns a string consisting of the name of the class of which the
     * object is an instance, the at-sign character `<code>@</code>', and
     * the unsigned hexadecimal representation of the hash code of the
     * object. In other words, this method returns a string equal to the
     * value of:
     * <blockquote>
     * <pre>
     * getClass().getName() + '@' + Integer.toHexString(hashCode())
     * </pre></blockquote>
     *
     * @return  a string representation of the object.
     * @fbscript
     */
    public String toString() {
        return super.toString();
    }
}