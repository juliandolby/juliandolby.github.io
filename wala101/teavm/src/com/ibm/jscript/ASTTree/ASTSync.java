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
import com.ibm.jscript.types.FBSValue;
import com.ibm.jscript.util.StringUtil;

public class ASTSync extends ASTNode{
    public static final int SYNC_EXPR=0;
    public static final int BLOCK=1;

    ASTNode syncExpr;
    ASTBlock block;

    public ASTSync() {
    }


    public ASTSync(ASTNode expr,ASTNode block) {
        this.addSyncExpr(expr);
        this.addBlock(block);
    }


    public void addSyncExpr(ASTNode exprNode){
        syncExpr=exprNode;
    }


    public void addBlock(ASTNode blockNode){
        block=(ASTBlock)blockNode;
    }


    public int getSlotCount(){
        return 2;
    }


    public ASTNode readSlotAt(int index){
        switch(index){
            case SYNC_EXPR: return syncExpr;
            case BLOCK: return block;
        }
        return null;
    }


    public void interpret(ASTNode caller, IExecutionContext context,InterpretResult result) throws JavaScriptException {
        try {
            syncExpr.interpret(this,context,result);
            FBSValue syncValue= result.getFBSValue();
            if (!syncValue.canFBSObject()){
                if( syncValue.isNull() ) {
                    throw new InterpretException(StringUtil.format(JScriptResources.getString("ASTSync.SyncValNotNull.Exception"),syncExpr.getTraceString())); //$NON-NLS-1$
                }
                if( syncValue.isUndefined() ) {
                    throw new InterpretException(StringUtil.format(JScriptResources.getString("ASTSync.SyncValUndefined.Exception"),syncExpr.getTraceString())); //$NON-NLS-1$
                }
                throw new InterpretException(StringUtil.format(JScriptResources.getString("ASTSync.SyncValNotAnObj.Exception"),syncExpr.getTraceString())); //$NON-NLS-1$
            }
            Object syncObject=syncValue.toFBSObject().getSyncObject();

            synchronized(syncObject){
                block.interpret(this,context,result);
            }
        } catch( InterpretException ie ) {
            throw ie.fillException(context,this);
        }
    }

    public Object visit(ASTNodeVisitor v, Object param) {
	return v.visitSync(this, param);
    }
}
