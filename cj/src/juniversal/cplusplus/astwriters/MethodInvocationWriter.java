package juniversal.cplusplus.astwriters;

import java.util.List;

import juniversal.cplusplus.Context;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.util.IMethodInfo;


public class MethodInvocationWriter extends ASTWriter {
	public MethodInvocationWriter(ASTWriters astWriters) {
		super(astWriters);
	}
	
	@Override
	public void write(ASTNode node, Context context) {
		MethodInvocation methodInvocation = (MethodInvocation) node;

		// See if the method call is static or not; we need to use the bindings to see that
		boolean isStatic;
		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
		isStatic = Modifier.isStatic(methodBinding.getModifiers());

		Expression expression = methodInvocation.getExpression();
		if (expression != null) {
			getASTWriters().writeNode(methodInvocation.getExpression(), context);
			context.copySpaceAndComments();

			context.matchAndWrite(".", isStatic ? "::" : "->");
			context.copySpaceAndComments();
		}

		context.matchAndWrite(methodInvocation.getName().getIdentifier());
		context.copySpaceAndComments();

		//TODO: Handle type arguments
		//TODO: Handle different reference operator used for stack objects

		context.matchAndWrite("(");
		context.copySpaceAndComments();

		List<?> arguments = methodInvocation.arguments();

		boolean first = true;
		for (Object object : arguments) {
			Expression argument = (Expression) object;

			if (! first) {
				context.matchAndWrite(",");
				context.copySpaceAndComments();
			}

			getASTWriters().writeNode(argument, context);
			context.copySpaceAndComments();

			first = false;
		}

		context.matchAndWrite(")");
	}
}
