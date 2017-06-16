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

import java.util.Iterator;

import com.ibm.jscript.InterpretException;
import com.ibm.jscript.engine.IExecutionContext;
import com.ibm.jscript.engine.JSOptions;
import com.ibm.jscript.util.StringUtil;

public class JavaPackageObject extends FBSObject{

	public static final String PACKAGES = "Packages";
	public static final String PACKAGES_START = "Packages.";
	
    private String packageName;

    private JavaPackageObject(String pckgName) {
        this.packageName = pckgName;
    }
    
    public String getPackageName() {
    	return packageName;
    }

    public FBSType getFBSType() {
        return FBSType.getJavaPackage(getTypeAsString());
    }

    public String getTypeAsString(){
        return packageName;
    }

    public boolean hasProperty(String name){
        return true;
    }

    public boolean isJavaNative(){
       return true;
    }

    public FBSValue get(int index){
        return FBSUndefined.undefinedValue;
    }

    public FBSValue get(String name){
        String className;
        if (StringUtil.isEmpty(packageName)){
            className = name;
        } else {
            className = packageName + "." + name;
        }

        // Try to load it as a class here
        try {
            Class c = JSOptions.get().loadClass(className);
            return new JavaAccessObject(c,null);
        } catch(Exception e) {
        }

        // OK, assume it is a package again...
        return new JavaPackageObject(className);
    }

    public static FBSValue getPackage(String name){
        return new JavaPackageObject(name);
    }

    public FBSObject construct(FBSValueVector args)throws InterpretException{
        throw new InterpretException("cannot instantiate object of JavaPackageObject");
    }

    public boolean supportConstruct(){
        return false;
    }

    public boolean hasInstance(FBSValue v){
        return false;
    }

    public FBSValue call(IExecutionContext context,FBSValueVector args,FBSObject _this)throws InterpretException{
        throw new InterpretException("cannot call object of JavaPackageObject");
    }

    public boolean supportCall(){
        return false;
    }

    public boolean createProperty(String name , int attribute, FBSValue value ){
        return false;
    }

    public void put(int index , FBSValue value){
    }

    public void put(String name , FBSValue value){
    }

    public boolean canPut(String name){
        return false;
    }

    public boolean delete(String name){
        return false;
    }

    public FBSValue getDefaultValue(String hint){
        return FBSUndefined.undefinedValue;
    }

    public FBSValue getDefaultValue(int hint){
        return FBSUndefined.undefinedValue;
    }

    public Iterator getPropertyKeys() {
        return null;
    }

    public Object toJavaObject(Class c) throws InterpretException{
        if (c==null || c==JavaPackageObject.class){
            return this;
        }
        throw new InterpretException("Cannot convert JavaPackageObject to "+c.getName());
    }
}
