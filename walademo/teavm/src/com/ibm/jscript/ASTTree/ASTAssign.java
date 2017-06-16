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
import com.ibm.jscript.JScriptResources;
import com.ibm.jscript.JavaScriptException;
import com.ibm.jscript.engine.IExecutionContext;
import com.ibm.jscript.engine.Utility;
import com.ibm.jscript.types.FBSReference;
import com.ibm.jscript.types.FBSType;
import com.ibm.jscript.types.FBSUndefined;
import com.ibm.jscript.types.FBSValue;
import com.ibm.jscript.util.StringUtil;

public class ASTAssign extends ASTNode{
    public static final int LEFT=0;
    public static final int RIGHT=1;

    private int typeId;
    private ASTNode leftNode;
    private ASTNode rightNode;

    public ASTAssign(String token) {
        typeId=ASTConstants.getConstant(token);
    }

    public ASTAssign(int type) {
        typeId=type;
    }

    public int getType(){
        return typeId;
    }

    public void add(ASTNode leftnode, ASTNode rightnode){
        this.leftNode=leftnode;
        this.rightNode=rightnode;
    }

    public void addLeft(ASTNode leftnode){
        this.leftNode=leftnode;
    }

    public void addRight(ASTNode rightnode){
        this.rightNode=rightnode;
    }

    public FBSType getResultType(FBSType.ISymbolCallback symbolCallback) {
        if( rightNode!=null ) {
            return rightNode.getResultType(symbolCallback);
        }
        return FBSType.undefinedType;
    }

    public String getTraceString(){
        return leftNode.getTraceString()+ASTConstants.getToken(typeId)+rightNode.getTraceString();
    }

    public ASTNode getLeftNode() {
        return leftNode;
    }

    public ASTNode getRightNode() {
        return rightNode;
    }

    public int getSlotCount(){
        return 2;
    }

    public ASTNode readSlotAt(int index){
        if (index<0 || index>1) return null;
        return (index==0)?leftNode:rightNode;
    }

    public void interpret(ASTNode caller, IExecutionContext context,InterpretResult result) throws JavaScriptException {
        try {
            leftNode.interpret(this,context,result);
            FBSReference ref=result.getReference();
            if( ref==null ) {
                throw new InterpretException(StringUtil.format(JScriptResources.getString("ASTAssign.CannotAssign.Exception"),ASTConstants.getToken(typeId))); //$NON-NLS-1$
            }


            rightNode.interpret(this,context,result);
            FBSValue v2=result.getFBSValue();

            if (typeId==ASTConstants.ASSIGN){
                ref.putValue(v2);
                result.setNormal(v2);
                return;
            }
            FBSValue v1=ref.getValue();
            FBSValue r=FBSUndefined.undefinedValue;

            switch(typeId){
                case ASTConstants.ADD_ASSIGN:
                    r=Utility.operation(v1,v2,ASTConstants.ADD);
                    break;

                case ASTConstants.SUB_ASSIGN:
                    r=Utility.operation(v1,v2,ASTConstants.SUBTRACT);
                    break;

                case ASTConstants.MUL_ASSIGN:
                    r=Utility.operation(v1,v2,ASTConstants.MULTIPLY);
                    break;

                case ASTConstants.DIV_ASSIGN:
                    r=Utility.operation(v1,v2,ASTConstants.DIVIDE);
                    break;

                case ASTConstants.MOD_ASSIGN:
                    r=Utility.operation(v1,v2,ASTConstants.MODULO);
                    break;

                case ASTConstants.LSH_ASSIGN:
                    r=Utility.operation(v1,v2,ASTConstants.SHIFT_LEFT);
                    break;

                case ASTConstants.RSH_ASSIGN:
                    r=Utility.operation(v1,v2,ASTConstants.SHIFT_RIGHT);
                    break;

                case ASTConstants.URSH_ASSIGN:
                    r=Utility.operation(v1,v2,ASTConstants.USHIFT_RIGHT);
                    break;

                case ASTConstants.AND_ASSIGN:
                    r=Utility.operation(v1,v2,ASTConstants.BITWISE_AND);
                    break;

                case ASTConstants.XOR_ASSIGN:
                    r=Utility.operation(v1,v2,ASTConstants.BITWISE_XOR);
                    break;

                case ASTConstants.OR_ASSIGN:
                    r=Utility.operation(v1,v2,ASTConstants.BITWISE_OR);
                    break;
            }
            ref.putValue(r);
            result.setNormal(r);
        } catch( InterpretException ie ) {
            throw ie.fillException(context,this);
        }
    }

    protected void extraInfo(PrintStream ps,String indent){
        ps.print("\""); //$NON-NLS-1$
        ps.print(ASTConstants.getToken(this.typeId));
        ps.print("\""); //$NON-NLS-1$
    }

    public Object visit(ASTNodeVisitor v, Object param) {
	return v.visitAssign(this, param);
    }
}
