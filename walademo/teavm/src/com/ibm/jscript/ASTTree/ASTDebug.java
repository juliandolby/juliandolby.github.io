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
package com.ibm.jscript.ASTTree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import com.ibm.jscript.InterpretException;
import com.ibm.jscript.JavaScriptException;
import com.ibm.jscript.debug.DebugInspector;
import com.ibm.jscript.engine.FunctionContext;
import com.ibm.jscript.engine.IExecutionContext;
import com.ibm.jscript.engine.JSOptions;
import com.ibm.jscript.engine.ProgramContext;
import com.ibm.jscript.std.FunctionObject;
import com.ibm.jscript.types.FBSObject;
import com.ibm.jscript.types.FBSReference;
import com.ibm.jscript.types.FBSType;
import com.ibm.jscript.types.FBSValue;

public class ASTDebug extends ASTNode implements ILabelledStatement {

	public static ASTNode create(ASTNode statement) {
		if(JSOptions.get().isDebugAllowed()) {
			return new ASTDebug(statement);
		}
		return statement;
	}
	
	private ASTNode statement;
	
	private DebugInspector getDebugInspector(IExecutionContext context)  {
		//JEFF: Just walk up to the ProgramContext and get the DebugInspector.  Phil, there's no chance 
		//      of an infinite loop here, is there?  
		while (context instanceof FunctionContext)
				context = ((FunctionContext)context).getParentContext();
		return ((ProgramContext)context).getDebugInspector();
						
	}
	
	public ASTDebug(ASTNode statement) {
    	// assert not null
    	this.statement = statement;
    }

	public void setLabelName(String label) {
		if( statement instanceof ILabelledStatement ) {
			((ILabelledStatement)statement).setLabelName(label);
		}
	}
	
    public int getSlotCount(){
        return 1;
    }

    public ASTNode readSlotAt(int index){
        return statement;
    }

    public FBSType getResultType(FBSType.ISymbolCallback symbolCallback) {
    	return statement.getResultType(symbolCallback);
    }
    
    public void interpret(ASTNode caller, IExecutionContext context,InterpretResult result) throws JavaScriptException {
    	//JEFF: Tthe DebugInspector is now created by the ProgramContext (since a static one couldn't easily handle
    	//      multiple invocations, or interpreters running on different threads), so we need to get the Debuginspector
    	//      from the ProgramContext:
    	if(DebugInspector.isDebuggerConnected()) {
    	    ASTDebug oldStatement = context.getDebugStatement();
    	    try {
    		    context.setDebugStatement(this);
    		    getDebugInspector(context).setExecutionContext(context, this);
    		    statement.interpret(caller,context,result);
    	    } finally {
    	        context.setDebugStatement(oldStatement);
    	    }
    	} else {
    	    statement.interpret(caller,context,result);
        }
    	// System.out.println("\nASTDebug");
        // dumpStackTrace(context);
        // dumpSymbols(context);
    }
    
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // Some IExecution context introspectors

    // SYMBOLS
    public void dumpSymbols(IExecutionContext context) {
		System.out.println( "  Symbols:" ); //$NON-NLS-1$
    	String[] symbols = context.getJSVariables();
    	for( int i=0; i<symbols.length; i++ ) {
    		String name = symbols[i];
    		FBSReference ref = context.findIdentifier(name);
    		try {
    			dump(2,name,ref.getValue());
    		} catch( InterpretException ex ) {    			
    		}
    	}
    }
    private void dump( int level, String name, FBSValue value ) {
    	// Prevent infinite recursion
    	if(level>4) {
    		return;
    	}
    	if( value instanceof FunctionObject ) {
    		// do not dump...
    		return;
    	}
    	if( !value.isObject() ) {
    		System.out.println(indent(level)+name+"="+value.stringValue()); //$NON-NLS-1$
    	} else {
    		FBSObject o = (FBSObject)value;
    		if( name!=null ) {
    			System.out.println(indent(level)+name+":"+o.getTypeAsString()); //$NON-NLS-1$
    		}
    		ArrayList l = new ArrayList();
    		for( Iterator it=o.getPropertyKeys(); it.hasNext(); ) {
    			l.add(it.next());
    		}
    		String[] ss = (String[])l.toArray(new String[l.size()]);
        	Arrays.sort(ss);
    		for( int i=0; i<ss.length; i++ ) {
    			String pname = ss[i];
        		try {
        			dump( level+1, pname, o.get(pname) );
        		} catch( InterpretException ex ) {    			
        		}
    		}
    	}
    }
    
    
    // STACK TRACE
    public void dumpStackTrace(IExecutionContext context) {
		System.out.println( "  Stack Trace:" ); //$NON-NLS-1$
		dumpStackTrace0(context);    	
    }
    private void dumpStackTrace0(IExecutionContext context) {
    	if( context instanceof FunctionContext ) {
    		// Dump the caller first
    		FunctionContext fctContext = (FunctionContext)context;
    		dumpStackTrace0(fctContext.getParentContext());
    		// And the current object
    		FunctionObject fct = fctContext.getFunctionObject();
    		System.out.println( "    "+fct.getFunctionNode().getName()+"()" ); //$NON-NLS-1$ //$NON-NLS-2$
    		try {
    			FBSValue args = fctContext.getVariableObject().get("arguments"); //$NON-NLS-1$
    			if( args!=null && !args.isUndefined() ) {
    			    dump( 3, null, args);
    			}
    		} catch( InterpretException ex ) {    			
    		}
    	}
    }
    
    // UTILITIES
    private String indent(int level) {
    	switch(level) {
    		case 0: 	return ""; //$NON-NLS-1$
    		case 1: 	return "  "; //$NON-NLS-1$
    		case 2: 	return "    "; //$NON-NLS-1$
    		case 3: 	return "      "; //$NON-NLS-1$
    		case 4: 	return "        "; //$NON-NLS-1$
    		case 5: 	return "          "; //$NON-NLS-1$
    		case 6: 	return "            "; //$NON-NLS-1$
    		case 7: 	return "              "; //$NON-NLS-1$
    	}
    	StringBuffer b = new StringBuffer(level*2);
    	for( int i=0; i<level; i++ ) {
    		b.append("  "); //$NON-NLS-1$
    	}
    	return b.toString();
    }

    public Object visit(ASTNodeVisitor v, Object param) {
	return v.visitDebug(this, param);
    }
}
