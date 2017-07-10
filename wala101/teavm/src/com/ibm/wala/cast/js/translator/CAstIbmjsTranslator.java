package com.ibm.wala.cast.js.translator;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.ibm.wala.cast.ir.translator.TranslatorToCAst;
import com.ibm.wala.cast.tree.CAstEntity;
import com.ibm.wala.cast.tree.impl.CAstImpl;
import com.ibm.wala.cast.tree.rewrite.CAstRewriter.CopyKey;
import com.ibm.wala.cast.tree.rewrite.CAstRewriter.RewriteContext;
import com.ibm.wala.cast.tree.rewrite.CAstRewriterFactory;
import com.ibm.wala.classLoader.ModuleEntry;

public class CAstIbmjsTranslator implements TranslatorToCAst {

	private final CAstImpl Ast = new CAstImpl();

	private final ModuleEntry M;
	  
	private final List<CAstRewriterFactory<?, ?>> rewriters = new LinkedList<CAstRewriterFactory<?,?>>();

	public CAstIbmjsTranslator(ModuleEntry m2) {
		this.M = m2;
		/*
		this.addRewriter(new CAstRewriterFactory<PropertyReadExpander.RewriteContext, ExpanderKey>() {
			public CAstRewriter<PropertyReadExpander.RewriteContext, ExpanderKey> createCAstRewriter(CAst ast) {
				return new PropertyReadExpander(ast);
			}
		}, true);
		*/
	}
	
	public CAstEntity translateToCAst() throws IOException {
	    CAstEntity entity = new IbmjsToCAstTranslator(Ast, M, M.getName()).translate();
	    
	    for(CAstRewriterFactory<?,?> rwf : rewriters) {
	        entity = rwf.createCAstRewriter(Ast).rewrite(entity);
	    }
	    
	    return entity;
	}

	public <C extends RewriteContext<K>, K extends CopyKey<K>> void addRewriter(
			CAstRewriterFactory<C, K> factory, boolean prepend) {
	    if(prepend) {
	        rewriters.add(0, factory);
	    } else {
	        rewriters.add(factory);
	    }
	}

}
