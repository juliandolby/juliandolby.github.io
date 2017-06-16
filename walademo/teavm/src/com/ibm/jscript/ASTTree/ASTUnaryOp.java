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
import com.ibm.jscript.types.FBSBoolean;
import com.ibm.jscript.types.FBSDefaultObject;
import com.ibm.jscript.types.FBSNumber;
import com.ibm.jscript.types.FBSObject;
import com.ibm.jscript.types.FBSString;
import com.ibm.jscript.types.FBSType;
import com.ibm.jscript.types.FBSUndefined;
import com.ibm.jscript.types.FBSValue;

public class ASTUnaryOp extends ASTNode{
    private int typeId;
    private boolean ispostfix=false;

    private ASTNode node;
    public ASTUnaryOp(String token) {
        typeId=ASTConstants.getConstant(token);
    }

    public ASTUnaryOp(String token,int line0,int col0, int line1, int col1) {
        typeId=ASTConstants.getConstant(token);
        this.setSourcePos(line0,col0,line1,col1);
    }

    public ASTUnaryOp(int type) {
        typeId=type;
    }

    public ASTUnaryOp(String token,boolean postfix) {
        typeId=ASTConstants.getConstant(token);
        ispostfix=postfix;
    }

    public ASTUnaryOp(int type,boolean postfix) {
        typeId=type;
        ispostfix=postfix;
    }

    public int getOperator(){
	return typeId;
    }

    public void add(ASTNode node){
        this.node=node;
    }

    public void setPostfix(boolean postfix){
        ispostfix=postfix;
    }

    public boolean isPostfix(){
        return ispostfix;
    }

    public int getSlotCount(){
        return 1;
    }

    public ASTNode readSlotAt(int index){
        return node;
    }

    public void interpret(ASTNode caller, IExecutionContext context,InterpretResult result) throws JavaScriptException {
        try {
            if (ispostfix){
                node.interpret(this,context,result);
                FBSNumber numVal=result.getFBSValue().toFBSNumber();
                if (typeId==ASTConstants.INC){
                    result.getReference().putValue(numVal.incValue());
                } else if (typeId==ASTConstants.DEC){
                    result.getReference().putValue(numVal.decValue());
                }
                result.setNormal(numVal);
                return;
            }

            node.interpret(this,context,result);
            double dblValue;
            FBSNumber num;
            FBSValue value=result.getFBSValue();
            switch(typeId){
                case ASTConstants.DELETE:
                        if (!result.isReference()){
                            result.setNormal(FBSBoolean.TRUE);
                            return;
                        }
                        FBSObject object=result.getReference().getBase();
                        if( object instanceof FBSDefaultObject ) {
                            String pName=result.getReference().getPropertyName();
                            boolean b= ( (FBSDefaultObject)object).delete(pName);
                            result.setNormal(FBSBoolean.get(b));
                        } else {
                            result.setNormal(FBSBoolean.FALSE);
                        }
                        break;

                case ASTConstants.VOID:
                        result.setNormal(FBSUndefined.undefinedValue);
                        break;

                case ASTConstants.TYPEOF:
                        if (result.isReference()){
                            if (result.getReference().getBase()==null){
                                result.setNormal(FBSUndefined.undefinedValue);
                                return;
                            }
                        }
                        value=result.getFBSValue();
                        switch(value.getType()){
                            case FBSValue.UNDEFINED_TYPE: result.setNormal(FBSString.get("undefined"));break; //$NON-NLS-1$
                            case FBSValue.NULL_TYPE: result.setNormal(FBSString.get("null"));break; //$NON-NLS-1$
                            case FBSValue.BOOLEAN_TYPE: result.setNormal(FBSString.get("boolean"));break; //$NON-NLS-1$
                            case FBSValue.NUMBER_TYPE: result.setNormal(FBSString.get("number"));break; //$NON-NLS-1$
                            case FBSValue.STRING_TYPE: result.setNormal(FBSString.get("string"));break; //$NON-NLS-1$
                            case FBSValue.FUNCTION_TYPE:result.setNormal(FBSString.get("function"));break; //$NON-NLS-1$
                            case FBSValue.OBJECT_TYPE: {
                                if(value instanceof FBSDefaultObject) {
                                    result.setNormal(FBSString.get("object")); //$NON-NLS-1$
                                } else {
                                    result.setNormal(FBSString.get(value.getTypeAsString()));
                                }
                            }
                        }
                        break;

                case ASTConstants.INC:
                        num=value.toFBSNumber().incValue();
                        result.getReference().putValue(num);
                        result.setNormal(num);
                        break;

                case ASTConstants.DEC:
                        num=value.toFBSNumber().decValue();
                        result.getReference().putValue(num);
                        result.setNormal(num);
                        break;

                case ASTConstants.ADD:
                        dblValue=value.numberValue();
                        num=FBSNumber.get(dblValue);
                        result.setNormal(num);
                        break;

                case ASTConstants.SUBTRACT:
                        dblValue=value.numberValue();
                        num=FBSNumber.get(-dblValue);
                        result.setNormal(num);
                        break;

                case ASTConstants.INV:
                        int intValue=value.intValue();
                        num=FBSNumber.get(~intValue);
                        result.setNormal(num);
                        break;

                case ASTConstants.NEG:
                        boolean boolValue=value.booleanValue();
                        result.setNormal(FBSBoolean.get(!boolValue));
                        break;
            }
        } catch( InterpretException ie ) {
            throw ie.fillException(context,this);
        }
    }
    protected void extraInfo(PrintStream ps,String indent){
        ps.print("\""); //$NON-NLS-1$
        ps.print(ASTConstants.getToken(this.typeId));
        ps.print("\""); //$NON-NLS-1$
    }

    public FBSType getResultType(FBSType.ISymbolCallback symbolCallback) {
        FBSType t = node.getResultType(symbolCallback);
         switch(typeId){
            case ASTConstants.DELETE:
                    return FBSType.undefinedType;

            case ASTConstants.VOID:
                    return FBSType.undefinedType;

            case ASTConstants.TYPEOF:
                    return FBSType.stringType;

            case ASTConstants.INC:
            case ASTConstants.DEC:
                    return FBSType.numberType;

            case ASTConstants.ADD:
            case ASTConstants.SUBTRACT:
                    return FBSType.numberType;

            case ASTConstants.INV:
                    return FBSType.numberType;

            case ASTConstants.NEG:
                    return FBSType.booleanType;
        }
        return FBSType.invalidType;
    }

    public Object visit(ASTNodeVisitor v, Object param) {
	return v.visitUnaryOp(this, param);
    }
}
