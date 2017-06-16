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
import com.ibm.jscript.engine.JSOptions;

public class ASTProfile extends ASTNode{

    public static final int PARAM=0;
    public static final int BLOCK=1;

    private String profileType;
    private ASTNode paramExpr;
    private ASTNode blockExpr;

    public ASTProfile() {
    }


    public ASTProfile(String profileType, ASTNode paramExpr, ASTNode blockExpr) {
        this.profileType = profileType;
        this.paramExpr = paramExpr;
        this.blockExpr = blockExpr;
    }

    public String getProfileType() {
        return profileType;
    }

    public int getSlotCount(){
        return 2;
    }

    public ASTNode readSlotAt(int index){
        switch(index){
            case PARAM: return paramExpr;
            case BLOCK: return blockExpr;
        }
        return null;
    }

    public void interpret(ASTNode caller, IExecutionContext context,InterpretResult result) throws JavaScriptException {
        try {
            if( JSOptions.get().isProfilerEnabled() ) {
                String pValue = null;
                if( paramExpr!=null ) {
                    paramExpr.interpret(this,context,result);
                    pValue = result.getFBSValue().stringValue();
                }
                JSOptions.Profiler profiler = JSOptions.get().getProfiler();
                Object agg = profiler.startProfileBlock(profileType,pValue);
                long ts = profiler.getCurrentTime();
                try {
                    blockExpr.interpret(this,context,result);
                } finally {
                	profiler.endProfileBlock(agg,ts);
                }
            } else {
                blockExpr.interpret(this,context,result);
            }
        } catch( InterpretException ie ) {
            throw ie.fillException(context,this);
        }
    }

    public Object visit(ASTNodeVisitor v, Object param) {
	return v.visitProfile(this, param);
    }
}
