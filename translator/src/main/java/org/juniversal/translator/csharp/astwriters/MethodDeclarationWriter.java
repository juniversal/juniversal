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

package org.juniversal.translator.csharp.astwriters;

import org.eclipse.jdt.core.dom.*;
import org.jetbrains.annotations.Nullable;
import org.juniversal.translator.core.Context;
import org.juniversal.translator.core.JUniversalException;

import java.util.ArrayList;
import java.util.List;

import static org.juniversal.translator.core.ASTUtil.*;

// TODO: Finish this

public class MethodDeclarationWriter extends CSharpASTWriter<MethodDeclaration> {
    public MethodDeclarationWriter(CSharpASTWriters cSharpASTWriters) {
        super(cSharpASTWriters);
    }

    @Override
    public void write(Context context, MethodDeclaration methodDeclaration) {
        TypeDeclaration typeDeclaration = context.getTypeDeclaration();

/*
        boolean isGeneric = !typeParameters.isEmpty();
        if (isGeneric && context.isWritingMethodImplementation()) {
            context.write("template ");
            writeTypeParameters(typeParameters, true, context);
            context.writeln();
        }
*/

        // TODO: Handle arrays with extra dimensions

        // Get return type if present
        @Nullable Type returnType = null;
        if (!methodDeclaration.isConstructor())
            returnType = methodDeclaration.getReturnType2();

        List<?> modifiers = methodDeclaration.modifiers();

        writeAccessModifier(context, modifiers);

        boolean classIsFinal = isFinal(typeDeclaration);
        boolean methodIsFinal = isFinal(methodDeclaration);
        boolean methodIsOverride = isOverride(methodDeclaration);

        // In Java methods are virtual by default whereas in C# they aren't, so add the virtual keyword when appropriate.
        // If the type is final nothing can be overridden whereas if it's an interface everything can be overridden; in
        // both cases there's no need for virtual.   If the method is final or private or static it can't be overridden,
        // so again no need for virtual.   If the method is an override, then it's already virtual (per the ancestor
        // class), so once again there's no need for virtual.   But otherwise, mark as virtual
        if (!classIsFinal && !typeDeclaration.isInterface() &&
            !methodIsFinal && !isPrivate(methodDeclaration) && !isStatic(methodDeclaration) &&
            !methodIsOverride)
            writeModifier(context, "virtual");

        if (methodIsOverride) {
            writeOverrideModifier(context);
            if (methodIsFinal)
                writeSealedModifier(context);
        }

        // Skip any modifiers & type parameters in the source
        context.setPositionToStartOfNode(returnType != null ? returnType : methodDeclaration.getName());
        
        if (returnType != null)
            writeNode(context, returnType);

        context.copySpaceAndComments();
        context.matchAndWrite(methodDeclaration.getName().getIdentifier());

        ArrayList<WildcardType> wildcardTypes = new ArrayList<>();
        for (Object parameterObject : methodDeclaration.parameters()) {
            SingleVariableDeclaration parameter = (SingleVariableDeclaration) parameterObject;
            addWildcardTypes(parameter.getType(), wildcardTypes);
        }

        writeTypeParameters(context, methodDeclaration, wildcardTypes);

        context.copySpaceAndComments();

        context.setMethodWildcardTypes(wildcardTypes);
        writeParameterList(context, methodDeclaration);
        context.setMethodWildcardTypes(null);

        writeTypeConstraints(context, methodDeclaration, wildcardTypes);

        // TODO: Ignore thrown exceptions
        writeThrownExceptions(methodDeclaration, context);

        if (methodDeclaration.isConstructor())
            writeOtherConstructorInvocation(context, methodDeclaration);

        Block body = methodDeclaration.getBody();
        if (body != null) {
            writeBody(context, body);
        } else {
            // TODO: Fix this up
            if (methodDeclaration.getBody() == null) {
                context.write(" = 0");
                context.copySpaceAndComments();
                context.matchAndWrite(";");
            } else {
                context.skipSpaceAndComments();
                context.write(";");
                context.setPositionToEndOfNode(methodDeclaration);
            }
        }
    }

    private void writeTypeParameters(Context context, MethodDeclaration methodDeclaration, ArrayList<WildcardType> wildcardTypes) {
        boolean outputTypeParameter = false;
        for (Object typeParameterObject : methodDeclaration.typeParameters()) {
            TypeParameter typeParameter = (TypeParameter) typeParameterObject;

            if (! outputTypeParameter)
                context.write("<");
            else context.write(", ");

            context.write(typeParameter.getName().getIdentifier());
            outputTypeParameter = true;
        }

        for (WildcardType wildcardType : wildcardTypes) {
            if (! outputTypeParameter)
                context.write("<");
            else context.write(", ");

            int wildcardIndex = wildcardTypes.indexOf(wildcardType) + 1;
            context.write("TWildcard" + wildcardIndex);
            outputTypeParameter = true;
        }

        if (outputTypeParameter)
            context.write(">");
    }

    private void writeTypeConstraints(Context context, MethodDeclaration methodDeclaration, ArrayList<WildcardType> wildcardTypes) {
        writeTypeParameterConstraints(context, methodDeclaration.typeParameters());

        for (WildcardType wildcardType : wildcardTypes) {
            @Nullable Type bound = wildcardType.getBound();
            if (bound == null)
                continue;;

            int index = wildcardTypes.indexOf(wildcardType);
            if (index == -1)
                throw new JUniversalException("Wildcard type not found in wildcard list");

            context.write(" where ");
            context.write("TWildcard" + (index + 1));
            context.write(" : ");

            if (! wildcardType.isUpperBound())
                throw new JUniversalException("Wildcard lower bounds ('? super') aren't supported; only upper bounds ('? extends') are supported");

            writeNodeFromOtherPosition(context, bound);
        }
    }

    private void writeParameterList(Context context, MethodDeclaration methodDeclaration) {
        context.matchAndWrite("(");

        List<?> parameters = methodDeclaration.parameters();

        boolean first = true;
        for (Object parameterObject : parameters) {
            SingleVariableDeclaration singleVariableDeclaration = (SingleVariableDeclaration) parameterObject;

            if (!first) {
                context.copySpaceAndComments();
                context.matchAndWrite(",");
            }

            context.copySpaceAndComments();
            writeNode(context, singleVariableDeclaration);

            first = false;
        }

        context.copySpaceAndComments();
        context.matchAndWrite(")");
    }

    private void writeThrownExceptions(MethodDeclaration methodDeclaration, Context context) {
        // If there are any checked exceptions, output them just as a comment. We don't turn them
        // into C++ checked exceptions because we don't declare runtime exceptions in the C++; since
        // we don't declare all exceptions for C++ we can't declare any since we never want
        // unexpected() to be called
        List<?> thrownExceptions = methodDeclaration.thrownExceptionTypes();
        boolean first;
        if (thrownExceptions.size() > 0) {
            context.copySpaceAndComments();
            context.write("/* ");
            context.matchAndWrite("throws");

            first = true;
            for (Object exceptionTypeObject : thrownExceptions) {
                Type exceptionType = (Type) exceptionTypeObject;

                context.skipSpaceAndComments();
                if (first)
                    context.write(" ");
                else {
                    context.matchAndWrite(",");

                    context.skipSpaceAndComments();
                    context.write(" ");
                }

                writeNode(context, exceptionType);

                first = false;
            }
            context.write(" */");
        }
    }

    @SuppressWarnings("unchecked")
    private void writeOtherConstructorInvocation(Context context, MethodDeclaration methodDeclaration) {
        Block body = methodDeclaration.getBody();

        List<Object> statements = body.statements();
        if (!statements.isEmpty()) {
            Statement firstStatement = (Statement) statements.get(0);

            if (firstStatement instanceof SuperConstructorInvocation || firstStatement instanceof ConstructorInvocation) {

                context.write(" : ");

                int savedPosition = context.getPosition();
                context.setPositionToStartOfNode(firstStatement);

                if (firstStatement instanceof SuperConstructorInvocation) {
                    SuperConstructorInvocation superConstructorInvocation = (SuperConstructorInvocation) firstStatement;

                    context.matchAndWrite("super", "base");

                    context.copySpaceAndComments();
                    writeMethodInvocationArgumentList(context, superConstructorInvocation.typeArguments(),
                            superConstructorInvocation.arguments());
                } else {
                    ConstructorInvocation constructorInvocation = (ConstructorInvocation) firstStatement;

                    context.matchAndWrite("this");

                    context.copySpaceAndComments();
                    writeMethodInvocationArgumentList(context, constructorInvocation.typeArguments(),
                            constructorInvocation.arguments());
                }

                context.setPosition(savedPosition);
            }
        }
    }

    private void writeBody(Context context, Block body) {
        context.copySpaceAndComments();
        context.matchAndWrite("{");

        boolean first = true;
        for (Object statementObject : (List<?>) body.statements()) {
            Statement statement = (Statement) statementObject;

            // If the first statement is a super constructor invocation, we skip it since
            // it's included as part of the method declaration in C++. If a super
            // constructor invocation is a statement other than the first, which it should
            // never be, we let that error out since writeNode won't find a match for it.
            if (first && (statement instanceof SuperConstructorInvocation || statement instanceof ConstructorInvocation))
                context.setPositionToEndOfNodeSpaceAndComments(statement);
            else {
                context.copySpaceAndComments();
                writeNode(context, statement);
            }

            first = false;
        }

        context.copySpaceAndComments();
        context.matchAndWrite("}");
    }
}
