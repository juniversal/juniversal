package cj.writecpp.astwriters;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;

import cj.ASTUtil;
import cj.writecpp.Context;

public class MethodDeclarationWriter extends ASTWriter {
	public MethodDeclarationWriter(ASTWriters astWriters) {
		super(astWriters);
	}

	@Override
	public void write(ASTNode node, Context context) {
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;

		// TODO: Handle virtual

		if (ASTUtil.containsStatic(methodDeclaration.modifiers()))
			context.write("static ");

		context.skipModifiers(methodDeclaration.modifiers());
		context.skipSpaceAndComments();

		// TODO: Handle arrays with extra dimensions

		if (! methodDeclaration.isConstructor()) {
			Type returnType = methodDeclaration.getReturnType2();
			if (returnType == null)
				context.matchAndWrite("void");
			else getASTWriters().writeNode(returnType, context);

			context.copySpaceAndComments();
		}

		context.matchAndWrite(methodDeclaration.getName().getIdentifier());
		context.copySpaceAndComments();

		context.matchAndWrite("(");
		context.copySpaceAndComments();

		List<?> parameters = methodDeclaration.parameters();

		boolean first = true;
		for (Object object : parameters) {
			SingleVariableDeclaration singleVariableDeclaration = (SingleVariableDeclaration) object;

			if (! first) {
				context.matchAndWrite(",");
				context.copySpaceAndComments();
			}

			getASTWriters().writeNode(singleVariableDeclaration, context);
			context.copySpaceAndComments();

			first = false;
		}

		context.matchAndWrite(")");
		context.copySpaceAndComments();

		// If there are any checked exceptions, output them just as a comment. We don't turn them
		// into C++ checked exceptions because we don't declare runtime exceptions in the C++; since
		// we don't declare all exceptions for C++ we can't declare any since we never want
		// unexpected() to be called
		List<?> thrownExceptions = methodDeclaration.thrownExceptions();
		boolean outputException = false;
		for (Object object : thrownExceptions) {
			Name exceptionName = (Name) object;

			if (! outputException) {
				context.write("/* ");
				context.matchAndWrite("throws");
				context.write(" ");
				context.skipSpaceAndComments();
			}
			else {
				context.matchAndWrite(",");
				context.write(" ");
				context.skipSpaceAndComments();
			}

			context.matchAndWrite(exceptionName.toString());
			context.write(" ");
			context.skipSpaceAndComments();

			outputException = true;
		}
		if (outputException)
			context.write("*/ ");

		if (context.isWritingMethodImplementation())
			getASTWriters().writeNode(methodDeclaration.getBody(), context);
		else context.matchAndWrite(";");
	}
}
