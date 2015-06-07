/*
 * Copyright (c) 2012-2015, Microsoft Mobile
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

package org.juniversal.translator.cplusplus;

import java.util.List;

import org.eclipse.jdt.core.dom.*;
import org.jetbrains.annotations.Nullable;
import org.juniversal.translator.core.JUniversalException;
import org.xuniversal.translator.core.TypeName;

public class MethodInvocationWriter extends CPlusPlusASTNodeWriter<Expression> {
	public MethodInvocationWriter(CPlusPlusTranslator cPlusPlusASTWriters) {
        super(cPlusPlusASTWriters);
    }

    @Override
	public void write(Expression node) {
		if (node instanceof SuperMethodInvocation) {
			SuperMethodInvocation superMethodInvocation = (SuperMethodInvocation) node;

			if (superMethodInvocation.getQualifier() != null)
				throw sourceNotSupported("Super method invocations with qualifiers before super aren't currently supported");

			writeMethodInvocation(true, null, superMethodInvocation.resolveMethodBinding(),
					superMethodInvocation.getName(), superMethodInvocation.typeArguments(),
					superMethodInvocation.arguments());
		} else if (node instanceof MethodInvocation) {
			MethodInvocation methodInvocation = (MethodInvocation) node;

			writeMethodInvocation(false, methodInvocation.getExpression(), methodInvocation.resolveMethodBinding(),
					methodInvocation.getName(), methodInvocation.typeArguments(), methodInvocation.arguments());
		}
	}

	private void writeMethodInvocation(boolean isSuper, Expression expression, IMethodBinding methodBinding,
                                       SimpleName name, List typeArguments, List<?> arguments) {
		// See if the method call is static or not; we need to use the bindings to see that
		boolean isStatic;
		if (methodBinding == null)
			throw new JUniversalException("No binding found for method '" + name.getFullyQualifiedName()
					+ "'; ensure the input source has no compile errors");

		isStatic = Modifier.isStatic(methodBinding.getModifiers());

		if (isSuper) {
			matchAndWrite("super");

			copySpaceAndComments();
			matchAndWrite(".", isStatic ? "::" : "->");

			copySpaceAndComments();
		} else if (expression != null) {
			if (isStatic ) {
				@Nullable ITypeBinding typeBinding = expression.resolveTypeBinding();
				getContext().addReferencedTargetType(getTargetType(typeBinding));
			}

            writeNode(expression);

			copySpaceAndComments();
			matchAndWrite(".", isStatic ? "::" : "->");

			copySpaceAndComments();
		}
		// Otherwise the method is invoked on the object itself


        // In C++ type arguments for methods come after the method name, not before ("foo.<String>bar(3)" in Java is
        // "foo.bar<String>(3)" in C++).   So change the order around here.
        if (typeArguments != null && !typeArguments.isEmpty()) {
            writeNodeAtDifferentPosition(name);

            matchAndWrite("<");
            writeCommaDelimitedNodes(typeArguments);

            copySpaceAndComments();
            matchAndWrite(">");

            setPositionToEndOfNode(name);
        } else matchAndWrite(name.getIdentifier());

		// TODO: Handle different reference operator used for stack objects

		copySpaceAndComments();
		matchAndWrite("(");

		boolean first = true;
		for (Object object : arguments) {
			Expression argument = (Expression) object;

			if (!first) {
				copySpaceAndComments();
				matchAndWrite(",");
			}

			copySpaceAndComments();
            writeNode(argument);

			first = false;
		}

		copySpaceAndComments();
		matchAndWrite(")");
	}
}
