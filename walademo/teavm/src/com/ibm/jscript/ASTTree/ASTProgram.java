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

import com.ibm.jscript.InterpretException;
import com.ibm.jscript.JSExpression;
import com.ibm.jscript.JScriptResources;
import com.ibm.jscript.JavaScriptException;
import com.ibm.jscript.engine.IExecutionContext;
import com.ibm.jscript.engine.JSOptions;
import com.ibm.jscript.engine.ProgramContext;
import com.ibm.jscript.types.FBSGlobalObject;
import com.ibm.jscript.types.FBSObject;
import com.ibm.jscript.types.FBSType;
import com.ibm.jscript.types.FBSUndefined;

public class ASTProgram extends ASTNode {

    private NodeVector nodes = new NodeVector();
   
    public ASTProgram() {
    }

    public void add(ASTNode node){
        nodes.add(node);
    }

    public int getSlotCount(){
        return nodes.getCount();
    }

    public ASTNode readSlotAt(int index){
        return nodes.get(index);
    }

    public FBSType getResultType(FBSType.ISymbolCallback symbolCallback) {
        if( nodes.size()>0 ) {
            FBSType t = null;
            for( int i=0; i<nodes.size(); i++ ) {
                if( t==null ) {
                    t = ((ASTNode)nodes.get(i)).getResultType(symbolCallback);
                } else {
                    FBSType t2 = ((ASTNode)nodes.get(i)).getResultType(symbolCallback);
                    if( !t.equals(t2) ) {
                        return FBSType.undefinedType;
                    }
                }
            }
            return t;
        }
        return FBSType.undefinedType;
    }

    public void interpret(ASTNode caller, IExecutionContext context,InterpretResult result) throws JavaScriptException {
        interpret(context,result);
    }

    public void interpret(IExecutionContext context,InterpretResult result) throws JavaScriptException {
        try {
            interpretEx(context,result);
        } catch( InterpretException ie ) {
            throw ie.fillException(context,this);
        }
    }

    /**
     * extended interpret method that returns at the end the GlobalObject.
     * This method is useful for libraries.
     */
    public FBSGlobalObject interpretEx(JSExpression expression, FBSGlobalObject globalObject, FBSObject scopePrototypes, FBSObject _this, String[] importedLibraries,InterpretResult result) throws InterpretException {
        // Create the execution context
    	if(globalObject==null) {
        	globalObject= new FBSGlobalObject();
    	}
        IExecutionContext prgContext= new ProgramContext(expression,globalObject,scopePrototypes,_this,importedLibraries);
        return interpretEx(prgContext,result);
    }

    public FBSGlobalObject interpretEx(IExecutionContext prgContext,InterpretResult result) throws InterpretException{
        try {
            for(int i=0;i<getSlotCount();i++){
                ASTNode n=readSlotAt(i);
                if (n!=null){
                    try { 
                        n.interpret(this, prgContext, result);
                    } catch( AbruptReturnException ex ) {
                        result.setNormal(ex.getValue());
                        return prgContext.getGlobalObject();
                    } catch( AbruptException ex ) {
                        result.setNormal(FBSUndefined.undefinedValue);
                        return prgContext.getGlobalObject();
                    }
                }
            }
        } catch( InterpretException ie ) {
            throw ie.fillException(prgContext,this);
        } catch( JavaScriptException ie ) {
            // Should not arrive!!!
            throw new InterpretException( ie, JScriptResources.getString("ASTProgram.Unexpected.Exception") ); //$NON-NLS-1$
        }
        if(prgContext instanceof ProgramContext) { // Not true in case of eval()
        	if( JSOptions.get().isDebugAllowed() ) {
        		((ProgramContext)prgContext).getDebugInspector().endOfProgram();
        	}
        }
        return prgContext.getGlobalObject();
    }
    
    public Object visit(ASTNodeVisitor v, Object param) {
	return v.visitProgram(this, param);
    }    
}
