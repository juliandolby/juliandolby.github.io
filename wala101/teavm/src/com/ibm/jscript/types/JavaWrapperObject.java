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

import com.ibm.jscript.InterpretException;
import com.ibm.jscript.engine.IExecutionContext;
import com.ibm.jscript.std.DateObject;

public abstract class JavaWrapperObject extends FBSObject {

    public JavaWrapperObject(){
    }

    public boolean equals( Object o ) {
    	if( o instanceof JavaWrapperObject ) {
    		JavaWrapperObject w = (JavaWrapperObject)o;
			return w.getJavaObject().equals(getJavaObject());
    	}
    	return false;
    }

//    public abstract void setJavaObject(Object o);
    public abstract Object getJavaObject();

    public FBSType getFBSType() {
        return FBSType.getTypeFromJavaClass(getJavaObject().getClass());
    }

    public Class getJavaClass() {
        Object jObject = getJavaObject();
        if( jObject!=null ) {
            return jObject.getClass();
        }
        return null;
    }

    public boolean isJavaObject(){
       return true;
    }

    public boolean isJavaNative(){
        return true;
    }

    public boolean isDate() {
        return    getJavaObject() instanceof DateObject
		       || getJavaObject() instanceof java.util.Date;
    }

    public DateObject dateValueJS() {
        Object object = getJavaObject();
        if( object instanceof DateObject ) {
            return (DateObject)object;
        }
        if( object instanceof java.util.Date ) {
            return new DateObject(((java.util.Date)object).getTime());
        }
        return null;
    }

    public String stringValue(){
        Object o = getJavaObject();
        return o!=null ? o.toString() : "<null>";
    }

    public boolean delete(String name){
        return false;
    }

    public boolean createProperty(String name , int attribute, FBSValue value ){
        return false;
    }

    public FBSValue getDefaultValue(String hint){
        return FBSUtility.wrap(getJavaObject().toString());
    }

    public FBSValue getDefaultValue(int hint){
        return FBSUtility.wrap(getJavaObject().toString());
    }

    /**
     * Get the real wrapper for a specific declared class.
     * It can be:
     * <UL>
     *   <LI>_this if it has the correct type
     *   <LI>_this.delegate if it can delegate to find the correct class
     *   <LI>null if it is a constructor (only static methods can be called)
     * </UL>
     */
    public static Object getWrappedObject(FBSObject _this, Class desiredClass) throws InterpretException{
        if( _this instanceof GeneratedWrapperObject.Constructor ) {
            return null;
        } else {
            Object obj = getDelegatedObject( ((JavaWrapperObject)_this).getJavaObject(), desiredClass );
            if( obj==null ) {
                throw new InterpretException("Cannot find the correct object class");
            }
            return obj;
        }
    }

    public boolean isJavaAssignableTo(Class c) {
        if( c!=null ) {
            Object object = getJavaObject();
            if( c.isPrimitive() ) {
                if( c==Boolean.TYPE ) c=Boolean.class;
                else if( c==Character.TYPE ) c=Character.class;
                else if( c==Byte.TYPE ) c=Byte.class;
                else if( c==Short.TYPE ) c=Short.class;
                else if( c==Integer.TYPE ) c=Integer.class;
                else if( c==Long.TYPE ) c=Long.class;
                else if( c==Float.TYPE ) c=Float.class;
                else if( c==Double.TYPE ) c=Double.class;
            }
            return object==null || getDelegatedObject(object,c)!=null;
        }
        return true;
    }

    public Object toJavaObject(Class c) throws InterpretException {
        Object object = getJavaObject();
        if( object==null ) {
            return null;
        }
        if( c==null ) {
            return object;
        }
        if( c.isPrimitive() ) {
            if( c==Boolean.TYPE ) c=Boolean.class;
            else if( c==Character.TYPE ) c=Character.class;
            else if( c==Byte.TYPE ) c=Byte.class;
            else if( c==Short.TYPE ) c=Short.class;
            else if( c==Integer.TYPE ) c=Integer.class;
            else if( c==Long.TYPE ) c=Long.class;
            else if( c==Float.TYPE ) c=Float.class;
            else if( c==Double.TYPE ) c=Double.class;
        }
        object = getDelegatedObject(object,c);
        if(object!=null){
            return object;
        }
        if (c==String.class) {
        	return stringValue();
        }
        throw new InterpretException("Can't convert "+this.getClass().getName()+" to "+c.getName());
    }

    /**
     * Static method that try to get the object corresponding to a specific class.
     */
    private static Object getDelegatedObject(Object obj, Class desiredClass) {
        // If the object correspond to the class, return it
        if( desiredClass.isInstance(obj) ) {
            return obj;
        }
        // Else, try for one of its delegation
        // This is done recursively, so nested delegations work properly
        if( obj instanceof IScriptDelegator ) {
            Object dlg = ((IScriptDelegator)obj).getScriptSingleDelegate();
            if( dlg!=null ) {
                return getDelegatedObject( dlg, desiredClass );
            } else {
                Object[] objs = ((IScriptDelegator)obj).getScriptMultipleDelegate();
                if( objs!=null ) {
                    for( int i=0; i<objs.length; i++ ) {
                        dlg = objs[i];
                        if( dlg!=null ) {
                            Object w = getDelegatedObject(dlg,desiredClass);
                            if( w!=null ) {
                                return w;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public FBSObject construct(FBSValueVector args)throws InterpretException{
        throw new InterpretException("construct() method is not implemented in JavaWrapperObject");
    }

    public boolean supportConstruct(){
        return false;
    }

    public boolean hasInstance(FBSValue v){
        // We should be a class, and v is the object to check
        if( v instanceof JavaWrapperObject ) {
            Object o = ((JavaWrapperObject)v).getJavaObject();
            if( o!=null && getJavaClass().isAssignableFrom(o.getClass()) ) {
                return true;
            }
        }
        return false;
    }

    public FBSValue call(IExecutionContext context,FBSValueVector args,FBSObject _this)throws InterpretException{
        throw new InterpretException("construct() method is not implemented in JavaWrapperObject");
    }

    public boolean supportCall(){
        return false;
    }

    public FBSValue get(int index){
        throw new RuntimeException("get(int index) not supported in JavaWrapperObject");
    }

    public void put(int index , FBSValue value){
        throw new RuntimeException("put(int index,FBSValue value) not supported in JavaWrapperObject");
    }

    public boolean hasMultipleValues(){
        Object o = getJavaObject();
        return    (o instanceof java.util.Iterator)
               || (o instanceof java.util.Enumeration)
               || (o instanceof java.util.Collection)
               || (o instanceof com.ibm.jscript.util.Iteratable);
    }


    public Object getSyncObject() {
        return getJavaObject();
    }


    /**
     *
     */
    public IValues getValues() {
        // The arrays are already wrapped
        // So, we need to handle the java collection & iterators...
        Object o = getJavaObject();
        if( o instanceof java.util.Iterator ) {
            return new IteratorValues( (java.util.Iterator)o );
        } else if( o instanceof java.util.Enumeration ) {
            return new EnumerationValues( (java.util.Enumeration)o );
        } else if( o instanceof java.util.Collection ) {
            return new IteratorValues( ((java.util.Collection)o).iterator() );
        } else if( o instanceof com.ibm.jscript.util.Iteratable ) {
            return new IteratorValues( ((com.ibm.jscript.util.Iteratable)o).iterator() );
        }
        // Standard unique value...
        return super.getValues();
    }

    private class IteratorValues implements IValues {
        java.util.Iterator it;
        private IteratorValues(java.util.Iterator it) {
            this.it = it;
        }
        public boolean hasNext() {
            return it.hasNext();
        }
        public FBSValue next() {
            try {
                return FBSUtility.wrapAsObjectWithNull(it.next());
            } catch( InterpretException e ) {
                return null;
            }
        }
    }

    private class EnumerationValues implements IValues {
        java.util.Enumeration en;
        private EnumerationValues(java.util.Enumeration en) {
            this.en = en;
        }
        public boolean hasNext() {
            return en.hasMoreElements();
        }
        public FBSValue next() {
            try {
                return FBSUtility.wrapAsObjectWithNull(en.nextElement());
            } catch( InterpretException e ) {
                return null;
            }
        }
    }

	public boolean canPut(String name) {
		// TODO Auto-generated method stub
		return false;
	}

	public FBSValue get(String name) throws InterpretException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean hasProperty(String name) {
		// TODO Auto-generated method stub
		return false;
	}

	public void put(String name, FBSValue value) throws InterpretException {
		// TODO Auto-generated method stub
		
	}

	public FBSBoolean toFBSBoolean() {
		return (FBSBoolean)FBSUtility.wrap(Boolean.valueOf(stringValue()).booleanValue());
	}

	public FBSNumber toFBSNumber() {
		String str = stringValue();
		if(str.indexOf(".") != -1)
			str = str.substring(0, str.indexOf("."));
		return (FBSNumber)FBSUtility.wrap(Integer.valueOf(str).intValue());
	}
}