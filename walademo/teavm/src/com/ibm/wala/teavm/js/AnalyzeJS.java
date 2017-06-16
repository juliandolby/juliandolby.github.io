package com.ibm.wala.teavm.js;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.teavm.jso.JSBody;
import org.teavm.jso.core.JSString;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLElement;

import com.ibm.wala.cast.ir.ssa.AstIRFactory;
import com.ibm.wala.cast.ir.translator.TranslatorToCAst.Error;
import com.ibm.wala.cast.js.callgraph.fieldbased.OptimisticCallgraphBuilder;
import com.ibm.wala.cast.js.callgraph.fieldbased.flowgraph.FlowGraph;
import com.ibm.wala.cast.js.callgraph.fieldbased.flowgraph.vertices.ObjectVertex;
import com.ibm.wala.cast.js.client.JavaScriptAnalysisEngine;
import com.ibm.wala.cast.js.ipa.callgraph.JSAnalysisOptions;
import com.ibm.wala.cast.js.ipa.callgraph.JSCallGraph;
import com.ibm.wala.cast.js.ipa.callgraph.JavaScriptEntryPoints;
import com.ibm.wala.cast.js.ipa.callgraph.JavaScriptFunctionApplyContextInterpreter;
import com.ibm.wala.cast.js.translator.CAstIbmjsTranslatorFactory;
import com.ibm.wala.cast.js.translator.JavaScriptTranslatorFactory;
import com.ibm.wala.cast.js.types.JavaScriptTypes;
import com.ibm.wala.cast.loader.AstMethod;
import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
import com.ibm.wala.cast.util.SourceBuffer;
import com.ibm.wala.cfg.ControlFlowGraph;
import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.NewSiteReference;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.IAnalysisCacheView;
import com.ibm.wala.ipa.callgraph.impl.Everywhere;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.callgraph.propagation.SSAContextInterpreter;
import com.ibm.wala.ipa.callgraph.propagation.cfa.DelegatingSSAContextInterpreter;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ipa.slicer.NormalStatement;
import com.ibm.wala.ipa.slicer.ParamCaller;
import com.ibm.wala.ipa.slicer.SDG;
import com.ibm.wala.ipa.slicer.Slicer.ControlDependenceOptions;
import com.ibm.wala.ipa.slicer.Slicer.DataDependenceOptions;
import com.ibm.wala.ipa.slicer.Statement;
import com.ibm.wala.ssa.DefUse;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.IRFactory;
import com.ibm.wala.ssa.IRView;
import com.ibm.wala.ssa.ISSABasicBlock;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.teavm.Analysis;
import com.ibm.wala.teavm.ipa.callgraph.TeaVMAnalysisCache;
import com.ibm.wala.teavm.loader.JSStringModule;
import com.ibm.wala.types.FieldReference;
import com.ibm.wala.util.NullProgressMonitor;
import com.ibm.wala.util.collections.EmptyIterator;

import de.iterable.teavm.jso.browser.Console;

public class AnalyzeJS extends Analysis {

	@JSBody(params = { }, script = "return prologue;")
	public static native JSString getPrologue();
	
	private static final class TeaVMContextInterpreter implements SSAContextInterpreter {
		private final IAnalysisCacheView cache;
		
		public TeaVMContextInterpreter(IAnalysisCacheView cache2) {
			this.cache = cache2;
		}
		
		@Override
		public IR getIR(CGNode node) {
			return cache.getIR(node.getMethod(), Everywhere.EVERYWHERE);
		}

		@Override
		  public Iterator<CallSiteReference> iterateCallSites(CGNode N) {
		    IR ir = getIR(N);
		    if (ir == null) {
		      return EmptyIterator.instance();
		    } else {
		      return ir.iterateCallSites();
		    }
		  }

		@Override
		  public int getNumberOfStatements(CGNode node) {
		    IR ir = getIR(node);
		    return (ir == null) ? -1 : ir.getInstructions().length;
		  }

		@Override
		  public boolean recordFactoryType(CGNode node, IClass klass) {
		    return false;
		  }

		@Override
		  public ControlFlowGraph<SSAInstruction, ISSABasicBlock> getCFG(CGNode N) {
		    IR ir = getIR(N);
		    if (ir == null) {
		      return null;
		    } else {
		      return ir.getControlFlowGraph();
		    }
		  }

		@Override
		  public Iterator<NewSiteReference> iterateNewSites(CGNode N) {
		    IR ir = getIR(N);
		    if (ir == null) {
		      return EmptyIterator.instance();
		    } else {
		      return ir.iterateNewSites();
		    }
		  }

		@Override
		public Iterator<FieldReference> iterateFieldsRead(CGNode node) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Iterator<FieldReference> iterateFieldsWritten(CGNode node) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean understands(CGNode node) {
				return true;
		}

		@Override
		public DefUse getDU(CGNode node) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public IRView getIRView(CGNode node) {
			return getIR(node);
		}
	}

	public static void main(String[] args) throws Error, IOException {
		setAnalyzer((JSString fileName, JSString script) -> {
			Console.log(getPrologue());
			
			JSStringModule prologue = new JSStringModule(JSString.valueOf("prologue.js"), getPrologue());
			JSStringModule scriptModule = new JSStringModule(fileName, script);

			JavaScriptTranslatorFactory translatorFactory = new CAstIbmjsTranslatorFactory();
			JavaScriptAnalysisEngine<ObjectVertex> engine = new JavaScriptAnalysisEngine.FieldBasedJavaScriptAnalysisEngine();
			engine.setTranslatorFactory(translatorFactory);
			engine.setModuleFiles(Arrays.asList(prologue, scriptModule));
			
			engine.buildAnalysisScope();
			IClassHierarchy cha = engine.buildClassHierarchy();

			JavaScriptEntryPoints eps = new JavaScriptEntryPoints(cha, cha.getLoader(JavaScriptTypes.jsLoader));

			AnalysisOptions options = engine.getDefaultOptions(eps);
			IRFactory<IMethod> irFactory = AstIRFactory.makeDefaultFactory();
			IAnalysisCacheView cache = new TeaVMAnalysisCache(irFactory, options.getSSAOptions());
			
			OptimisticCallgraphBuilder cgBuilder = new OptimisticCallgraphBuilder(cha, options, cache, true);
			NullProgressMonitor monitor = new NullProgressMonitor();
			FlowGraph flowGraph = cgBuilder.buildFlowGraph(monitor);
			
			SSAContextInterpreter interpreter = new TeaVMContextInterpreter(cache);
		    if (options instanceof JSAnalysisOptions && ((JSAnalysisOptions)options).handleCallApply()) {
		      interpreter = new DelegatingSSAContextInterpreter(new JavaScriptFunctionApplyContextInterpreter(options, cache), interpreter);
		    }
		    final JSCallGraph cg = cgBuilder.extract(interpreter, flowGraph, eps, monitor);
			PointerAnalysis<ObjectVertex> ptrs = flowGraph.getPointerAnalysis(cg, cache, monitor);
			
			SDG<ObjectVertex> sdg = new SDG<ObjectVertex>(cg, ptrs, DataDependenceOptions.NO_BASE_NO_HEAP_NO_EXCEPTIONS, ControlDependenceOptions.NONE);
			
			HTMLDocument document = HTMLDocument.current();
			HTMLElement div = document.createElement("div");
			HTMLElement pre = document.createElement("pre");
			div.appendChild(pre);
			for(List<Statement> path : TaintAnalysis.getPaths(sdg, TaintAnalysis.documentUrlSource, TaintAnalysis.documentWriteSink)) {
				String x = "";
				for(Statement s : path) {
					if (s.getKind() == Statement.Kind.NORMAL) {
						Position p = ((AstMethod)s.getNode().getMethod()).getSourcePosition(((NormalStatement)s).getInstructionIndex());
						x = p.toString() + "\n" + x;
					} else if (s.getKind() == Statement.Kind.PARAM_CALLER) {
						Position p = ((AstMethod)s.getNode().getMethod()).getSourcePosition(((ParamCaller)s).getInstructionIndex());
						x = p.toString() + "\n" + x; 						
					}
				}
				pre.appendChild(document.createTextNode(x));
			}
			document.getBody().appendChild(div);
			
			return JSString.valueOf(cha.toString());
		});
		 
	}
}
