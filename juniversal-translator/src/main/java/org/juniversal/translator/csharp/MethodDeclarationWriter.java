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

package org.juniversal.translator.csharp;

import org.eclipse.jdt.core.dom.*;
import org.jetbrains.annotations.Nullable;
import org.juniversal.translator.core.JUniversalException;

import java.util.ArrayList;
import java.util.List;

import static org.juniversal.translator.core.ASTUtil.*;

// TODO: Finish this

public class MethodDeclarationWriter extends CSharpASTNodeWriter<MethodDeclaration> {
    public MethodDeclarationWriter(CSharpSourceFileWriter cSharpASTWriters) {
        super(cSharpASTWriters);
    }

    @Override
    public void write(MethodDeclaration methodDeclaration) {
        AbstractTypeDeclaration typeDeclaration = getContext().getTypeDeclaration();
        boolean isInterface = isInterface(typeDeclaration);
        boolean classIsFinal = isFinal(typeDeclaration);
        boolean methodIsAbstract = isAbstract(methodDeclaration);
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

        // Write the access modifier.  For C# interfaces, methods are always public and the access modifier isn't allowed
        if (!isInterface)
            writeAccessModifier(modifiers);

        // Write the virtual/abstract/override/static/sealed modifiers, which, for lack of a better term, we'll call the
        // "overridability" modifiers
        if (methodIsStatic)
            writeStaticModifier();
        else if (isInterface || methodIsConstructor) {
            // C# interface methods can't take any overridability modifiers--they are always implicitly abstract;
            // constructors can't take modifiers
        } else if (methodIsAbstract) {
            // Java methods (as well as C# methods) can be both overrides & abstract; perhaps the method is redeclared
            // here (thus the override) just to update its Javadoc.   Or perhaps a method with a default implementation
            // (non-abstract) higher up in the inheritance tree is forced abstract here, so that subclasses need to
            // supply their own implementations
            if (methodIsOverride)
                writeOverrideModifier();
            writeAbstractModifier();
        } else if (methodIsOverride) {
            writeOverrideModifier();
            if (methodIsFinal)
                writeSealedModifier();
        } else if (!classIsFinal && !methodIsFinal && !isPrivate(methodDeclaration)) {
            // In Java methods are virtual by default whereas in C# they aren't, so add the virtual keyword when
            // appropriate. If the type is final nothing can be overridden.   If the method is final or private it
            // can't be overridden, so again no need for virtual.   Otherwise, mark as virtual
            writeModifier("virtual");
        }

        // Skip any modifiers & type parameters in the source
        setPositionToStartOfNode(returnType != null ? returnType : methodDeclaration.getName());

        if (returnType != null)
            writeNode(returnType);

        copySpaceAndComments();

        // Map overridden Object methods to their appropriate name in C#
        String mappedMethodName;
        if (isThisMethod(methodDeclaration, "equals", "java.lang.Object"))
            mappedMethodName = "Equals";
        else if (isThisMethod(methodDeclaration, "hashCode"))
            mappedMethodName = "GetHashCode";
        else if (isThisMethod(methodDeclaration, "toString"))
            mappedMethodName = "ToString";
        else mappedMethodName = methodDeclaration.getName().getIdentifier();

        matchAndWrite(methodDeclaration.getName().getIdentifier(), mappedMethodName);

        ArrayList<WildcardType> wildcardTypes = new ArrayList<>();
        forEach(methodDeclaration.parameters(), (SingleVariableDeclaration parameter) -> {
            addWildcardTypes(parameter.getType(), wildcardTypes);
        });

        if (methodIsConstructor && !wildcardTypes.isEmpty())
            throw sourceNotSupported("C# constructors can't take arguments that use generic wildcard types; consider replacing this constructor with a static create method instead, taking the same generic arguments");

        writeTypeParameters(methodDeclaration, wildcardTypes);

        copySpaceAndComments();

        getContext().setMethodWildcardTypes(wildcardTypes);
        writeParameterList(methodDeclaration);
        getContext().setMethodWildcardTypes(null);

        // Write the generic type constraints, unless the method is an override in which case C# (for some reason)
        // requires the type constraints to only be specified on the top level method, disallowing them being repeated
        // on overrides
        if (!methodIsOverride)
            writeTypeConstraints(methodDeclaration, wildcardTypes);

        // TODO: Ignore thrown exceptions
        writeThrownExceptions(methodDeclaration);

        if (methodDeclaration.isConstructor())
            writeOtherConstructorInvocation(methodDeclaration);

        Block body = methodDeclaration.getBody();
        if (body != null) {
            writeBody(body);
        } else {
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
            write(" where ");
            writeWildcardTypeSyntheticName(wildcardTypes, wildcardType);
            write(" : ");

            @Nullable Type bound = wildcardType.getBound();
            if (bound == null) {
                write("class ");
            }
            else {
                // TODO: Fix this isSimpleType check; should only include parameterized types
                if (bound.isSimpleType())
                    write("class, ");

                if (!wildcardType.isUpperBound())
                    throw new JUniversalException("Wildcard lower bounds ('? super') aren't supported; only upper bounds ('? extends') are supported");

                writeNodeFromOtherPosition(bound);
            }
        }
    }

    private void writeParameterList(MethodDeclaration methodDeclaration) {
        matchAndWrite("(");

        forEach(methodDeclaration.parameters(), (SingleVariableDeclaration singleVariableDeclaration, boolean first) -> {
            if (!first) {
                copySpaceAndComments();
                matchAndWrite(",");
            }

            copySpaceAndComments();
            writeNode(singleVariableDeclaration);
        });

        copySpaceAndComments();
        matchAndWrite(")");
    }

    private void writeThrownExceptions(MethodDeclaration methodDeclaration) {
        // If there are any checked exceptions, output them just as a comment. We don't turn them
        // into C++ checked exceptions because we don't declare runtime exceptions in the C++; since
        // we don't declare all exceptions for C++ we can't declare any since we never want
        // unexpected() to be called
        List<?> thrownExceptions = methodDeclaration.thrownExceptionTypes();
        if (thrownExceptions.size() > 0) {
            copySpaceAndComments();
            write("/* ");
            matchAndWrite("throws");

            forEach(thrownExceptions, (Type exceptionType, boolean first) -> {
                skipSpaceAndComments();
                if (first)
                    write(" ");
                else {
                    matchAndWrite(",");

                    skipSpaceAndComments();
                    write(" ");
                }

                writeNode(exceptionType);
            });

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

        forEach(body.statements(), (Statement statement, boolean first) -> {
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
        });

        copySpaceAndComments();
        matchAndWrite("}");
    }
}
