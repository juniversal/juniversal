package juniversal.cplusplus.astwriters;

import java.util.List;

import juniversal.cplusplus.Context;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;


public class MethodInvocationWriter extends ASTWriter {
	public MethodInvocationWriter(ASTWriters astWriters) {
		super(astWriters);
	}
	
	@Override
	public void write(ASTNode node, Context context) {
		MethodInvocation methodInvocation = (MethodInvocation) node;
		
		Expression expression = methodInvocation.getExpression();
		if (expression != null) {
			getASTWriters().writeNode(methodInvocation.getExpression(), context);
			context.copySpaceAndComments();
			
			context.matchAndWrite(".", "->");
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
