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
import com.ibm.jscript.types.FBSObject;
import com.ibm.jscript.types.FBSType;
import com.ibm.jscript.types.FBSValue;
import com.ibm.jscript.types.FBSValueVector;
import com.ibm.jscript.types.JavaAccessObject;
import com.ibm.jscript.types.JavaFactory;
import com.ibm.jscript.types.JavaPackageObject;
import com.ibm.jscript.util.FastStringBuffer;
import com.ibm.jscript.util.FastStringBufferPool;

public class ASTNew extends ASTNode{
    private ASTNode memberExpression;
    private ASTNode argumentExpression;

    public ASTNew() {
    }

    public void add(ASTNode expr,ASTNode arguments){
        memberExpression=expr;
        argumentExpression=arguments;
    }

    public void addExpression(ASTNode expr){
        memberExpression=expr;
    }

    public void addArguments(ASTNode arguments){
        argumentExpression=arguments;
    }



    public int getSlotCount(){
        return argumentExpression==null?1:2;
    }

    public ASTNode readSlotAt(int index){
        switch(index){
            case 0:return memberExpression;
            case 1:return argumentExpression;
        }
        return null;
    }

    public void interpret(ASTNode caller, IExecutionContext context,InterpretResult result) throws JavaScriptException {
        try {
            // evaluate the parameters first
            FBSValueVector args=null;
            if (argumentExpression!=null){
                args=new FBSValueVector();
                for(int i=0;i<argumentExpression.getSlotCount();i++){
                    ASTNode node=argumentExpression.readSlotAt(i);
                    if (node!=null){
                        node.interpret(this,context,result);
                        args.add(result.getFBSValue());
                    }
                }
            }
            if (args==null){
                args=FBSValueVector.emptyVector; // don't send a null object to the construct
            }

            FBSValue value=null;
            // check if it can be a java object instantiation
            // java objects must always be qualified (package1.package2.className)
            if (isJavaArray()){
                JavaArrayClass ja=resolveJavaArray(context,result);
                if(ja!=null){
                	String name = ja.getClassName();
                    try{
                        value=JavaFactory.instantiate(name,ja.getDim());
                        result.setNormal(value);
                    }catch(Exception e){
                        throw new InterpretException(e,com.ibm.jscript.util.StringUtil.format(JScriptResources.getString("ASTNew.ArrayInstantiateFailed.Exception"),name,ja.getDim())); //$NON-NLS-1$
                    }
                }
            }else{// if it is not a java array
                memberExpression.interpret(this,context,result);
                value=result.getFBSValue();
                
                if (!(value instanceof FBSObject)){
                    throw new InterpretException(JScriptResources.getString("ASTNew.NonObjInstantiate.Exception")); //$NON-NLS-1$
                }
                FBSObject object=(FBSObject)value;
                if (object.supportConstruct()){
                    FBSObject newObject=object.construct(context,args);
                    result.setNormal(newObject);
                    return;
                }
                throw new InterpretException(JScriptResources.getString("ASTNew.ObjInstantiationNotSupported.Exception")); //$NON-NLS-1$
            }
        } catch( InterpretException ie ) {
            throw ie.fillException(context,this);
        }
    }

    public FBSType getResultType(FBSType.ISymbolCallback symbolCallback) {
        if( isJavaArray() ) {
            return FBSType.getType(getJavaArrayType());
        }
        String name=""; //$NON-NLS-1$
        if (memberExpression instanceof ASTMember){
            name=getMemberAsString((ASTMember)memberExpression);
        } else if (memberExpression instanceof ASTIdentifier){
            name=((ASTIdentifier)memberExpression).getIdentifierName();
        } else {
            return FBSType.invalidType;
        }
        return FBSType.getType(name);
    }


    private JavaArrayClass resolveJavaArray(IExecutionContext context,InterpretResult result) throws JavaScriptException{
        JavaArrayClass javaArray= new JavaArrayClass();
        resolveJavaArray(memberExpression,context,result,javaArray);
        return javaArray;
    }
    private void resolveJavaArray(ASTNode member, IExecutionContext context,InterpretResult result, ASTNew.JavaArrayClass javaArray) throws JavaScriptException {
        if (member instanceof ASTArrayMember){
            ASTArrayMember arrayMember = (ASTArrayMember)member;
            arrayMember.getRightNode().interpret(this, context, result);
            FBSValue rightValue= result.getFBSValue();
            if (!rightValue.isNumber()){
                throw new InterpretException(JScriptResources.getString("ASTNew.IndexMustBeNumeric.Exception")); //$NON-NLS-1$
            }
            int d=rightValue.intValue();
            javaArray.insertDim(d);

            ASTNode leftNode = arrayMember.getLeftNode();
            resolveJavaArray(leftNode,context,result,javaArray);
        } else {
	        // Evaluate the left part.
	        // Not that it should be a JavaAccessObject in order to be instanciable
        	if(member instanceof ASTIdentifier) {
        		String cName = ((ASTIdentifier)member).getIdentifierName();
        		if(cName.equals("char")) { //$NON-NLS-1$
        	        FBSValue value=result.getFBSValue();
        	        javaArray.setValue(new JavaAccessObject(Character.TYPE,null));
        	        return;
        		}
        		if(cName.equals("byte")) { //$NON-NLS-1$
        	        FBSValue value=result.getFBSValue();
        	        javaArray.setValue(new JavaAccessObject(Byte.TYPE,null));
        	        return;
        		}
        		if(cName.equals("short")) { //$NON-NLS-1$
        	        FBSValue value=result.getFBSValue();
        	        javaArray.setValue(new JavaAccessObject(Short.TYPE,null));
        	        return;
        		}
        		if(cName.equals("int")) { //$NON-NLS-1$
        	        FBSValue value=result.getFBSValue();
        	        javaArray.setValue(new JavaAccessObject(Integer.TYPE,null));
        	        return;
        		}
        		if(cName.equals("long")) { //$NON-NLS-1$
        	        FBSValue value=result.getFBSValue();
        	        javaArray.setValue(new JavaAccessObject(Long.TYPE,null));
        	        return;
        		}
        		if(cName.equals("float")) { //$NON-NLS-1$
        	        FBSValue value=result.getFBSValue();
        	        javaArray.setValue(new JavaAccessObject(Float.TYPE,null));
        	        return;
        		}
        		if(cName.equals("double")) { //$NON-NLS-1$
        	        FBSValue value=result.getFBSValue();
        	        javaArray.setValue(new JavaAccessObject(Double.TYPE,null));
        	        return;
        		}
        		if(cName.equals("boolean")) { //$NON-NLS-1$
        	        FBSValue value=result.getFBSValue();
        	        javaArray.setValue(new JavaAccessObject(Boolean.TYPE,null));
        	        return;
        		}
        	}
	    	member.interpret(this,context,result);
	        FBSValue value=result.getFBSValue();
	        javaArray.setValue(value);
        }
    }

    private String getMemberAsString(ASTMember node) {
        FastStringBuffer buf = FastStringBufferPool.get();
        try {
        	appendMemberString(buf,node);
            // Remove the Packages prefix if needed
            String name = buf.toString();
            if(name.startsWith(JavaPackageObject.PACKAGES_START)) {
            	name = name.substring(JavaPackageObject.PACKAGES_START.length());
            }
            return name;
        } finally {
            FastStringBufferPool.recycle(buf);
        }
    }
    static private void appendMemberString(FastStringBuffer buf, ASTMember node) {
        if (node.getLeftNode() instanceof ASTMember){
        	appendMemberString(buf,(ASTMember)node.getLeftNode());
        }else{
            String idn=((ASTIdentifier)node.getLeftNode()).getIdentifierName();
            buf.append(idn);
        }
        buf.append('.');
        buf.append(node.getMemberName());
    }



    // this is an exceptional case.
    // every new expression that ends with [] it is considered as Java Array instantiation
    private boolean isJavaArray(){
        if (memberExpression instanceof ASTArrayMember){
            return true;
        }
        return false;
    }
    private String getJavaArrayType(){
        if (memberExpression instanceof ASTArrayMember){
            StringBuffer b = new StringBuffer();
            doGetJavaArrayType(b,memberExpression);
            return b.toString();
        }
        return null;
    }
    private boolean doGetJavaArrayType(StringBuffer b, ASTNode node){
        if( node instanceof ASTArrayMember ) {
            ASTArrayMember arrayMember = (ASTArrayMember)node;
            if( doGetJavaArrayType(b,arrayMember.getLeftNode()) ) {
                b.append( "[]" ); //$NON-NLS-1$
                return true;
            }
        } else if( node instanceof ASTMember ) {
            ASTMember member = (ASTMember)node;
            if( doGetJavaArrayType(b,member.getLeftNode()) ) {
                b.append( "." ); //$NON-NLS-1$
                b.append( member.getMemberName() );
                return true;
            }
        } else if( node instanceof ASTIdentifier ) {
            b.append( ((ASTIdentifier)node).getIdentifierName() );
            return true;
        }
        return false;
    }

    // Class to hold the temporary Java Array parameters
    private static class JavaArrayClass {
        FBSValue value;
        int[] dim=null;

        public void setValue(FBSValue v){
            this.value=v;
        }

        public String getClassName() throws InterpretException {
        	if( value instanceof JavaAccessObject ) {
        		JavaAccessObject ac = (JavaAccessObject)value;
        		return ac.getClassName();
        	}
            throw new InterpretException(null,com.ibm.jscript.util.StringUtil.format(JScriptResources.getString("ASTNew.NotAClass.Exception"))); //$NON-NLS-1$
        }

        public void insertDim(int d){
            int l=dim==null?0:dim.length;
            int[] newdim=new int[l+1];
            newdim[0]=d;
            if (l>0){
                System.arraycopy(dim,0,newdim,1,l);
            }
            dim=newdim;
        }

        public int[] getDim(){
            return dim;
        }
    }

    public Object visit(ASTNodeVisitor v, Object param) {
	return v.visitNew(this, param);
    }
}
