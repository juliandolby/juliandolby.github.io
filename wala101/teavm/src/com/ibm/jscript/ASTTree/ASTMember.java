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
import com.ibm.jscript.engine.JSOptions;
import com.ibm.jscript.parser.ParseException;
import com.ibm.jscript.parser.Token;
import com.ibm.jscript.types.FBSObject;
import com.ibm.jscript.types.FBSReference;
import com.ibm.jscript.types.FBSReferenceByName;
import com.ibm.jscript.types.FBSType;
import com.ibm.jscript.types.FBSValue;
import com.ibm.jscript.types.JavaAccessObject;
import com.ibm.jscript.types.JavaPackageObject;
import com.ibm.jscript.util.FastStringBuffer;
import com.ibm.jscript.util.FastStringBufferPool;
import com.ibm.jscript.util.StringUtil;

public class ASTMember extends ASTNode {

    public static final int LEFT=0;
    public static final int RIGHT=1;

    private ASTNode leftNode;
    private String memberName;
    private Class optimizedClass;

    public ASTMember(Token t, ASTNode leftNode, String memberName) {
        this.leftNode = leftNode;
        this.memberName = memberName;

	setSourcePos(-1, -1, t.endLine, t.endColumn);
    }

    public String getTraceString() {
        return leftNode.getTraceString()+"."+memberName; //$NON-NLS-1$
    }


    public int getSlotCount(){
        return 1;
    }

    public ASTNode readSlotAt(int index){
        switch(index){
            case LEFT:   return leftNode;
            //case RIGHT:  return rightNode;
        }
        return null;
    }

    public ASTNode getLeftNode() {
        return leftNode;
    }

    public String getMemberName() {
        return memberName;
    }

    public FBSType getResultType(FBSType.ISymbolCallback symbolCallback) {
    	Class c = getOptimizedJavaClass();
        if( c!=null ) {
        	return FBSType.getType(c.getName());
        }
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
            t=FBSType.getJavaPackage(idn);
        }
        return t.getMember(memberName);
    }

    public void interpret(ASTNode caller, IExecutionContext context,InterpretResult result) throws JavaScriptException {
        try {
            if( optimizedClass!=null ) {
                // PHIL: this should be optimized!!!!!
                FBSObject optimizedObject = new JavaAccessObject(optimizedClass,null);
                result.setNormal(optimizedObject);
            } else {
                leftNode.interpret(this,context,result);

                FBSValue leftValue;
                try {
                    leftValue = result.getFBSValue();
                } catch(InterpretException e0) {
                    if( leftNode instanceof ASTIdentifier ) {
                        String idn=((ASTIdentifier)leftNode).getIdentifierName();
                        leftValue=JavaPackageObject.getPackage(idn);
                    } else {
                        throw e0;
                    }
                }
                if (!leftValue.canFBSObject()) {
                    if( leftValue.isNull() ) {
                        throw new InterpretException(StringUtil.format(JScriptResources.getString("ASTMember.LeftValueIsNull.Exception"),leftNode.getTraceString())); //$NON-NLS-1$
                    }
                    if( leftValue.isUndefined() ) {
                        throw new InterpretException(StringUtil.format(JScriptResources.getString("ASTMember.LeftValueIsUndefined.Exception"),leftNode.getTraceString())); //$NON-NLS-1$
                    }
                    throw new InterpretException(StringUtil.format(JScriptResources.getString("ASTMember.LeftValueIsNotAnObj.Exception"),leftNode.getTraceString())); //$NON-NLS-1$
                }
                FBSObject leftObject=leftValue.toFBSObject();
                FBSReference ref = leftObject.getPropertyReference(memberName);
                if(ref==null) {
                	// If there is no existing reference, then create a virtual one that will be resolved later
                	// during an assignment.
                	ref = new FBSReferenceByName(leftObject,memberName);
                }
                result.setNormal(ref);
            }
        } catch( InterpretException ie ) {
            throw ie.fillException(context,this);
        }
    }

    protected void extraInfo(PrintStream ps,String indent){
        ps.print("member="); //$NON-NLS-1$
        ps.print(getMemberName());
    }

    /*
     * Optimize the tree.
     */
    public void optimize() throws ParseException {
        // === Java static member access ==================================
        //  Check for a package.javaclass
    	Class c = getOptimizedJavaClass();
    	if(c!=null) {
    		optimizedClass = c;
        }
        super.optimize();
    }

    private Class getOptimizedJavaClass() {
        // === Java static member access ==================================
        //  Check for a package.javaclass
        FastStringBuffer b = getJavaIdentifier(this);
        if( b!=null ) {
            // Check if it is an existing class
            try {
                String className = b.toString();
                if(className.startsWith(JavaPackageObject.PACKAGES_START)) {
                	if( JSOptions.get().hasRhinoExtensions() ) {
                		className = className.substring(JavaPackageObject.PACKAGES_START.length());
                	}
                }
                Class c = JSOptions.get().loadClass(className);
                return c;
                // If so, create a ready to use JavaAccessObject
                //TDiag.trace( "Found static: '{0}'", className );
            } catch(Throwable e) {}
        }
        return null;
    }
    private static FastStringBuffer getJavaIdentifier(ASTNode node) {
        if( node instanceof ASTIdentifier ) {
            ASTIdentifier i2= (ASTIdentifier)node;
            FastStringBuffer buffer = FastStringBufferPool.get();
            buffer.append( i2.getIdentifierName() );
            return buffer;
        } else if( node instanceof ASTMember ) {
            ASTMember m2 = (ASTMember)node;
            FastStringBuffer buffer = getJavaIdentifier( m2.getLeftNode() );
            if( buffer!=null ) {
                buffer.append('.');
                buffer.append(m2.getMemberName());
            }
            return buffer;
        } else {
            return null;
        }
    }

    public Object visit(ASTNodeVisitor v, Object param) {
	return v.visitMember(this, param);
    }
}
