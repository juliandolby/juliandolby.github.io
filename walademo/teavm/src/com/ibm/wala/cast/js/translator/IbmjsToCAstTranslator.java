package com.ibm.wala.cast.js.translator;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ibm.jscript.ASTTree.ASTArgumentList;
import com.ibm.jscript.ASTTree.ASTArrayLiteral;
import com.ibm.jscript.ASTTree.ASTArrayMember;
import com.ibm.jscript.ASTTree.ASTAssign;
import com.ibm.jscript.ASTTree.ASTBlock;
import com.ibm.jscript.ASTTree.ASTBreak;
import com.ibm.jscript.ASTTree.ASTCall;
import com.ibm.jscript.ASTTree.ASTCase;
import com.ibm.jscript.ASTTree.ASTConstants;
import com.ibm.jscript.ASTTree.ASTContinue;
import com.ibm.jscript.ASTTree.ASTDebug;
import com.ibm.jscript.ASTTree.ASTDoWhile;
import com.ibm.jscript.ASTTree.ASTExpression;
import com.ibm.jscript.ASTTree.ASTFor;
import com.ibm.jscript.ASTTree.ASTForIn;
import com.ibm.jscript.ASTTree.ASTFunction;
import com.ibm.jscript.ASTTree.ASTIdentifier;
import com.ibm.jscript.ASTTree.ASTIf;
import com.ibm.jscript.ASTTree.ASTImport;
import com.ibm.jscript.ASTTree.ASTLabel;
import com.ibm.jscript.ASTTree.ASTLiteral;
import com.ibm.jscript.ASTTree.ASTMember;
import com.ibm.jscript.ASTTree.ASTNew;
import com.ibm.jscript.ASTTree.ASTNode;
import com.ibm.jscript.ASTTree.ASTNodeVisitor;
import com.ibm.jscript.ASTTree.ASTObjectLiteral;
import com.ibm.jscript.ASTTree.ASTProfile;
import com.ibm.jscript.ASTTree.ASTProgram;
import com.ibm.jscript.ASTTree.ASTRegExp;
import com.ibm.jscript.ASTTree.ASTReturn;
import com.ibm.jscript.ASTTree.ASTSwitch;
import com.ibm.jscript.ASTTree.ASTSync;
import com.ibm.jscript.ASTTree.ASTTernaryOp;
import com.ibm.jscript.ASTTree.ASTThis;
import com.ibm.jscript.ASTTree.ASTThrow;
import com.ibm.jscript.ASTTree.ASTTry;
import com.ibm.jscript.ASTTree.ASTUnaryOp;
import com.ibm.jscript.ASTTree.ASTVariableDecl;
import com.ibm.jscript.ASTTree.ASTWhile;
import com.ibm.jscript.ASTTree.ASTWith;
import com.ibm.jscript.ASTTree.binaryop.ASTBinaryAdd;
import com.ibm.jscript.ASTTree.binaryop.ASTBinaryDefaultOp;
import com.ibm.jscript.ASTTree.binaryop.ASTBinaryRelAnd;
import com.ibm.jscript.ASTTree.binaryop.ASTBinaryRelOr;
import com.ibm.jscript.parser.FBScript2;
import com.ibm.jscript.parser.ParseException;
import com.ibm.jscript.types.FBSValue;
import com.ibm.wala.cast.js.html.MappedSourceModule;
import com.ibm.wala.cast.js.ipa.callgraph.JSSSAPropagationCallGraphBuilder;
import com.ibm.wala.cast.js.loader.JavaScriptLoader;
import com.ibm.wala.cast.js.types.JavaScriptTypes;
import com.ibm.wala.cast.tree.CAst;
import com.ibm.wala.cast.tree.CAstAnnotation;
import com.ibm.wala.cast.tree.CAstControlFlowMap;
import com.ibm.wala.cast.tree.CAstEntity;
import com.ibm.wala.cast.tree.CAstNode;
import com.ibm.wala.cast.tree.CAstNodeTypeMap;
import com.ibm.wala.cast.tree.CAstQualifier;
import com.ibm.wala.cast.tree.CAstSourcePositionMap;
import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
import com.ibm.wala.cast.tree.CAstType;
import com.ibm.wala.cast.tree.impl.CAstOperator;
import com.ibm.wala.cast.tree.impl.CAstSymbolImpl;
import com.ibm.wala.classLoader.ModuleEntry;
import com.ibm.wala.util.collections.EmptyIterator;
import com.ibm.wala.util.debug.Assertions;

public class IbmjsToCAstTranslator {

  private final boolean DEBUG = true;

  private interface WalkContext extends JavaScriptTranslatorToCAst.WalkContext<WalkContext, ASTNode> {

  }

  /*
  private interface WalkContext {

    String script();

    ASTNode top();

    void addScopedEntity(CAstNode construct, CAstEntity e);

    Map<CAstNode, CAstEntity> getScopedEntities();

    void addInitializer(CAstNode n);

    Collection<CAstNode> getInitializers();

    CAstControlFlowRecorder cfg();

    CAstSourcePositionRecorder pos();

    CAstNode getCatchTarget();

    ASTNode getContinueFor(String label);

    ASTNode getBreakFor(String label);

    int setOperation(ASTNode node);

    boolean foundMemberOperation(ASTNode node);

    void copyOperation(ASTNode from, ASTNode to);
  };
*/
  
  public static class RootContext extends JavaScriptTranslatorToCAst.RootContext<WalkContext,ASTNode> implements WalkContext {
	  
  }

  private static class FunctionContext extends JavaScriptTranslatorToCAst.FunctionContext<WalkContext,ASTNode> implements WalkContext {
	protected FunctionContext(WalkContext parent, ASTNode s) {
		super(parent, s);
	}
  }
  
  private static class ScriptContext extends FunctionContext {
    private final String script;

    ScriptContext(WalkContext parent, ASTProgram s, String script) {
      super(parent, s);
      this.script = script;
    }

    public String script() { return script; }
  }

  private static class TryCatchContext extends JavaScriptTranslatorToCAst.TryCatchContext<WalkContext, ASTNode> implements WalkContext {
	protected TryCatchContext(WalkContext parent, CAstNode catchNode) {
		super(parent, catchNode);
	}
  }

  private class SwitchContext extends JavaScriptTranslatorToCAst.BreakContext<WalkContext, ASTNode> implements WalkContext {
	protected SwitchContext(WalkContext parent, ASTNode breakTarget, String label) {
		super(parent, breakTarget, label);
	}
  }

  private class LoopContext extends JavaScriptTranslatorToCAst.LoopContext<WalkContext, ASTNode> implements WalkContext {
	protected LoopContext(WalkContext parent, ASTNode breakTo,
			ASTNode continueTo, String label) {
		super(parent, breakTo, continueTo, label);
	}	  
  }

  private static class MemberDestructuringContext extends JavaScriptTranslatorToCAst.MemberDestructuringContext<WalkContext, ASTNode> implements WalkContext {
	protected MemberDestructuringContext(WalkContext parent,
			ASTNode initialBaseFor, int operationIndex) {
		super(parent, initialBaseFor, operationIndex);
	}	  
  }
  
  /*
    private final Set<ASTNode> baseFor = new HashSet<ASTNode>();
    private final int operationIndex;
    private boolean foundBase = false;

    MemberDestructuringContext(WalkContext parent, ASTNode initialBaseFor, int operationIndex) {
      super(parent);
      baseFor.add( initialBaseFor );
      this.operationIndex = operationIndex;
    }

    public int setOperation(ASTNode node) { 
      if (baseFor.contains( node )) {
    	  foundBase = true;
    	  return operationIndex;
      } else {
    	  return -1;
      }
    }
      
    public boolean foundMemberOperation(ASTNode node) {
      return foundBase;
    }

    public void copyOperation(ASTNode from, ASTNode to) {
      if (baseFor.contains(from)) baseFor.add(to);
    }
  }
  */
  
  CAstOperator translateOpcode(int op) {
    switch (op) {
    case ASTConstants.INC:
    case ASTConstants.ADD:
    case ASTConstants.ADD_ASSIGN:
      return CAstOperator.OP_ADD;

    case ASTConstants.DEC:
    case ASTConstants.SUBTRACT:
    case ASTConstants.SUB_ASSIGN:
      return CAstOperator.OP_SUB;
	  
    case ASTConstants.MULTIPLY:
    case ASTConstants.MUL_ASSIGN:
      return CAstOperator.OP_MUL;

    case ASTConstants.DIVIDE:
    case ASTConstants.DIV_ASSIGN:
      return CAstOperator.OP_DIV;

    case ASTConstants.MODULO:
    case ASTConstants.MOD_ASSIGN:
      return CAstOperator.OP_MOD;

    case ASTConstants.SHIFT_LEFT:
    case ASTConstants.LSH_ASSIGN:
      return CAstOperator.OP_LSH;
      
    case ASTConstants.SHIFT_RIGHT:
    case ASTConstants.RSH_ASSIGN:
      return CAstOperator.OP_RSH;
      
    case ASTConstants.USHIFT_RIGHT:
    case ASTConstants.URSH_ASSIGN:
      return CAstOperator.OP_URSH;
      
    case ASTConstants.BITWISE_AND:
    case ASTConstants.AND_ASSIGN:
      return CAstOperator.OP_BIT_AND;
      
    case ASTConstants.BITWISE_XOR:
    case ASTConstants.XOR_ASSIGN:
      return CAstOperator.OP_BIT_XOR;

    case ASTConstants.BITWISE_OR:
    case ASTConstants.OR_ASSIGN:
      return CAstOperator.OP_BIT_OR;

    case ASTConstants.GREATER:
      return CAstOperator.OP_GT;

    case ASTConstants.LESS:
      return CAstOperator.OP_LT;

    case ASTConstants.LESS_EQUAL:
      return CAstOperator.OP_LE;

    case ASTConstants.GREATER_EQUAL:
      return CAstOperator.OP_GE;

    case ASTConstants.EQUAL:
    case ASTConstants.STRICT_EQUAL:
      return CAstOperator.OP_EQ;

    case ASTConstants.NOT_EQUAL:
    case ASTConstants.STRICT_NOT_EQUAL:
      return CAstOperator.OP_NE;

    case ASTConstants.REL_AND:
      return CAstOperator.OP_REL_AND;

    case ASTConstants.REL_OR:
      return CAstOperator.OP_REL_OR;

    case ASTConstants.INV:
      return CAstOperator.OP_BITNOT;

    case ASTConstants.NEG:    
      return CAstOperator.OP_NOT;

    default:
      Assertions.UNREACHABLE("do not understand opcode " + op);
      return null;
    }
  }

  private CAstNode makeBuiltinNew(String typeName) {
    return Ast.makeNode(CAstNode.NEW, Ast.makeConstant( typeName ));
  }

  private boolean isPrologueScript() {
    return JavaScriptLoader.bootstrapFileNames.contains( scriptName );
  }

  private boolean isPrimitiveCall(ASTNode n) {
    return 
      isPrologueScript() 
	        &&
      n instanceof ASTCall
	        &&
      n.readSlotAt(0) instanceof ASTIdentifier
	        &&
      ((ASTIdentifier)n.readSlotAt(0)).getIdentifierName().equals("primitive");
  }

  private boolean isPrimitiveCreation(ASTNode n) {
    return 
      isPrologueScript() 
	        &&
      n instanceof ASTNew
	        &&
      n.readSlotAt(0) instanceof ASTIdentifier
	        &&
      ((ASTIdentifier)n.readSlotAt(0)).getIdentifierName().equals("Primitives");
  }

  private Position makePosition(final ASTNode o) {
	 
	  class IbmjsSourcePosition /* extends AbstractSourcePosition */ implements Position {
		  private final URL url;
		  private final int firstLine;
		  private final int lastLine;
		 
		  protected IbmjsSourcePosition(URL url, int firstLine, int lastLine) {
			super();
			this.url = url;
			this.firstLine = firstLine;
			this.lastLine = lastLine;
		}
		  
		  public int getFirstLine() { return firstLine; }
		  public int getLastLine() { return lastLine; }

		  // IbmJS seems to number columns starting from 1 rather than 0, which
		  // defies the conventions I am used to,  Hence, I subtract 1 from all
		  // column numbers
		  public int getFirstCol() { return o.getBeginCol()-1; }
		  public int getLastCol() { return o.getEndCol()-1; }

		  public int getFirstOffset() { return -1; }
		  public int getLastOffset() { return -1; }

		  public URL getURL() {
			  return url;
		  }
		  public Reader getReader() throws IOException { 
			  return new InputStreamReader(url.openConnection().getInputStream());
		  }
		  public String toString() {
			  return "["+getFirstLine()+":"+getFirstCol()+"]->["+getLastLine()+":"+getLastCol()+"]";
		  }

		public int compareTo(Object o) {
			// TODO Auto-generated method stub
			return 0;
		}

	  };
	  
	  Position p = new IbmjsSourcePosition(null, o.getBeginLine(), o.getEndLine());

	  if (sourceModule instanceof MappedSourceModule) {
		  Position np = ((MappedSourceModule) sourceModule).getMapping().getIncludedPosition(p);
		  if (np != null) {
			  return np;
		  }
	  }
	  
	  return p;
  }

  private CAstNode notePosition(WalkContext context, final ASTNode o, CAstNode n) {
    context.pos().setPosition(n, makePosition(o));
    return n;
  }

  private CAstNode walkNodes(final ASTNode n, final WalkContext context) {
    if (n == null) return Ast.makeNode(CAstNode.EMPTY);
    return notePosition(context, n, (CAstNode)n.visit(new ASTNodeVisitor() {
        private CAstNode makeVarRef(String varName) {
        	CAstNode ref = Ast.makeNode(CAstNode.VAR, Ast.makeConstant(varName));
        	context.cfg().map(ref, ref);
        	return ref;
        }

        public Object visitArgumentList(ASTArgumentList x, Object param) {
	Assertions.UNREACHABLE();
      	return null;
      }

      private CAstNode makeCall(ASTNode originalAst, CAstNode fun, CAstNode thisptr, ASTArgumentList args, WalkContext context, String calleeName) {
	int i = 0;
	CAstNode call;
	boolean hasThis = (thisptr != null);
	if (args == null) {
	  CAstNode arguments[] = new CAstNode[ hasThis? 3: 2 ];
	  arguments[i++] = fun;
	  arguments[i++] = Ast.makeConstant( calleeName );
	  if (thisptr!=null) arguments[i++] = thisptr;
	  call = Ast.makeNode(CAstNode.CALL, arguments);
	} else {
	  int children = args.getSlotCount();
	  int nargs = (thisptr==null)? children+2: children+3;
	  CAstNode arguments[] = new CAstNode[ nargs ];
	  arguments[i++] = fun;
	  arguments[i++] = Ast.makeConstant( calleeName );
	  if (thisptr!=null) arguments[i++] = thisptr;
	  for(int j = 0; j < children; j++)
	    arguments[i++] = walkNodes( args.readSlotAt(j), context );
	  
	  call = Ast.makeNode(CAstNode.CALL, arguments);
	}

	Object src = (originalAst==null)? (Object)call: (Object)originalAst;
	context.cfg().map(src, call);
	if (context.getCatchTarget() != null) {
	  context.cfg().add(src, context.getCatchTarget(), null);
	} 

	return call;
      }
	  
      private CAstNode makeCall(CAstNode fun, CAstNode thisptr, ASTCall x, WalkContext context, String calleeName) {
	return makeCall(x, fun, thisptr, x.getASTArgumentList(), context, calleeName);
      }

      private CAstNode handleNew(WalkContext context, ASTNode newAst, CAstNode value, ASTArgumentList arguments) {
	return makeCall(newAst, value, null, arguments, context, "ctor");
      }

      private CAstNode handleNew(WalkContext context, String globalName, ASTArgumentList arguments) {
	return handleNew(context, null, makeVarRef(globalName), arguments);
      }

      public Object visitArrayLiteral(ASTArrayLiteral x, Object param) {
	ArrayList<CAstNode> args = new ArrayList<CAstNode>();
	args.add( 
	  (isPrologueScript())?
	    makeBuiltinNew("Array"):
	    handleNew(context, "Array", null));
	for(int i = 0; i < x.getSlotCount(); i++) {
	  if (x.readSlotAt(i) != null) {
	    args.add(Ast.makeConstant( (double)i ));
	    args.add( walkNodes(x.readSlotAt(i), context) );
	  }
	}

	CAstNode lit = Ast.makeNode(CAstNode.OBJECT_LITERAL, 
	  args.toArray(new CAstNode[ args.size() ]));
	context.cfg().map(x, lit);
	return lit;
      }
     
      private String operationReceiverName(int operationIndex) {
    	  return "$$destructure$rcvr" + operationIndex;
      }

      private CAstNode operationReceiverVar(int operationIndex) {
    	  return Ast.makeNode(CAstNode.VAR, Ast.makeConstant(operationReceiverName(operationIndex)));
      }
      
      private String operationElementName(int operationIndex) {
    	  return "$$destructure$elt" + operationIndex;
      }

      private CAstNode operationElementVar(int operationIndex) {
    	  return Ast.makeNode(CAstNode.VAR, Ast.makeConstant(operationElementName(operationIndex)));
      }

      private CAstNode doMember(ASTNode expr, CAstNode elt) {
    	  CAstNode rcvr = walkNodes(expr.readSlotAt(0), context);
    	  int operationIndex = context.setOperation(expr);

    	  if (operationIndex != -1) {
    		  return Ast.makeNode(CAstNode.BLOCK_EXPR,
    			Ast.makeNode(CAstNode.ASSIGN, operationReceiverVar(operationIndex), rcvr),
    			Ast.makeNode(CAstNode.ASSIGN, operationElementVar(operationIndex), elt));
    	  } else {
    		  CAstNode get = Ast.makeNode(CAstNode.OBJECT_REF, rcvr, elt);
    		  context.cfg().map(get, get);
    		  context.cfg().add(
    		      get, 
    		      context.getCatchTarget() != null? context.getCatchTarget(): CAstControlFlowMap.EXCEPTION_TO_EXIT,
    		      JavaScriptTypes.TypeError);
    		  return get;
    	  }
      }

      public Object visitArrayMember(ASTArrayMember x, Object param) {
	return doMember(x, walkNodes(x.readSlotAt(1), context));
      }
      
      public Object visitAssign(ASTAssign x, Object param) {
	if (x.getType() == ASTConstants.ASSIGN)
	  return Ast.makeNode(CAstNode.ASSIGN,			      
	    walkNodes( x.readSlotAt(0), context ),
	    walkNodes( x.readSlotAt(1), context ));
	else 
	  return Ast.makeNode(CAstNode.ASSIGN_POST_OP,
	    walkNodes( x.readSlotAt(0), context ),
	    walkNodes( x.readSlotAt(1), context ),
	    translateOpcode( x.getType() ));
      }
      
      public Object visitBinaryAdd(ASTBinaryAdd x, Object param) {
	return Ast.makeNode(CAstNode.BINARY_EXPR,
	  CAstOperator.OP_ADD,
	  walkNodes( x.readSlotAt(0), context ),
	  walkNodes( x.readSlotAt(1), context ));
      }
      
      public Object visitBinaryDefaultOp(ASTBinaryDefaultOp x, Object param) {
    	  if (x.getOperator() == ASTConstants.INSTANCEOF) {
    		  return Ast.makeNode(CAstNode.INSTANCEOF,
    	   	    		walkNodes( x.readSlotAt(0), context ),
        	    		walkNodes( x.readSlotAt(1), context ));
    	  } else if (x.getOperator() == ASTConstants.IN) {
    		  return Ast.makeNode(CAstNode.IS_DEFINED_EXPR,
  	   	    		walkNodes( x.readSlotAt(0), context ),
      	    		walkNodes( x.readSlotAt(1), context ));    		  
    	  } else {
    	    return Ast.makeNode(CAstNode.BINARY_EXPR,
    	    		translateOpcode( x.getOperator() ),
    	    		walkNodes( x.readSlotAt(0), context ),
    	    		walkNodes( x.readSlotAt(1), context ));
    	  }
      }
      
      public Object visitBinaryRelAnd(ASTBinaryRelAnd x, Object param) {
	return Ast.makeNode(CAstNode.IF_EXPR, 
	  walkNodes(x.readSlotAt(0), context),
	  walkNodes(x.readSlotAt(1), context),
	  Ast.makeConstant( false ));
      }
      
      public Object visitBinaryRelOr(ASTBinaryRelOr x, Object param) {
	return Ast.makeNode(CAstNode.IF_EXPR, 
	  walkNodes(x.readSlotAt(0), context),
	  Ast.makeConstant( true ),
	  walkNodes(x.readSlotAt(1), context));
      }
      
      public Object visitBlock(ASTBlock x, Object param) {
	return Ast.makeNode(CAstNode.BLOCK_STMT, gatherChildren(x, context));
      }
      
      public Object visitBreak(ASTBreak x, Object param) {
	ASTNode target = context.getBreakFor(x.getTargetLabel());
	context.cfg().add(x, target, null);
	CAstNode result = Ast.makeNode(CAstNode.GOTO);
	context.cfg().map(x, result);
	return result;
      }
      
      public Object visitCall(ASTCall x, Object param) {
	if (! isPrimitiveCall(x)) {
	  ASTNode callee = x.readSlotAt(0);
	  int thisBaseVarNum = ++baseVarNum;
	  WalkContext child = new MemberDestructuringContext(context, callee, thisBaseVarNum);
	  CAstNode fun = walkNodes( callee, child );

	  if (child.foundMemberOperation(callee))
	    return Ast.makeNode(CAstNode.LOCAL_SCOPE,
	      Ast.makeNode(CAstNode.BLOCK_EXPR,
	        Ast.makeNode(CAstNode.DECL_STMT, 
	        	Ast.makeConstant(new CAstSymbolImpl(operationReceiverName(thisBaseVarNum), JSAstTranslator.Any)),
	        	Ast.makeConstant(null)),
		    Ast.makeNode(CAstNode.DECL_STMT, 
		    	Ast.makeConstant(new CAstSymbolImpl(operationElementName(thisBaseVarNum), JSAstTranslator.Any)),
		    	Ast.makeConstant(null)),
		    fun,
		    makeCall(operationElementVar(thisBaseVarNum), operationReceiverVar(thisBaseVarNum), x, context, "dispatch")));
	  else 
	    return makeCall(fun, makeVarRef(JSSSAPropagationCallGraphBuilder.GLOBAL_OBJ_VAR_NAME), x, context, "do");
	} else {
	  return Ast.makeNode(CAstNode.PRIMITIVE, 
	    gatherChildren(x.readSlotAt(1), context));
	}
      }
      
      public Object visitCase(ASTCase x, Object param) {
    	  return
    	  	(x.getSlotCount() == 0)?
    	  	  Ast.makeNode(CAstNode.EMPTY):
    	  	  Ast.makeNode(CAstNode.BLOCK_STMT, 
    	  	    gatherChildren(x, context));
      }
      
      public Object visitContinue(ASTContinue x, Object param) {
	ASTNode target = context.getContinueFor(x.getTargetLabel());
	context.cfg().add(x, target, null);
	CAstNode result = Ast.makeNode(CAstNode.GOTO);
	context.cfg().map(x, result);
	return result;
      }  
      
      public Object visitDebug(ASTDebug x, Object param) {
	Assertions.UNREACHABLE();
	return null;
      }
      
      public Object visitDoWhile(ASTDoWhile x, Object param) {  
	ASTNode headerLabel = new ASTLabel( "entry" );
	ASTNode breakLabel = new ASTLabel( "break" );
	ASTNode contLabel = new ASTLabel( "continue" );

	CAstNode loopGoto = Ast.makeNode(CAstNode.IFGOTO,
	  walkNodes( x.readSlotAt(ASTDoWhile.EXPRESSION), context ));
	context.cfg().add(loopGoto, headerLabel, Boolean.TRUE);
	context.cfg().map(loopGoto, loopGoto);

	WalkContext child = new LoopContext(context, breakLabel, contLabel, x.getLabelName());
	return Ast.makeNode(CAstNode.BLOCK_STMT,
	  walkNodes(headerLabel, context),
	  walkNodes(x.readSlotAt(ASTDoWhile.STATEMENT),child),
	  walkNodes(contLabel, context),	  
	  loopGoto,
	  walkNodes(breakLabel, context));
      }
      
      public Object visitExpression(ASTExpression x, Object param) {
        return Ast.makeNode(CAstNode.BLOCK_EXPR, gatherChildren(x, context));
      }
      
      public Object visitFor(ASTFor x, Object param) {
	ASTNode breakLabel = new ASTLabel( "break" );
	ASTNode contLabel = new ASTLabel( "continue" );
	WalkContext child = new LoopContext(context, breakLabel, contLabel, x.getLabelName());

	return Ast.makeNode(CAstNode.BLOCK_STMT,
	  walkNodes( x.readSlotAt(ASTFor.INIT), context ),
	  Ast.makeNode(CAstNode.LOOP,
	    walkNodes( x.readSlotAt(ASTFor.CONDITION), context ),
	    Ast.makeNode(CAstNode.BLOCK_STMT,
	      walkNodes( x.readSlotAt(ASTFor.STATEMENT), child ),
	      walkNodes(contLabel, context),	  
	      walkNodes( x.readSlotAt(ASTFor.UPDATE), context ))),
	  walkNodes(breakLabel, context));
      }

      public Object visitForIn(ASTForIn x, Object param) {
	ASTNode breakLabel = new ASTLabel( "break" );
	ASTNode contLabel = new ASTLabel( "continue" );
	WalkContext child = new LoopContext(context, breakLabel, contLabel, x.getLabelName());

	return Ast.makeNode(CAstNode.BLOCK_STMT,
	  Ast.makeNode(CAstNode.DECL_STMT, 
	    Ast.makeConstant(new CAstSymbolImpl("obj tmp", JSAstTranslator.Any, false)),
	    walkNodes(x.readSlotAt(1), context)),
	  
	  Ast.makeNode(CAstNode.LOOP,
	    Ast.makeNode(CAstNode.BINARY_EXPR,
	      CAstOperator.OP_NE,
		  Ast.makeConstant(null),
	      Ast.makeNode(CAstNode.ASSIGN,
	        walkNodes(x.readSlotAt(0), context),
	        Ast.makeNode(CAstNode.EACH_ELEMENT_GET,
	          Ast.makeNode(CAstNode.VAR, Ast.makeConstant("obj tmp")),
	          walkNodes(x.readSlotAt(0), context)))),
	    Ast.makeNode(CAstNode.BLOCK_STMT,
	       walkNodes(x.readSlotAt(2), child),
	       walkNodes(contLabel, context))),
	  	walkNodes(breakLabel, context));
      }
      
      public Object visitFunction(ASTFunction x, Object param) {
	CAstEntity fne = walkEntity(x, context);

	CAstNode ast;
	if (x.getExpression()) {
	  ast = Ast.makeNode(CAstNode.FUNCTION_EXPR, Ast.makeConstant( fne ));
	} else {
	  context.addNameDecl(
	    Ast.makeNode(CAstNode.FUNCTION_STMT, Ast.makeConstant( fne )) );

	  ast = Ast.makeNode( CAstNode.EMPTY );
	}

	context.addScopedEntity(ast, fne);
	return ast;	  
      }
      
      public Object visitIdentifier(ASTIdentifier x, Object param) {
      	CAstNode ref = Ast.makeNode(CAstNode.VAR, Ast.makeConstant( x.getIdentifierName() ));
      	context.cfg().map(x, ref);
      	return ref;
      }
      
      public Object visitIf(ASTIf x, Object param) {
	if (x.readSlotAt( ASTIf.ELSESTATEMENT) != null)
	  return Ast.makeNode(CAstNode.IF_STMT,
	    walkNodes( x.readSlotAt(ASTIf.CONDITION), context ),
	    walkNodes( x.readSlotAt(ASTIf.STATEMENT), context ),
	    walkNodes( x.readSlotAt(ASTIf.ELSESTATEMENT), context ));
	else
	  return Ast.makeNode(CAstNode.IF_STMT,
	    walkNodes( x.readSlotAt(ASTIf.CONDITION), context ),
	    walkNodes( x.readSlotAt(ASTIf.STATEMENT), context ));
      }
      
      public Object visitImport(ASTImport x, Object param) {
	Assertions.UNREACHABLE();
      	return null;
      }
      
      public Object visitLabel(ASTLabel x, Object param) {
	CAstNode result = Ast.makeNode(CAstNode.LABEL_STMT, 
          Ast.makeConstant( x.getLabel() ),
          Ast.makeNode(CAstNode.EMPTY));

	context.cfg().map(x, result);
	return result;
      }
      
      public Object visitLiteral(ASTLiteral x, Object param) {
	FBSValue v = x.getValue();
	if (v.isNumber())
	  return Ast.makeConstant( v.numberValue() );
	else if (v.isBoolean())
	  return Ast.makeConstant( v.booleanValue() );
	else if (v.isString())
	  return Ast.makeConstant( v.stringValue() );
	else if (v.isNull())
	  return Ast.makeConstant( null );
	else if (v.isUndefined())
	  return makeVarRef("undefined");
	else {
	  throw new RuntimeException("cannot translate literal " + x + " with FBSValue " + v);
	}
      }
      
      public Object visitMember(ASTMember x, Object param) {
	return doMember(x, Ast.makeConstant( x.getMemberName() ));
      }      
      
      public Object visitNew(ASTNew x, Object param) {
	if (isPrimitiveCreation(n)) {
	  return makeBuiltinNew(((ASTIdentifier)x.readSlotAt(0)).getIdentifierName());
	} else {
	  return handleNew(context, x, walkNodes(x.readSlotAt(0), context), (ASTArgumentList)x.readSlotAt(1));
	}
      }
      
      public Object visitObjectLiteral(ASTObjectLiteral x, Object param) {
	int propertyCount = x.getSlotCount();
	CAstNode[] args = new CAstNode[ propertyCount*2 + 1 ];
	int i = 0;
	args[i++] = 
	  ((isPrologueScript())?
	   makeBuiltinNew("Object"):
	   handleNew(context, "Object", null));

	for(int j = 0; j < propertyCount; j++) {
	  ASTObjectLiteral.Initializer init = x.readInitAt(j);
	  args[i++] = Ast.makeConstant( init.field.toString() );
	  args[i++] = walkNodes( init.init, context );
	}

	CAstNode lit = Ast.makeNode(CAstNode.OBJECT_LITERAL, args);
   	context.cfg().map(x, lit);
    	return lit;
      }

      public Object visitProfile(ASTProfile x, Object param) {
	Assertions.UNREACHABLE();
      	return null;
      }
      
      public Object visitProgram(ASTProgram x, Object param) {
	Assertions.UNREACHABLE();
      	return null;
      }
      
      public Object visitRegExp(ASTRegExp x, Object param) {
	ASTArgumentList fake = new ASTArgumentList();
	fake.add( new ASTLiteral( x.getRegExp() ) );
	fake.add( new ASTLiteral( x.getFlags() ) );
	return handleNew(context, "RegExp", fake);
      }
	    
      public Object visitReturn(ASTReturn x, Object param) {
	if (x.getSlotCount() == 1) {
	  return Ast.makeNode(CAstNode.RETURN, walkNodes(x.readSlotAt(0), context));
	} else {
	  return Ast.makeNode(CAstNode.RETURN);
	}
      }
      
      public Object visitSwitch(ASTSwitch x, Object param) {
    	  List<CAstNode> children = new ArrayList<CAstNode>();
    	  ASTNode breakLabel = new ASTLabel( "break" );
    	  CAstNode breakAst = walkNodes(breakLabel, context);

    	  WalkContext child = new SwitchContext(context, breakLabel, null);

    	  for(int i = 1; i < x.getSlotCount(); i++) {
    		  CAstNode label = 
    			  Ast.makeNode(CAstNode.LABEL_STMT, 
    					  Ast.makeConstant(String.valueOf(i)), 
    					  Ast.makeNode(CAstNode.EMPTY));
    		  context.cfg().map(label, label);
    		  children.add(label);
    		  ASTCase n = (ASTCase) x.readSlotAt(i);
    		  if (n.isDefault())
    			  context.cfg().add(x, label, CAstControlFlowMap.SWITCH_DEFAULT);
    		  else {
    			  CAstNode labelCAst = walkNodes(n.getExpression(), context);
    			  context.cfg().add(x, label, labelCAst);
    		  }
	 
    		  children.add(walkNodes(n, child));
    	  }
	    
    	  CAstNode switchAst = 
    		  Ast.makeNode(CAstNode.SWITCH, 
    				  walkNodes(x.readSlotAt(0), context),
    				  Ast.makeNode(CAstNode.BLOCK_STMT, 
    						  children.toArray(new CAstNode[children.size()])));
    						  
    	  context.cfg().map(x, switchAst);

    	  return Ast.makeNode(CAstNode.BLOCK_STMT, switchAst, breakAst);
      }
      
      public Object visitSync(ASTSync x, Object param) {
	Assertions.UNREACHABLE();
      	return null;
      }
      
      public Object visitTernaryOp(ASTTernaryOp x, Object param) {
	return Ast.makeNode(CAstNode.IF_EXPR,
	  walkNodes( x.readSlotAt(ASTTernaryOp.CONDITION), context ),
	  walkNodes( x.readSlotAt(ASTTernaryOp.TRUESTATEMENT), context ),
	  walkNodes( x.readSlotAt(ASTTernaryOp.FALSESTATEMENT), context ));
      }
      
      public Object visitThis(ASTThis x, Object param) {
    	  if (context.top() instanceof ASTProgram) {
  		    return makeVarRef(JSSSAPropagationCallGraphBuilder.GLOBAL_OBJ_VAR_NAME);
  		  } else {
  		    return Ast.makeNode(CAstNode.VAR, Ast.makeConstant("this"));
  		  }
      }
      
      public Object visitThrow(ASTThrow x, Object param) {
	CAstNode result = Ast.makeNode(CAstNode.THROW, 
	  walkNodes(x.readSlotAt(0), context));

	context.cfg().map(x, result);

	CAstNode catchNode = context.getCatchTarget();
	if (catchNode != null)
	  context.cfg().add(x, context.getCatchTarget(), null);
	else
	  context.cfg().add(x, CAstControlFlowMap.EXCEPTION_TO_EXIT, null);

	return result;
      }
      
      public Object visitTry(ASTTry x, Object param) {

	// try { } catch { } case
	if (x.readSlotAt(2) == null) {

	  CAstNode catchNode = Ast.makeNode(CAstNode.CATCH,
	    Ast.makeConstant( x.getCatchId() ),
	    walkNodes(x.readSlotAt(1), context));
	  context.cfg().map(catchNode, catchNode);

	  WalkContext child = new TryCatchContext(context, catchNode);

	  return Ast.makeNode(CAstNode.TRY,
	    walkNodes(x.readSlotAt(0), child),
	    catchNode);

	// try { } finally { } case
	} else if (x.readSlotAt(1) == null) {

	    return Ast.makeNode(CAstNode.UNWIND,
	      walkNodes(x.readSlotAt(0), context),
	      walkNodes(x.readSlotAt(2),context));
	    
	// try { } catch { } finally { } case
	} else {
	  CAstNode catchNode = Ast.makeNode(CAstNode.CATCH,
	    Ast.makeConstant( x.getCatchId() ),
	    walkNodes(x.readSlotAt(1), context));
	  context.cfg().map(catchNode, catchNode);

	  WalkContext child = new TryCatchContext(context, catchNode);

	  return Ast.makeNode(CAstNode.UNWIND,
	    Ast.makeNode(CAstNode.TRY,
	      walkNodes(x.readSlotAt(0), child),
	      catchNode),
	    walkNodes(x.readSlotAt(2),context));
	}
      }
      
      public Object visitUnaryOp(ASTUnaryOp x, Object param) {
	int op = x.getOperator();
	if (op == ASTConstants.TYPEOF) {
	  return Ast.makeNode(CAstNode.TYPE_OF, 
	    walkNodes(x.readSlotAt(0), context));
	} else if (ASTConstants.isIncDec(op)) {
	  return Ast.makeNode((x.isPostfix()?
			       CAstNode.ASSIGN_POST_OP:
			       CAstNode.ASSIGN_PRE_OP),
	    walkNodes( x.readSlotAt(0), context ),
	    Ast.makeConstant(1),
	    translateOpcode( x.getOperator() ));
	} else if (op == ASTConstants.DELETE) {
		ASTNode target = x.readSlotAt(0);
		if (target instanceof ASTCall) {
			target = target.readSlotAt(0);
		}
		return Ast.makeNode(CAstNode.ASSIGN, walkNodes(target, context), Ast.makeConstant(null));
	} else if (translateOpcode(op) == CAstOperator.OP_ADD) {
	  return walkNodes( x.readSlotAt(0), context );
	} else if (translateOpcode(op) == CAstOperator.OP_SUB) {
	  CAstNode v = walkNodes( x.readSlotAt(0), context );
	  Object c = v.getValue();
	  if (c instanceof Integer) {
	    return Ast.makeConstant(-1 * ((Integer)c).intValue());
	  } else if (c instanceof Double) {
	    return Ast.makeConstant(-1.0 * ((Double)c).doubleValue());
	  } else {
	    return Ast.makeNode(CAstNode.BINARY_EXPR,
	      translateOpcode( x.getOperator() ),
	      Ast.makeConstant(0),
	      v);
	  }
	} else {
	  return Ast.makeNode(CAstNode.UNARY_EXPR,
	    translateOpcode( x.getOperator() ),
	    walkNodes( x.readSlotAt(0), context ));
	}
      }
	    
      public Object visitVariableDecl(ASTVariableDecl x, Object param) {
	Set<CAstNode> results = new HashSet<CAstNode>();
	for(int i = 0; i < x.getEntryCount(); i++) {
	  ASTVariableDecl.Entry v = x.getEntryAt(i);

	  context.addNameDecl( 
	    Ast.makeNode(CAstNode.DECL_STMT,
	      Ast.makeConstant(new CAstSymbolImpl( v.getName(), JSAstTranslator.Any )),
	      makeVarRef("undefined")));

	  if (v.getInitNode() != null)
	    results.add(Ast.makeNode(CAstNode.ASSIGN,
	      Ast.makeNode(CAstNode.VAR, Ast.makeConstant( v.getName() )),
	      walkNodes( v.getInitNode(), context )));
	}
	
	if (! results.isEmpty())
	  return Ast.makeNode(CAstNode.BLOCK_STMT, 
	    results.toArray( new CAstNode[ results.size() ] ));
	else
	  return Ast.makeNode(CAstNode.VAR, Ast.makeConstant(x.getEntryAt(x.getEntryCount()-1).getName()));
      }
      
      public Object visitWhile(ASTWhile x, Object param) {
	ASTNode breakLabel = new ASTLabel( "break" );
	ASTNode contLabel = new ASTLabel( "continue" );
	WalkContext child = new LoopContext(context, breakLabel, contLabel, x.getLabelName());

	return Ast.makeNode(CAstNode.BLOCK_STMT,
	  Ast.makeNode(CAstNode.LOOP,
	    walkNodes( x.readSlotAt(ASTWhile.CONDITION), context ),
	    Ast.makeNode(CAstNode.BLOCK_STMT,
	      walkNodes( x.readSlotAt(ASTWhile.STATEMENT), child ),
	      walkNodes(contLabel, context))),
	  walkNodes(breakLabel, context));
      }

      public Object visitWith(ASTWith x, Object param) {
	Assertions.UNREACHABLE();
      	return null;
      }
    }));
  }
			     
  private CAstNode[] gatherChildren(ASTNode n, WalkContext context) {
    return gatherChildren(n, context, 0);
  }

  private CAstNode[] gatherChildren(ASTNode n, WalkContext context, int start) {
    CAstNode[] result = new CAstNode[ n.getSlotCount()-start ];
    for(int i = start; i < n.getSlotCount(); i++) {
      result[i-start] = walkNodes(n.readSlotAt(i), context);
    }

    return result;
  }

  protected CAstEntity walkEntity(final ASTNode n, WalkContext context) {
    final WalkContext child =
      (n instanceof ASTProgram)?
	new ScriptContext(context, (ASTProgram)n, scriptName):
	new FunctionContext(context, (ASTFunction)n);

    CAstNode[] stmts = gatherChildren(n, child);

    // add initializers, if any
    if (! child.getNameDecls().isEmpty()) {
      CAstNode[] newStmts = new CAstNode[ stmts.length + 1];

      newStmts[0] =
	Ast.makeNode(CAstNode.BLOCK_STMT,
	  child.getNameDecls().toArray( new CAstNode[ child.getNameDecls().size() ] ));

      for(int i = 0; i < stmts.length; i++)
	newStmts[i+1] = stmts[i];

      stmts = newStmts;
    }
	      
    final CAstNode ast = Ast.makeNode(CAstNode.BLOCK_STMT, stmts);
    final CAstControlFlowMap map = child.cfg();
    final CAstSourcePositionMap pos = child.pos();


    final Map<CAstNode, Collection<CAstEntity>> subs = new HashMap<CAstNode, Collection<CAstEntity>>();
    for(Iterator<CAstNode> keys = child.getScopedEntities().keySet().iterator();
	keys.hasNext(); )
    {
      CAstNode key = keys.next();
      subs.put(key, child.getScopedEntities().get(key));
    }

    return new CAstEntity() {
      private final String name;
      private final String[] arguments;

      // constructor of inner class
      {
        if (n instanceof ASTFunction) {
          ASTFunction f = (ASTFunction)n;

	  if (f.getName() == null)
	    name = scriptName + "_anonymous_" + anonymousCounter++;
	  else 
	    name = f.getName();

	  int i = 0;
	  arguments = new String[ f.getParameterCount() + 2 ];
	  arguments[i++] = name;
	  arguments[i++] = "this";
	  for(int j = 0; j < f.getParameterCount(); j++) {
	    arguments[i++] = f.getParameterAt(j).getName();
	  }
	} else {
	  name = scriptName;
	  arguments = new String[]{ "_script" };
	}
      }

      public String toString() {
	return "CAst: " + getName();
      }

      public String getName() {
	return name;
      }
	    
      public String getSignature() {
	Assertions.UNREACHABLE();
	return null;
      }

      public int getKind() {
        if (n instanceof ASTFunction)
          return CAstEntity.FUNCTION_ENTITY;
	else
          return CAstEntity.SCRIPT_ENTITY;
      }

      public String[] getArgumentNames() {
	return arguments;
      }

      public CAstNode[] getArgumentDefaults() {
	return new CAstNode[0];
      }

      public int getArgumentCount() {
        return arguments.length;
      }

      public Map<CAstNode, Collection<CAstEntity>> getAllScopedEntities() {
	return subs;
      }
	      
      public Iterator<CAstEntity> getScopedEntities(CAstNode construct) {
	if (subs.containsKey(construct))
	  return subs.get(construct).iterator();
	else 
	  return EmptyIterator.instance();
      }
	      
      public CAstNode getAST() {
	return ast;
      }

      public CAstControlFlowMap getControlFlow() {
	return map;
      }

      public CAstSourcePositionMap getSourceMap() {
	return pos;
      }

      public CAstSourcePositionMap.Position getPosition() {
	return makePosition(n);
      }

      public CAstNodeTypeMap getNodeTypeMap() {
	return null;
      }

    public Collection<CAstQualifier> getQualifiers() {
	  Assertions.UNREACHABLE("JuliansUnnamedCAstEntity$1.getQualifiers()");
	return null;
    }

    public CAstType getType() {
        return JSAstTranslator.Any;
    }

	public Collection<CAstAnnotation> getAnnotations() {
		// TODO Auto-generated method stub
		return null;
	}

   };
  }

  public CAstEntity translate() 
      throws IOException 
  {
    try {
      FBScript2 P = new FBScript2(new InputStreamReader(sourceModule.getInputStream()));
      if (DEBUG) System.err.println("translating " + scriptName + " with Ibmjs");
      return walkEntity(P.program(), new RootContext());
    } catch (ParseException e) {
      System.err.println("cannot parse " + scriptName + ":" + e);
      return null;
    }
  }

  private final CAst Ast;
  private final String scriptName;
  private final ModuleEntry sourceModule;
  private int anonymousCounter = 0;
  private int baseVarNum = 0;

  public IbmjsToCAstTranslator(CAst Ast, ModuleEntry m, String scriptName) {
    this.Ast = Ast;
    this.scriptName = scriptName;
    this.sourceModule = m;
  }

}
