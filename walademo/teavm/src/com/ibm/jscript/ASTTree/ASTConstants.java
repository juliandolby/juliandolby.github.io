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
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2000
 * Company:
 * @author
 * @version 1.0
 */

public class ASTConstants {

    public static final int ASSIGN=0;
    public static final int ADD_ASSIGN=1;
    public static final int SUB_ASSIGN=2;
    public static final int MUL_ASSIGN=3;
    public static final int DIV_ASSIGN=4;
    public static final int MOD_ASSIGN=5;
    public static final int LSH_ASSIGN=6;
    public static final int RSH_ASSIGN=7;
    public static final int URSH_ASSIGN=8;
    public static final int AND_ASSIGN=9;
    public static final int XOR_ASSIGN=10;
    public static final int OR_ASSIGN=11;
    public static final int MULTIPLY=12;
    public static final int DIVIDE=13;
    public static final int MODULO=14;
    public static final int ADD=15;
    public static final int SUBTRACT=16;
    public static final int SHIFT_LEFT=17;
    public static final int SHIFT_RIGHT=18;
    public static final int USHIFT_RIGHT=19;
    public static final int GREATER=20;
    public static final int LESS=21;
    public static final int LESS_EQUAL=22;
    public static final int GREATER_EQUAL=23;
    public static final int INSTANCEOF=24;
    public static final int IN=25;
    public static final int EQUAL=26;
    public static final int STRICT_EQUAL=27;
    public static final int NOT_EQUAL=28;
    public static final int STRICT_NOT_EQUAL=29;
    public static final int BITWISE_AND=30;
    public static final int BITWISE_XOR=31;
    public static final int BITWISE_OR=32;
    public static final int REL_AND=33;
    public static final int REL_OR=34;
    public static final int DELETE =35;
    public static final int VOID = 36;
    public static final int TYPEOF = 37;
    public static final int INC = 38;
    public static final int DEC = 39;
    public static final int INV=40;
    public static final int NEG=41;

    public static final String[] tokens ={
    "=", //$NON-NLS-1$
    "+=", //$NON-NLS-1$
    "-=", //$NON-NLS-1$
    "*=", //$NON-NLS-1$
    "/=", //$NON-NLS-1$
    "%=", //$NON-NLS-1$
    "<<=", //$NON-NLS-1$
    ">>=", //$NON-NLS-1$
    ">>>=", //$NON-NLS-1$
    "&=", //$NON-NLS-1$
    "^=", //$NON-NLS-1$
    "|=", //$NON-NLS-1$
    "*", //$NON-NLS-1$
    "/", //$NON-NLS-1$
    "%", //$NON-NLS-1$
    "+", //$NON-NLS-1$
    "-", //$NON-NLS-1$
    "<<", //$NON-NLS-1$
    ">>", //$NON-NLS-1$
    ">>>", //$NON-NLS-1$
    ">", //$NON-NLS-1$
    "<", //$NON-NLS-1$
    "<=", //$NON-NLS-1$
    ">=", //$NON-NLS-1$
    "instanceof", //$NON-NLS-1$
    "in", //$NON-NLS-1$
    "==", //$NON-NLS-1$
    "===", //$NON-NLS-1$
    "!=", //$NON-NLS-1$
    "!==", //$NON-NLS-1$
    "&", //$NON-NLS-1$
    "^", //$NON-NLS-1$
    "|", //$NON-NLS-1$
    "&&", //$NON-NLS-1$
    "||", //$NON-NLS-1$
    "delete", //$NON-NLS-1$
    "void", //$NON-NLS-1$
    "typeof", //$NON-NLS-1$
    "++", //$NON-NLS-1$
    "--", //$NON-NLS-1$
    "~", //$NON-NLS-1$
    "!" //$NON-NLS-1$
    };


    public static int getConstant(String token){
        for(int i=0;i<tokens.length;i++){
            if (tokens[i].equals(token)){
                return i;
            }
        }
        return -1;
    }

    public static String getToken(int constant){
        if (constant<0 || constant>=tokens.length){
            return null;
        }
        return tokens[constant];
    }

    public static boolean isRelationalOperator(String token){
        return isRelationalOperator(getConstant(token));
    }
    public static boolean isRelationalOperator(int constant){
        switch(constant){
            case GREATER:
            case LESS:
            case LESS_EQUAL:
            case GREATER_EQUAL:
            case INSTANCEOF:
            case IN:
            case EQUAL:
            case NOT_EQUAL:
            case STRICT_EQUAL:
            case STRICT_NOT_EQUAL:
            case REL_AND:
            case REL_OR:
                return true;
        }
        return false;
    }

    public static boolean isBinaryOperator(String token){
        return isBinaryOperator(getConstant(token));
    }

    public static boolean isBinaryOperator(int constant){
        switch(constant){
            case MULTIPLY:
            case DIVIDE:
            case MODULO:
            case ADD:
            case SUBTRACT:
            case SHIFT_LEFT:
            case SHIFT_RIGHT:
            case USHIFT_RIGHT:
            case BITWISE_AND:
            case BITWISE_XOR:
            case BITWISE_OR:
                 return true;
        }
        return false;
    }

    public static boolean isAssignOperator(String token){
        return isAssignOperator(getConstant(token));
    }

    public static boolean isAssignOperator(int constant){
        switch(constant){
            case ASSIGN:
            case ADD_ASSIGN:
            case SUB_ASSIGN:
            case MUL_ASSIGN:
            case DIV_ASSIGN:
            case MOD_ASSIGN:
            case LSH_ASSIGN:
            case RSH_ASSIGN:
            case URSH_ASSIGN:
            case AND_ASSIGN:
            case XOR_ASSIGN:
            case OR_ASSIGN:
                 return true;
        }
        return false;
    }

    public static boolean isIncDec(int op) {
      return op==INC || op==DEC;
    }
}