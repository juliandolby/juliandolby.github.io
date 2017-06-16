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

import java.util.ArrayList;

import com.ibm.jscript.InterpretException;
import com.ibm.jscript.JavaScriptException;
import com.ibm.jscript.engine.IExecutionContext;
import com.ibm.jscript.types.FBSDefaultObject;
import com.ibm.jscript.types.FBSObject;
import com.ibm.jscript.types.FBSUndefined;

public class ASTVariableDecl extends ASTNode{

    ArrayList entries = new ArrayList();

    public ASTVariableDecl() {
    }

    public final class Entry{
        String varName;
        String varType;
        ASTNode initNode;

        public Entry(String name, String type, ASTNode init){
            varName=name;
            varType=type;
            initNode=init;
        }
        public String getName(){
            return varName;
        }
        public String getType() {
            return varType;
        }
        public ASTNode getInitNode(){
            return initNode;
        }
    }

    public void add(String name, String type, ASTNode initialize){
        entries.add(new Entry(name,type,initialize));
    }

    public int getSlotCount(){
        return getEntryCount();
    }
    public ASTNode readSlotAt(int index){
        return getEntryAt(index).getInitNode();
    }

    public int getEntryCount(){
        return entries.size();
    }

    public Entry getEntryAt(int index){
        return (Entry)entries.get(index);
    }

    public void interpret(ASTNode caller, IExecutionContext context,InterpretResult result) throws JavaScriptException {
        try {
            FBSDefaultObject vObj=context.getVariableObject();
            for(int i=0;i<getEntryCount();i++){
                Entry e= getEntryAt(i);
                ASTNode node=e.getInitNode();
                if (node!=null){
                    node.interpret(this,context,result);
                }
                if (!vObj.hasProperty(e.getName())){
                    // createProperty creates a property only if it does not exist
                    vObj.createProperty(e.getName(),FBSObject.P_NONE,node==null?FBSUndefined.undefinedValue:result.getFBSValue());
                }else{
                     vObj.put(e.getName(),node==null?FBSUndefined.undefinedValue:result.getFBSValue());
                }
            }
        } catch( InterpretException ie ) {
            throw ie.fillException(context,this);
        }
    }

    public Object visit(ASTNodeVisitor v, Object param) {
	return v.visitVariableDecl(this, param);
    }
}
