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


/**
 * Compilation error.
 */
public class ScriptError {

    public static final int WARNING = 0;
    public static final int ERROR   = 1;
    
    private int type;
    private int code;
    private String message;
    private int errorLine;
    private int errorCol;
    
    public ScriptError(int type, int code, String message, int errorLine, int errorCol) {
        this.type = type;
        this.code = code;
        this.message = message;
        this.errorLine = errorLine;
        this.errorCol = errorCol;
    }
    
    /**
     * @return Returns the code.
     */
    public int getCode() {
        return code;
    }
    
    /**
     * @return Returns the errorCol.
     */
    public int getErrorCol() {
        return errorCol;
    }
    
    /**
     * @return Returns the errorLine.
     */
    public int getErrorLine() {
        return errorLine;
    }
    
    /**
     * @return Returns the message.
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * @return Returns the type.
     */
    public int getType() {
        return type;
    }
}
