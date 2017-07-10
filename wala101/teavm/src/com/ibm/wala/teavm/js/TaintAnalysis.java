package com.ibm.wala.teavm.js;

import java.util.List;
import java.util.Set;

import com.ibm.wala.ipa.slicer.NormalStatement;
import com.ibm.wala.ipa.slicer.ParamCallee;
import com.ibm.wala.ipa.slicer.Statement;
import com.ibm.wala.ipa.slicer.Statement.Kind;
import com.ibm.wala.ssa.SSAGetInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.util.Predicate;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.collections.NonNullSingletonIterator;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.graph.traverse.BFSPathFinder;

public class TaintAnalysis {

	@FunctionalInterface
	interface EndpointFinder<T> {
		
		boolean endpoint(T s);
		
	}
	
	public static EndpointFinder<Statement> documentWriteSink = (Statement s) -> {
		if (s.getKind()==Kind.PARAM_CALLEE) {
			String ref = ((ParamCallee)s).getNode().getMethod().toString();
			if (ref.contains("Document_prototype_write")) {
				System.err.println(s);
				return true;
			}
		}

		return false;
	};

	public static EndpointFinder<Statement> documentUrlSource = (Statement s) -> {
		if (s.getKind()==Kind.NORMAL) {
			NormalStatement ns = (NormalStatement) s;
			SSAInstruction inst = ns.getInstruction();
			if (inst instanceof SSAGetInstruction) {
				if (((SSAGetInstruction)inst).getDeclaredField().getName().toString().equals("URL")) {
					System.err.println(s);
					return true;
				}
			}
		}

		return false;
	};
	
	public static <T> Set<List<T>> getPaths(Graph<T> G, EndpointFinder<T> sources, EndpointFinder<T> sinks) {
		Set<List<T>> result = HashSetFactory.make();
		for(T src : G) {
			if (sources.endpoint(src)) {
				for(final T dst : G) {
					if (sinks.endpoint(dst)) {
						BFSPathFinder<T> paths = 
								new BFSPathFinder<T>(G, new NonNullSingletonIterator<T>(src), new Predicate<T>() {
									@Override
									public boolean test(T t) {
										return t.equals(dst);
									}
								});
						List<T> path;
						if ((path = paths.find()) != null) {
							System.err.println(path);
							result.add(path);
						}
					}
				}
			}
		}
		return result;
	}
}
