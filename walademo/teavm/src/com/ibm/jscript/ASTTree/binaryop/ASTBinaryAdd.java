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
package com.ibm.jscript.ASTTree.binaryop;

import java.util.ArrayList;

import com.ibm.jscript.InterpretException;
import com.ibm.jscript.JavaScriptException;
import com.ibm.jscript.ASTTree.ASTBinaryOp;
import com.ibm.jscript.ASTTree.ASTConstants;
import com.ibm.jscript.ASTTree.ASTNode;
import com.ibm.jscript.ASTTree.ASTNodeVisitor;
import com.ibm.jscript.ASTTree.InterpretResult;
import com.ibm.jscript.engine.IExecutionContext;
import com.ibm.jscript.engine.JSOptions;
import com.ibm.jscript.engine.Utility;
import com.ibm.jscript.types.FBSNumber;
import com.ibm.jscript.types.FBSString;
import com.ibm.jscript.types.FBSUtility;
import com.ibm.jscript.types.FBSValue;

public class ASTBinaryAdd extends ASTBinaryOp {

    private ASTNode[] optimizedNodes;

    public ASTBinaryAdd(ASTNode leftNode, ASTNode rightNode) {
        super(leftNode,rightNode);
    }

    public int getOperator() {
        return ASTConstants.ADD;
    }

    public void interpret(ASTNode caller, IExecutionContext context,InterpretResult result) throws JavaScriptException {
        try {
            if(optimizedNodes!=null ) {
                FBSValue current = null;
                StringBuffer b = null;
                double sum = 0.0;
                for( int i=0; i<optimizedNodes.length; i++ ) {
                    optimizedNodes[i].interpret(this, context, result);
                    FBSValue v = result.getFBSValue();
                    if( current!=null ) {
                        current = Utility.operation(current,v,ASTConstants.ADD);
                    } else if( v.isList() ) {
                        if( i>0 ) {
                            if( b!=null ) {
                                current = FBSUtility.wrap(b.toString());
                            } else {
                                current = FBSUtility.wrap(sum);
                            }
                            current = Utility.operation(current,v,ASTConstants.ADD);
                        } else {
                            current = v;
                        }
                    } else {
                        if(b!=null) {
                            b.append(v.stringValue());
                        } else {
                            if( v.isString() ) {
                                b = new StringBuffer();
                                if(i>0) {
                                    b.append(FBSNumber.stringValue(sum));
                                }
                                b.append(v.stringValue());
                            } else {
                                sum += v.numberValue();
                            }
                        }
                    }
                }
                if( current!=null ) {
                    result.setNormal(current);
                } else if(b!=null) {
                    result.setNormal(FBSString.get(b.toString()));
                } else {
                    result.setNormal(FBSNumber.get(sum));
                }
            } else {
                // Evaluate the left node
                leftNode.interpret(this, context, result);
                FBSValue v1=result.getFBSValue();

                // Evaluate the right node
                rightNode.interpret(this, context, result);
                FBSValue v2=result.getFBSValue();

                // In case of lists, use generic operation
                if( JSOptions.get().hasListOperator() && (v1.isList() || v2.isList()) ) {
                    FBSValue r=Utility.operation(v1,v2,getOperator());
                    result.setNormal(r);
                } else {
                    // Optimized shortCut
                    if(v1.isString()||v2.isString()) {
                        result.setNormal(FBSString.get(v1.stringValue()+v2.stringValue()));
                    } else {
                        double d1=v1.numberValue();
                        double d2=v2.numberValue();
                        double r=d1+d2;
                        result.setNormal(FBSNumber.get(r));
                    }
                }
            }
        } catch( InterpretException ie ) {
            throw ie.fillException(context,this);
        }
    }

//    public FBSType getResultType(FBSType.ISymbolCallback symbolCallback) {
//    }

    // Add optimization
    // This is mainly done for string concatenation optimization
    // We get all the nodes coming from the left part of a tree like
    //                                +
    //                              +  7
    //                            +  6
    //                          +  5
    //                        +  4
    //                      +  3
    //                    1  2
    // Which comes to the following array:
    //   {1,2,3,4,5,6,7}
    // This will be then evaluated using the same string buffer.
    // Some others optimization can be done:
    //     * Compute constants values
    //     * Take care of minus sign '-' within the expression
    //     * Optimize the left part of the tree
    public void optimize() {
        ArrayList list = new ArrayList(16);
        listAdd(this,list);
        if( list.size()>2 ) {
            this.optimizedNodes = (ASTNode[])list.toArray(new ASTNode[list.size()]);
        }
    }
    private void listAdd(ASTBinaryAdd node, ArrayList list) {
        ASTNode n1 = node.getLeftNode();
        if( n1 instanceof ASTBinaryAdd ) {
            listAdd((ASTBinaryAdd)n1,list);
        } else {
            list.add(n1);
        }
        ASTNode n2 = node.getRightNode();
        list.add(n2);
    }

    public Object visit(ASTNodeVisitor v, Object param) {
	return v.visitBinaryAdd(this, param);
    }
}
