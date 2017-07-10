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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.ibm.jscript.InterpretException;
import com.ibm.jscript.JavaScriptException;
import com.ibm.jscript.engine.IExecutionContext;

public abstract class FBSDefaultObject extends FBSObject implements Externalizable {

	
	public static class Property extends FBSReference {
		
		private Property next;
		private Property prev;
		
		private FBSDefaultObject base;
		private long hashCode;
		private String name;
		private int attributes;
		private FBSValue value;
		
		Property(FBSDefaultObject base, long hashCode, String name, int attributes, FBSValue value) {
            this.base = base;
			this.hashCode = hashCode;
			this.name = name;
			this.attributes = attributes;
			this.value = value;
		}

		public FBSObject getBase() {
			return base;
		}
		public String getPropertyName() {
			return name;
		}
		public FBSValue getValue() {
			return value;
		}
		public void putValue(FBSValue value) {
			if(!isReadOnly()) {
				this.value = value;
			}
		}
		public int getAttributes() {
			return attributes;
		}

        public boolean isReadOnly(){
            return (attributes & P_READONLY )!=0;
        }

        public boolean isNoDelete(){
            return (attributes & P_NODELETE )!=0;
        }

        public boolean isNoEnum(){
            return (attributes & P_NOENUM)!=0;
        }

        public boolean isInternal(){
            return (attributes & P_INTERNAL )!=0;
        }

        public boolean isHidden(){
            return (attributes & P_HIDDEN )!=0;
        }

        public void setReadOnly(boolean readonly){
            if (readonly){
            	attributes = (attributes | P_READONLY);
            }else{
            	attributes = (attributes & ~P_READONLY);
            }
        }

        public void setNoDelete(boolean nodelete){
            if (nodelete){
            	attributes = (attributes | P_NODELETE);
            }else{
            	attributes = (attributes & ~P_NODELETE);
            }
        }

        public void setNoEnum(boolean noenum){
            if (noenum){
            	attributes = (attributes | P_NOENUM);
            }else{
            	attributes = (attributes & ~P_NOENUM);
            }
        }

        public void setInternal(boolean internal){
            if (internal){
            	attributes = (attributes | P_INTERNAL);
            }else{
            	attributes = (attributes & ~P_INTERNAL);
            }
        }

        public void setHidden(boolean hidden){
            if (hidden){
            	attributes = (attributes | P_HIDDEN);
            }else{
                attributes = (attributes & ~P_HIDDEN);
            }
        }
	}
	
	private static final int MAP_LENGTH = 13;
	
	private class PropertyMap {
		
		private int count;
		private Property[] entries = new Property[MAP_LENGTH];
		
		public int size() {
			return count;
		}
		
		public Property getEntry(String key) {
			long h = key.hashCode();
			int slot = (((int)h) & 0x7FFFFFFF) % MAP_LENGTH;
			for( Property e=entries[slot]; e!=null; e=e.next ) {
				if( e.hashCode==h && e.name.equals(key) ) {
					return e;
				}
			}
			return null;
		}
		
		public FBSValue get(String key) {
			Property p = getEntry(key);
			return p!=null ? p.value : null;
		}
		
		public void put( String key, FBSValue value ) {
			long h = key.hashCode();
			int slot = (((int)h) & 0x7FFFFFFF) % MAP_LENGTH;
			// Search for an existing property
			for( Property e=entries[slot]; e!=null; e=e.next ) {
				if( e.hashCode==h && e.name.equals(key) ) {
					e.value = value;
					e.attributes = P_NONE;
					return;
				}
			}
			// Insert the new property
			Property p = new Property(FBSDefaultObject.this, h,key,0,value);
			if(entries[slot]!=null) {
				entries[slot].prev = p;
				p.next = entries[slot];
			}
			entries[slot] = p;
			count++;
		}
		
		public boolean create( String key, int attributes, FBSValue value ) {
			long h = key.hashCode();
			int slot = (((int)h) & 0x7FFFFFFF) % MAP_LENGTH;
			// Search for an existing property, and ignore if it already exists
			for( Property e=entries[slot]; e!=null; e=e.next ) {
				if( e.hashCode==h && e.name.equals(key) ) {
					return false;
				}
			}
			// Insert the new property
			Property p = new Property(FBSDefaultObject.this,h,key,attributes,value);
			if(entries[slot]!=null) {
				entries[slot].prev = p;
				p.next = entries[slot];
			}
			entries[slot] = p;
			count++;
			return true;
		}

		public void remove( String key ) {
			long h = key.hashCode();
			int slot = (((int)h) & 0x7FFFFFFF) % MAP_LENGTH;
			// Search for an existing property
			for( Property e=entries[slot]; e!=null; e=e.next ) {
				if( e.hashCode==h && e.name.equals(key) ) {
					if(e.prev!=null) {
						e.prev.next = e.next;
					} else {
						entries[slot] = e.next;
					}
					if(e.next!=null) {
						e.next.prev = e.prev;
					}
					count--;
					return;
				}
			}
		}
		
		public Iterator entryIterator() {
			return new EntryMapIterator(this);
		}
		
//		public Iterator valuesIterator() {
//			return new ValueMapIterator(this);
//		}		
	}
	
	private static abstract class MapIterator implements Iterator {
		private PropertyMap map;
		private int currentSlot;
		private Property currentProperty;
		MapIterator(PropertyMap map) {
			this.map = map;
			for( int i=0; i<map.entries.length; i++ ) {
				if(map.entries[i]!=null) {
					currentSlot = i;
					currentProperty = map.entries[i]; 
					break;
				}
			}
		}
	    public boolean hasNext() {
	    	return currentProperty!=null;
	    }
	    public Object next() {
	    	if(currentProperty!=null) {
	    		Property p = currentProperty;
	    		currentProperty = currentProperty.next;
    			if(currentProperty==null) {
    				for( currentSlot++; currentSlot<map.entries.length; currentSlot++ ) {
    					if(map.entries[currentSlot]!=null) {
    						currentProperty = map.entries[currentSlot]; 
    						break;
    					}
    				}
	    		}
		    	return getValue(p);
	    	}
	    	return null;
	    }
	    public void remove() {}
	    
	    abstract Object getValue(Property p);
	}

	private static class EntryMapIterator extends MapIterator {
		EntryMapIterator(PropertyMap map) {
			super(map);
		}
	    Object getValue(Property p) {
	    	return p;
	    }
	}

//	private static class ValueMapIterator extends MapIterator {
//		ValueMapIterator(PropertyMap map) {
//			super(map);
//		}
//	    Object getValue(Property p) {
//	    	return p.getValue();
//	    }
//	}
	
	
	
    private PropertyMap properties;
    private FBSDefaultObject prototype;

    public FBSDefaultObject() {
        properties= new PropertyMap();
    }

    public FBSDefaultObject(FBSDefaultObject prototype) {
        properties= new PropertyMap();
        this.prototype = prototype;
        createProperty("prototype",FBSObject.P_NODELETE|FBSObject.P_NOENUM|FBSObject.P_READONLY,prototype); //$NON-NLS-1$
    }

    public boolean isJSObject(){
       return true;
    }

    public String getTypeAsString(){
        return "Object"; //$NON-NLS-1$
    }

    public FBSType getFBSType() {
        return new FBSType.JSObjectType(this);
    }

    public FBSDefaultObject getPrototype(){
        return prototype;
    }

    public FBSObject construct(FBSValueVector args)throws InterpretException{
        throw new InterpretException("construct is not supported for this object");
    }

    public boolean supportConstruct(){
        return false;
    }

    public boolean hasInstance(FBSValue v){
        return false;
    }

    public FBSValue call(IExecutionContext context, FBSValueVector args, FBSObject _this) throws JavaScriptException {
        return null;
    }

    public boolean supportCall(){
        return true;
    }

    public boolean isJavaObject(){
       return true;
    }

    public boolean isJavaNative(){
       return false;
    }

    public boolean isJavaAssignableTo(Class c) {
        if( c==null || c.isAssignableFrom(Map.class) ) {
            return true;
        }
        if( c.isAssignableFrom(FBSDefaultObject.class) ) {
            return true;
        }
        return false;
    }

    public Object toJavaObject(Class _class)throws InterpretException{
        if( _class==null || _class.isAssignableFrom(Map.class) ) {
            // Get the list of properties
            HashMap map = new HashMap();
            for( Iterator it=properties.entryIterator(); it.hasNext(); ) {
                Property e = (Property)it.next();
                FBSValue value = (FBSValue)e.getValue();
                if( value.isPrimitive() ) {
                    map.put( e.getPropertyName(), value.stringValue() );
                } else if( value.isJavaNative() ) {
                    map.put( e.getPropertyName(), value.toJavaObject(Object.class) );
                }
            }
            return map;
        }
        if( _class.isAssignableFrom(FBSDefaultObject.class) ) {
            return this;
        }
        throw new InterpretException("Can't convert "+this.getClass().getName()+" to "+_class.getName());
    }

    /**
     * try to create a property with name , attribute and value.
     * Creation is successfull if the property does not alreay exist.
     */
    public boolean createProperty(String name , int attribute, FBSValue value ){
        return properties.create(name,attribute,value);
    }


    public FBSValue get(int index) throws InterpretException {
        throw new InterpretException("get(int index) not supported in FBSDefaultObject");
    }
    public void put(int index , FBSValue value) throws InterpretException {
        throw new InterpretException("put(int index,FBSValue value) not supported in FBSDefaultObject");
    }

    /**
     * Assign a property 'name' with 'value'.
     * If property does not exist it is created.
     */
    public void put(String name , FBSValue value) throws InterpretException {
        if (canPut(name)){
        	properties.put(name,value);
        }
    }

    /**
     * Get the value of the property determined by name.
     * First the current object is searched if not found then the prototype of the
     * object is searched.
     * If not found FBSUndefined is returned
     * @return the value of the property
     */
    public FBSValue get(String name) throws InterpretException {
        Property p = properties.getEntry(name);
        if (p!=null){
            return p.value;
        }
        if (getPrototype()==null) {
            return FBSUndefined.undefinedValue;
        } else {
            return getPrototype().get(name);
        }
    }

    public boolean canPut(String name){
        Property p = (Property) properties.getEntry(name);
        if (p!=null){
            return !p.isReadOnly();
        }
        if (getPrototype()!=null) {
            return getPrototype().canPut(name);
        }
        return true;
    }

    public boolean hasProperty(String name){
        if (properties.getEntry(name)!=null) {
            return true;
        }
        if (getPrototype()==null) {
            return false;
        }
        return getPrototype().hasProperty(name);
    }

    public boolean delete(String name){
        Property p=(Property) properties.getEntry(name);
        if(p!=null) {
	        if (p.isNoDelete()){
	            return false;
	        }
	        properties.remove(name);
	        return true;
        }
        if (getPrototype()!=null) {
            return getPrototype().delete(name);
        }
        return false;
    }

    public void clear(){
        properties= new PropertyMap();
    }

    public FBSValue getDefaultValue(String hint){
        return FBSUndefined.undefinedValue;
    }

    public FBSValue getDefaultValue(int hint){
        return FBSUndefined.undefinedValue;
    }

    public Property getPropertyByName(String name){
        return properties.getEntry(name);
    }

    public FBSReference getPropertyReference(String name) {
    	// Look at the object properties
    	// We also dig into the hierarchy of prototypes
        for( FBSDefaultObject obj=this; obj!=null; obj=obj.getPrototype()) {
            Property p = properties.getEntry(name);
            if(p!=null) {
            	return p;
            }
        }
        // Else, return a default property reference, that will be resolved later
        // Phil: we should look here only at the properties that are not in the properties list
        // => performance optimization
        if(hasProperty(name)) {
        	return new FBSReferenceByName(this,name);
        }
        return null;
    }


    public Iterator getPropertyKeys() {
        return new com.ibm.jscript.util.FilteredIterator(properties.entryIterator()) {
            protected boolean accept( Object object ) {
                Property p=(Property)object;
                return !p.isNoEnum();
            }
            public Object next() {
                Property p=(Property)super.next();
                if( p!=null ) {
                    return p.name;
                }
                return null;
            }
        };
    }

    public void getAllPropertiesForHelp(Set members) {
        getAllPropertiesForHelp(this,members);
    }
    public void getAllPropertiesForHelp(FBSDefaultObject obj, Set members){
        for( Iterator it=obj.properties.entryIterator(); it.hasNext(); ) {
            Property p = (Property)it.next();
            if( p.getValue() instanceof FBSObject ) {
                FBSObject op = (FBSObject)p.getValue();
                if( op.supportConstruct() ){
                    String[] params = op.getCallParameters();
                    if( params!=null ) {
                        for( int i=0; i<params.length; i++ ) {
                            Descriptor.Constructor c = new Descriptor.Constructor(p.name,params[i]);
                            members.add(c);
                        }
                    } else {
                    	Descriptor.Constructor c = new Descriptor.Constructor(p.name,(Descriptor.Param[])null);
                        members.add(c);
                    }
                } else if( op.supportCall() ){
                    String[] params = op.getCallParameters();
                    if( params!=null ) {
                        for( int i=0; i<params.length; i++ ) {
                            Descriptor.Method m = new Descriptor.Method(p.name,params[i]);
                            members.add(m);
                        }
                    } else {
                        Descriptor.Method m = new Descriptor.Method(p.name,FBSType.undefinedType,null);
                        members.add(m);
                    }
                } else {
                	// Which datatype to use<
                    Descriptor.Field f = new Descriptor.Field(p.name,FBSType.undefinedType,false);
                    members.add(f);
                }
            }
        }
        FBSObject proto = obj.getPrototype();
        if( proto instanceof FBSDefaultObject ) {
            getAllPropertiesForHelp((FBSDefaultObject)proto, members);
        }
    }


    public Iterator getPropertyIterator() {
        return new com.ibm.jscript.util.FilteredIterator(properties.entryIterator()) {
            protected boolean accept( Object object ) {
                Property p=(Property)object;
                return !p.isNoEnum();
            }
        };
    }
//
//    public HashMap getProperties(){
//        return properties;
//    }



    // ========================================================================
    // Java Object IO.
    // ========================================================================

    public void readExternal( ObjectInput in ) throws IOException {
        int count = in.readInt();
        if( count>=0 ) {
            this.properties = new PropertyMap();
            for( int i=0; i<count; i++ ) {
                String name = in.readUTF();
                int attribute = in.readInt();
                FBSValue value = FBSValue.readValue(in);
                //TDiag.trace( "Deserializing '{0}'='{1}' ({2})'", name, value.getClass(), value );
                properties.create(name,attribute,value);
            }
        } else {
            this.properties = null;
        }
    }

    public void writeExternal( ObjectOutput out ) throws IOException {
        PropertyMap map = properties;
        if(map!=null) {
            out.writeInt(map.size());
            for( Iterator it=map.entryIterator(); it.hasNext(); ) {
                Property p = (Property)it.next();
                //TDiag.trace( "Serializing '{0}'='{1}' ({2})'", p.getName(), p.getValue().getClass(), p.getValue() );
                out.writeUTF(p.getPropertyName());
                out.writeInt(p.getAttributes());
                FBSValue.writeValue(out,p.getValue());
            }
        } else {
            out.writeInt(-1);
        }
    }
}
