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
import com.ibm.jscript.std.ArrayObject;
import com.ibm.jscript.types.FBSType;
import com.ibm.jscript.types.FBSUndefined;
import com.ibm.jscript.types.FBSValueVector;

public class ASTArrayLiteral extends ASTNode{
    private NodeVector nodes= new NodeVector();

    public ASTArrayLiteral() {
    }

    public void add(ASTNode node){
        nodes.add(node);
    }

    public FBSType getResultType(FBSType.ISymbolCallback symbolCallback) {
        return FBSType.jsArrayType;
    }

    public int getSlotCount(){
        return nodes.getCount();
    }
    public void increaseSizeBy(int delta){
        if (delta>0){
            nodes.setSize(nodes.getCount()+delta);
        }
    }

    public ASTNode readSlotAt(int index){
        return  nodes.get(index);
    }

    public void interpret(ASTNode caller,IExecutionContext context,InterpretResult result) throws JavaScriptException {
        try {
            FBSValueVector vec= new FBSValueVector();
            for(int i=0;i<nodes.getCount();i++){
                ASTNode n=nodes.get(i);
                if (n==null){
                    vec.add(FBSUndefined.undefinedValue);
                }else{
                    n.interpret(this,context,result);
                    vec.add(result.getFBSValue());
                }
            }
            ArrayObject array= new ArrayObject(vec);
            result.setNormal(array);
        } catch( InterpretException ie ) {
            throw ie.fillException(context,this);
        }
    }

    public Object visit(ASTNodeVisitor v, Object param) {
	return v.visitArrayLiteral(this, param);
    }
}
