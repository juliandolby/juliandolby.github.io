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
package com.ibm.jscript.engine;

import java.io.PrintStream;

import com.ibm.jscript.IValue;
import com.ibm.jscript.InterpretException;
import com.ibm.jscript.types.FBSObject;
import com.ibm.jscript.types.JavaWrapperObject;
import com.ibm.jscript.util.StringUtil;
import com.ibm.jscript.util.SystemCache;

/**
 * Options management.
 */
public class JSOptions {

	private static JSOptions jsOptions;

	public static JSOptions get() {
        if(jsOptions==null) {
            String optionsClass = System.getProperty("ibmjs.options");
            if(!StringUtil.isEmpty(optionsClass)) {
                try {
                    Class c = Class.forName(optionsClass);
                    jsOptions = (JSOptions)c.newInstance();
                } catch(Throwable t ) {
                    t.printStackTrace();
                }
            }
            if(jsOptions==null) {
                jsOptions = new JSOptions(false);
            }
        }
		return jsOptions;
	}

	public static void setOptions( JSOptions options ) {
		jsOptions = options;
	}
    
	// Data members
	private SystemCache exprCache;

	protected JSOptions(boolean useCache) {
		if(useCache) {
			this.exprCache = new SystemCache("JS Expression", 400); //$NON-NLS-1$
		}
	}
	

	// Regular expression management /////////////////////////////////////////
	
	public static final int REGEXP_NOTSUPPORTED   = 0;
	public static final int REGEXP_JAKARTA_REGEXP = 1;
	public static final int REGEXP_JDK_14         = 2;
	
	public int getRegExpMode() {
		return REGEXP_JAKARTA_REGEXP;
	}
	
	
	// Language extensions ///////////////////////////////////////////////////
	
	public boolean hasStringLengthAsMethod() {
		return false;
	}

	public boolean hasStringExtendedMethods() {
		return false;
	}
	
	public boolean hasGlobalObjectExtensions() {
		return false;
	}

	public boolean hasJUnitExtensions() {
		return false;
	}

	public boolean hasObjectPrototypeExtensions() {
		return false;
	}

	public boolean hasMathExtensions() {
		return false;
	}

	public boolean hasListOperator() {
		return false;
	}

	public boolean hasRhinoExtensions() {
		return false;
	}

	public boolean hasJavaBeanAccess() {
		return false;
	}

	public boolean autoConvertJavaArgsToString() {
		return false;
	}

	public boolean ignoreJavaCallAmbiguities() {
		return false;
	}
	
	
	
	// Log ///////////////////////////////////////////////////////////////////
	
	public PrintStream getStandardStream() {
		return System.out;
	}
	
	public void print(String str) {
		getStandardStream().print(str);
	}
	
	public void println(String str) {
		getStandardStream().println(str);
	}
	
	public void exception(Throwable t, String msg) {
		if(!StringUtil.isEmpty(msg)) {
			println(msg);
		}
		t.printStackTrace(getStandardStream());
	}

	
	// Debugger //////////////////////////////////////////////////////////////

	public boolean isDebugAllowed() {
		return false;
	}

	// Expression cache //////////////////////////////////////////////////////

	public boolean hasExpressionCache() {
		return exprCache!=null;		
	}
	
	public Object getCachedExpression(String expr) {
		return exprCache.get(expr);
	}
	
	public void putCachedExpression(String expr, Object expression) {
		exprCache.put(expr,expression);
	}
	
	
	// Profiler //////////////////////////////////////////////////////////////

	public boolean isProfilerEnabled() {
		return false;
	}
	
	public Profiler getProfiler() {
		return null;
	}
	
	public interface Profiler {
		public long getCurrentTime();
		public Object startProfileBlock(String profileType, String param);
		public void endProfileBlock(Object aggregator, long startTime);
	}

	
	// Libraries resolver  ///////////////////////////////////////////////////
	
	public interface ILibrary {
		public String getName();
		public boolean isExpired() throws InterpretException;
		public void reload() throws InterpretException;
		public FBSObject getGlobalObject() throws InterpretException;
	}
	
	public boolean hasJSLibrary() {
		return false;
	}
	
	public ILibrary loadLibrary(String libName) throws InterpretException {
		return null;
	}

	
	// Java class loader /////////////////////////////////////////////////////
    public Class loadClass( String className ) throws ClassNotFoundException {
    	// Default class loader
    	ClassLoader c = Thread.currentThread().getContextClassLoader();
    	if(c!=null) {
            try {
                return c.loadClass(className);
            } catch(ClassNotFoundException ex) {
                // Fails to the standard class loader
            }
    	}
        return Class.forName(className);
    }

    // Generated wrappers
    public boolean useGeneratedWrappers() {
    	return false;
    }

    // Custom wrapper creation
    public JavaWrapperObject wrapObject(Object o) {
    	return null;
    }

    
    // Custom properties access
    public IValue getProperty(Object object, String propertyName) throws InterpretException {
    	return null;
    }

    public boolean putProperty(Object object, String propertyName, IValue value) throws InterpretException {
    	return false;
    }
}
