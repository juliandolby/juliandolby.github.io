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

import com.ibm.jscript.ASTTree.ASTNode;
import com.ibm.jscript.engine.IExecutionContext;
import com.ibm.jscript.types.FBSValue;
import com.ibm.jscript.types.JavaAccessObject;
import com.ibm.jscript.util.StringUtil;

/**
 * Javascript Interpreter Exception.
 * This exception is thrown when the interpret encounters a runtime exception,
 * for example when calling a method that does not exist.<br>
 * This exception shoud only be instanciated by the engine, not by custom code.
 */
public class InterpretException extends JavaScriptException {

    // Execution points
    private ASTNode node;
    private String message;
    private JSExpression expression;
    private FBSValue exceptionObject;

    public InterpretException() {
        super((Exception)null, JScriptResources.getString("InterpretException.Interpret.Exception")); //$NON-NLS-1$
    }

    public InterpretException(Exception e) {
        super(e, JScriptResources.getString("InterpretException.Interpret.Exception")); //$NON-NLS-1$
        if( e!=null ) {
            this.exceptionObject=new JavaAccessObject(e.getClass(), e);
        }
    }

    public InterpretException(Exception e,String msg) {
        super(e,msg);
        if( e!=null ) {
            this.exceptionObject=new JavaAccessObject(e.getClass(), e);
        }
    }

    public InterpretException(Throwable javaException, Exception e,String msg) {
        super(e,msg);
        this.exceptionObject = new JavaAccessObject(javaException.getClass(),javaException);
    }

    public InterpretException(String err) {
        super(err);
    }

    public InterpretException(FBSValue exceptionObject, Exception e,String msg) {
        super(e,msg);
        this.exceptionObject = exceptionObject;
    }

    public String getMessage() {
        if( StringUtil.isEmpty(message) ) {
            return super.getMessage();
        }
        return message;
    }

    public JSExpression getExpression() {
        return expression;
    }

    public void setExpression(JSExpression expression) {
        this.expression = expression;
    }

    /**
     * Get the line number where the error occurred.
     * @return the error line number
     */
    public int getErrorLine() {
        return this.node!=null ? node.getBeginLine() : -1;
    }

    /**
     * Get the column number where the error occurred.
     * @return the error column number
     */
    public int getErrorCol() {
        return this.node!=null ? node.getBeginCol() : -1;
    }

    /**
     * Get the line number where the error occurred ends.
     * @return the error line number
     */
    public int getErrorLineEnd() {
        return this.node!=null ? node.getEndLine() : -1;
    }

    /**
     * Get the column number where the error occurred ends.
     * @return the error column number
     */
    public int getErrorColEnd() {
        return this.node!=null ? node.getEndCol() : -1;
    }

    public InterpretException fillException(IExecutionContext context, ASTNode node ) {
        if( this.node==null ) {
            this.node = node;
            int line = node.getBeginLine();
            int col = node.getBeginCol();
            message = StringUtil.format( JScriptResources.getString("InterpretException.ScriptError.Exception"), //$NON-NLS-1$
                        StringUtil.toString(line),
                        StringUtil.toString(col),
                        super.getMessage() );
        }
        return this;
    }

    public ASTNode getNode() {
        return node;
    }

    public FBSValue getExceptionObject() {
        return exceptionObject;
    }
}
