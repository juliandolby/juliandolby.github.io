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

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Iterator;

import com.ibm.jscript.IValue;
import com.ibm.jscript.InterpretException;
import com.ibm.jscript.JScriptResources;
import com.ibm.jscript.engine.IExecutionContext;
import com.ibm.jscript.engine.JSOptions;
import com.ibm.jscript.util.FastStringBuffer;
import com.ibm.jscript.util.FastStringBufferPool;
import com.ibm.jscript.util.StringUtil;

/**
 * Java objects can hold dynamicaly created properties.
 */
public class JavaAccessObject extends JavaWrapperObject {

    private static final boolean INVOKE_BUG = true;

    private Class clazz;
    private Object instance;
    private MethodCache methodCache;

    public JavaAccessObject(Class clazz,Object object) {
        this.clazz=clazz;
        this.instance=object;
    }

    public FBSType getFBSType() {
        return FBSType.getTypeFromJSName(clazz.getName());
    }

    public Object getJavaObject() {
        return instance;
    }

    public Class getJavaClass() {
        return clazz;
    }

    public String getTypeAsString() {
        return clazz.getName();
    }

    public void setJavaObject(Object object) {
        this.instance = object;
    }

    public String getClassName(){
        return clazz.getName();
    }

    public String stringValue(){
        if (instance!=null){
            return instance.toString();
        }
        return "null"; //$NON-NLS-1$
    }
    
    public int getType() {
    	if(instance!=null) {
    		if(instance instanceof Number) {
    			return NUMBER_TYPE;
    		}
    		if(instance instanceof Boolean) {
    			return BOOLEAN_TYPE;
    		}
    		if(instance instanceof String) {
    			return STRING_TYPE;
    		}
    	}
    	return OBJECT_TYPE;
    }

    public boolean isNumber() {
        return instance instanceof Number;
    }

    public boolean isBoolean() {
        return instance instanceof Boolean;
    }

    public boolean isString() {
        return instance instanceof String;
    }

    public double numberValue(){
        if (instance!=null){
        	if(instance instanceof Number) {
        		return ((Number)instance).doubleValue();
        	}
        }
        return 0;
    }

    public boolean booleanValue(){
        if (instance!=null) {
        	if(instance instanceof Boolean) {
        		return ((Boolean)instance).booleanValue();
        	}
        }
        return true;
    }

    public boolean supportConstruct(){
        return true;
    }

    public FBSObject construct(IExecutionContext context,FBSValueVector args)throws InterpretException{
        // Rhino allows such things:
        //    new java.lang.Long.valueOf('F', 16)  
        if (methodCache!=null) {
            if(JSOptions.get().hasRhinoExtensions()) { 
                // In this case, just delegate to a function call
                FBSValue v = call(context,args,null);
                return v.toFBSObject();
            }
            throw new InterpretException("Cannot use a function call as a constructor");
        }

        // Find the best constructor using 2 passes
        ConstructorCache cache = getClassInfoCache(clazz).getConstructors();
        ConstructorCache m = (ConstructorCache)findCallable(args,cache,true);
        if(m==null && JSOptions.get().autoConvertJavaArgsToString() ) {
            m = (ConstructorCache)findCallable(args,cache,false);
        }
        
        if(m!=null) {
        	// Compute the parameters
            Object[] callParams = null;
            if(args.size()>0) {
                Class[] argClasses=m.argClasses;
            	callParams = new Object[args.size()];
            	for( int i=0; i<args.size(); i++ ) {
                    callParams[i]=args.get(i).toJavaObject(argClasses[i]);
            	}
            }

            // Create the java object
            Object result;
            try {
                result=m.constructor.newInstance(callParams);
            } catch( Exception e ) {
                throw new InterpretException(e,StringUtil.format(JScriptResources.getString("JavaFactory.CtorCall.Exception"),getMethodSignature(clazz.getName(),args),e.getClass().getName())); //$NON-NLS-1$
            }

            // When creating a new Object using this specific syntax, we ensure that no
            // conversion is done. This allows the call of overloaded methods
            return FBSUtility.wrapAsObject(result);
        }
        throw new InterpretException(StringUtil.format(JScriptResources.getString("JavaFactory.CtorNotFound.Exception"),getMethodSignature(clazz.getName(),args))); //$NON-NLS-1$
    }

    public FBSValue call(IExecutionContext context,FBSValueVector args,FBSObject _this)throws InterpretException{
    	// This may happen when a constructor is called as a function
        if (methodCache==null) {
            // In this case, just delegate to construct in case of Rhino compatibility
            if(JSOptions.get().hasRhinoExtensions()) {
                return construct(context,args);
            }
            throw new InterpretException("Cannot use a constructor as a function");
        }

        // Find the best method using 2 passes
        MethodCache m = (MethodCache)findCallable(args,methodCache,true);
        if(m==null && JSOptions.get().autoConvertJavaArgsToString() ) {
            m = (MethodCache)findCallable(args,methodCache,false);
        }
        
        if(m!=null) {
        	// Compute the parameters
            Object[] callParams = null;
            if(args.size()>0) {
                Class[] argClasses=m.argClasses;
            	callParams = new Object[args.size()];
            	for( int i=0; i<args.size(); i++ ) {
                    callParams[i]=args.get(i).toJavaObject(argClasses[i]);
            	}
            }
            Object result;
            try {
                /**
                 * PHIL: Due to a bug in Sun's VM, public methods in private
                 * classes are not accessible by default (Sun Bug #4071593).
                 * We have to explicitly set the method accessible
                 * via method.setAccessible(true).
                 * We do that only for classes that are not declared as public, because
                 * this method is checked by the security manager.
                 */
                if(INVOKE_BUG&& (clazz.getModifiers()&Modifier.PUBLIC)!=Modifier.PUBLIC) {
                    try {
                        m.method.setAccessible(true);
                        result=m.method.invoke(instance, callParams);
                    } catch(SecurityException e) {
                        result=heavyInvoke(m.method, instance, callParams);
                    }
                } else {
                    result=m.method.invoke(instance, callParams);
                }
            } catch(java.lang.reflect.InvocationTargetException e) {
                Throwable target = e.getTargetException();
                if( target instanceof RuntimeException ) {
                    // If it is a runtime exception, throw it as is
                    throw (RuntimeException)target;
                }
                if( target instanceof Error ) {
                    // If it is an error exception, throw it as is
                    throw (Error)target;
                }
                Exception e0= (target instanceof Exception)? (Exception)e.getTargetException():null;
                throw new InterpretException(e0,
                        StringUtil.format(JScriptResources.getString("JavaAccessObject.MethodCallError.Exception"), //$NON-NLS-1$
                        getMethodSignature(m.method.getName(),args), clazz.getName()));
            } catch(Exception e) {
                throw new InterpretException(e,
                        StringUtil.format(JScriptResources.getString("JavaAccessObject.MethodCallError.Exception"), //$NON-NLS-1$
                        getMethodSignature(m.method.getName(),args), clazz.getName()));
            }

            Class retType=m.method.getReturnType();
            if(retType==Void.TYPE) {
                return FBSUndefined.undefinedValue;
            }

            if(retType.isPrimitive()) {
                FBSValue v=convertToFBSValue(result);
                return v;
            }
            
            // We let the Java object being a java object
            return FBSUtility.wrapAsObjectWithNull(result);
        }
        
        throw new InterpretException(
                StringUtil.format(JScriptResources.getString("JavaAccessObject.MethodNotFound.Exception"), //$NON-NLS-1$
                getMethodSignature(methodCache.method.getName(),args),clazz.getName()));
    }

    private CallableCache findCallable(FBSValueVector args, CallableCache cache, boolean strictMatch ) throws InterpretException {
    	CallableCache found = null;
    	Class[] argsFound = null;
loop:   for(CallableCache m=cache; m!=null; m=m.next) {
            if( instance==null && !m.isStatic() ) {
                continue;
            }
            Class[] argClasses=m.argClasses;
            if(argClasses.length!=args.size()) {
                continue;
            }

            for(int j=0; j<args.size(); j++) {
                FBSValue param=args.getFBSValue(j);
                // If the parameter is already a compatible value, get it as is
                if( IValue.class.isAssignableFrom(argClasses[j]) && argClasses[j].isAssignableFrom(param.getClass()) ) {
                } else {
                    // Check if we can assign the current FBSValue to that Java Class
                    if(!param.isJavaAssignableTo(argClasses[j])) {
                    	// If not in "strict match" mode, then we can assume that 
                    	// everything can be converted to a string, as Rhino is doing
                    	if(strictMatch || argClasses[j]!=String.class) {
                            continue loop;
                    	}
                    } else {
	                    // Do the conversion if possible
                    }
                }
            }
            
            // Compare this method to the other one found
            // We try to keep the more specific here
            if(found!=null) {
            	int r = compareArguments(argsFound,argClasses);
            	if(r==0) {
            		// Ambiguity here
            		if(JSOptions.get().ignoreJavaCallAmbiguities()) {
            			continue loop;
            		}
            		throw new InterpretException(StringUtil.format("Ambiguity when calling {0}{1} and {0}{2}",m.getName(),methodSignature(argsFound),methodSignature(argClasses)));
            	}
            	if(r==1) {
            		// Keep the old one as it is more specific
            		continue loop;
            	}
            }
            
            // Set this method as the one to use
            found = m;
            argsFound = argClasses;
		}
    	
        return found;
    }
    private String methodSignature(Class[] c) {
    	StringBuffer b = new StringBuffer();
    	b.append("(");
    	if(c!=null) {
    		for( int i=0; i<c.length; i++ ) {
    			if(i>0) {
    				b.append(", ");
    			}
    			b.append(c[i].getName());
    		}
    	}
    	b.append(")");
    	return b.toString();
    }
    
    // According to the Java spec, we are looking for the most specific method
    // "The informal intuition is that one method is more specific than another if any invocation 
    // handled by the first method could be passed on to the other one without a compile-time type error."
    // This method returns 3 values:
    //    0:  incompatible. This leads to an error
    //    1:  a1 is more specific than a2
    //    -1: a2 is more specific than a1
    private int compareArguments(Class[] a1, Class[] a2) {
    	int result = 0; 
    	int length = a1.length;
    	for(int i=0; i<length; i++ ) {
			Class c1 = a1[i];  
			Class c2 = a2[i];
			if( c1.isPrimitive() ) {
				c1 = getObjectTypeFromPrimitive(c1);
			}
			if( c2.isPrimitive() ) {
				c2 = getObjectTypeFromPrimitive(c2);
			}
			if(c1!=c2) {
	    		if( c1.isAssignableFrom(c2) ) {
	    			if(result==1) {
	    				return 0;
	    			}
	    			result = -1;
	    		} if( c2.isAssignableFrom(c1) ) {
	    			if(result==-1) {
	    				return 0;
	    			}
	    			result = 1;
	    		}
			}
    	}
    	return result;
    }
    private Class getObjectTypeFromPrimitive(Class c) {
    	// Transform a primitive to its Object based class
    	if(c==Character.TYPE) {
    		return Character.class;
    	}
    	if(c==Byte.TYPE) {
    		return Byte.class;
    	}
    	if(c==Short.TYPE) {
    		return Short.class;
    	}
    	if(c==Integer.TYPE) {
    		return Integer.class;
    	}
    	if(c==Long.TYPE) {
    		return Long.class;
    	}
    	if(c==Float.TYPE) {
    		return Float.class;
    	}
    	if(c==Double.TYPE) {
    		return Double.class;
    	}
    	if(c==Boolean.TYPE) {
    		return Boolean.class;
    	}
    	return Void.class;
    }
    
    private String getMethodSignature(String functionName, FBSValueVector args) {
        FastStringBuffer b = FastStringBufferPool.get();
        try {
        	if(!StringUtil.isEmpty(functionName)) {
        		b.append( functionName );
        	}
            b.append( "(" ); //$NON-NLS-1$
            if( args!=null ) {
                for( int i=0; i<args.size(); i++ ) {
                    if(i>0) {
                        b.append( ", " ); //$NON-NLS-1$
                    }
                    if( args.get(i)!=null ) {
                        b.append(args.get(i).getTypeAsString());
                    } else {
                        b.append( "<null>" ); //$NON-NLS-1$
                    }
                }
            }
            b.append( ")" ); //$NON-NLS-1$
            return b.toString();
        } finally {
            FastStringBufferPool.recycle(b);
        }
    }

    // Find the first public Method instance...
    private static Object heavyInvoke(Method method, Object bean, Object[] args) throws InvocationTargetException, IllegalAccessException {
        String mName = method.getName();
        Class[] pTypes = method.getParameterTypes();

        Class currentClass = bean.getClass();
        while(currentClass != null) {
            try {
                Method iMethod = currentClass.getMethod(mName, pTypes);
                if (!method.equals(iMethod)) {
                    return iMethod.invoke(bean, args);
                }
            } catch (NoSuchMethodException ex2) {
            } catch (IllegalAccessException ex2) {}

            Class intfaces[] = currentClass.getInterfaces();
            for (int i=0; i<intfaces.length; i++) {
                try {
                    Method iMethod = intfaces[i].getMethod(mName, pTypes);
                    return iMethod.invoke(bean, args);
                } catch (NoSuchMethodException ex2) {
                } catch (IllegalAccessException ex2) {}
            }
            currentClass = currentClass.getSuperclass();
        }

        throw new InvocationTargetException(null,"Error while calling method "+mName);
    }


    public boolean supportCall(){
        return true;
    }

    public boolean hasProperty(String name){
        // PHIL: always assume thet the property is available
        // We will do the check at runtime!
        return true;
    }


    public FBSValue get(String name) throws InterpretException {
    	// Look for a custom property
    	if(instance!=null) {
    		IValue v = JSOptions.get().getProperty(instance,name);
    		if(v!=null) {
    			return (FBSValue)v;
    		}
    	}

    	MemberCache m=getClassInfoCache(clazz).getMembers(name);

    	// If it is a field, return its value right now
    	if(m instanceof FieldCache) {
    		FieldCache fc = (FieldCache)m;
            try {
                Field f=fc.field;

                Class t=f.getType();
                Object o=f.get(instance);
                if (o==null) {
                    return FBSNull.nullValue;
                }

                // If the result is a primitive, return the value
                if (t.isPrimitive()){
                    FBSValue v=convertToFBSValue(o);
                    if (v==null){
                        return FBSNull.nullValue;
                    }
                    return v;
                }

                // Else, return the java object
                return FBSUtility.wrapAsObject(o);
            } catch(Exception e){
                throw new RuntimeException(StringUtil.format(JScriptResources.getString("JavaAccessObject.FieldAccessError.Exception"),fc.field.getName(),clazz.getName())); //$NON-NLS-1$
            }
    	}
    	// If it is a property, same thing
    	if(m instanceof PropertyCache) {
    		if(instance==null) {
				throw new InterpretException(StringUtil.format("Cannot access statically a Java Bean property", name));
    		}
    		PropertyCache fc = (PropertyCache)m;
			Method read = fc.desc.getReadMethod();
			if(read==null) {
				throw new InterpretException(StringUtil.format(JScriptResources.getString("JavaAccessObject.NoReadMethod.Exception"), name)); //$NON-NLS-1$
			}
			try {
				return FBSUtility.wrapAsObject(read.invoke(instance,null));
			} catch(Exception e) {
				throw new InterpretException(e,StringUtil.format(JScriptResources.getString("JavaAccessObject.BeanAccessError.Exception"), name)); //$NON-NLS-1$
			}
    	}
    	// If it is a method, then return self and the method is set as a global
    	if(m instanceof MethodCache) {
    		// In case method cache has not yet been used for this object, we can sefetly return it
    		if(methodCache==null || methodCache==m) {
        		this.methodCache = (MethodCache)m;
                return this;
    		}
    		// Else, we make a copy of the object to ensure safety
    		JavaAccessObject j2 = new JavaAccessObject(clazz,instance);    		
    		j2.methodCache = (MethodCache)m;
            return j2;
    	}

		throw new InterpretException(StringUtil.format(JScriptResources.getString("JavaAccessObject.UnknownMember.Exception"), name, clazz.getName())); //$NON-NLS-1$
    }

    /**
     * Can't put any property to a Java Object
     */
    public boolean canPut(String name){
        return true;
    }

    /**
     * Can't put any property to a Java Object
     */
    public void put(String name, FBSValue value) throws InterpretException {
    	// Look for a custom property
    	if(instance!=null) {
    		if(JSOptions.get().putProperty(instance,name,value)) {
    			return;
    		}
    	}

    	MemberCache m=getClassInfoCache(clazz).getMembers(name);

        // If it is a field, return its value right now
    	if(m instanceof FieldCache) {
    		FieldCache fc = (FieldCache)m;
            try {
                Field f=fc.field;
	        	if(value.isJavaAssignableTo(f.getType()) ) {
	        		f.set(instance,value.toJavaObject(f.getType()));
	        		return;
	        	}
	        	throw new InterpretException(null,StringUtil.format(JScriptResources.getString("JavaAccessObject.SetValError.Exception"),clazz.getName(),name)); //$NON-NLS-1$
            } catch( Exception e ) {
            	throw new InterpretException(e,StringUtil.format(JScriptResources.getString("JavaAccessObject.SetValError.Exception"),clazz.getName(),name)); //$NON-NLS-1$
            }
    	}
    	// If it is a property, same thing
    	if(m instanceof PropertyCache) {
    		if(instance==null) {
				throw new InterpretException(StringUtil.format("Cannot access statically a Java Bean property", name));
    		}
    		PropertyCache fc = (PropertyCache)m;
			Method write = fc.desc.getWriteMethod();
			if(write==null) {
				throw new InterpretException(StringUtil.format("Java Bean property '{0}' does not have a write method", name));
			}
			try {
				write.invoke(instance,new Object[]{value.toJavaObject()});
			} catch(Exception e) {
				throw new InterpretException(e,StringUtil.format(JScriptResources.getString("JavaAccessObject.BeanAccessError.Exception"), name)); //$NON-NLS-1$
			}
    	}
    	// If it is a method, then return self and the method is set as a global
    	if(m instanceof MethodCache) {
    		throw new InterpretException(StringUtil.format(JScriptResources.getString("JavaAccessObject.AssignValError.Exception"), name, clazz.getName())); //$NON-NLS-1$
    	}

		throw new InterpretException(StringUtil.format(JScriptResources.getString("JavaAccessObject.UnknownMember.Exception"), name, clazz.getName())); //$NON-NLS-1$
    }

    /**
     * Can't create any property in a Java Object
     */
    public boolean createProperty(String name , int attribute, FBSValue value ){
        return false;
    }

    /**
     * Can't delete any property from a Java Object
     */
    public boolean delete(String name){
        return false;
    }

    public String getName(){
        return clazz.getName();
    }


    // REALLY TEMP!!
    public Iterator getPropertyKeys() {
        return null;
    }


    private FBSValue convertToFBSValue(Object o){
    	if( o instanceof Number ) {
            Number num=(Number)o;
            return FBSNumber.get(num.doubleValue());
    	} else if (o instanceof Boolean){
            Boolean bool=(Boolean)o;
            return FBSBoolean.get(bool.booleanValue());
        } else if (o instanceof Character){
            Character c=(Character)o;
            return FBSString.get(c.toString());
        }
        // Should not happen...
        return FBSUndefined.undefinedValue;
    }


    // ===================================================================================
    // Java access optimization
    // ===================================================================================
    
    private static ClassInfoCache getClassInfoCache(Class c) {
    	ClassInfoCache ci = (ClassInfoCache)Registry.getClassInfo(c);
    	if(ci==null) {
    		ci = new ClassInfoCache(c);
    		Registry.putClassInfo(c,ci);
    	}
    	return ci;
    }
    
    private static abstract class MemberCache {
    }
    
    private static abstract class CallableCache extends MemberCache {
    	CallableCache next;
        Class[] argClasses;
        CallableCache(Class[] argClasses) {
        	this.argClasses = argClasses;
        }
        abstract boolean isStatic();
        abstract String getName();
    }

    private static class MethodCache extends CallableCache {
    	Method	method;
        MethodCache(Method method) {
        	super(method.getParameterTypes());
        	this.method = method;
        }
        boolean isStatic() {
	        int modifier=method.getModifiers();
	        return (modifier&Modifier.STATIC)!=0;
        }
        String getName() {
        	return method.getName();
        }
    }
    
    private static class ConstructorCache extends CallableCache {
    	Constructor constructor;
        ConstructorCache(Constructor constructor) {
        	super(constructor.getParameterTypes());
        	this.constructor = constructor;
        }
        boolean isStatic() {
        	return true;
        }
        String getName() {
        	return "new "+constructor.getName();
        }
    }

    private static class FieldCache extends MemberCache {
    	Field field;
        FieldCache(Field field) {
        	this.field = field;
        }
    }

    private static class PropertyCache extends MemberCache {
    	PropertyDescriptor desc;
    	PropertyCache(PropertyDescriptor desc) {
        	this.desc = desc;
        }
    }
    
    private static class ClassInfoCache {
    
    	private Class clazz;
    	private ConstructorCache constructors;
    	private HashMap members;
    	
    	ClassInfoCache(Class clazz) {
    		this.clazz = clazz;
    		this.members = new HashMap();
    	}
    	
    	ConstructorCache getConstructors() {
    		if(constructors==null) {
                Constructor[] c=clazz.getConstructors();
                for(int i=0; i<c.length; i++) {
                    if( (c[i].getModifiers()&Modifier.PUBLIC)==0 ) {
                        continue;
                    }
                    ConstructorCache mc = new ConstructorCache(c[i]);
                    mc.next = constructors;
                    constructors = mc;
                }
    		}
    		return constructors;
    	}
    	
    	MemberCache getMembers(String name) {
    		MemberCache cached = (MemberCache)members.get(name);
    		if(cached!=null) {
    			return cached;
    		}
    		
    		// If the member name is empty, it is because we are looking for a constructor
    		if(name.length()==0) {
    			ConstructorCache first = null;
                return first;
    		}
    		
    		// Look if there is a field with that name
            try {
                Field f=clazz.getField(name);
                if( f!=null && (f.getModifiers()&Modifier.PUBLIC)==Modifier.PUBLIC ) {
                    // Put it in the list
                    FieldCache fc = new FieldCache(f);
                    members.put(name,fc);
                    return fc;
                }
            } catch( NoSuchFieldException e ) {
            }
            
            // Look for methods with that name
            MethodCache first = null;
// PHIL: this doesn't work as the same method can be declared at different class levels
// and overloaded -> leads to duplicate            
//            for( Class c=clazz; c!=null; c=c.getSuperclass() ) {
//                Method[] m2=c.getDeclaredMethods();
//                for(int i=0; i<m.length; i++) {
//                    if(!m[i].getName().equals(name)) {
//                        continue;
//                    }
//                    if( (m[i].getModifiers()&Modifier.PUBLIC)==0 ) {
//                        continue;
//                    }
//                    MethodCache mc = new MethodCache(m[i]);
//                    mc.next = first;
//                    first = mc;
//                }
//            }
            Method[] m=clazz.getMethods();
            for(int i=0; i<m.length; i++) {
                if(!m[i].getName().equals(name)) {
                    continue;
                }
                if( (m[i].getModifiers()&Modifier.PUBLIC)==0 ) {
                    continue;
                }
                MethodCache mc = new MethodCache(m[i]);
                mc.next = first;
                first = mc;
            }
            if(first!=null) {
                members.put(name,first);
                return first;
            }
            
            // Look for a javabean property
            if(JSOptions.get().hasJavaBeanAccess()) {
            	try { 
            		BeanInfo bi = Introspector.getBeanInfo(clazz);
            		PropertyDescriptor[] desc =  bi.getPropertyDescriptors();
            		if(desc!=null) {
            			for(int i=0; i<desc.length; i++) {
            				if(desc[i].getName().equals(name)) {
            					PropertyCache pc = new PropertyCache(desc[i]);
            	                members.put(name,pc);
            	                return pc;
            				}
            			}
            		}
            	} catch(Exception ex) {}
            }
            
            // Nothing corresponds here...
            return null;
    	}
    }
}
