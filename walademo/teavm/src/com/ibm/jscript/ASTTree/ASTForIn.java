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

import java.util.Iterator;

import com.ibm.jscript.InterpretException;
import com.ibm.jscript.JScriptResources;
import com.ibm.jscript.JavaScriptException;
import com.ibm.jscript.engine.IExecutionContext;
import com.ibm.jscript.types.FBSObject;
import com.ibm.jscript.types.FBSReference;
import com.ibm.jscript.types.FBSString;
import com.ibm.jscript.types.FBSValue;
import com.ibm.jscript.types.JavaAccessObject;
import com.ibm.jscript.types.JavaWrapperObject;
import com.ibm.jscript.util.StringUtil;

public class ASTForIn extends ASTNode implements ILabelledStatement {
    public static final int ITERATOR=0;
    public static final int COLLECTION=1;
    public static final int STATEMENT=2;

    ASTNode iteratorNode;
    ASTNode collectionNode;
    ASTNode statementNode;
    String labelName;

    public ASTForIn() {
    }
    
	public void setLabelName(String label) {
		this.labelName = label;
	}

    public void add(ASTNode iterator,ASTNode collection,ASTNode statement){
        iteratorNode=iterator;
        collectionNode=collection;
        statementNode=statement;
    }

    public void addIterator(ASTNode iterator){
        iteratorNode=iterator;
    }

    public void addCollection(ASTNode collection){
        collectionNode=collection;
    }

    public void addStatement(ASTNode statement){
        statementNode=statement;
    }


    public int getSlotCount(){
        return 3;
    }

    public ASTNode readSlotAt(int index){
        switch(index){
            case ITERATOR:     return iteratorNode;
            case COLLECTION:   return collectionNode;
            case STATEMENT:    return statementNode;
        }
        return null;
    }

    public void interpret(ASTNode caller, IExecutionContext context,InterpretResult result) throws JavaScriptException {
        try {
            if (iteratorNode instanceof ASTVariableDecl){
                iteratorNode.interpret(this,context,result);
                // after evaluating the declaration, convert the iteratorNode to ASTIdentifier
                // because it will be evaluated repeatedly
                String name=((ASTVariableDecl)iteratorNode).getEntryAt(0).getName();
                iteratorNode=new ASTIdentifier(name,iteratorNode.getBeginCol(),iteratorNode.getBeginLine(),
                                                iteratorNode.getEndCol(),iteratorNode.getEndLine());
            }

            collectionNode.interpret(this,context,result);
    /*        if (!result.isFBSValue()){
                throw new InterpretException("Illegal result type. Value expected",getLine(),getCol());
            }
    */
            FBSValue v=null;
            FBSObject obj=result.getFBSValue().toFBSObject();
            boolean javaObject=isJavaObject(obj);
            FBSValue.IValues values=null;
            Iterator itProperty=null;

            if (javaObject){
                values=obj.getValues();
            }else{
                itProperty=obj.getPropertyKeys();
            }


            for(;;){
                if (javaObject){
                    if (!values.hasNext()){
                        break;
                    }
                }else
                    if (!itProperty.hasNext()){
                        break;
                    }
                v=null;

                iteratorNode.interpret(this,context,result);

                if (result.isReference()){
                    FBSReference ref=result.getReference();
                    if(javaObject){
                        ref.putValue(values.next());
                    }else{
                        ref.putValue(FBSString.get((String)itProperty.next()));
                    }
                }else{
                    throw new InterpretException(JScriptResources.getString("ASTForIn.IllegalResultType.Exception")); //$NON-NLS-1$
                }

                try {
                    statementNode.interpret(this, context, result);
                } catch( AbruptBreakException ex ) {
                	if(!StringUtil.isEmpty(ex.getLabel())) {
                		if(!StringUtil.equals(ex.getLabel(),labelName)) {
                			throw ex;
                		}
                	}
                    // Break the loop
                    break;
                } catch( AbruptContinueException ex ) {
                	if(!StringUtil.isEmpty(ex.getLabel())) {
                		if(!StringUtil.equals(ex.getLabel(),labelName)) {
                			throw ex;
                		}
                	}
                    // Continue the loop
                    //continue;
                }
            }
            result.setNormal(v);
        } catch( InterpretException ie ) {
            throw ie.fillException(context,this);
        }
    }


    private boolean isJavaObject (FBSObject o){
        if (o instanceof JavaAccessObject
            || o instanceof JavaWrapperObject ) {
            return true;
        }
        return false;
    }


    private Object getJavaObject(FBSObject o){
        if (o==null){
            return null;
        }

        if (o instanceof JavaAccessObject){
            return ((JavaAccessObject)o).getJavaObject();
        }

        if (o instanceof JavaWrapperObject){
            return ((JavaWrapperObject)o).getJavaObject();
        }

        return null;
    }

    public Object visit(ASTNodeVisitor v, Object param) {
	return v.visitForIn(this, param);
    }

	public String getLabelName() {
		return labelName;
	}
}
