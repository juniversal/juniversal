/*
 * Copyright (c) 2011-2014, Microsoft Mobile
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.juniversal.translator.cplusplus.astwriters;

import java.util.List;

import org.juniversal.translator.core.JUniversalException;
import org.juniversal.translator.core.Context;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;

public class MethodInvocationWriter extends CPlusPlusASTWriter {
    private CPlusPlusASTWriters cPlusPlusASTWriters;

    public MethodInvocationWriter(CPlusPlusASTWriters cPlusPlusASTWriters) {
        super(cPlusPlusASTWriters);
    }

    @Override
	public void write(Context context, ASTNode node) {
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
            writeNode(context, expression);

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
            writeNode(context, argument);

			first = false;
		}

		context.copySpaceAndComments();
		context.matchAndWrite(")");
	}
}
