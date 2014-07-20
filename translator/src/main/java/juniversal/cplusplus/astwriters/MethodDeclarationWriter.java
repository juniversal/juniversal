package juniversal.cplusplus.astwriters;

import java.util.List;

import juniversal.ASTUtil;
import juniversal.cplusplus.Context;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeParameter;


public class MethodDeclarationWriter extends ASTWriter {
	public MethodDeclarationWriter(ASTWriters astWriters) {
		super(astWriters);
	}

	@Override
	public void write(ASTNode node, Context context) {
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;

		TypeDeclaration typeDeclaration = context.getTypeDeclaration();

		// If we're writing the implementation of a generic method, include the "template<...>" prefix
		@SuppressWarnings("unchecked")
		List<TypeParameter> typeParameters = (List<TypeParameter>) context.getTypeDeclaration().typeParameters();

		boolean isGeneric = !typeParameters.isEmpty();
		if (isGeneric && context.isWritingMethodImplementation()) {
			context.write("template ");
			ASTWriterUtil.writeTypeParameters(typeParameters, true, context);
			context.writeln();
		}

		// Writer static & virtual modifiers, in the class definition
		if (! context.isWritingMethodImplementation()) {
			if (ASTUtil.containsStatic(methodDeclaration.modifiers()))
				context.write("static ");
			else {
				boolean isFinal = ASTUtil.containsFinal(typeDeclaration.modifiers())
						|| ASTUtil.containsFinal(methodDeclaration.modifiers());
	
				if (! isFinal)
					context.write("virtual ");
			}
		}

		context.skipModifiers(methodDeclaration.modifiers());
		context.skipSpaceAndComments();

		// TODO: Handle arrays with extra dimensions

		// Write return type if present
		if (! methodDeclaration.isConstructor()) {
			Type returnType = methodDeclaration.getReturnType2();
			if (returnType == null)
				context.matchAndWrite("void");
			else getASTWriters().writeType(returnType, context, false);

			context.copySpaceAndComments();
		}

		if (context.isWritingMethodImplementation()) {
			context.write(context.getTypeDeclaration().getName().getIdentifier());

			if (isGeneric)
				ASTWriterUtil.writeTypeParameters(typeParameters, false, context);

			context.write("::");
		}
		context.matchAndWrite(methodDeclaration.getName().getIdentifier());
		context.copySpaceAndComments();

		writeParameterList(methodDeclaration, context);
		writeThrownExceptions(methodDeclaration, context);

		if (context.isWritingMethodImplementation())
			writeSuperConstructorInvocation(methodDeclaration, context);

		if (context.isWritingMethodImplementation()) {
			context.copySpaceAndComments();
			getASTWriters().writeNode(methodDeclaration.getBody(), context);
		}
		else {
			if (methodDeclaration.getBody() == null) {
				context.write(" = 0");
				context.copySpaceAndComments();
				context.matchAndWrite(";");
			}
			else {
				context.skipSpaceAndComments();
				context.write(";");
				context.setPositionToEndOfNode(methodDeclaration);
			}
		}
	}

	private void writeParameterList(MethodDeclaration methodDeclaration, Context context) {
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
	}

	private void writeThrownExceptions(MethodDeclaration methodDeclaration, Context context) {
		// If there are any checked exceptions, output them just as a comment. We don't turn them
		// into C++ checked exceptions because we don't declare runtime exceptions in the C++; since
		// we don't declare all exceptions for C++ we can't declare any since we never want
		// unexpected() to be called
		List<?> thrownExceptions = methodDeclaration.thrownExceptions();
		boolean first;
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
	}

	@SuppressWarnings("unchecked")
	private void writeSuperConstructorInvocation(MethodDeclaration methodDeclaration, Context context) {
		Block body = methodDeclaration.getBody();
		if (body == null)
			return;

		SuperConstructorInvocation superConstructorInvocation = null;
		for (Statement statement : (List<Statement>) body.statements()) {
			if (statement instanceof SuperConstructorInvocation)
				superConstructorInvocation = (SuperConstructorInvocation) statement;
			break;
		}

		if (superConstructorInvocation == null)
			return;

		int originalPosition = context.getPosition();
		context.setPositionToStartOfNode(superConstructorInvocation);

		// TODO: Support <expression>.super
		if (superConstructorInvocation.getExpression() != null)
			context.throwSourceNotSupported("<expression>.super constructor invocation syntax not currently supported");
		
		// TODO: Support type arguments here
		if (! superConstructorInvocation.typeArguments().isEmpty())
			context.throwSourceNotSupported("super constructor invocation with type arguments not currently supported");

		context.write(" : ");
		context.matchAndWrite("super");
		context.copySpaceAndComments();
		context.matchAndWrite("(");

		List<?> arguments = superConstructorInvocation.arguments();
	
		boolean first = true;
		for (Expression argument : (List<Expression>) arguments) {
			if (! first) {
				context.copySpaceAndComments();
				context.matchAndWrite(",");
			}

			context.copySpaceAndComments();
			getASTWriters().writeNode(argument, context);
	
			first = false;
		}

		context.copySpaceAndComments();
		context.matchAndWrite(")");

		context.write(" ");

		context.setPosition(originalPosition);
	}
}
