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

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.ibm.jscript.JavaScriptSourceInfo;
import com.ibm.jscript.std.ArrayObject;
import com.ibm.jscript.std.ArrayPrototype;
import com.ibm.jscript.std.BooleanPrototype;
import com.ibm.jscript.std.DateObject;
import com.ibm.jscript.std.DatePrototype;
import com.ibm.jscript.std.NumberPrototype;
import com.ibm.jscript.std.RegExpObject;
import com.ibm.jscript.std.RegExpPrototype;
import com.ibm.jscript.std.StringPrototype;
import com.ibm.jscript.util.StringUtil;

/**
 * Definition of an advanced javascript type.
 */
public abstract class FBSType {

    /**
     * Symbol callback interface.
     * That interface is used by the compiler to ensure that the types are correct
     * and by the code editor, to provide an auto-completion function.
     */
    public interface ISymbolCallback {
        public Descriptor.Member[] getAllSymbols();
        public FBSType  getSymbolType(String name);
    }

    public static class DefaultSymbolCallback implements ISymbolCallback {

        private FBSObject[] globalObjects;
        private ISymbolCallback delegate;

        public DefaultSymbolCallback( FBSObject[] globalObjects, ISymbolCallback delegate ) {
            this.globalObjects = globalObjects;
            this.delegate = delegate;
        }

        public DefaultSymbolCallback( FBSObject[] globalObjects ) {
            this(globalObjects,null);
        }

        public DefaultSymbolCallback( FBSObject globalObject ) {
            this(new FBSObject[]{globalObject});
        }

        public Descriptor.Member[] getAllSymbols() {
        	Set members = new HashSet();
            if(globalObjects!=null) {
                for( int i=0; i<globalObjects.length; i++ ) {
                    FBSObject obj = globalObjects[i];
                    obj.getAllPropertiesForHelp(members);
                }
            }
            if( delegate!=null ) {
            	Descriptor.Member[] d =  delegate.getAllSymbols();
            	if(d!=null) {
            		for( int i=0; i<d.length; i++ ) {
            			members.add(d[i]);
            		}
            	}
            }        	
            return (Descriptor.Member[])members.toArray(new Descriptor.Member[members.size()]);
        }

        public FBSType getSymbolType( String name ) {
            if(globalObjects!=null) {
                for( int i=0; i<globalObjects.length; i++ ) {
                    FBSObject obj = globalObjects[i];
                    if( obj.hasProperty(name) ) {
                        try {
                            return FBSType.getTypeFromValue(obj.get(name));
                        } catch( Exception ex ) {}
                        return FBSType.invalidType;
                    }
                }
            }
            if( delegate!=null ) {
                return delegate.getSymbolType(name);
            }
            return FBSType.invalidType;
        }
    }

    // Published types
    public static final FBSType undefinedType   = new JSUndefinedType();
    public static final FBSType invalidType     = new JSInvalidType();
    public static final FBSType multipleType    = new JSMultipleType();

    public static final FBSType voidType        = new JSVoidType();
    public static final FBSType booleanType     = new JSBooleanType();

    public static final FBSType charType        = new JavaCharType();
    public static final FBSType stringType      = new JavaStringType();

    public static final FBSType byteType        = new JavaByteType();
    public static final FBSType shortType       = new JavaShortType();
    public static final FBSType intType         = new JavaIntType();
    public static final FBSType longType        = new JavaLongType();
    public static final FBSType floatType       = new JavaFloatType();
    public static final FBSType doubleType      = new JavaDoubleType();
    public static final FBSType numberType      = doubleType;

    public static final FBSType dateType        = new JSDateType();
    public static final FBSType regExpType      = new JSRegExpType();
    public static final FBSType jsArrayType     = new JSArrayType();
    public static final FBSType jsObjectType    = new JSObjectType(null);

    public static FBSType getJavaArrayType( Class type ) {
    	if(type!=null) {
    		return new JavaArrayType(type);
    	}
    	return undefinedType;
    }
    
    public static final FBSType getJavaScriptType(FBSValue value) {
//        if( value!=null ) {
//            if( value instanceof BuiltinFunction ) {
//                return new JSFunction((BuiltinFunction)value);
//            }
//        }
        return value!=null ? value.getFBSType() : undefinedType;
    }

    public static FBSType getJavaPackage(String packageName) {
        // For now....
        return undefinedType;
    }


    // 2 methods maintained for compatibility...
    public static final FBSType getType(Class javaClass) {
        return getTypeFromJavaClass(javaClass);
    }
    public static final FBSType getType(String name) {
        return getTypeFromJSName(name);
    }
    public static final FBSType getTypeFromValue(Object object) {
        // Check the null value
        if( object==null ) {
            return FBSType.undefinedType;
        }

        // Check if it is an FBSValue
        if( object instanceof FBSValue ) {
            FBSValue v = (FBSValue)object;
            return v.getFBSType();
        }

        // Return the corresponding java object
        return getType(object.getClass());
    }



    public static final FBSType getTypeFromJavaClass(Class javaClass) {
        // Check the java primitive
        if( javaClass.isPrimitive() ) {
            if( javaClass==Character.TYPE ) {
                return stringType;
            } else if( javaClass==Byte.TYPE ) {
                return numberType;
            } else if( javaClass==Short.TYPE ) {
                return numberType;
            } else if( javaClass==Integer.TYPE ) {
                return numberType;
            } else if( javaClass==Long.TYPE ) {
                return numberType;
            } else if( javaClass==Float.TYPE ) {
                return numberType;
            } else if( javaClass==Double.TYPE ) {
                return numberType;
            } else if( javaClass==Boolean.TYPE ) {
                return booleanType;
            } else if (javaClass==Void.TYPE ) {
            	return voidType;
            }
            
            throw new RuntimeException( StringUtil.format("Unknown java primitive type {0}", javaClass) );
        }
        // Check some predefined objects
        if( javaClass==Character.class ) {
            return stringType;
        } else if( javaClass==Byte.class ) {
            return numberType;
        } else if( javaClass==Short.class ) {
            return numberType;
        } else if( javaClass==Integer.class ) {
            return numberType;
        } else if( javaClass==Long.class ) {
            return numberType;
        } else if( javaClass==Float.class ) {
            return numberType;
        } else if( javaClass==Double.class ) {
            return numberType;
        } else if( javaClass==Boolean.class ) {
            return booleanType;
        } else if (javaClass==Void.class ) {
        	return voidType;
        } else if( javaClass==String.class ) {
            return stringType;
        } else if( java.util.Date.class.isAssignableFrom(javaClass) ) {
            return dateType;
        }
        // Check std wrappers
        if( javaClass==com.ibm.jscript.std.ArrayObject.class ) {
        	return jsArrayType;
        } else if( javaClass==com.ibm.jscript.std.NumberObject.class ) {
        	return numberType;
        } else if( javaClass==com.ibm.jscript.std.StringObject.class ) {
        	return stringType;
        } else if( javaClass==com.ibm.jscript.std.BooleanObject.class ) {
        	return booleanType;
        } else if( javaClass==com.ibm.jscript.std.DateObject.class ) {
        	return dateType;
        }
        // Check if this object has a wrapper
        Class wrapper = FBSUtility.searchWrapper(javaClass);
        if( wrapper!=null ) {
            try {
                GeneratedWrapperObject jo= (GeneratedWrapperObject)wrapper.newInstance();
                return new JSWrappedJavaObject(jo);
            } catch( Exception ex ) {}
            return undefinedType;
        }

        // Else it is a standard java class...
        return new JavaClassType(javaClass);
    }

    public static final FBSType getTypeFromJSName(String name) {
        // Check for a java wrapper
    	if( name.startsWith("com.ibm.script.std.") ) {
	        if( name.equals("com.ibm.script.std.ArrayObject" ) ) {
	        	name = "Array";
	        } else if( name.equals("com.ibm.script.std.NumberObject" ) ) {
	        	name = "number";
	        } else if( name.equals("com.ibm.script.std.StringObject" ) ) {
	        	name = "string";
	        } else if( name.equals("com.ibm.script.std.BooleanObject" ) ) {
	        	name = "boolean";
	        } else if( name.equals("com.ibm.script.std.FunctionObject" ) ) {
	        	name = "Function";
	        } else if( name.equals("com.ibm.script.std.ObjectObject" ) ) {
	        	name = "Object";
	        } else if( name.equals("com.ibm.script.std.RegExpObject" ) ) {
	        	name = "RegExp";
	        } else if( name.equals("com.ibm.script.std.DateObject" ) ) {
	        	name = "Date";
	        }
    	}
        
        // Check for a basic type
        switch(name.charAt(0)) {
            case 'A': {
                if( name.equals("Array") ) {
                    return jsArrayType;
                }
            } break;
            case 'b': {
                if( name.equals("boolean") ) {
                    return booleanType;
                }
                if( name.equals("byte") ) {
                    return byteType;
                }
            } break;
            case 'c': {
                if( name.equals("char") ) {
                    return charType;
                }
            } break;
            case 'd': {
                if( name.equals("double") ) {
                    return doubleType;
                }
            } break;
            case 'D': {
                if( name.equals("Date") ) {
                    return dateType;
                }
            } break;
            case 'f': {
                if( name.equals("float") ) {
                    return floatType;
                }
            } break;
            case 'i': {
                if( name.equals("int") ) {
                    return intType;
                }
            } break;
            case 'l': {
                if( name.equals("long") ) {
                    return longType;
                }
            } break;
            case 'n': {
                if( name.equals("number") ) {
                    return numberType;
                }
            } break;
            case 'O': {
                if( name.equals("Object") ) {
                    return jsObjectType;
                }
            } break;
            case 'R': {
                if( name.equals("RegExp") ) {
                    return regExpType;
                }
            } break;
            case 's': {
                if( name.equals("string") ) {
                    return stringType;
                }
                if( name.equals("short") ) {
                    return shortType;
                }
            } break;
        }

        // Check for a java array
        // It ends with '[]'
        if( name.endsWith("[]") ) {
    		FBSType arrayType = getTypeFromJSName(name.substring(0,name.length()-2));
    		//TODO: check if it can be a java array
    		return new JavaArrayType(arrayType.getJavaClass());
        }

        // If it contains a 'dot', then it is a java class (with a package)
        // Else, it is an alias for script
        if( name.indexOf('.')>0 ) {
            // Not implemented for now....
            try {
                Class c= Class.forName(name);
//            	com.ibm.workplace.designer.Application app = com.ibm.workplace.designer.Application.get();
//                Class c= app!=null ? app.loadClass(name) : Class.forName(name);
                return getType(c);
            } catch( Exception e ) {
                com.ibm.jscript.util.TDiag.exception(e);
            }
            return undefinedType;
        }

        // Else, try a registered class
        try {
            FBSValue v = Registry.getRegistryObject().get(name);
            if( v instanceof GeneratedWrapperObject.Constructor ) {
                return new JSWrappedJavaClass((GeneratedWrapperObject.Constructor)v);
            }
        } catch( Exception e ) {
            com.ibm.jscript.util.TDiag.exception(e);
        }

        // Undefined type
        return undefinedType;
    }


    // ========================================================================
    // *FBSTYPE* IMPLEMENTATION
    // ========================================================================

    private FBSType() {
    }

    public String toString() {
        return getJSName();
    }

    public final int getType() {return 0; }
    public abstract boolean equals(Object otherType);
    public abstract String getJSName();
    public abstract String getDescString();

    public boolean isPrimitive() {return false;}
    public boolean isUndefined() { return false; }
    public boolean isInvalid() { return false; }
    public boolean isNumber() { return false; }
    public boolean isString() { return false; }
    public boolean isBoolean() { return false; }
    public boolean isDate() { return false; }
    public boolean isRegExp() { return false; }
    public boolean isVoid() { return false; }
    public boolean isMultiple() { return false; }
    public boolean isJSArray() { return false; }
    public boolean isJSObject() { return false; }
    public boolean isJavaObject() { return false; }
    public boolean isMethod() { return false; }

    public boolean isAdvanced() { return false; }

    public final FBSType getMember(String name) {
        return getMemberType(name);
    }

    public final FBSType getCall(FBSType[] parameters) {
        return getCallType(parameters);
    }

    public final FBSType getNew(FBSType[] parameters) {
        return getNewType(parameters);
    }

    public final Descriptor.Member[] getMembers() {
        return getTypeMembers();
    }

    public abstract boolean isAssignableFrom(FBSType t);

    public Class getJavaClass() {
        return null;
    }


    // =======================================================================
    // INTROSPECTION HELPERS
    // =======================================================================

    // Get the type of a field
    protected abstract FBSType getMemberType( String name );

    // Get the type of a specific method
    protected abstract FBSType getCallType( FBSType[] parameters );

    // Get the type when a new is done
    protected abstract FBSType getNewType( FBSType[] parameters );

    protected abstract Descriptor.Member[] getTypeMembers();


    // =======================================================================
    // Utility methods
    // =======================================================================

    private static boolean match( Descriptor.IMethodParameters methodParameters, FBSType[] callParameters ) {
        int nParams = callParameters!=null ? callParameters.length : 0;
        int ptr1=0; int ptr2=0;
        while( true ) {
            if( ptr1>=methodParameters.getParameterCount() ) {
                return ptr2>=nParams;
            }
            if( ptr2>=nParams ) {
                if( ptr1<methodParameters.getParameterCount() ) {
                    return methodParameters.getParameterType(ptr1).isMultiple();
                }
                return false;
            }
            FBSType t1 = methodParameters.getParameterType(ptr1++);
            FBSType t2 = callParameters[ptr2++];
            if( !t1.isAssignableFrom(t2) ) {
                return false;
            }
        }
    }

    // =======================================================================
    // JAVASCRIPT GLOBAL OBJECTS
    // =======================================================================

    public static class JSSymbolCallback extends FBSType {
        private ISymbolCallback    symbolCallback;
        public JSSymbolCallback( ISymbolCallback symbolCallback ) {
            this.symbolCallback = symbolCallback;
        }
        public String getJSName() {
            return "SymbolCallback";
        }
        public String getDescString() {
            return "???";
        }
        public boolean equals(Object otherType) {
            if( otherType instanceof JSSymbolCallback ) {
                JSSymbolCallback jf = (JSSymbolCallback)otherType;
                return jf.symbolCallback==symbolCallback;
            }
            return false;
        }
        protected FBSType getMemberType( String name ) {
            return symbolCallback.getSymbolType(name);
        }
        protected FBSType getNewType( FBSType[] parameters ) {
            // Instantiation not available
            return invalidType;
        }
        protected FBSType getCallType( FBSType[] parameters ) {
            // Function call not available
            return invalidType;
        }
        protected Descriptor.Member[] getTypeMembers() {
            return symbolCallback.getAllSymbols();
        }
        public boolean isAssignableFrom(FBSType t) {
        	return false; //Not applicable
        }
    }

    // Simulate the type for new operator accessing 
    public static class JSNewOperator extends FBSType {
        public JSNewOperator() {
        }
        public String getJSName() {
            return "JSNew";
        }
        public String getDescString() {
            return "???";
        }
        public boolean equals(Object otherType) {
            if( otherType instanceof ISymbolCallback ) {
                return true;
            }
            return false;
        }
        protected FBSType getMemberType( String name ) {
            return invalidType;
        }
        protected FBSType getNewType( FBSType[] parameters ) {
            // Instantiation not available
            return invalidType;
        }
        protected FBSType getCallType( FBSType[] parameters ) {
            // Function call not available
            return invalidType;
        }
        public boolean isAssignableFrom(FBSType t) {
        	return false; //Not applicable
        }
        
        protected Descriptor.Member[] getTypeMembers() {
        	// Browse the internal registry
        	FBSDefaultObject o = Registry.getRegistryObject();
        	return o.getMembers(new FBSObject.IMemberFilter() {
        		public boolean accept( Descriptor.Member member ) {
        			return member instanceof Descriptor.Constructor; 
        		}
        	});
        }
    }

    
    // =======================================================================
    // JAVASCRIPT BUILTIN FUNCTION
    // =======================================================================

    public static class JSBuiltinFunction extends FBSType {
        private BuiltinFunction jsFunction;
        public JSBuiltinFunction(BuiltinFunction jsFunction) {
            this.jsFunction = jsFunction;
        }
        public String getJSName() {
            return "function";
        }
        public String getDescString() {
            return "???";
        }
        public String[] getCallParameters(){
            return jsFunction.getCallParameters();
        }
        public boolean equals(Object otherType) {
            if( otherType instanceof JSBuiltinFunction ) {
            	JSBuiltinFunction jf = (JSBuiltinFunction)otherType;
                return jf.jsFunction==jsFunction;
            }
            return false;
        }
        protected FBSType getMemberType( String name ) {
            // Function does not have a member
            return FBSType.invalidType;
        }
        protected FBSType getNewType( FBSType[] parameters ) {
            // Instantiation not available
            return invalidType;
        }
        protected FBSType getCallType( FBSType[] parameters ) {
            // PHIL: we should get the correct method, depending on the parameters.
            FBSType type = null;
            String[] callParameters = jsFunction.getCallParameters();
            if( callParameters!=null ) {
                //addFunction(FCT_ERROR,          "@Error","():LObject;");
                for( int i=0; i<callParameters.length; i++ ) {
                    String s = callParameters[i];
                    String t = s.substring(s.indexOf(')')+2); // skip ':'
                    FBSType newtype = Descriptor.getFBSType(t);
                    if( type!=null && type!=newtype ) {
                        type = FBSType.undefinedType;
                        break;
                    }
                    type = newtype;
                }
            }
            return type!=null ? type : FBSType.undefinedType;
        }
        protected Descriptor.Member[] getTypeMembers() {
        	return null;
        }
        public boolean isAssignableFrom(FBSType t) {
        	return false; //Not applicable
        }
    }

    // Function that is declared inside a script file
    public static class JSScriptFunction extends FBSType {
    	private JavaScriptSourceInfo.JSFunction jsFunction;
        public JSScriptFunction(JavaScriptSourceInfo.JSFunction jsFunction) {
        	this.jsFunction = jsFunction;
        }
        public String getJSName() {
            return "function";
        }
        public String getDescString() {
            return "???";
        }
        public boolean equals(Object otherType) {
            if( otherType instanceof JSScriptFunction ) {
            	JSScriptFunction jf = (JSScriptFunction)otherType;
                //return jf.jsFunction==jsFunction;
                return true;
            }
            return false;
        }
        protected FBSType getMemberType( String name ) {
            // Function does not have a member
            return FBSType.invalidType;
        }
        protected FBSType getNewType( FBSType[] parameters ) {
            // Instantiation not available
            return invalidType;
        }
        protected FBSType getCallType( FBSType[] parameters ) {
            if( match(jsFunction,parameters) ) {
                return jsFunction.getReturnType();
            }
            return FBSType.undefinedType;
        }
        protected Descriptor.Member[] getTypeMembers() {
        	return null;
        }
        public boolean isAssignableFrom(FBSType t) {
        	return false; //Not applicable
        }
    }

    
    // =======================================================================
    // STANDARD JAVASCRIPT TYPES IMPLEMENTATION
    // =======================================================================

    private abstract static class JSPrimitiveType extends FBSType {
        private JSPrimitiveType() {
        }
        public boolean isPrimitive() {
        	return true;
        }
        public boolean equals(Object otherType) {
            // All the primitive types are instanciated only once
            // As a consequence, testing the object tests the type
            return otherType==this;
        }
        protected FBSType getMemberType( String name ) {
            return invalidType;
        }
        protected FBSType getCallType( FBSType[] parameters ) {
            // Call not available
            return invalidType;
        }
        protected FBSType getNewType( FBSType[] parameters ) {
            // Instantiation not available
            return invalidType;
        }
        protected Descriptor.Member[] getTypeMembers() {
        	return null;
        }
    }
    private static class JSMultipleType extends JSPrimitiveType {
        private JSMultipleType() {
        }
        public boolean isMultiple() {
            return true;
        }
        public String getJSName() {
            return "...";
        }
        public String getDescString() {
            return "N";
        }
        public String toString() {
            return "...";
        }
        public boolean isAssignableFrom(FBSType t) {
        	return true;
        }
    }
    private static class JSUndefinedType extends JSPrimitiveType {
        private JSUndefinedType() {
        }
        public boolean isUndefined() {
            return true;
        }
        public String getJSName() {
            return "any";
        }
        public String getDescString() {
            return "W";
        }
        public String toString() {
            return "any";
        }
        public boolean isAssignableFrom(FBSType t) {
        	return false; //Not applicable
        }
    }
    private static class JSInvalidType extends JSPrimitiveType {
        private JSInvalidType() {
        }
        public boolean isInvalid() {
            return true;
        }
        public String getJSName() {
            return "invalid";
        }
        public String getDescString() {
            return "???";
        }
        public String toString() {
            return "invalid";
        }
        public boolean isAssignableFrom(FBSType t) {
        	return false; //Not applicable
        }
    }
    private static class JSVoidType extends JSPrimitiveType {
        private JSVoidType() {
        }
        public boolean isVoid() {
            return true;
        }
        public String getJSName() {
            return "void";
        }
        public String getDescString() {
            return "V";
        }
        public String toString() {
            return "void";
        }
        public boolean isAssignableFrom(FBSType t) {
        	return false; //Not applicable
        }
    }
    private static class JSBooleanType extends JSPrimitiveType {
        private JSBooleanType() {
        }
        public boolean isBoolean() {
            return true;
        }
        public String getJSName() {
            return "boolean";
        }
        public String getDescString() {
            return "Z";
        }
        public String toString() {
            return "boolean";
        }
        protected FBSType getMemberType( String name ) {
        	try {
        		return BooleanPrototype.getBooleanPrototype().getPropertyFBSType(name);
        	} catch(Exception ex) {}
            return undefinedType;
        }
        protected Descriptor.Member[] getTypeMembers() {
        	return BooleanPrototype.getBooleanPrototype().getMembers();
        }
        public boolean isAssignableFrom(FBSType t) {
        	return t instanceof JSBooleanType;
        }
    }

    //// STRINGS //////////////////////////////////////////////////////////////
    private abstract static class JSStringType extends JSPrimitiveType {
        private JSStringType() {
        }
        public boolean isString() {
            return true;
        }
        public FBSType getMemberType( String name ) {
            if( StringPrototype.getStringPrototype().hasProperty(name) ) {
                try {
                    return FBSType.getTypeFromValue(StringPrototype.getStringPrototype().get(name));
                } catch(Exception e) {}
            }
            return undefinedType;
        }
        protected Descriptor.Member[] getTypeMembers() {
        	return StringPrototype.getStringPrototype().getMembers(new FBSObject.IMemberFilter() {
        		public boolean accept( Descriptor.Member member ) {
        			return !(member instanceof Descriptor.Constructor); 
        		}
        	});
        }
        public final boolean isAssignableFrom(FBSType t) {
        	return t instanceof JSStringType;
        }
    }
    private static class JavaCharType extends JSStringType {
        private JavaCharType() {
        }
        public String getJSName() {
            return "char";
        }
        public String getDescString() {
            return "C";
        }
        public String toString() {
            return "char";
        }
        protected FBSType getNewType( FBSType[] parameters ) {
            return charType;
        }
        public Class getJavaClass() {
        	return Character.TYPE;
        }        
    }
    private static class JavaStringType extends JSStringType {
        private JavaStringType() {
        }
        public String getJSName() {
            return "string";
        }
        public String getDescString() {
            return "T";
        }
        public String toString() {
            return "string";
        }
        protected FBSType getNewType( FBSType[] parameters ) {
            return stringType;
        }
        public Class getJavaClass() {
        	return String.class;
        }        
    }

    //// NUMBERS //////////////////////////////////////////////////////////////
    private static abstract class JSNumberType extends JSPrimitiveType {
        private JSNumberType() {
        }
        public boolean isNumber() {
            return true;
        }
        protected FBSType getMemberType( String name ) {
        	try {
        		return NumberPrototype.getNumberPrototype().getPropertyFBSType(name);
        	} catch(Exception ex) {}
            return undefinedType;
        }
        protected Descriptor.Member[] getTypeMembers() {
        	return NumberPrototype.getNumberPrototype().getMembers();
        }
        public final boolean isAssignableFrom(FBSType t) {
        	return t instanceof JSNumberType;
        }
    }
    private static class JavaByteType extends JSNumberType {
        private JavaByteType() {
        }
        public String getJSName() {
            return "byte";
        }
        public String getDescString() {
            return "B";
        }
        public String toString() {
            return "byte";
        }
        protected FBSType getNewType( FBSType[] parameters ) {
            return byteType;
        }
        public Class getJavaClass() {
        	return Byte.TYPE;
        }        
    }
    private static class JavaShortType extends JSNumberType {
        private JavaShortType() {
        }
        public String getJSName() {
            return "short";
        }
        public String getDescString() {
            return "S";
        }
        public String toString() {
            return "short";
        }
        protected FBSType getNewType( FBSType[] parameters ) {
            return shortType;
        }
        public Class getJavaClass() {
        	return Short.TYPE;
        }        
    }
    private static class JavaIntType extends JSNumberType {
        private JavaIntType() {
        }
        public String getJSName() {
            return "int";
        }
        public String getDescString() {
            return "I";
        }
        public String toString() {
            return "int";
        }
        protected FBSType getNewType( FBSType[] parameters ) {
            return intType;
        }
        public Class getJavaClass() {
        	return Integer.TYPE;
        }        
    }
    private static class JavaLongType extends JSNumberType {
        private JavaLongType() {
        }
        public String getJSName() {
            return "long";
        }
        public String getDescString() {
            return "J";
        }
        public String toString() {
            return "long";
        }
        protected FBSType getNewType( FBSType[] parameters ) {
            return longType;
        }
        public Class getJavaClass() {
        	return Long.TYPE;
        }        
    }
    private static class JavaFloatType extends JSNumberType {
        private JavaFloatType() {
        }
        public String getJSName() {
            return "float";
        }
        public String getDescString() {
            return "F";
        }
        public String toString() {
            return "float";
        }
        protected FBSType getNewType( FBSType[] parameters ) {
            return floatType;
        }
        public Class getJavaClass() {
        	return Float.TYPE;
        }        
    }
    private static class JavaDoubleType extends JSNumberType {
        private JavaDoubleType() {
        }
        public String getJSName() {
            return "double";
        }
        public String getDescString() {
            return "D";
        }
        public String toString() {
            return "double";
        }
        protected FBSType getNewType( FBSType[] parameters ) {
            return doubleType;
        }
        public Class getJavaClass() {
        	return Double.TYPE;
        }        
    }

    private static abstract class JSAbstractObjectType extends FBSType {
        private JSAbstractObjectType() {
        }
    }

    public static class JSObjectType extends JSAbstractObjectType {
    	FBSObject object;
        public JSObjectType(FBSObject object) {
            this.object = object;
        }
        public boolean isJSObject() {
            return true;
        }
        public String getJSName() {
            return "Object";
        }
        public String getDescString() {
            return "???";
        }
        public boolean equals(Object otherType) {
            if( otherType instanceof JSObjectType ) {
                JSObjectType ct = (JSObjectType)otherType;
                return ct.object==object;
            }
            return false;
        }
        public FBSType getMemberType( String name ) {
            if( object!=null && object.hasProperty(name) ) {
                try {
                    return FBSType.getTypeFromValue(object.get(name));
                } catch(Exception e) {}
            }
            return undefinedType;
        }
        protected Descriptor.Member[] getTypeMembers() {
            if( object!=null ) {
            	return object.getMembers(new FBSObject.IMemberFilter() {
            		public boolean accept( Descriptor.Member member ) {
            			return !(member instanceof Descriptor.Constructor); 
            		}
            	});
            }
            return null;
        }
        public FBSType getCallType( FBSType[] parameters ) {
            return invalidType;
        }
        protected FBSType getNewType( FBSType[] parameters ) {
            return invalidType;
        }
        public final boolean isAssignableFrom(FBSType t) {
        	return t instanceof JSObjectType;
        }
    }

    // Should be moved to a efaultObject with a prototype
    private static class JSDateType extends JSPrimitiveType {
        private JSDateType() {
        }
        public boolean isDate() {
            return true;
        }
        public boolean equals(Object otherType) {
            return otherType instanceof JSDateType;
        }
        public String getJSName() {
            return "Date";
        }
        public String getDescString() {
            return "Y";
        }
        public String toString() {
            return "Date";
        }
        public Class getJavaClass() {
        	return DateObject.class;
        }        
        public FBSType getMemberType( String name ) {
            if( DatePrototype.getDatePrototype().hasProperty(name) ) {
                try {
                    return FBSType.getTypeFromValue(DatePrototype.getDatePrototype().get(name));
                } catch(Exception e) {}
            }
            return undefinedType;
        }
        protected Descriptor.Member[] getTypeMembers() {
        	return DatePrototype.getDatePrototype().getMembers(new FBSObject.IMemberFilter() {
        		public boolean accept( Descriptor.Member member ) {
        			return !(member instanceof Descriptor.Constructor); 
        		}
        	});
        }
        public final boolean isAssignableFrom(FBSType t) {
        	return t instanceof JSDateType;
        }
    }

    private static class JSRegExpType extends JSObjectType {
        private JSRegExpType() {
            super(RegExpPrototype.getRegExpPrototype());
        }
        public boolean equals(Object otherType) {
            return otherType instanceof JSRegExpType;
        }
        public boolean isRegExp() { 
        	return true; 
        }
        public String getJSName() {
            return "RegExp";
        }
        public String getDescString() {
            return "???";
        }
        public String toString() {
            return "RegExp";
        }
        public Class getJavaClass() {
        	return RegExpObject.class;
        }        
    }

    private static class JSArrayType extends JSObjectType {
        private JSArrayType() {
            super(ArrayPrototype.getFBSArrayPrototype());
        }
        public boolean isJSArray() {
            return true;
        }
        public boolean equals(Object otherType) {
            return otherType instanceof JSArrayType;
        }
        public String getJSName() {
            return "Array";
        }
        public String getDescString() {
            return "???";
        }
        public String toString() {
            return "Array";
        }
        public Class getJavaClass() {
        	return ArrayObject.class;
        }        
    }

    private static class JSClassType extends FBSType {
        String className;
        private JSClassType(String className) {
            this.className = className;
        }
        public String getJSName() {
            return "Class";
        }
        public String getDescString() {
            return "???";
        }
        public boolean equals(Object otherType) {
            if( otherType instanceof JSClassType ) {
                JSClassType ct = (JSClassType)otherType;
                return ct.className==className;
            }
            return false;
        }
        public FBSType getMemberType( String name ) {
            return undefinedType;
        }
        public FBSType getCallType( FBSType[] parameters ) {
            return invalidType;
        }
        protected FBSType getNewType( FBSType[] parameters ) {
            if( className.equals("Object") ) {
                return new JSObjectType(null);
            }
            if( className.equals("Array") ) {
                return new JSArrayType();
            }
            if( className.equals("Date") ) {
                return new JSDateType();
            }
            return invalidType;
        }
        protected Descriptor.Member[] getTypeMembers() {
            return null;
        }
        public final boolean isAssignableFrom(FBSType t) {
        	return false; // Not applicable
        }
    }



    // =======================================================================
    // JAVA CLASSES AND OBJECTS IMPLEMENTATION
    // =======================================================================

    private static abstract class JSJavaClassType extends FBSType {
        private JSJavaClassType() {
        }
        public final boolean isAssignableFrom(FBSType t) {
        	if( t instanceof JSJavaClassType ) {
        		Class c1 = getJavaClass();
        		Class c2 = ((JSJavaClassType)t).getJavaClass();
        		return c1.isAssignableFrom(c2);
        	}
        	return false;
        }
    }

    private static abstract class JSJavaObjectType extends FBSType {
        private JSJavaObjectType() {
        }
        public final boolean isAssignableFrom(FBSType t) {
        	if( t instanceof JSJavaObjectType ) {
        		Class c1 = getJavaClass();
        		Class c2 = ((JSJavaObjectType)t).getJavaClass();
        		return c1.isAssignableFrom(c2);
        	}
        	return false;
        }
    }

    private static abstract class JSJavaFieldType extends FBSType {
        private JSJavaFieldType() {
        }
        public String getDescString() {
            return "???";
        }
    }

    private static abstract class JSJavaMethodType extends FBSType {
        private JSJavaMethodType() {
        }
        public String getDescString() {
            return "???";
        }
        public boolean isMethod() { 
        	return true; 
        }
        public FBSType getMemberType( String name ) {
            // A method does'nt have any member
            return invalidType;
        }
        protected FBSType getNewType( FBSType[] parameters ) {
            // Not instanciable
            return invalidType;
        }
    }

    public static class JSWrappedJavaClass extends JSJavaClassType {
        private GeneratedWrapperObject.Constructor wrapper;
        public JSWrappedJavaClass(GeneratedWrapperObject.Constructor wrapper) {
            this.wrapper = wrapper;
        }
        public boolean isAdvanced() { 
        	return wrapper.isAdvanced(); 
        }
        public String getJSName() {
            return wrapper.getName();
        }
        public String getDescString() {
            return "???";
        }
        public boolean equals(Object otherType) {
            if(otherType instanceof JSWrappedJavaClass) {
                JSWrappedJavaClass c = (JSWrappedJavaClass)otherType;
                return c.wrapper==wrapper;
            }
            return false;
        }
        public FBSType getMemberType( String name ) {
            //GeneratedWrapperObject w = wrapper.get(name);
            FBSValue value = wrapper.get(name);
            return FBSType.getJavaScriptType(value);
        }
        public FBSType getCallType( FBSType[] parameters ) {
            // A class cannot be called
            return invalidType;
        }
        protected FBSType getNewType( FBSType[] parameters ) {
            //TODO: check the constructor parameters
            return new JSWrappedJavaObject(wrapper.getWrapperObject());
            //return invalidType;
        }
        protected Descriptor.Member[] getTypeMembers() {
        	return wrapper.getMembers(new FBSObject.IMemberFilter() {
        		public boolean accept( Descriptor.Member member ) {
        			return !(member instanceof Descriptor.Constructor); 
        		}
        	});
        }
        public Class getJavaClass() {
            return wrapper.getWrappedClass();
        }
    }

    private static class JSWrappedJavaObject extends JSJavaObjectType {
        private GeneratedWrapperObject wrapper;
        private JSWrappedJavaObject(GeneratedWrapperObject wrapper) {
            this.wrapper = wrapper;
        }
        public boolean isJavaObject() {
            return true;
        }
        public boolean isAdvanced() { 
        	return wrapper.isAdvanced(); 
        }
        public String getJSName() {
            return wrapper.getName();
        }
        public String getDescString() {
            return "L"+wrapper.getWrappedClass().getName()+";";
        }
        public boolean equals(Object otherType) {
            if(otherType instanceof JSWrappedJavaObject) {
                JSWrappedJavaObject c = (JSWrappedJavaObject)otherType;
                return c.wrapper==wrapper;
            }
            return false;
        }
        public FBSType getMemberType( String name ) {
            FBSValue value = wrapper.get(name);
            return FBSType.getJavaScriptType(value);
            //return wrapper.getPropertyFBSType(name);
        }
        public FBSType getCallType( FBSType[] parameters ) {
            // An object cannot be called
            return invalidType;
        }
        protected FBSType getNewType( FBSType[] parameters ) {
            // Not instanciable
            return invalidType;
        }
        protected Descriptor.Member[] getTypeMembers() {
        	return wrapper.getMembers(new FBSObject.IMemberFilter() {
        		public boolean accept( Descriptor.Member member ) {
        			return !(member instanceof Descriptor.Constructor); 
        		}
        	});
        }
        public Class getJavaClass() {
            return wrapper.getWrappedClass();
        }
    }

//    private static class JSWrappedJavaFieldType extends JSJavaFieldType {
//        private JSWrappedJavaFieldType() {
//        }
//    }

    public static class JSWrappedJavaMethodType extends JSJavaMethodType {
        private GeneratedWrapperObject.Function function;
        public JSWrappedJavaMethodType(GeneratedWrapperObject.Function function) {
            this.function = function;
        }
        public boolean isAdvanced() { 
        	return function.isAdvanced(); 
        }
        public String getJSName() {
            return "Function " + function.getName();
        }
        public FBSType getCallType( FBSType[] parameters ) {
            FBSType result = null;
            String[] callParameters = function.getCallParameters();
            if( callParameters!=null ) {
                for( int i=0; i<callParameters.length; i++ ) {
                    Descriptor.Method method = new Descriptor.Method(function.getName(),callParameters[i]);
                    if( match(method,parameters) ) {
                        FBSType t = method.getType();
                        if( result==null ) {
                            result = t;
                        } else {
                            if( result.equals(t) ) {
                                result = undefinedType;
                            }
                        }
                    }
                }
            }
            return result!=null ? result : FBSType.undefinedType;
        }
        protected Descriptor.Member[] getTypeMembers() {
        	return null;
        }
        public boolean equals(Object otherType) {
            return this==otherType;
        }
        public final boolean isAssignableFrom(FBSType t) {
        	return false; // Not applicable
        }
    }


    private static class JavaArrayType extends FBSType {
        Class    arrayType;
        private JavaArrayType(Class arrayType) {
            this.arrayType = arrayType;
        }
        public String getJSName() {
            return getType(arrayType).getJSName()+"[]";
        }
        public String getDescString() {
            return "["+getType(arrayType).getDescString();
        }
        public boolean equals(Object otherType) {
            if( otherType instanceof JavaArrayType ) {
                JavaArrayType ct = (JavaArrayType)otherType;
                return ct.arrayType.equals(arrayType);
            }
            return false;
        }
        public FBSType getMemberType( String name ) {
            return undefinedType;
        }
        public FBSType getCallType( FBSType[] parameters ) {
            if( parameters!=null && parameters.length==1 ) {
                if( parameters[0].isNumber() ) {
                    return getType(arrayType);
                }
            }
            return invalidType;
        }
        protected FBSType getNewType( FBSType[] parameters ) {
            return invalidType;
        }
        protected Descriptor.Member[] getTypeMembers() {
        	return new Descriptor.Member[] {
                new Descriptor.Field("length",intType,false)
        	};
        }
        public Class getJavaClass() {
        	// Is there a best way to do this?
        	try {
        		return Class.forName(arrayType.getName()+"[]");
        	} catch( ClassNotFoundException e ) {}
        	return null;
        }        
        public final boolean isAssignableFrom(FBSType t) {
        	if( t instanceof JavaArrayType ) {
        		Class c1 = getJavaClass();
        		Class c2 = ((JavaArrayType)t).getJavaClass();
        		return c1.isAssignableFrom(c2);
        	}
        	return false;
        }
    }

    // =======================================================================
    // STANDARD JAVA TYPES IMPLEMENTATION
    // =======================================================================
/*    
    private static class JavaPackage extends JSJavaObjectType {

        private String packageName;

        private JavaPackage(String packageName) {
            this.packageName=packageName;
        }

        public int getType() {
            return TYPE_JAVAPACKAGE;
        }

        public boolean equals(Object otherType) {
            return (otherType instanceof JavaPackage) && ((JavaPackage)otherType).packageName.equals(packageName);
        }

        public String toString() {
            return packageName;
        }

        // TODO: use generated info!!
        protected FBSType getMemberType(String name) {
            String className=packageName+"."+name;
            try {
                Class javaClass=Class.forName(className);
                JavaWrapperObject jw = null;
                try {
                    Class w = Registry.getWrapper(javaClass);
                    if( w!=null ) {
                        jw = (JavaWrapperObject)w.newInstance();
                    }
                } catch( Exception e ) {}

                if(jw!=null) {
                    //TODO...
                } else {
                    // Find if there is a public field with the name
                    try {
                        java.lang.reflect.Field field=javaClass.getField(name);
                        if(field!=null) {
                            if( (field.getModifiers()&Modifier.PUBLIC)!=0) {
                                if( (field.getModifiers()&Modifier.STATIC)!=0) {
                                    return getType(field.getType());
                                }
                            }
                        }
                    } catch(Exception e) {}
                    // Else, try to find if there is a method
                    try {
                        java.lang.reflect.Method[] methods=javaClass.getMethods();
                        if(methods!=null) {
                            for(int i=0; i<methods.length; i++) {
                                if(methods[i].getName().equals(name)) {
                                    return new JavaMethod(javaClass, name, true);
                                }
                            }
                        }
                    } catch(Exception e) {}
                    // Else, it is invalid (unknown field or method)
                    return invalidType;
                }
            } catch( Exception ex ) {}
            return new JavaPackage(className);
        }

        protected FBSType getCallType(FBSType[] parameters) {
            return invalidType;
        }

        // TODO: find a way to get the package list and classes within the packages.
        protected Member[] getMembers() {
            return null;
        }
    }
*/
    private static class JavaMethod extends JSJavaMethodType {
        private Class javaClass;
        private String methodName;
        private boolean staticOnly;
        private JavaMethod(Class javaClass, String methodName, boolean staticOnly) {
            this.javaClass=javaClass;
            this.methodName=methodName;
            this.staticOnly=staticOnly;
        }
        public boolean equals(Object otherType) {
            if(otherType instanceof JavaMethod) {
                JavaMethod m = (JavaMethod)otherType;
                return m.javaClass.equals(javaClass) && m.methodName.endsWith(methodName);
            }
            return false;
        }

        public String getJSName() {
        	return "Method "+javaClass.getName()+"."+methodName;
        }
        public String toString() {
            return javaClass+"."+methodName+"()";
        }
        protected Descriptor.Member[] getTypeMembers() {
        	return null;
        }

        public final boolean isAssignableFrom(FBSType t) {
        	return false; // Not applicable
        }

        // TODO: use generated info!!
        protected FBSType getCallType(FBSType[] parameters) {
            int paramCount = parameters!=null ? parameters.length : 0;
            // Find if there is a public method with that name that matches the parameters
            java.lang.reflect.Method[] m=javaClass.getMethods();
loop:       for( int i=0; i<m.length; i++){
                if(!m[i].getName().equals(methodName)) {
                    continue;
                }
                Class[] argClasses=m[i].getParameterTypes();
                if(argClasses.length!=paramCount) {
                    continue;
                }
                int modifier=m[i].getModifiers();
                if( (modifier&Modifier.PUBLIC)==0) {
                    continue;
                }
                if(staticOnly && (modifier&Modifier.STATIC)==0 ) {
                    continue;
                }
                for( int j=0; j<paramCount; j++) {
                    FBSType type=parameters[j];
                    // Check if we can assign the current FBSValue to that Java Class
                    if(!type.getJavaClass().isAssignableFrom(argClasses[j])) {
                        continue loop;
                    }
                }
                // We found the method
                return FBSType.getType(m[i].getReturnType());
            }
            return invalidType;
        }
    }

    private static class JavaClassType extends JSJavaClassType {
        private Class javaClass;
        private JavaClassType( Class javaClass ) {
            this.javaClass = javaClass;
        }
        public boolean equals(Object otherType) {
            if(otherType instanceof JavaClassType) {
            	JavaClassType t = (JavaClassType)otherType;
                return t.javaClass.equals(javaClass);
            }
            return false;
        }
        public String toString() {
            return javaClass.getName();
        }
        public Class getJavaClass() {
            return javaClass;
        }
        public boolean isJavaObject() {
            return true;
        }
        public String getJSName() {
            return javaClass.getName();
        }
        public String getDescString() {
            return "L"+javaClass.getName()+";";
        }

        protected FBSType getNewType( FBSType[] parameters ) {
            //TODO: check the constructor parameters
        	if(!javaClass.isInterface()) {
        		return FBSType.getType(javaClass);
        	}
            return undefinedType;
        }
        // TODO: use generated info!!
        protected FBSType getMemberType( String name ) {
            // Find if there is a public field with the name
            try {
                java.lang.reflect.Field field = javaClass.getField(name);
                if( field!=null ) {
                    if( (field.getModifiers()&Modifier.PUBLIC)!=0 ) {
                        return getType(field.getType());
                    }
                }
            } catch( Exception e ){}
            // Else, try to find if there is a method
            try {
                java.lang.reflect.Method[] methods = javaClass.getMethods();
                if( methods!=null ) {
                    for( int i=0; i<methods.length; i++ ) {
                        if( methods[i].getName().equals(name) ) {
                            return new JavaMethod(javaClass,name,false);
                        }
                    }
                }
            } catch( Exception e ){}
            // Else, it is undefined (not known)
            return undefinedType;
        }
        protected FBSType getCallType( FBSType[] parameters ) {
            return invalidType;
        }
        protected boolean isJavaAssignable( Class type ) {
            return type.isAssignableFrom(javaClass);
        }

        protected Descriptor.Member[] getTypeMembers() {
            ArrayList list = new ArrayList();
            // Compose the list of fields
            try {
                java.lang.reflect.Field[] fields = javaClass.getFields();
                if( fields!=null ) {
                    for( int i=0; i<fields.length; i++ ) {
                        java.lang.reflect.Field field = fields[i];
                        if( (field.getModifiers()&Modifier.PUBLIC)!=0 ) {
                            list.add( new Descriptor.Field(field.getName(),getType(field.getType()),(field.getModifiers()&Modifier.STATIC)!=0) );
                        }
                    }
                }
            } catch( Exception e ){}
            // Compose the list of constructors
            try {
                java.lang.reflect.Constructor[] ctors = javaClass.getConstructors();
                if( ctors!=null ) {
                    for( int i=0; i<ctors.length; i++ ) {
                        java.lang.reflect.Constructor ctor = ctors[i];
                        if( (ctor.getModifiers()&Modifier.PUBLIC)!=0 ) {
                            Class[] pt=ctor.getParameterTypes();
                            Descriptor.Param[] p = new Descriptor.Param[pt.length];
                            for( int j=0; j<pt.length; j++ ) {
                            	p[j] = new Descriptor.Param("p"+j,getType(pt[j]));
                            }
                            list.add(new Descriptor.Constructor(ctor.getName(),p));
                        }
                    }
                }
            } catch( Exception e ){}
            // Compose the list of methods
            try {
                java.lang.reflect.Method[] methods = javaClass.getMethods();
                if( methods!=null ) {
                    for( int i=0; i<methods.length; i++ ) {
                        java.lang.reflect.Method method = methods[i];
                        if( (method.getModifiers()&Modifier.PUBLIC)!=0 ) {
                            Class[] pt=method.getParameterTypes();
                            Descriptor.Param[] p = new Descriptor.Param[pt.length];
                            for( int j=0; j<pt.length; j++ ) {
                            	p[j] = new Descriptor.Param("p"+j,getType(pt[j]));
                            }
                            list.add(new Descriptor.Method(method.getName(),getType(method.getReturnType()),p,(method.getModifiers()&Modifier.STATIC)!=0,false,false,false));
                        }
                    }
                }
            } catch( Exception e ){
            }

            return (Descriptor.Member[])list.toArray(new Descriptor.Member[list.size()]);
        }
    }

    
    // =======================================================================
    // Some utilities
    // =======================================================================

    
/*
 * TODO implement this using Eclipse JDT

    private static class JavaIntrospector {

        private ArrayList javaRoots = new ArrayList();
        private HashMap javaClasses = new HashMap();

        int getRootCount() {
            return javaRoots.size();
        }
        JavaRoot getRoot( int index ) {
            return (JavaRoot)javaRoots.get(index);
        }
        void add( JavaRoot root ) {
            javaRoots.add(root);
        }
        JavaClass getJavaClass(String className) {
            // Look into the cache
            JavaClass jc = (JavaClass)javaClasses.get(className);
            if( jc!=null && jc.checkUpToDate() ) {
                return jc;
            }
            // Else, read the class
            for( int i=0; i<getRootCount(); i++ ) {
                JavaRoot root = getRoot(i);
                jc = root.loadClass(className);
                if( jc!=null ) {
                    javaClasses.put(className,jc);
                }
            }
            // Class is not available...
            return null;
        }
    }

    private static JavaIntrospector introspector = new JavaIntrospector();


    public static void addClassPath( VFSResource classPath ) {
        try {
            if( classPath!=null && classPath.exists() ) {
                if( classPath.isDirectory() ) {
                    ClasspathDirectory cp = new ClasspathDirectory((VFSFolder)classPath);
                    introspector.add(cp);
                } else if( classPath.getPath().endsWith(".jar") ) {
                    ClasspathJarFile cp = new ClasspathJarFile((VFSFile)classPath);
                    introspector.add(cp);
                }
            }
        } catch( VFSException ex ) {
            com.ibm.workplace.designer.util.TDiag.exception(ex);
        }
    }

    public static void addJarDirectory( VFSFolder jarDirectory ) {
        try {
            List l=jarDirectory.findFiles("*.jar");
            for( Iterator it=l.iterator(); it.hasNext(); ) {
                VFSFile f = (VFSFile)it.next();
                addClassPath(f);
            }
        } catch( VFSException ex ) {
            com.ibm.workplace.designer.util.TDiag.exception(ex);
        }
    }

    public abstract static class ClassObject {

        private String name;

        ClassObject(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public abstract List getChildren(List list);
        public abstract boolean isClass();
    }

    public static class JavaPackage extends ClassObject {
        private JavaPackage( String name ) {
            super( name );
        }
        public List getChildren(List list) {
            // Browse all the java roots
            for(int i=0; i<introspector.getRootCount(); i++) {
               introspector.getRoot(i).getChildren(list,getName());
            }
            return list;
        }
        public boolean isClass() {
            return false;
        }
    }

    public static class JavaClass extends ClassObject {

        public abstract static class JavaMember {
        }

        public static class JavaField extends JavaMember {
        }

        public static class JavaMethod extends JavaMember {
        }

        private JavaRoot root;

        private JavaClass( JavaRoot root, String name ) {
            super(name);
            this.root = root;
        }

        boolean checkUpToDate() {
            return true;
        }

        public List getChildren(List list) {
            return list;
        }

        public boolean isClass() {
            return true;
        }

        public JavaField[] getFields() {
            return null;
        }

        public JavaMethod[] getMethods() {
            return null;
        }
    }


    // File access
    private abstract static class JavaRoot {

        abstract List getChildren(List list, String pack);
        abstract JavaClass loadClass(String className);

        // Utility that extract a class name from a file path
        final String addPackage( String pack, String value ) {
            if( value.endsWith(".class") ) {
                value = value.substring(0,value.length()-6);
            }
            if( TString.isEmpty(pack) ) {
                return value;
            }
            return pack + "." + value;
        }
    }
    private static class ClasspathDirectory extends JavaRoot {
        private VFSFolder baseDirectory;
        ClasspathDirectory( VFSFolder baseDirectory ) {
            this.baseDirectory = baseDirectory;
        }
        public String toString() {
            return baseDirectory.getPath();
        }
        JavaClass loadClass(String className) {
            return null;
        }
        List getChildren(List list, String pack) {
            try {
                VFSFolder directory = baseDirectory.getFolder(TString.replace(pack,'.','/'));
                if( directory.isDirectory() ) {
                    List children=directory.findFiles();
                    for(Iterator it=children.iterator(); it.hasNext(); ) {
                        VFSFile child= (VFSFile)it.next();
                        if( child.getPath().endsWith(".class") ) {
                            String fName = child.getName();
                            if( fName.indexOf('$')<0 ) { // Ignore inner classes in this version
                                list.add(new JavaClass(this, addPackage(pack, fName)));
                            }
                        } else if( child.isDirectory() ) {
                            String fName = child.getName();
                            list.add( new JavaPackage(addPackage(pack,fName)) );
                        }
                    }
                }
                // Add all the inner .class
                // TODO...
            } catch( VFSException ex ) {
                com.ibm.workplace.designer.util.TDiag.exception(ex);
            }
            return list;
        }
    }

    public static class ClasspathJarFile extends JavaRoot {
        private VFSFile jarFile;
        private HashMap packages;

        ClasspathJarFile( VFSFile jarFile) {
            this.jarFile = jarFile;
        }
        public String toString() {
            return jarFile.getPath();
        }
        JavaClass loadClass(String className) {
            return null;
        }
        List getChildren(List list, String pack) {
            // Read the .class entries if not already read
            if( packages==null ) {
                packages = new HashMap();
                try {
                    ZipInputStream zin=new ZipInputStream(jarFile.getBufferedInputStream());
                    try {
                        ZipEntry e;
                        while( (e=zin.getNextEntry())!=null) {
                            if(!e.isDirectory() && e.getName().endsWith(".class")) {
                                // Get the package
                                String packName = extractPackage(e.getName());
                                ArrayList pList = addPackage(packName);

                                // Add the class
                                String cName=e.getName().substring(0, e.getName().length()-6);
                                pList.add( new JavaClass(this,TString.replace(cName,'/','.')));
                            }
                            zin.closeEntry();
                        }
                    } finally {
                        zin.close();
                    }
                } catch(Exception e) {
                    com.ibm.workplace.designer.util.TDiag.exception(e);
                }
            }

            // And now look for the desired data
            ArrayList entries = (ArrayList)packages.get(pack);
            if( entries!=null ) {
                // Add all the packages
                for( int i=0; i<entries.size(); i++ ) {
                    list.add( entries.get(i) );
                }
            }
            return list;
        }

        private ArrayList addPackage( String packName ) {
            // Add the new package, if not already added
            ArrayList list = (ArrayList)packages.get(packName);
            if( list==null ) {
                list = new ArrayList();
                packages.put(packName,list);
                // Recursively add the parent
                String parent=extractPackage(packName);
                if(!TString.isEmpty(parent)) {
                    ArrayList pList = addPackage(parent);
                    pList.add( new JavaPackage(packName) );
                }
            }
            return list;
        }

        private String extractPackage( String className ) {
            int pos = className.lastIndexOf('.');
            if( pos>=0 ) {
                return className.substring(0,pos);
            }
            return "";
        }
    }

    public static void main( String[] args ) {
        try {
            VFS vfs=VFSFactory.getFileSystem(TSystem.getBaseDirectory());
            vfs.open();
            //addClassPath(vfs.getFile("fbapp/WEB-INF/classes"));
            addJarDirectory(vfs.getFolder("fbapp/WEB-INF/lib"));
            for(int i=0; i<introspector.getRootCount(); i++) {
                TDiag.trace( "*******************************************************" );
                TDiag.trace( "INTROSPECTING: {0}", introspector.getRoot(i) );
                deep( introspector.getRoot(i), "");
            }
        } catch( Exception ex ) {
            com.ibm.workplace.designer.util.TDiag.exception(ex);
        }
    };
    private static void deep(JavaRoot root, String path) {
        TDiag.trace( "Package '{0}'", path );
        List cl = root.getChildren(new ArrayList(),path);
        if( cl!=null ) {
            for(int i=0; i<cl.size(); i++) {
                ClassObject o= (ClassObject)cl.get(i);
                if(o instanceof JavaClass) {
                    TDiag.trace("Class {0}", o.getName());
                } else if( o instanceof JavaPackage) {
                    TDiag.trace("Package {0}", o.getName());
                    deep( root, o.getName() );
                }
            }
        }
    }
*/    
}
