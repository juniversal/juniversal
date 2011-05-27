package cj.writecpp;

import org.eclipse.jdt.core.dom.ASTNode;

public abstract class WriteCPPVisitor {

	abstract public void write(ASTNode node, WriteCPPContext context);
}
