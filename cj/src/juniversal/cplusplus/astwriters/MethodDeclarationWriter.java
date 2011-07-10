package juniversal.cplusplus.astwriters;

import java.util.List;

import juniversal.ASTUtil;
import juniversal.cplusplus.Context;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;


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
			else getASTWriters().writeType(returnType, context, false);

			context.copySpaceAndComments();
		}

		if (context.isWritingMethodImplementation())
			context.write(context.getTypeDeclarationName() + "::");
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

		// If there are any checked exceptions, output them just as a comment. We don't turn them
		// into C++ checked exceptions because we don't declare runtime exceptions in the C++; since
		// we don't declare all exceptions for C++ we can't declare any since we never want
		// unexpected() to be called
		List<?> thrownExceptions = methodDeclaration.thrownExceptions();
		if (thrownExceptions.size() > 0) {

			context.copySpaceAndComments();

			context.write("/* ");
			context.matchAndWrite("throws");

			first = true;
			for (Object exceptionNameObject : thrownExceptions) {
				Name exceptionName = (Name) exceptionNameObject;

				context.skipSpaceAndComments();
				if (first)
					context.write(" ");
				else {
					context.matchAndWrite(",");

					context.skipSpaceAndComments();
					context.write(" ");
				}
	
				context.matchAndWrite(exceptionName.toString());
	
				first = false;
			}
			context.write(" */");
		}

		if (context.isWritingMethodImplementation()) {
			context.copySpaceAndComments();
			getASTWriters().writeNode(methodDeclaration.getBody(), context);
		}
		else {
			context.skipSpaceAndComments();
			context.write(";");
			context.setPosition(ASTUtil.getEndPosition(methodDeclaration));
		}
	}
}
