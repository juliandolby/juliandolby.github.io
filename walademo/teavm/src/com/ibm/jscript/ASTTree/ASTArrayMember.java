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
import com.ibm.jscript.types.FBSObject;
import com.ibm.jscript.types.FBSReference;
import com.ibm.jscript.types.FBSReferenceByName;
import com.ibm.jscript.types.FBSType;
import com.ibm.jscript.types.FBSValue;
import com.ibm.jscript.util.StringUtil;

public final class ASTArrayMember extends ASTNode{

    public static final int LEFT=0;
    public static final int RIGHT=1;

    private ASTNode leftNode;
    private ASTNode rightNode;

    public ASTArrayMember(ASTNode leftNode, ASTNode rightNode) {
        this.leftNode = leftNode;
        this.rightNode = rightNode;
    }

    public String getTraceString(){
        return leftNode.getTraceString()+"["+rightNode.getTraceString()+"]"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    public int getSlotCount(){
        return 2;
    }

    public ASTNode readSlotAt(int index){
        switch(index){
            case LEFT: return leftNode;
            case RIGHT: return rightNode;
        }
        return null;
    }

    public ASTNode getLeftNode() {
        return leftNode;
    }

    public ASTNode getRightNode() {
        return rightNode;
    }

    public FBSType getResultType(FBSType.ISymbolCallback symbolCallback) {
        FBSType t=leftNode.getResultType(symbolCallback);
        if( t.isUndefined() ) {
            return t;
        }
        if( t.isInvalid() ) {
        	// TODO...
            if( !(leftNode instanceof ASTIdentifier) ) {
                return t;
            }
            String idn=((ASTIdentifier)leftNode).getIdentifierName();
            t = FBSType.getJavaPackage(idn);
        }
        if( t.isJSArray() ) {
            return FBSType.undefinedType;
        }
        if( t.isJavaObject() ) {
            Class jc = t.getJavaClass();
            if( jc!=null && jc.isArray() ) {
                return FBSType.getType(jc.getComponentType());
            }
        }

        // if the parameter is a constant, use it as the member name
        if( rightNode.isConstant() ) {
            try {
                FBSValue v = rightNode.evaluateConstant();
                return t.getMember(v.stringValue());
            } catch( InterpretException ex ) {}
        }
        return FBSType.undefinedType;
    }

    public void interpret(ASTNode caller, IExecutionContext context,InterpretResult result) throws JavaScriptException {
        try {
            leftNode.interpret(this,context,result);
            FBSValue leftValue = result.getFBSValue();
            if (!leftValue.canFBSObject()){
                if( leftValue.isNull() ) {
                    throw new InterpretException(StringUtil.format(JScriptResources.getString("ASTArrayMember.LeftSideIsNull.Exception"),leftNode.getTraceString())); //$NON-NLS-1$
                }
                if( leftValue.isUndefined() ) {
                    throw new InterpretException(StringUtil.format(JScriptResources.getString("ASTArrayMember.LeftSideIsUndefined.Exception"),leftNode.getTraceString())); //$NON-NLS-1$
                }
                throw new InterpretException(StringUtil.format(JScriptResources.getString("ASTArrayMember.LeftSideIsNotAnObj.Exception"),leftNode.getTraceString())); //$NON-NLS-1$
            }
            FBSObject leftObject=leftValue.toFBSObject();

            rightNode.interpret(this,context,result);
            FBSValue rightValue= result.getFBSValue();

            // Optimize array access, by using a number property instead of a string
            if( leftObject.hasIndexedProperty() && rightValue.isNumber() ) {
                result.setNormal(leftObject.getPropertyReference(rightValue.intValue()));
            } else {
            	String id = rightValue.stringValue();
            	FBSReference ref = leftObject.getPropertyReference(id);
            	if(ref==null) {
            		// PHIL: there is not here any variable creation, only properties that may return undefined 
            		ref = new FBSReferenceByName(leftObject,id);
            	}
                result.setNormal(ref);
            }
        } catch( InterpretException ie ) {
                throw ie.fillException(context,this);
        }
    }

    public Object visit(ASTNodeVisitor v, Object param) {
	return v.visitArrayMember(this, param);
    }
}
