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
import java.util.ArrayList;

import com.ibm.jscript.InterpretException;
import com.ibm.jscript.JScriptResources;
import com.ibm.jscript.JavaScriptException;
import com.ibm.jscript.engine.IExecutionContext;
import com.ibm.jscript.parser.ParseException;
import com.ibm.jscript.parser.Token;
import com.ibm.jscript.types.FBSType;
import com.ibm.jscript.types.FBSValue;
import com.ibm.jscript.util.StringUtil;

public abstract class ASTNode {

    private int beginLine;
    private int beginCol;
    private int endLine;
    private int endCol;

    public ASTNode() {
    }

    public ASTNode(Token t) {
        this.beginLine=t.beginLine;
        this.beginCol=t.beginColumn;
        this.endLine=t.endLine;
        this.endCol=t.endColumn;
    }

    /**
     * Get the resulting node type.
     */
    public FBSType getResultType(FBSType.ISymbolCallback symbolCallback) {
        return FBSType.undefinedType;
    }

    /**
     * @return a string representing the token/expression being executed.
     */
    public String getTraceString(){
        return ""; //$NON-NLS-1$
    }

    /**
     * Each node has a fixed or variable number of slots,
     * which are not necessarly filled so they can contain null value,
     * This methods returns the number of slots reserved in this node.
     * @return number of slots.
    */
    public abstract int getSlotCount();

    /**
     * reads the slot at position 'index'.
     * Value may be null.
     * @param index to be read
     * @return returns the value of a slot.
     */
    public abstract ASTNode readSlotAt(int index);

    /**
     * Add a new child node.
     * @param node the child to add
     * PHIL: don't know if that method is of any interest, at ASTNode level...
     */
    public void add(ASTNode node){
        throw new RuntimeException(StringUtil.format(JScriptResources.getString("ASTNode.NotSupported.Exception"),this.getClass().getName())); //$NON-NLS-1$
    }

    /**
     * Check the tree.
     */
    public void check() throws ParseException {

    }

    /**
     * Optimize the tree.
     */
    public void optimize() throws ParseException {
        // By default, optimize all the children node
        int count = getSlotCount();
        for( int i=0; i<count; i++ ) {
            ASTNode node = readSlotAt(i);
            if( node!=null ) {
                node.optimize();
            }
        }
    }

    /**
     * Check if the node is a constant.
     */
    public boolean isConstant() {
        return false;
    }
    public FBSValue evaluateConstant() throws InterpretException {
        throw new InterpretException( JScriptResources.getString("ASTNode.NotAConstant.Exception") ); //$NON-NLS-1$
    }

    /**
     * node evaluation.
     * @param caller
     * @param context
     * @param result
     * @throws JavaScriptException
     */
    public abstract void interpret(ASTNode caller, IExecutionContext context,InterpretResult result) throws JavaScriptException;

    /**
     * used by dump, each node implements this method to add extra informatgion about itself.
     */
    protected void extraInfo(PrintStream ps,String indent){
    }

    /**
     * dumps this node and all its children.
     * format is :
     *      <i>NodeName</i> : [<i>S/N</i>] [<i>SlotCount</i>]
     * @param ps, stream to which the dump is done
     * @param indent , indentation to be used.
     */
    private void dump(PrintStream ps, ArrayList nodes, FBSType.ISymbolCallback symbolCallback){
        // Print the indentation
        for( int lv=1; lv<nodes.size(); lv++ ) {
            ASTNode parent = (ASTNode)nodes.get(lv-1);
            if( parent.readSlotAt(parent.getSlotCount()-1)==nodes.get(lv) ) {
                ps.print( "    " ); //$NON-NLS-1$
            } else {
                ps.print( "|   " ); //$NON-NLS-1$
            }
        }

        nodes.add(this);
        try {

            Class c =getClass();
            String name=c.getName();
            int pos=name.lastIndexOf('.');
            name=name.substring(pos<0?0:pos+1);

            if(nodes.size()>1 ) {
                ps.print("+-"); //$NON-NLS-1$
            }
            ps.print(name);
            ps.print(" : "); //$NON-NLS-1$
            int cnt=getSlotCount();
            ps.print("[#child="); //$NON-NLS-1$
            ps.print(cnt);
            ps.print("] "); //$NON-NLS-1$
            extraInfo(ps,""); //$NON-NLS-1$
            if(symbolCallback!=null) {
                FBSType t = getResultType(symbolCallback);
                ps.print(", type="+t.toString()); //$NON-NLS-1$
            }
            ps.print("\n"); //$NON-NLS-1$

            for(int i=0;i<cnt;i++){
                ASTNode node=this.readSlotAt(i);
                if(node!=null){
                    node.dump(ps,nodes,symbolCallback);
                }else{
                    ps.print("<null node>\r\n"); //$NON-NLS-1$
                }
            }
        } finally {
            nodes.remove( nodes.size()-1 );
        }
    }


    /**
     * Dumps this node and all its children to the standard output.
     */
    public void dump(PrintStream ps, FBSType.ISymbolCallback symbolCallback ){
        if(ps==null) {
            ps = com.ibm.jscript.util.TDiag.getOutputStream();
        }
        dump(ps,new ArrayList(),symbolCallback);
    }

    public void dump(PrintStream ps){
        dump(ps,null);
    }

    public void dump(){
        dump(null,null);
    }

    public void setSourcePos(int beginLine,int beginCol, int endLine, int endCol) {
        this.beginLine=beginLine;
        this.beginCol=beginCol;
        this.endLine=endLine;
        this.endCol=endCol;
    }

    public int getBeginLine(){
        if( beginLine<=0 ) {
            computeBeginPositions();
        }
        return beginLine;
    }

    public int getBeginCol(){
        if( beginCol<=0 ) {
            computeBeginPositions();
        }
        return beginCol;
    }

    public int getEndLine(){
        if( endLine<=0 ) {
            computeEndPositions();
        }
        return endLine;
    }

    public int getEndCol(){
        if( endCol<=0 ) {
            computeEndPositions();
        }
        return endCol;
    }

    private void computeBeginPositions() {
        // Must be a non terminal node
        if (getSlotCount()<=0){ // bug #418, final solution will be with bug #213
            return;
        }
        // Compute the begin position fror the first slot
        ASTNode searchBegin = readSlotAt(0);
        if (searchBegin!=null){
            this.beginLine = searchBegin.getBeginLine();
            this.beginCol = searchBegin.getBeginCol();
        }
    }

    private void computeEndPositions() {
        // Must be a non terminal node
        if (getSlotCount()<=0){ // bug #418, final solution will be with bug #213
            return;
        }
        // Compute the last position from the last slot
        ASTNode searchEnd = readSlotAt(getSlotCount()-1);
        if (searchEnd!=null){
            this.endLine = searchEnd.getEndLine();
            this.endCol = searchEnd.getEndCol();
        }
    }

    public abstract Object visit(ASTNodeVisitor x, Object argument);

    public Object visit(ASTNodeVisitor x) { return visit(x, null); }
	
}
