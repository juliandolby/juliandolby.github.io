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
import com.ibm.jscript.JScriptResources;
import com.ibm.jscript.JavaScriptException;
import com.ibm.jscript.engine.IExecutionContext;
import com.ibm.jscript.engine.JSOptions;
import com.ibm.jscript.types.FBSDefaultObject;
import com.ibm.jscript.types.FBSObject;
import com.ibm.jscript.types.FBSString;
import com.ibm.jscript.types.FBSUtility;
import com.ibm.jscript.types.FBSValue;
import com.ibm.jscript.types.FBSValueVector;

/**
 * Regular expression object. This class provides the common features to regular expressions
 * such as property management (source, global etc). This class is not expected to be subclassed
 * where various regexp implementations implement the 
 */
public class RegExpObject extends FBSDefaultObject {

    public interface REImplementation {
                
        /**
         * Init the engine. 
         * @throws InterpretException
         */
        public void init() throws InterpretException;
        
        /**
         * Apply the regexp pattern (from the constructor) to <i>str</i>.
         * 
         * @param str The value to apply this regexp pattern to
         * @return A FBSValueVector with the matched elements
         * @throws JavaScriptException
         */
        public FBSValueVector exec(String str) throws JavaScriptException;
        
        /**
         * Splits a string based on the regexp from the constructor.
         * 
         * @param str The string to split
         * @return A FBSValueVector with the matched elements
         * @throws JavaScriptException
         */
        public FBSValueVector split(String str, int limit) throws JavaScriptException;
        
        /**
         * Returns true if str matches this regexp
         * @param str
         * @return
         * @throws JavaScriptException
         */
        public FBSValueVector match(String str) throws JavaScriptException;

        /**
         * Returns the index of the first regex match in str
         * 
         * @param str The string to search
         * @return
         * @throws JavaScriptException
         */
        public FBSValue search(String str) throws JavaScriptException;
        
        /**
         * Replaces str with replacement using this.jdkRE regexp.
         * 
         * @param str The source string
         * @param replacement The replacement string
         * @return
         * @throws JavaScriptException
         */
        public FBSValue replace(String str, String replacement) throws JavaScriptException;
        
        /**
         * Replaces current regex with replacement
         * 
         * @param str The new regex
         * @param flags The new flags
         * @return
         * @throws JavaScriptException
         */
        public void compile(String re, String flags) throws JavaScriptException;
    }
    
    
    /**
     * Local vars - Regular Expression engine.
     */
	private REImplementation engine;
	
	/**
	 *  Properties
	 */
	protected static final String PROP_REGEX = "source"; //$NON-NLS-1$
	protected static final String PROP_GLOBAL = "global"; //$NON-NLS-1$
	protected static final String PROP_IGNORECASE = "ignoreCase"; //$NON-NLS-1$
	protected static final String PROP_MULTILINE = "multiline"; //$NON-NLS-1$
	protected static final String PROP_LASTINDEX = "lastIndex"; //$NON-NLS-1$
    
    /**
     * Valid RegExp flags
     */
	protected static final String VALID_FLAGS = "mgi"; //$NON-NLS-1$
    
    /**
     * Standard properties
     */
	protected String source = null;
	protected String flags = null;
	protected boolean global = false;
	protected boolean ignoreCase = false;
	protected boolean multiline = false;
	protected int lastIndex = 0;

    // Internal, for Registry
    public RegExpObject() {
        super(RegExpPrototype.getRegExpPrototype());
    }
	
	/**
	 * Constructor for RegExpObject - calls RegExpObject(str, "").
	 * 
	 * @param regExpStr	The regular expression
	 * @throws InterpretException
	 */
	public RegExpObject(String regExpStr) throws InterpretException {
		this(regExpStr, ""); //$NON-NLS-1$
	}
	
	/**
	 * Constructor for RegExpObject
	 * 
	 * @param regExpStr	The regular expression
	 * @param flags	Regular expression flags (M, I, G)
	 * @throws InterpretException
	 */
	public RegExpObject(String regExpStr, String flags) throws InterpretException {
		super(RegExpPrototype.getRegExpPrototype());
		
    	// Set flag/properties
		setFlags(flags);
		this.source = regExpStr;

    	switch(JSOptions.get().getRegExpMode()) {
    	case JSOptions.REGEXP_JDK_14:
            this.engine = new RegExpJDK14(this);
    		break;
    	case JSOptions.REGEXP_JAKARTA_REGEXP:
            this.engine = new RegExpJakarta(this);
    		break;
    	default:
    		throw new InterpretException(JScriptResources.getString("RegExpObject.RegExpNotSupported.Exception")); //$NON-NLS-1$
    	}
        engine.init();
	}
	
	public void setFlags(String flags) throws InterpretException {
		// verify flags
        if(flags!=null) {
        	for (int i = 0; i < flags.length(); i++) {
        		if (VALID_FLAGS.indexOf(flags.substring(i,i)) == -1 )
        			throw new InterpretException("Invalid RegExp flag '" + flags.substring(i,i));
        	}
            this.global = (flags.indexOf("g") != -1); //$NON-NLS-1$
            this.ignoreCase = (flags.indexOf("i") != -1); //$NON-NLS-1$
            this.multiline = (flags.indexOf("m") != -1); //$NON-NLS-1$
        }
		this.flags = flags;
	}
	
    public FBSValue call(IExecutionContext context,FBSValueVector args,FBSObject _this) throws JavaScriptException {
        if(args.getCount()==2) {
            FBSValue v1 = args.get(0);
            if( v1.isJavaObject() ) {
                if( v1.toJavaObject() instanceof RegExpObject ) {
                    FBSValue v2 = args.get(0);
                    if(v2.isUndefined()) {
                        return v1;
                    }
                }
            }
        }
        return construct(args);
    }
    
    public boolean supportCall() {
        return true;
    }
    
    ////// Constructor
    //TODO: metadata...
    public boolean supportConstruct(){
        return true;
    }

    public FBSObject construct(FBSValueVector args)throws InterpretException{
        JSOptions options = JSOptions.get();
        switch(options.getRegExpMode()) {
        case JSOptions.REGEXP_NOTSUPPORTED:
            throw new InterpretException(JScriptResources.getString("RegExpObject.RegExpNotSupported.Exception")); //$NON-NLS-1$
        case JSOptions.REGEXP_JAKARTA_REGEXP:
        case JSOptions.REGEXP_JDK_14:
            if (args.size() == 1)
                return new RegExpObject(args.get(0).stringValue());
            else if (args.size() == 2)
                return new RegExpObject(args.get(0).stringValue(), args.get(1).stringValue());
            else if (args.size() == 0)
                return new RegExpObject("");
            else
                throw new InterpretException(JScriptResources.getString("RegExpObject.InvalidNumArgs.Exception")); //$NON-NLS-1$
        default:
            throw new InterpretException("Invalid JSOptions.RegExpMode: " + options.getRegExpMode());
        }
    }


    public String getTypeAsString(){
        return "RegExp"; //$NON-NLS-1$
    }

    public String getName(){
        return "RegExp"; //$NON-NLS-1$
    }
        
    public FBSString getFBSString() {
    	return FBSString.get(source);
    }
    
	public FBSValue get(String name) throws InterpretException {
		// TODO Auto-generated method stub
		if (name.equals(PROP_REGEX))
			return FBSUtility.wrap(source);
		else if (name.equals(PROP_GLOBAL))
			return FBSUtility.wrap(global);
		else if (name.equals(PROP_IGNORECASE))
			return FBSUtility.wrap(ignoreCase);
		else if (name.equals(PROP_MULTILINE))
			return FBSUtility.wrap(multiline);
		else if (name.equals(PROP_LASTINDEX))
			return FBSUtility.wrap(lastIndex);
		else
			return super.get(name);
	}
	
	
	/* (non-Javadoc)
	 * @see com.ibm.jscript.types.FBSObject#put(java.lang.String, com.ibm.jscript.types.FBSValue)
	 */
	public void put(String name, FBSValue value) throws InterpretException {
		if(name.equals(PROP_REGEX))
			this.source = value.stringValue();
		else
			super.put(name, value);
	}
	
	/**
     * Apply the regexp pattern (from the constructor) to <i>str</i>.
     * 
     * @param str The value to apply this regexp pattern to
     * @return A FBSValueVector with the matched elements
     * @throws JavaScriptException
     */
    public FBSValueVector exec(String str) throws JavaScriptException {
        return engine.exec(str);
    }
    
    /**
     * Splits a string based on the regexp from the constructor.
     * 
     * @param str The string to split
     * @return A FBSValueVector with the matched elements
     * @throws JavaScriptException
     */
    public FBSValueVector split(String str) throws JavaScriptException {	
    	return split(str, 0);
    }
    
    public FBSValueVector split(String str, int limit) throws JavaScriptException {	
        return engine.split(str, limit);
    }
    
    /**
     * Returns true if str matches this regexp
     * @param str
     * @return
     * @throws JavaScriptException
     */
    public FBSValueVector match(String str) throws JavaScriptException {
        return engine.match(str);
    }
    
    /**
     * Returns the index of the first regex match in str
     * 
     * @param str The string to search
     * @return
     * @throws JavaScriptException
     */
    public FBSValue search(String str) throws JavaScriptException {
        return engine.search(str);
    }
    
    /**
     * Replaces str with replacement using this.jdkRE regexp.
     * 
     * @param str The source string
     * @param replacement The replacement string
     * @return
     * @throws JavaScriptException
     */
    public FBSValue replace(String str, String replacement) throws JavaScriptException {
        return engine.replace(str,replacement);
    }
   
    /**
     * Replaces this.jdkRE regexp with str
     * 
     * @param str The new regular expression
     * @return
     * @throws JavaScriptException
     */
    public FBSValue compile(String str, String flags) throws JavaScriptException {
        engine.compile(str, flags);
        this.source = str;
        this.flags = flags;
		return FBSUtility.wrap(stringValue());
    }

    /**
     * This is an alias for the match(String str) method.
     * 
     * @param str The string to perform a test match for
     * @return
     * @throws JavaScriptException
     */
    public FBSValue test(String str) throws JavaScriptException {
        FBSValueVector val = engine.match(str);
		if (val == null || val.getCount() == 0)
			return FBSUtility.wrap(false);
		else
			return FBSUtility.wrap(true);
    }
    
    public String stringValue() {
		return "/" + source + "/" + flags;
    }
}