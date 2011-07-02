package cj.writecpp.astwriters;

import org.eclipse.jdt.core.dom.ASTNode;

import cj.writecpp.Context;

public abstract class ASTWriter {
	class Foo {
		Foo(int abc) { }
	}
	
	// Data
	private ASTWriters astWriters;

	public ASTWriter() {
	}

	public ASTWriter(ASTWriters writeCPP) {
		this.astWriters = writeCPP; 
	}

	public ASTWriters getASTWriters() { return this.astWriters; }

	abstract public void write(ASTNode node, Context context);
}
