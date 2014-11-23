/*
 * Copyright (c) 2012-2014, Microsoft Mobile
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
    public void write(MethodDeclaration methodDeclaration) {
        TypeDeclaration typeDeclaration = getContext().getTypeDeclaration();
        boolean isInterface = typeDeclaration.isInterface();
        boolean classIsFinal = isFinal(typeDeclaration);
        boolean methodIsFinal = isFinal(methodDeclaration);
        boolean methodIsOverride = isOverride(methodDeclaration);
        boolean methodIsStatic = isStatic(methodDeclaration);
        boolean methodIsConstructor = isConstructor(methodDeclaration);

/*
        boolean isGeneric = !typeParameters.isEmpty();
        if (isGeneric && context.isWritingMethodImplementation()) {
            write("template ");
            writeTypeParameters(typeParameters, true, context);
            writeln();
        }
*/

        // TODO: Handle arrays with extra dimensions

        // Get return type if present
        @Nullable Type returnType = null;
        if (!methodDeclaration.isConstructor())
            returnType = methodDeclaration.getReturnType2();

        List<?> modifiers = methodDeclaration.modifiers();

        // For C# interfaces, methods are always public and the access modifier isn't allowed
        if (! isInterface)
            writeAccessModifier(modifiers);

        // In Java methods are virtual by default whereas in C# they aren't, so add the virtual keyword when appropriate.
        // If the type is final nothing can be overridden whereas if it's an interface everything can be overridden; in
        // both cases there's no need for virtual.   If the method is final or private or static it can't be overridden,
        // so again no need for virtual.   If the method is an override, then it's already virtual (per the ancestor
        // class), so once again there's no need for virtual.   But otherwise, mark as virtual
        if ( !classIsFinal && !isInterface && !methodIsFinal && !methodIsOverride && !methodIsConstructor &&
                !isPrivate(methodDeclaration) && !methodIsStatic)
            writeModifier("virtual");

        if (methodIsOverride) {
            writeOverrideModifier();
            if (methodIsFinal)
                writeSealedModifier();
        }

        if (methodIsStatic)
            writeStaticModifier();

        // Skip any modifiers & type parameters in the source
        setPositionToStartOfNode(returnType != null ? returnType : methodDeclaration.getName());

        if (returnType != null)
            writeNode(returnType);

        copySpaceAndComments();

        // Map overridden Object methods to their appropriate name in C#
        String mappedMethodName;
        if (isMethod(methodDeclaration, "equals", "java.lang.Object"))
            mappedMethodName = "Equals";
        else if (isMethod(methodDeclaration, "hashCode"))
            mappedMethodName = "GetHashCode";
        else if (isMethod(methodDeclaration, "toString"))
            mappedMethodName = "ToString";
        else mappedMethodName = methodDeclaration.getName().getIdentifier();

        matchAndWrite(methodDeclaration.getName().getIdentifier(), mappedMethodName);

        ArrayList<WildcardType> wildcardTypes = new ArrayList<>();
        for (Object parameterObject : methodDeclaration.parameters()) {
            SingleVariableDeclaration parameter = (SingleVariableDeclaration) parameterObject;
            addWildcardTypes(parameter.getType(), wildcardTypes);
        }

        writeTypeParameters(methodDeclaration, wildcardTypes);

        copySpaceAndComments();

        getContext().setMethodWildcardTypes(wildcardTypes);
        writeParameterList(methodDeclaration);
        getContext().setMethodWildcardTypes(null);

        writeTypeConstraints(methodDeclaration, wildcardTypes);

        // TODO: Ignore thrown exceptions
        writeThrownExceptions(methodDeclaration);
        
        if (methodDeclaration.isConstructor())
            writeOtherConstructorInvocation(methodDeclaration);

        Block body = methodDeclaration.getBody();
        if (body != null) {
            writeBody(body);
        } else {
            if (! isInterface)
                write(" = 0");
            copySpaceAndComments();
            matchAndWrite(";");
        }
    }

    private void writeTypeParameters(MethodDeclaration methodDeclaration, ArrayList<WildcardType> wildcardTypes) {
        boolean outputTypeParameter = false;
        for (Object typeParameterObject : methodDeclaration.typeParameters()) {
            TypeParameter typeParameter = (TypeParameter) typeParameterObject;

            if (!outputTypeParameter)
                write("<");
            else write(", ");

            write(typeParameter.getName().getIdentifier());
            outputTypeParameter = true;
        }

        for (WildcardType wildcardType : wildcardTypes) {
            if (!outputTypeParameter)
                write("<");
            else write(", ");

            writeWildcardTypeSyntheticName(wildcardTypes, wildcardType);
            outputTypeParameter = true;
        }

        if (outputTypeParameter)
            write(">");
    }

    private void writeTypeConstraints(MethodDeclaration methodDeclaration, ArrayList<WildcardType> wildcardTypes) {
        writeTypeParameterConstraints(methodDeclaration.typeParameters());

        for (WildcardType wildcardType : wildcardTypes) {
            @Nullable Type bound = wildcardType.getBound();
            if (bound == null)
                continue;

            write(" where ");
            writeWildcardTypeSyntheticName(wildcardTypes, wildcardType);
            write(" : ");

            if (!wildcardType.isUpperBound())
                throw new JUniversalException("Wildcard lower bounds ('? super') aren't supported; only upper bounds ('? extends') are supported");

            writeNodeFromOtherPosition(bound);
        }
    }

    private void writeParameterList(MethodDeclaration methodDeclaration) {
        matchAndWrite("(");

        List<?> parameters = methodDeclaration.parameters();

        boolean first = true;
        for (Object parameterObject : parameters) {
            SingleVariableDeclaration singleVariableDeclaration = (SingleVariableDeclaration) parameterObject;

            if (!first) {
                copySpaceAndComments();
                matchAndWrite(",");
            }

            copySpaceAndComments();
            writeNode(singleVariableDeclaration);

            first = false;
        }

        copySpaceAndComments();
        matchAndWrite(")");
    }

    private void writeThrownExceptions(MethodDeclaration methodDeclaration) {
        // If there are any checked exceptions, output them just as a comment. We don't turn them
        // into C++ checked exceptions because we don't declare runtime exceptions in the C++; since
        // we don't declare all exceptions for C++ we can't declare any since we never want
        // unexpected() to be called
        List<?> thrownExceptions = methodDeclaration.thrownExceptionTypes();
        boolean first;
        if (thrownExceptions.size() > 0) {
            copySpaceAndComments();
            write("/* ");
            matchAndWrite("throws");

            first = true;
            for (Object exceptionTypeObject : thrownExceptions) {
                Type exceptionType = (Type) exceptionTypeObject;

                skipSpaceAndComments();
                if (first)
                    write(" ");
                else {
                    matchAndWrite(",");

                    skipSpaceAndComments();
                    write(" ");
                }

                writeNode(exceptionType);

                first = false;
            }
            write(" */");
        }
    }

    @SuppressWarnings("unchecked")
    private void writeOtherConstructorInvocation(MethodDeclaration methodDeclaration) {
        Block body = methodDeclaration.getBody();

        List<Object> statements = body.statements();
        if (!statements.isEmpty()) {
            Statement firstStatement = (Statement) statements.get(0);

            if (firstStatement instanceof SuperConstructorInvocation || firstStatement instanceof ConstructorInvocation) {
                write(" : ");

                int savedPosition = getPosition();
                setPositionToStartOfNode(firstStatement);

                if (firstStatement instanceof SuperConstructorInvocation) {
                    SuperConstructorInvocation superConstructorInvocation = (SuperConstructorInvocation) firstStatement;

                    matchAndWrite("super", "base");

                    copySpaceAndComments();
                    writeMethodInvocationArgumentList(superConstructorInvocation.typeArguments(),
                            superConstructorInvocation.arguments());
                } else {
                    ConstructorInvocation constructorInvocation = (ConstructorInvocation) firstStatement;

                    matchAndWrite("this");

                    copySpaceAndComments();
                    writeMethodInvocationArgumentList(constructorInvocation.typeArguments(),
                            constructorInvocation.arguments());
                }

                setPosition(savedPosition);
            }
        }
    }

    private void writeBody(Block body) {
        copySpaceAndComments();
        matchAndWrite("{");

        boolean first = true;
        for (Object statementObject : (List<?>) body.statements()) {
            Statement statement = (Statement) statementObject;

            // If the first statement is a super constructor invocation, we skip it since
            // it's included as part of the method declaration in C++. If a super
            // constructor invocation is a statement other than the first, which it should
            // never be, we let that error out since writeNode won't find a match for it.
            if (first && (statement instanceof SuperConstructorInvocation || statement instanceof ConstructorInvocation))
                setPositionToEndOfNodeSpaceAndComments(statement);
            else {
                copySpaceAndComments();
                writeNode(statement);
            }

            first = false;
        }

        copySpaceAndComments();
        matchAndWrite("}");
    }
}
