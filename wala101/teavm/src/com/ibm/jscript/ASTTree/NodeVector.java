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


/**
 * Optimized node vector.
 */
public final class NodeVector {

    private static final int INITIAL = 8;
    private static final int DELTA   = 8;

    private int count;
    private ASTNode[] nodes;

    public NodeVector() {
        this.count = 0;
        this.nodes = new ASTNode[INITIAL];
    }

    public void add(ASTNode node) {
        if( count==nodes.length ) {
            ASTNode[] old = nodes;
            nodes = new ASTNode[old.length+DELTA];
            System.arraycopy(old,0,nodes,0,old.length);
        }
        nodes[count++] = node;
    }

    public ASTNode get(int index) {
        // PHIL: why that test???
        //if (index<0 || index>=count) return null;
        return nodes[index];
    }

    public void remove(int index) {
        System.arraycopy(nodes,index+1,nodes,index,nodes.length-index-1);
        nodes[count-1]=null;
        count--;
    }

    public void clear() {
        for( int i=0; i<count; i++ ) {
            nodes[i] = null;
        }
        count = 0;
    }

    public int getCount(){
        return count;
    }

    public int size(){
        return count;
    }

    public void setSize(int size){
        if (size>count) {
            if(size>nodes.length) {
                ASTNode[] old=nodes;
                nodes=new ASTNode[size];
                System.arraycopy(old, 0, nodes, 0, old.length);
            }
        } else {
            for (int i=size ; i<count ; i++) {
                nodes[i] = null;
            }
        }
        count = size;
    }

}

/* OLD VERSION
public class NodeVector {
    Vector vector= new Vector();
    public NodeVector() {
    }

    public void add(ASTNode node){
        vector.add(node);
    }

    public ASTNode get(int index){
        if (index<0 || index>=getCount()) return null;
        return (ASTNode) vector.elementAt(index);
    }

    public void remove(int index){
        vector.remove(index);
    }

    public void clear(){
        vector.clear();
    }

    public int getCount(){
        return vector.size();
    }

    public int size(){
        return vector.size();
    }

    public void setSize(int size){
        vector.setSize(size);
    }

}
*/