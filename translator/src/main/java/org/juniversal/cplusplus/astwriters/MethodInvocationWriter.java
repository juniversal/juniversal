package org.juniversal.cplusplus.astwriters;

import java.util.List;

import org.juniversal.core.JUniversalException;
import org.juniversal.cplusplus.Context;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;

public class MethodInvocationWriter extends ASTWriter {
	public MethodInvocationWriter(ASTWriters astWriters) {
		super(astWriters);
	}

	@Override
	public void write(ASTNode node, Context context) {
		if (node instanceof SuperMethodInvocation) {
			SuperMethodInvocation superMethodInvocation = (SuperMethodInvocation) node;

			if (superMethodInvocation.getQualifier() != null)
				context.throwSourceNotSupported("Super method invocations with qualifiers before super aren't currently supported");

			writeMethodInvocation(true, null, superMethodInvocation.resolveMethodBinding(),
					superMethodInvocation.getName(), superMethodInvocation.typeArguments(),
					superMethodInvocation.arguments(), context);
		} else if (node instanceof MethodInvocation) {
			MethodInvocation methodInvocation = (MethodInvocation) node;

			writeMethodInvocation(false, methodInvocation.getExpression(), methodInvocation.resolveMethodBinding(),
					methodInvocation.getName(), methodInvocation.typeArguments(), methodInvocation.arguments(), context);
		}
	}

	private void writeMethodInvocation(boolean isSuper, Expression expression, IMethodBinding methodBinding,
			SimpleName name, List<?> typeArguments, List<?> arguments, Context context) {

		// See if the method call is static or not; we need to use the bindings to see that
		boolean isStatic;
		if (methodBinding == null)
			throw new JUniversalException("No binding found for method '" + name.getFullyQualifiedName()
					+ "'; ensure the input source has no compile errors");

		isStatic = Modifier.isStatic(methodBinding.getModifiers());

		if (isSuper) {
			context.matchAndWrite("super");

			context.copySpaceAndComments();
			context.matchAndWrite(".", isStatic ? "::" : "->");

			context.copySpaceAndComments();
		} else if (expression != null) {
			getASTWriters().writeNode(expression, context);

			context.copySpaceAndComments();
			context.matchAndWrite(".", isStatic ? "::" : "->");

			context.copySpaceAndComments();
		}
		// Otherwise the method is invoked on the object itself

		context.matchAndWrite(name.getIdentifier());

		// TODO: Handle type arguments
		if (! typeArguments.isEmpty())
			context.throwSourceNotSupported("Type arguments not currently supported on a method invocation");

		// TODO: Handle different reference operator used for stack objects

		context.copySpaceAndComments();
		context.matchAndWrite("(");

		boolean first = true;
		for (Object object : arguments) {
			Expression argument = (Expression) object;

			if (!first) {
				context.copySpaceAndComments();
				context.matchAndWrite(",");
			}

			context.copySpaceAndComments();
			getASTWriters().writeNode(argument, context);

			first = false;
		}

		context.copySpaceAndComments();
		context.matchAndWrite(")");
	}
}
