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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.ibm.jscript.IValue;
import com.ibm.jscript.InterpretException;
import com.ibm.jscript.JavaScriptException;
import com.ibm.jscript.ASTTree.ASTArgumentList;
import com.ibm.jscript.ASTTree.ASTNode;
import com.ibm.jscript.ASTTree.InterpretResult;
import com.ibm.jscript.engine.IExecutionContext;

public abstract class FBSObject extends FBSValue {

    public static final int P_READONLY= 1;
    public static final int P_NOENUM= 2;
    public static final int P_NODELETE= 4;
    public static final int P_INTERNAL= 8;
    public static final int P_HIDDEN= 16;

    public static final int P_NONE=0;

    public boolean equals( Object o ) {
    	return this==o;
    }
    
    /**
     * @return OBJECT_TYPE
     */
    public int getType(){
        return OBJECT_TYPE;
    }

    public String getTypeAsString(){
        return "object"; //$NON-NLS-1$
    }

    // PHIL: for code completion
    public boolean isAdvanced() {
		return false;
	}
	

    public final FBSType getPropertyFBSType(String name) throws InterpretException { 
    	FBSValue prop = get(name); 
    	if(prop!=null && !prop.isUndefined()) {
    		return prop.getFBSType();
    	}
    	return FBSType.invalidType;
    }

    public String stringValue(){
        return ""; //$NON-NLS-1$
    }

    public double numberValue(){
        return 0;
    }

    public boolean booleanValue(){
        return true;
    }

    public FBSValue toFBSPrimitive(){
        return getDefaultValue(""); //$NON-NLS-1$
    }

    public FBSBoolean toFBSBoolean(){
        return FBSBoolean.TRUE;
    }

    public FBSNumber toFBSNumber(){
        return FBSNumber.Zero;
    }

    public FBSString toFBSString(){
        return FBSString.get(stringValue());
    }

    public boolean isPrimitive(){
        return false;
    }

    public boolean isObject() {
        return true;
    }

    public FBSObject toFBSObject(){
        return this;
    }

    public FBSObject construct(IExecutionContext context, FBSValueVector args) throws InterpretException {
    	return construct(args);
    }
    // PHIL: the following function is currently deprecated and maintained for compatibility
    /** @deprecated */
    public abstract FBSObject construct(FBSValueVector args) throws InterpretException;
    public abstract boolean supportConstruct();
    public abstract boolean hasInstance(FBSValue v);
    public abstract FBSValue call(IExecutionContext context,FBSValueVector args,FBSObject _this) throws JavaScriptException;
    public abstract boolean supportCall();

    
    // PHIL: separated the calls here so that a particular function implementation
    // may only evalute the arguments it is interested by. This is typically for 
    // functions like @if()
    public void call(IExecutionContext context, ASTNode caller, ASTArgumentList arguments, FBSObject _this, InterpretResult result) throws JavaScriptException {
	    FBSValueVector args=null;
	    if (arguments!=null && arguments.getSlotCount()>0){
	        arguments.interpret(caller,context,result);
	        args=(FBSValueVector)result.getFBSValue().toJavaObject();
	    }else{
	        args=FBSValueVector.emptyVector; // to avoid sending null object to the function call
	    }
        result.setNormal(call(context,args,_this));
    }


    // IValue support
    public IValue getProperty( String name ) throws InterpretException {
        return get(name);
    }
    public void putProperty( String name, IValue value ) throws InterpretException {
        put(name,(FBSValue)value);
    }


    // This is for helping the user
    protected String[] getCallParameters() { return null; }


    /**
     * Assign a property 'name' with 'value'.
     * If property does not exist it is created.
     */
    public abstract void put(String name , FBSValue value) throws InterpretException;

    /**
     * get the value of the property determined by name.
     * First the current object is searched if not found then the prototype of the
     * object is searched.
     * If not found FBSUndefined is returned
     * @return the value of the property
     */
    public abstract FBSValue get(String name) throws InterpretException;

    /**
     * Assign a property at position'index' with 'value'.
     * If property does not exist it is created.
     */
    public abstract void put(int index , FBSValue value) throws InterpretException;


    /**
     * get the value of the property determined by 'index'.
     * First the current object is searched if not found then the prototype of the
     * object is searched.
     * If not found FBSUndefined is returned
     * @return the value of the property
     */
    public abstract FBSValue get(int index) throws InterpretException;


    /**
     * Check if a property can be accessed with a number index.
     * This is an optimization in case of array, which prevent from converting
     * the index to and from a string.
     */
    public boolean hasIndexedProperty(){
        return false;
    }

    public abstract boolean canPut(String name);

    public abstract boolean hasProperty(String name);

    public FBSReference getPropertyReference(String name) {
        if(hasProperty(name)) {
        	return new FBSReferenceByName(this,name);
        }
        return null;
    }

    public FBSReference getPropertyReference(int index) {
    	if(hasIndexedProperty()) {
    		return new FBSReferenceByIndex(this,index);
    	}
    	return null;
    }

    public abstract FBSValue getDefaultValue(String hint);

    public abstract FBSValue getDefaultValue(int hint);

    public boolean isJavaNative(){
        return false;
    }


    /**
     * Gives an object for a synchronization.
     * This methods returns an object used to be synchronized with in a multithread
     * environment.
     * Java object wrappers should override this method to return their underlying
     * java object. This java object must be used for synchronization.
     * @return object for synchronization.
     */
     public Object getSyncObject(){
        return this;
     }

     /**
      * Return an iterator for the key elements of this object, that is all is
      * enumerable properties and the (non hidden) one of its prototypes, etc...
      * as used in the for statement.
      * @return the iterator
      */
    public Iterator getPropertyKeys() {
        return null;
    }

    /**
     * get names of properties inside this object.
     * the main purpose of this method is to provide a help
     * useful for visual component to display in order to
     * expose available properties for the user.
     * Must not be confused by getNextPropertyName since the latter
     * returns only the next ENUMERABLE property in the object.
     *
     * @return array of strings, does not return null in any case.
     *
     */
    public void getAllPropertiesForHelp(Set members){
    }
    
    public interface IMemberFilter {
    	public boolean accept(Descriptor.Member member);
    }
    
    public final Descriptor.Member[] getMembers(final IMemberFilter filter){
    	Set members = new HashSet() {
    		public boolean add(Object o) {
    			if(filter==null || filter.accept((Descriptor.Member)o)) {
    				return super.add(o);
    			}
    			return false;
    		}
    	};
    	getAllPropertiesForHelp(members);
    	return (Descriptor.Member[])members.toArray(new Descriptor.Member[members.size()]);
    }
    
    public final Descriptor.Member[] getMembers(){
    	return getMembers(null);
    }
}
