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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.ibm.jscript.InterpretException;
import com.ibm.jscript.engine.JSOptions;
import com.ibm.jscript.lib.BooleanArrayWrapper;
import com.ibm.jscript.lib.ByteArrayWrapper;
import com.ibm.jscript.lib.CharArrayWrapper;
import com.ibm.jscript.lib.DoubleArrayWrapper;
import com.ibm.jscript.lib.FloatArrayWrapper;
import com.ibm.jscript.lib.IntArrayWrapper;
import com.ibm.jscript.lib.LongArrayWrapper;
import com.ibm.jscript.lib.ObjectArrayWrapper;
import com.ibm.jscript.lib.ShortArrayWrapper;
import com.ibm.jscript.std.ArrayObject;
import com.ibm.jscript.std.BooleanObject;
import com.ibm.jscript.std.DateObject;
import com.ibm.jscript.std.FunctionObject;
import com.ibm.jscript.std.MathObject;
import com.ibm.jscript.std.NumberObject;
import com.ibm.jscript.std.ObjectObject;
import com.ibm.jscript.std.RegExpObject;
import com.ibm.jscript.std.StringObject;
import com.ibm.jscript.util.QuickSort;
import com.ibm.jscript.util.StringUtil;

public final class Registry {

    private static HashMap wrapperMap=new HashMap();
    private static FBSDefaultObject regObject= new ObjectObject();
    private static ArrayList prototypes = new ArrayList();
    private static ArrayList packages = new ArrayList();

    static{
        // Register the std java classes
        int pckgID = (6<<20);// 6 is the number of the standrad package
        int attrib = FBSObject.P_READONLY + pckgID;
        registerPackage("Standard",pckgID);

        // Standard classes
        registerStandard("Object",new ObjectObject());
        registerStandard("Function",new FunctionObject());
        registerStandard("Array",new ArrayObject());
        registerStandard("String",new StringObject(FBSString.emptyString));
        registerStandard("Boolean",new BooleanObject(FBSBoolean.TRUE));
        registerStandard("Number",new NumberObject(FBSNumber.NaN));
        registerStandard("Date",new DateObject(0));
        registerStandard("RegExp",new RegExpObject());
        registerStandard("Math",new MathObject());

        // TODO: implement Error objects?

        // Register the java array wrapper classes
        registerWrapper(byte[].class,ByteArrayWrapper.class);
        registerWrapper(short[].class,ShortArrayWrapper.class);
        registerWrapper(int[].class,IntArrayWrapper.class);
        registerWrapper(long[].class,LongArrayWrapper.class);
        registerWrapper(float[].class,FloatArrayWrapper.class);
        registerWrapper(double[].class,DoubleArrayWrapper.class);
        registerWrapper(boolean[].class,BooleanArrayWrapper.class);
        registerWrapper(char[].class,CharArrayWrapper.class);
        registerWrapper(Object[].class,ObjectArrayWrapper.class);
        
        if( JSOptions.get().hasRhinoExtensions() ) {
            int attrib2=FBSObject.P_NODELETE|FBSObject.P_READONLY;
            regObject.createProperty(JavaPackageObject.PACKAGES,attrib2,JavaPackageObject.getPackage(null));
        }
    }

    
    // ======================================================================================
    // Java classinfo cache
    // ======================================================================================

    private static HashMap classInfos = new HashMap();
    
    public static Object getClassInfo(Class c) {
    	return classInfos.get(c);
    }
    public static void putClassInfo(Class c, Object ci) {
    	classInfos.put(c,ci);
    }

    
    // ------------------------------------------------------------------------
    // Global Object registry
    // All the properties of these prototypes are available directly
    // ------------------------------------------------------------------------

    public static void registerStandard(String name ,FBSValue v){
        synchronized(regObject) {
            try {
                int pckgID = (6<<20);// 6 is the number of the standrad package
                int attrib = FBSObject.P_READONLY + pckgID;
                regObject.createProperty(name,attrib,v);
            } catch( Exception e ) {}
        }
    }

    public static FBSDefaultObject getRegistryObject(){
        return regObject;
    }


    
    // ------------------------------------------------------------------------
    // Global prototypes
    // All the properties of these prototypes are available directly
    // ------------------------------------------------------------------------

    private static class PrototypeItem {
    	String name;
    	FBSObject object;
    	PrototypeItem(String name, FBSObject object) {
    		this.name = name;
    		this.object = object;
    	}
    }
    public static void registerGlobalPrototype(String name, FBSObject object){
        synchronized(prototypes) {
            try {
            	prototypes.add(new PrototypeItem(name,object));
            } catch( Exception e ) {}
        }
    }
    
    public static int getGlobalPrototypeCount() {
    	return prototypes.size();
    }
    
    public static String getGlobalPrototypeName( int index ) {
    	return ((PrototypeItem)prototypes.get(index)).name;
    }
    
    public static FBSObject getGlobalPrototype( int index ) {
    	return ((PrototypeItem)prototypes.get(index)).object;
    }


    // ------------------------------------------------------------------------
    // Java wrapper registry
    // ------------------------------------------------------------------------

    public static void registerWrapper(Class javaClass,Class scriptWrapper){
        synchronized(wrapperMap) {
            wrapperMap.put(javaClass,scriptWrapper);
        }
    }

    public static Class getWrapper(Class javaClass){
        //TDiag.trace( "Found wrapper '{0}' for {1}", (Class)map.get(javaClass), javaClass );
        return (Class)wrapperMap.get(javaClass);
    }

    public static Class getClassForAlias(String alias) throws InterpretException {
        if (!StringUtil.isSpace(alias)) {
            FBSValue v = Registry.getRegistryObject().get(alias);
            if (v instanceof GeneratedWrapperObject.Constructor) {
                return ((GeneratedWrapperObject.Constructor)v).getWrappedClass();
            }
        }
        return null;
    }

    public static void clear(){
        wrapperMap.clear();
    }


    // ======================================================================================
    // User help support (inspector)
    // ======================================================================================

    public static void registerPackage( String packName, int packageID ) {
        for (int i=0;i<packages.size();i++){
            Package p = (Package)packages.get(i);
            if (p.getID()==packageID){
                p.setName(packName);
                return;
            }
        }
        packages.add(new Package(packName,packageID));
    }

    public static Package getPackage(int packageID ) {
        for(int i=0;i<packages.size();i++){
            Package pkg=(Package)packages.get(i);
            if (pkg.id==packageID){
                return pkg;
            }
        }
        return null;
    }

    public static class Package {
        String name;
        int id;
        public Package( String name, int id ) {
            this.name = name;
            this.id = id&0xFFFF0000;
        }
        public String getName() {
            return name;
        }
        public void setName(String name){
            this.name = name;
        }
        public int getID(){
            return id;
        }
    }

    public static class JSClass {
        protected String name;
    	protected FBSObject object;
    	
    	public JSClass( String name, FBSObject object ) {
    		this.name = name;
    		this.object = object;
    	}
    	
    	public String getName() {
    		return name;
    	}
    	
    	public FBSObject getObject() {
    		return object;
    	}
    }


    public static Package[] getRegisteredPackages() {
        Package[] array = (Package[])packages.toArray(new Package[packages.size()]);
        new QuickSort.ObjectArray(array) {
            public int compare(Object o1, Object o2) {
                Package p1 = (Package)o1;
                Package p2 = (Package)o2;
                return compareString(p1.getName(),p2.getName());
            }
        }.sort();
        return array;
    }

    public static Package getPackageForObject(FBSObject o) {
        if( o!=null ) {
            // Browse the registy object and get the classes with the desired id
            for( Iterator it=regObject.getPropertyIterator(); it.hasNext(); ) {
                FBSDefaultObject.Property p=(FBSDefaultObject.Property)it.next();
                if( p.getValue()==o ) {
                    int id = p.getAttributes()&0xFFFF0000;
                    // Find the package with the ID
                    for( int i=0; i<packages.size(); i++ ) {
                        Package pack = (Package)packages.get(i);
                        if( pack.id==id ) {
                            return pack;
                        }
                    }
                    return null;
                }
            }
        }
        return null;
    }

    public static FBSObject getGlobalObject(String objectName) {
        FBSDefaultObject.Property p=regObject.getPropertyByName(objectName);
        if(p!=null) {
            FBSValue v = (FBSValue)p.getValue();
            if( v instanceof FBSObject ) {
                return (FBSObject)v;
            }
        }
        return null;
    }

    public static JSClass[] getRegisteredClasses(Package pack) {
        try {
            if(pack!=null) {
                int id=pack.id;
                // Browse the registy object and get the classes with the desired if
                ArrayList list = new ArrayList();
                for( Iterator it=regObject.getPropertyIterator(); it.hasNext(); ) {
                    FBSDefaultObject.Property p=(FBSDefaultObject.Property)it.next();
                    if( (p.getAttributes()&0xFFFF0000)==id) {
                    	JSClass d = new JSClass(p.getPropertyName(),(FBSObject)regObject.get(p.getPropertyName()));
                    	list.add(d);
                    }
                }
                JSClass[] classes = (JSClass[])list.toArray(new JSClass[list.size()]);
                new QuickSort.ObjectArray(classes) {
                    public int compare(Object o1, Object o2) {
                        JSClass c1 = (JSClass)o1;
                        JSClass c2 = (JSClass)o2;
                        return compareString(c1.getName(),c2.getName());
                    }
                }.sort();
                return classes;
            }
        } catch( Exception e ) {}
        return null;
    }

    public static Descriptor.Member[] getMethods(FBSObject o) {
        // Get all the descriptors
        Set set = new HashSet();
        o.getAllPropertiesForHelp(set);
        if (set.size()==0){
            if (o instanceof GeneratedWrapperObject){
                GeneratedWrapperObject gwo=(GeneratedWrapperObject)o;
                //fillAllPropertiesForHelp(set,gwo.getMethodMap());
                gwo.getAllPropertiesForHelp(set);
            }
        }

        // Convert it to a sorted array
        Descriptor.Member[] members = (Descriptor.Member[])set.toArray(new Descriptor.Member[set.size()]);
        new QuickSort.ObjectArray(members) {
            public int compare(Object o1, Object o2) {
                Descriptor.Member m1 = (Descriptor.Member)o1;
                Descriptor.Member m2 = (Descriptor.Member)o2;
                int r = m1.getMemberType() - m2.getMemberType();
                if( r!=0 ) {
                    return r;
                }
                if( m1.isStatic()!=m2.isStatic() ) {
                    return m1.isStatic() ? 1 : -1;
                }
                return compareString(m1.getName(),m2.getName());
            }
        }.sort();
        return members;
    }
    
    public static int compareString(String s1, String s2) {
    	// Take care of the "Notes" extension with @Functions
        int len1=s1.length();
        int len2=s2.length();
        for (int i1=0, i2=0; i1<len1 && i2<len2; i1++, i2++) {
            char c1 = s1.charAt(i1);
            char c2 = s2.charAt(i2);
            if (c1 != c2) {
            	if(c1=='@' && c2!='@') return 1;
            	if(c1!='@' && c2=='@') return -1;
                c1 = Character.toLowerCase(c1);
                c2 = Character.toLowerCase(c2);
                if (c1 != c2) {
                    return c1 - c2;
                }
            }
        }
        return len1 - len2;
    }
}