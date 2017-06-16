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

import java.io.PrintStream;
import java.util.ArrayList;

/**
 * Result of a script analysis.
 */
public class ParserResult {

    private ArrayList errors;
    
    ParserResult() {
        this.errors = new ArrayList();
    }
    
    public int getErrorCount() {
        return errors.size();
    }

    public ScriptError getError(int index) {
        return (ScriptError)errors.get(index);
    }
    
    public void addError(ScriptError err) {
        errors.add(err);
    }
    
    public void dumpErrors(PrintStream ps) {
        ps.println(getErrorCount()+" errors"); //$NON-NLS-1$
        for( int i=0; i<getErrorCount(); i++) {
            ScriptError err = getError(i);
            ps.println(err.getErrorLine()+","+err.getErrorCol()+": "+err.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }
}
