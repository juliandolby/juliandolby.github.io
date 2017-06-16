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

public abstract class FBSReference extends FBSResult {

    public FBSReference() {
    }
    
    public abstract String getPropertyName();

    public abstract FBSObject getBase();
    public abstract FBSValue getValue() throws InterpretException;
    public abstract void putValue(FBSValue value) throws InterpretException;
}