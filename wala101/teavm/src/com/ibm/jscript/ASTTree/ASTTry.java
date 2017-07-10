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
import com.ibm.jscript.JavaScriptException;
import com.ibm.jscript.engine.IExecutionContext;
import com.ibm.jscript.std.ObjectObject;
import com.ibm.jscript.types.FBSObject;
import com.ibm.jscript.types.FBSString;
import com.ibm.jscript.types.FBSUtility;

public class ASTTry extends ASTNode{

    ASTNode blockNode;
    ASTNode catchNode;
    ASTNode finallyNode;
    String catchId;

    public ASTTry() {
    }

    public void add(ASTNode blockNode,String catchid,ASTNode catchNode,ASTNode finallyNode){
        this.blockNode=blockNode;
        this.catchNode=catchNode;
        this.catchId=catchid;
        this.finallyNode=finallyNode;
    }

    public void addBlock(ASTNode blockNode){
        this.blockNode=blockNode;
    }

    public void addCatch(String id,ASTNode catchNode){
        catchId=id;
        this.catchNode=catchNode;
    }

    public void addCatch(ASTNode catchNode){
        this.catchNode=catchNode;
    }


    public void setCatchId(String catchid){
        catchId=catchid;
    }

    public String getCatchId(){
	return catchId;
    }
 
    public void addFinally(ASTNode finallyNode){
        this.finallyNode=finallyNode;
    }

    public int getSlotCount(){
        return 3;
    }

    public ASTNode readSlotAt(int index){
        switch(index){
            case 0:return blockNode;
            case 1:return catchNode;
            case 2:return finallyNode;
        }
        return null;
    }

    public void interpret(ASTNode caller, IExecutionContext context,InterpretResult result) throws JavaScriptException {
        // When we are evaluating a constant, we rely on an exception thrown
        // to detect that it is not a constant.
        // To ensure that this exception won't be caught, we manage a flag
        // at the expression level.
        if( context.getExpression().isEvaluateConstant() ) {
            blockNode.interpret(this, context, result);
        } else {
            if(catchNode!=null&&finallyNode!=null) {
                try {
                    blockNode.interpret(this, context, result);
                } catch(InterpretException ie) {
                    interpretCatch(this, context, result, ie);
                } finally {
                    InterpretResult tmpResult=new InterpretResult();
                    finallyNode.interpret(this, context, tmpResult);
                }
            } else if(catchNode!=null) {
                try {
                    blockNode.interpret(this, context, result);
                } catch(InterpretException ie) {
                    interpretCatch(this, context, result, ie);
                }
            } else if(finallyNode!=null) {
                try {
                    blockNode.interpret(this, context, result);
                } finally {
                    InterpretResult tmpResult=new InterpretResult();
                    finallyNode.interpret(this, context, tmpResult);
                }
            } else {
                blockNode.interpret(this, context, result);
            }
        }
    }

    private void interpretCatch(ASTNode caller, IExecutionContext context,InterpretResult result,InterpretException ie) throws JavaScriptException{
        ObjectObject catchObj= new ObjectObject();
        if( ie.getExceptionObject()!=null ) {
            catchObj.createProperty(catchId,FBSObject.P_NODELETE,ie.getExceptionObject());
        } else {
            //catchObj.createProperty(catchId,FBSObject.P_NODELETE,FBSString.get(ie.getMessage()));
            catchObj.createProperty(catchId,FBSObject.P_NODELETE,FBSUtility.wrapAsObject(ie));
        }
        catchObj.createProperty("errorMsg",FBSObject.P_NODELETE,FBSString.get(ie.getMessage())); //$NON-NLS-1$
        context.pushToScope(catchObj);
        catchNode.interpret(this,context,result);
        context.popFromScope();
    }
/*
    public static void main( String[] args ) {
        try {
            for( int i=0; i<5; i++ ) {
                check();
            }
        } catch( Exception e ) {
            com.ibm.workplace.designer.util.TDiag.exception(e);
        }
    }
    static final int COUNT = 100000000;

    private static void check() throws Exception {
        long start; long end;
        cpt=0;
        start=System.currentTimeMillis();
        for( int i=0; i<COUNT; i++ ) {
            m1(false);
        }
        end=System.currentTimeMillis();
        TDiag.trace("NORMAL: ={0}, cpt={1}", TString.toString(end-start), TString.toString(cpt));
        cpt=0;
        start=System.currentTimeMillis();
        for( int i=0; i<COUNT; i++ ) {
            m();
        }
        end=System.currentTimeMillis();
        TDiag.trace("DIRECT: ={0}, cpt={1}", TString.toString(end-start), TString.toString(cpt));
        cpt=0;
        start=System.currentTimeMillis();
        for( int i=0; i<COUNT/2; i++ ) {
            m1(true);
        }
        end=System.currentTimeMillis();
        TDiag.trace("DEBUG: ={0}, cpt={1}", TString.toString(end-start), TString.toString(cpt));
    }
    static void m1(boolean d) throws Exception {
        if(d) {
            try {
                m();
            } catch(Exception e) {
                m();
            } finally {
                m();
            }
        } else {
            m();
        }
    }
    private static int m() {
        int a = 90;
        a = a + cpt++;
        int b = 165;
        a = a + b;
        return a - 90 -165;
    }
    private static int cpt = 0;
*/

    public Object visit(ASTNodeVisitor v, Object param) {
	return v.visitTry(this, param);
    }
}
