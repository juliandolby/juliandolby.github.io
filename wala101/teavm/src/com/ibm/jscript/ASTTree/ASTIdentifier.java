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

import java.io.PrintStream;

import com.ibm.jscript.InterpretException;
import com.ibm.jscript.JavaScriptException;
import com.ibm.jscript.engine.IExecutionContext;
import com.ibm.jscript.types.FBSObject;
import com.ibm.jscript.types.FBSReference;
import com.ibm.jscript.types.FBSReferenceByName;
import com.ibm.jscript.types.FBSType;
import com.ibm.jscript.types.Registry;

public class ASTIdentifier extends ASTNode {

    private String idName;

    public ASTIdentifier(){
    }

    public ASTIdentifier(String name){
        setIdentifierName(name);
    }

    public ASTIdentifier(String name,int line0,int col0, int line1, int col1) {
        setIdentifierName(name);
        this.setSourcePos(line0,col0,line1,col1);
    }

    public FBSType getResultType(FBSType.ISymbolCallback symbolCallback) {
        if( idName!=null ) {
            // 1. look for a variable

            // 2. look for a function parameter

            // 3. look for an application global symbol
            if(symbolCallback!=null) {
                FBSType type = symbolCallback.getSymbolType(idName);
                if(!type.isInvalid()) {
                    return type;
                }
            }

            // 4. Look in the global registry
            if( Registry.getRegistryObject().hasProperty(idName) ) {
                return Registry.getRegistryObject().getFBSType().getMember(idName);
            }

            // 5. look into the global prototypes
            for( int i=0; i<Registry.getGlobalPrototypeCount(); i++) {
            	FBSObject o = Registry.getGlobalPrototype(i);
                if( o.hasProperty(idName) ) {
                    return o.getFBSType().getMember(idName);
                }
            }
        }
        return FBSType.undefinedType;
    }

    public void setIdentifierName(String name){
        idName=name;
    }

    public void setToken(String token){
        setIdentifierName(token);
    }

    public String getIdentifierName(){
        return idName;
    }

    public String getTraceString(){
        return getIdentifierName();
    }


    public int getSlotCount(){
        return 0;
    }

    public ASTNode readSlotAt(int index){
        return null;
    }

    public void interpret(ASTNode caller, IExecutionContext context,InterpretResult result) throws JavaScriptException {
        try {
            FBSReference ref = context.findIdentifier(idName);
            if( ref!=null ) {
                result.setNormal(ref);
                return;
            }

            // not found !
            // check the library
            FBSObject libObj=context.getGlobalObject().searchLibrary(idName);
            if (libObj!=null){
                result.setNormal(libObj.getPropertyReference(idName));
                return;
            }

            // Ok, so in this case, the symbol does not currently exist.
            // So we make an implicit reference to the global object, in case an assign operator
            // wants to set a value to it.
            result.setNormal(new FBSReferenceByName.UndefinedVariable(context.getGlobalObject(),idName));
        } catch( InterpretException ie ) {
            throw ie.fillException(context,this);
        }
    }

    public String toString(){
        return "ASTIdentifier : "+getIdentifierName(); //$NON-NLS-1$
    }

    protected void extraInfo(PrintStream ps,String indent){
        ps.print("name="); //$NON-NLS-1$
        ps.print(idName);
    }

    public Object visit(ASTNodeVisitor v, Object param) {
	return v.visitIdentifier(this, param);
    }
}
