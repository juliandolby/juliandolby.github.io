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
import com.ibm.jscript.JScriptResources;
import com.ibm.jscript.JavaScriptException;
import com.ibm.jscript.engine.IExecutionContext;
import com.ibm.jscript.engine.JSOptions;
import com.ibm.jscript.std.FunctionObject;
import com.ibm.jscript.std.StringObject;
import com.ibm.jscript.types.FBSObject;
import com.ibm.jscript.types.FBSReference;
import com.ibm.jscript.types.FBSType;
import com.ibm.jscript.types.FBSValue;
import com.ibm.jscript.types.FBSValueVector;
import com.ibm.jscript.types.JavaPackageObject;
import com.ibm.jscript.util.FastStringBuffer;
import com.ibm.jscript.util.FastStringBufferPool;
import com.ibm.jscript.util.StringUtil;

public class ASTCall extends ASTNode{

    private ASTNode leftNode;
    private ASTArgumentList arguments;

    public ASTCall(ASTNode leftNode, ASTArgumentList arguments) {
        this.leftNode = leftNode;
        this.arguments = arguments;
    }

    public String getTraceString(){
        return leftNode.getTraceString()+"()"; //$NON-NLS-1$
    }

    public ASTNode getLeftNode() {
        return leftNode;
    }

    public ASTArgumentList getASTArgumentList() {
        return arguments;
    }

    public int getSlotCount(){
        return arguments==null ? 1 : 2;
    }

    public ASTNode readSlotAt(int index){
        switch(index){
            case 0:  return leftNode;
            case 1:  return arguments;
        }
        return null;
    }

    public FBSType getResultType(FBSType.ISymbolCallback symbolCallback) {
        FBSType t=leftNode.getResultType(symbolCallback);
        if( t.isUndefined() || t.isInvalid() ) {
            return t;
        }
        if( arguments!=null ) {
            FBSType[] args=new FBSType[arguments.getSlotCount()];
            for(int i=0; i<args.length; i++) {
                args[i]=getASTArgumentList().readSlotAt(i).getResultType(symbolCallback);
            }
            return t.getCall(args);
        }
        return t.getCall(new FBSType[0]);
    }

    public void interpret(ASTNode caller, IExecutionContext context,InterpretResult result) throws JavaScriptException {
        try {
            // Construct the object instance
            leftNode.interpret(this,context,result);
            FBSValue exprValue= result.getFBSValue();
            FBSObject o = (exprValue instanceof FBSObject) ? (FBSObject)exprValue : null;
            if (o==null || !o.supportCall()){
                if (result.isReference()) {
                    FBSReference r=(FBSReference)result.getReference();
                    String member=r.getPropertyName();

                    // Construct the arguments for debug puprpose only
                    FBSValueVector args=null;
                    if (arguments!=null && arguments.getSlotCount()>0){
                        arguments.interpret(this,context,result);
                        args=(FBSValueVector)result.getFBSValue().toJavaObject();
                    }else{
                        args=FBSValueVector.emptyVector; // to avoid sending null object to the function call
                    }

                    // Special case for string.length()
                	if( r.getBase() instanceof StringObject && member.equals("length")) { //$NON-NLS-1$
    	            	if( JSOptions.get().hasStringLengthAsMethod() ) {
    	            		if(args.getCount()==0) {
    	                        result.setNormal(exprValue);
    	                        return;
    	            		}
    	            	}
                	}
                	
                    // If we have an error here, this is because the symbol is not known and
                    // a java package object has been created.
                    // So we report a more comprehensive error here.
                    if( r.getBase() instanceof JavaPackageObject ) {
                        if( leftNode instanceof ASTMember ) {
                            ASTMember m=(ASTMember)leftNode;
                            throw new InterpretException(
                                StringUtil.format(JScriptResources.getString("ASTCall.NotAValidObject.Exception"), //$NON-NLS-1$
                                getMethodSignature(member,args), m.getLeftNode().getTraceString()));
                        }
                        throw new InterpretException(
                            StringUtil.format(JScriptResources.getString("ASTCall.NotAValidObject.Exception"), //$NON-NLS-1$
                            getMethodSignature(member,args), leftNode.getTraceString()));
                    }
                    String type=r.getBase().getTypeAsString();
                    throw new InterpretException(
                            StringUtil.format(JScriptResources.getString("ASTCall.MethodCall.Exception"), //$NON-NLS-1$
                            getMethodSignature(member,args), type));
                }
                throw new InterpretException(
                            StringUtil.format(JScriptResources.getString("ASTCall.CannotCallObj.Exception"), //$NON-NLS-1$
                            leftNode.getTraceString()));
            }

            FBSObject object;
            if (result.isReference()) {
                object=result.getReference().getBase();
            }else{
                object=null;
            }
            if (object instanceof FunctionObject.FBSActivationObject){
                object=null;
            }

            FBSObject func=(FBSObject)exprValue;
            func.call(context,this,arguments,object,result);
        } catch( InterpretException ie ) {
            throw ie.fillException(context,this);
        }
    }
    private String getMethodSignature(String functionName, FBSValueVector args) {
        FastStringBuffer b = FastStringBufferPool.get();
        try {
            b.append( functionName );
            b.append( "(" ); //$NON-NLS-1$
            if( args!=null ) {
                for( int i=0; i<args.size(); i++ ) {
                    if(i>0) {
                        b.append( ", " ); //$NON-NLS-1$
                    }
                    if( args.get(i)!=null ) {
                        b.append(args.get(i).getTypeAsString());
                    } else {
                        b.append( "<null>" ); //$NON-NLS-1$
                    }
                }
            }
            b.append( ")" ); //$NON-NLS-1$
            return b.toString();
        } finally {
            FastStringBufferPool.recycle(b);
        }
    }

    public Object visit(ASTNodeVisitor v, Object param) {
	return v.visitCall(this, param);
    }
}
