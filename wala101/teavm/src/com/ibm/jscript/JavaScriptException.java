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
package com.ibm.jscript;

import com.ibm.jscript.util.TException;

/**
 * Abstract javascript exception
 */
public abstract class JavaScriptException extends TException {

    public JavaScriptException() {
        super(null,JScriptResources.getString("JavaScriptException.JavaScript.Exception")); //$NON-NLS-1$
    }

    public JavaScriptException(Exception e) {
        super(e,JScriptResources.getString("JavaScriptException.JavaScript.Exception")); //$NON-NLS-1$
    }

    public JavaScriptException(String msg) {
        super(null,msg);
    }

    public JavaScriptException(Exception e,String msg) {
        super(e,msg);
    }
}