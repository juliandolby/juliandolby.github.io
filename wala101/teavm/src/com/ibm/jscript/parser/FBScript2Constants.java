/* Generated By:JavaCC: Do not edit this line. FBScript2Constants.java */
package com.ibm.jscript.parser;

public interface FBScript2Constants {

  int EOF = 0;
  int EOL = 4;
  int SINGLE_LINE_COMMENT = 7;
  int FORMAL_COMMENT = 9;
  int MULTI_LINE_COMMENT = 10;
  int BREAK = 12;
  int CASE = 13;
  int CATCH = 14;
  int CONTINUE = 15;
  int _DEFAULT = 16;
  int DELETE = 17;
  int DO = 18;
  int ELSE = 19;
  int FALSE = 20;
  int FINALLY = 21;
  int FOR = 22;
  int FUNCTION = 23;
  int IF = 24;
  int IN = 25;
  int INFINITY = 26;
  int INSTANCEOF = 27;
  int NEW = 28;
  int NULL = 29;
  int UNDEFINED = 30;
  int RETURN = 31;
  int SWITCH = 32;
  int THIS = 33;
  int THROW = 34;
  int TRUE = 35;
  int TRY = 36;
  int TYPEOF = 37;
  int VAR = 38;
  int VOID = 39;
  int WHILE = 40;
  int WITH = 41;
  int __RETLAST = 42;
  int ABSTRACT = 43;
  int BOOLEAN = 44;
  int BYTE = 45;
  int CHAR = 46;
  int CLASS = 47;
  int CONST = 48;
  int DEBUGGER = 49;
  int DOUBLE = 50;
  int ENUM = 51;
  int EXPORT = 52;
  int EXTENDS = 53;
  int FINAL = 54;
  int FLOAT = 55;
  int GOTO = 56;
  int IMPLEMENTS = 57;
  int IMPORT = 58;
  int INT = 59;
  int INTERFACE = 60;
  int LONG = 61;
  int NATIVE = 62;
  int PACKAGE = 63;
  int PRIVATE = 64;
  int PROTECTED = 65;
  int PUBLIC = 66;
  int SHORT = 67;
  int STATIC = 68;
  int SUPER = 69;
  int SYNCHRONIZED = 70;
  int PROFILE = 71;
  int THROWS = 72;
  int TRANSIENT = 73;
  int VOLATILE = 74;
  int NUMERIC_LITERAL = 75;
  int INTEGER_LITERAL = 76;
  int DECIMAL_LITERAL = 77;
  int HEX_LITERAL = 78;
  int OCTAL_LITERAL = 79;
  int FLOATING_POINT_LITERAL = 80;
  int EXPONENT = 81;
  int STRING_LITERAL = 82;
  int ESCAPE_SEQUENCE = 83;
  int DATE_LITERAL = 84;
  int IDENTIFIER = 85;
  int LETTER = 86;
  int DIGIT = 87;
  int LPAREN = 88;
  int RPAREN = 89;
  int LBRACE = 90;
  int RBRACE = 91;
  int LBRACKET = 92;
  int RBRACKET = 93;
  int SEMICOLON = 94;
  int COMMA = 95;
  int DOT = 96;
  int ASSIGN = 97;
  int GT = 98;
  int LT = 99;
  int BANG = 100;
  int TILDE = 101;
  int HOOK = 102;
  int COLON = 103;
  int EQ = 104;
  int LE = 105;
  int GE = 106;
  int NE = 107;
  int SC_OR = 108;
  int SC_AND = 109;
  int INCR = 110;
  int DECR = 111;
  int PLUS = 112;
  int MINUS = 113;
  int STAR = 114;
  int SLASH = 115;
  int BIT_AND = 116;
  int BIT_OR = 117;
  int XOR = 118;
  int REM = 119;
  int LSHIFT = 120;
  int RSIGNEDSHIFT = 121;
  int RUNSIGNEDSHIFT = 122;
  int PLUSASSIGN = 123;
  int MINUSASSIGN = 124;
  int STARASSIGN = 125;
  int SLASHASSIGN = 126;
  int ANDASSIGN = 127;
  int ORASSIGN = 128;
  int XORASSIGN = 129;
  int REMASSIGN = 130;
  int LSHIFTASSIGN = 131;
  int RSIGNEDSHIFTASSIGN = 132;
  int RUNSIGNEDSHIFTASSIGN = 133;
  int REGEXP_BODY = 134;
  int REGEXP_END = 135;

  int DEFAULT = 0;
  int IN_SINGLE_LINE_COMMENT = 1;
  int IN_FORMAL_COMMENT = 2;
  int IN_MULTI_LINE_COMMENT = 3;
  int IN_REGEXP = 4;

  String[] tokenImage = {
    "<EOF>",
    "\" \"",
    "\"\\t\"",
    "\"\\f\"",
    "<EOL>",
    "<token of kind 5>",
    "\"/*\"",
    "<SINGLE_LINE_COMMENT>",
    "<token of kind 8>",
    "\"*/\"",
    "\"*/\"",
    "<token of kind 11>",
    "\"break\"",
    "\"case\"",
    "\"catch\"",
    "\"continue\"",
    "\"default\"",
    "\"delete\"",
    "\"do\"",
    "\"else\"",
    "\"false\"",
    "\"finally\"",
    "\"for\"",
    "\"function\"",
    "\"if\"",
    "\"in\"",
    "\"infinity\"",
    "\"instanceof\"",
    "\"new\"",
    "\"null\"",
    "\"undefined\"",
    "\"return\"",
    "\"switch\"",
    "\"this\"",
    "\"throw\"",
    "\"true\"",
    "\"try\"",
    "\"typeof\"",
    "\"var\"",
    "\"void\"",
    "\"while\"",
    "\"with\"",
    "\"__retlast\"",
    "\"abstract\"",
    "\"boolean\"",
    "\"byte\"",
    "\"char\"",
    "\"class\"",
    "\"const\"",
    "\"debugger\"",
    "\"double\"",
    "\"enum\"",
    "\"export\"",
    "\"extends\"",
    "\"final\"",
    "\"float\"",
    "\"goto\"",
    "\"implements\"",
    "\"import\"",
    "\"int\"",
    "\"interface\"",
    "\"long\"",
    "\"native\"",
    "\"package\"",
    "\"private\"",
    "\"protected\"",
    "\"public\"",
    "\"short\"",
    "\"static\"",
    "\"super\"",
    "\"synchronized\"",
    "\"__profile\"",
    "\"throws\"",
    "\"transient\"",
    "\"volatile\"",
    "<NUMERIC_LITERAL>",
    "<INTEGER_LITERAL>",
    "<DECIMAL_LITERAL>",
    "<HEX_LITERAL>",
    "<OCTAL_LITERAL>",
    "<FLOATING_POINT_LITERAL>",
    "<EXPONENT>",
    "<STRING_LITERAL>",
    "<ESCAPE_SEQUENCE>",
    "<DATE_LITERAL>",
    "<IDENTIFIER>",
    "<LETTER>",
    "<DIGIT>",
    "\"(\"",
    "\")\"",
    "\"{\"",
    "\"}\"",
    "\"[\"",
    "\"]\"",
    "\";\"",
    "\",\"",
    "\".\"",
    "\"=\"",
    "\">\"",
    "\"<\"",
    "\"!\"",
    "\"~\"",
    "\"?\"",
    "\":\"",
    "\"==\"",
    "\"<=\"",
    "\">=\"",
    "\"!=\"",
    "\"||\"",
    "\"&&\"",
    "\"++\"",
    "\"--\"",
    "\"+\"",
    "\"-\"",
    "\"*\"",
    "\"/\"",
    "\"&\"",
    "\"|\"",
    "\"^\"",
    "\"%\"",
    "\"<<\"",
    "\">>\"",
    "\">>>\"",
    "\"+=\"",
    "\"-=\"",
    "\"*=\"",
    "\"/=\"",
    "\"&=\"",
    "\"|=\"",
    "\"^=\"",
    "\"%=\"",
    "\"<<=\"",
    "\">>=\"",
    "\">>>=\"",
    "<REGEXP_BODY>",
    "<REGEXP_END>",
    "\"===\"",
    "\"!==\"",
  };

}
