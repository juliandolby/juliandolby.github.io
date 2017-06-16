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
package com.ibm.jscript.util;

import java.io.PrintStream;
import java.io.PrintWriter;

import com.ibm.jscript.JScriptResources;


/**
 * Define a global exception.
 * @author Philippe Riand
 */
public class TException extends Exception {

    private static boolean hasCause = true;
        
    private Throwable causeMember = this;
	
    /**
     *
     */
    public TException(Exception nextException) {
        this(nextException, nextException==null?"":nextException.getMessage() ); //$NON-NLS-1$
    }
    public TException(Exception nextException, String msg) {
        super(StringUtil.format(msg));
        initCause(nextException);
    }
    public TException( Exception nextException, String msg, Object p1 ) {
        this( nextException,format(msg, p1, null, null, null, null) );
    }
    public TException( Exception nextException, String msg, Object p1, Object p2 ) {
        this( nextException,format(msg, p1, p2, null, null, null) );
    }
    public TException( Exception nextException, String msg, Object p1, Object p2, Object p3 ) {
        this( nextException,format(msg, p1, p2, p3, null, null) );
    }
    public TException( Exception nextException, String msg, Object p1, Object p2, Object p3, Object p4 ) {
        this( nextException,format(msg, p1, p2, p3, p4, null) );
    }
    public TException( Exception nextException, String msg, Object p1, Object p2, Object p3, Object p4, Object p5 ) {
        this( nextException,format(msg, p1, p2, p3, p4, p5) );
    }
    
    private static String format( String msg, Object p1, Object p2, Object p3, Object p4, Object p5 ) {
    	// Phil: do not use MessageFormat for localization
        //return MessageFormat.format(msg, o);
    	FastStringBuffer b = new FastStringBuffer();
    	b.appendFormat(msg,p1,p2,p3,p4,p5);
        return b.toString();
    }

    public static Throwable initCause(Throwable ext, Throwable cause) {
        if(hasCause) {
            ext.initCause(cause);
        } else {
            if(ext instanceof TException) {
                ((TException)ext).initCause(cause);
            }
        }
        return ext;
    }

    public Throwable initCause(Throwable cause) {
        if(hasCause) {
            return super.initCause(cause);
        } else {
            if(this.causeMember!=this) {
                throw new IllegalStateException(JScriptResources.getString("TException.OverwriteCause.Exception")); //$NON-NLS-1$
            }
            if(cause==this) {
                throw new IllegalArgumentException(JScriptResources.getString("TException.AssignSelfAsCause.Exception")); //$NON-NLS-1$
            }
            this.causeMember = cause;
            return this;
        }
    }
    
    public Throwable getCause() {
        if(hasCause) {
            return super.getCause();
        } else {
            return causeMember==this ? null : causeMember;
        }
    }
        
    public void printStackTrace() { 
        if(hasCause) {
            super.printStackTrace();
        } else {
            printStackTrace(System.err);
        }
    }

    public void printStackTrace(PrintStream s) {
        if(hasCause) {
            super.printStackTrace(s);
        } else {
            synchronized(s) {
                for( Throwable t=this; t!=null; ) {
                    if(t!=this) {
                        s.println("Caused by:"); //$NON-NLS-1$
                    }
                    if( t instanceof TException ) {
                        TException te = (TException)t; 
                        te.superPrintStackTrace(s);
                        t = te.getCause();
                    } else {
                        t.printStackTrace(s);
                        t = null;
                    }
                }
            }
        }
    }
    private void superPrintStackTrace(PrintStream s) {
        super.printStackTrace(s);
    }

    public void printStackTrace(PrintWriter w) {
        if(hasCause) {
            super.printStackTrace(w);
        } else {
            synchronized(w) {
                for( Throwable t=this; t!=null; ) {
                    if(t!=this) {
                        w.println("Caused by:"); //$NON-NLS-1$
                    }
                    if( t instanceof TException ) {
                        TException te = (TException)t; 
                        te.superPrintStackTrace(w);
                        t = te.getCause();
                    } else {
                        t.printStackTrace(w);
                        t = null;
                    }
                }
            }
        }
    }
    private void superPrintStackTrace(PrintWriter w) {
        super.printStackTrace(w);
    }
    
/*    
    public static void main( String[] args ) {
        check(new NullPointerException()); 
        check(new TException(new NullPointerException())); 
        check(new TException(new TException(new NullPointerException()))); 
    }
    private static void check(Throwable t) {
        try {
            throw t;
        } catch(Throwable ex) {
            ex.printStackTrace();
        }
    }
*/    
}
