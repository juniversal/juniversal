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

import org.eclipse.jdt.core.dom.*;
import org.jetbrains.annotations.Nullable;
import org.xuniversal.translator.cplusplus.ReferenceKind;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.juniversal.translator.core.ASTUtil.*;


public class MethodDeclarationWriter extends CPlusPlusASTNodeWriter<MethodDeclaration> {
    public MethodDeclarationWriter(CPlusPlusTranslator cPlusPlusFileTranslator) {
        super(cPlusPlusFileTranslator);
    }

    @Override
    public void write(MethodDeclaration methodDeclaration) {
        AbstractTypeDeclaration typeDeclaration = getContext().getTypeDeclaration();
        boolean classIsFinal = isFinal(typeDeclaration);
        boolean methodIsAbstract = isAbstract(methodDeclaration);
        boolean methodIsFinal = isFinal(methodDeclaration);
        boolean methodIsStatic = isStatic(methodDeclaration);
        boolean methodIsConstructor = isConstructor(methodDeclaration);

        // Get return type if present
        @Nullable Type returnType = null;
        if (!methodIsConstructor)
            returnType = methodDeclaration.getReturnType2();

        List classTypeParameters = new ArrayList<>();
        if (typeDeclaration instanceof TypeDeclaration) {
            classTypeParameters = ((TypeDeclaration) typeDeclaration).typeParameters();
        }

        List methodTypeParameters = methodDeclaration.typeParameters();

        ArrayList<WildcardType> wildcardTypes = new ArrayList<>();
        forEach(methodDeclaration.parameters(), (SingleVariableDeclaration parameter) -> {
            addWildcardTypes(parameter.getType(), wildcardTypes);
        });

        @Nullable AbstractTypeDeclaration parentTypeDeclaration = null;
        ASTNode parent = getContext().getTypeDeclaration().getParent();
        if (parent != null && parent instanceof AbstractTypeDeclaration)
            parentTypeDeclaration = (AbstractTypeDeclaration) parent;

        boolean classIsGeneric = ! classTypeParameters.isEmpty();
        boolean methodIsGeneric = ! methodDeclaration.typeParameters().isEmpty() || ! wildcardTypes.isEmpty();

        if (!getContext().isWritingMethodImplementation()) {
            // If we're writing the declaration of a generic method (with type parameters for the method itself,
            // regardless of where the class in generic), include the "template<...>" prefix
            if (methodIsGeneric) {
                write("template ");
                writeTypeParameters(methodDeclaration.typeParameters(), wildcardTypes, "typename ", null);
                write("  ");
            }
        }
        else {
            if (parentTypeDeclaration != null && parentTypeDeclaration instanceof TypeDeclaration) {
                List typeParameters = ((TypeDeclaration) parentTypeDeclaration).typeParameters();
                if (! typeParameters.isEmpty()) {
                    write("template ");
                    writeTypeParameters(typeParameters, "typename ", "Unused");
                    writeln();
                }
            }

            if (classIsGeneric || methodIsGeneric) {
                write("template ");

                ArrayList<TypeParameter> consolidatedTypeParameters = new ArrayList<>();
                forEach(classTypeParameters, (Consumer<TypeParameter>) consolidatedTypeParameters::add);
                forEach(methodTypeParameters, (Consumer<TypeParameter>) consolidatedTypeParameters::add);

                writeTypeParameters(consolidatedTypeParameters, wildcardTypes, "typename ", null);
                writeln();
            }
        }

        // Write static & virtual modifiers, in the class definition
        if (!getContext().isWritingMethodImplementation()) {
            if (methodIsStatic)
                write("static ");
            else if (methodIsConstructor) {
                // Constructors can't take modifiers
            }
            else if (!classIsFinal && !methodIsFinal && !isPrivate(methodDeclaration)) {
                // In Java methods are virtual by default whereas in C# they aren't, so add the virtual keyword when
                // appropriate. If the type is final nothing can be overridden.   If the method is final or private it
                // can't be overridden, so again no need for virtual.   Otherwise, mark as virtual
                write("virtual ");
            }
        }

        // Skip any modifiers & type parameters in the source
        setPositionToStartOfNode(returnType != null ? returnType : methodDeclaration.getName());

        if (returnType != null)
            writeTypeReference(returnType, ReferenceKind.SharedPtr);

        copySpaceAndComments();

        // TODO: Handle arrays with extra dimensions

        if (getContext().isWritingMethodImplementation()) {
            if (parentTypeDeclaration != null) {
                writeTypeDeclarationType(parentTypeDeclaration, "Unused");
                write("::");
            }

            writeTypeDeclarationType(getContext().getTypeDeclaration(), null);
            write("::");
        }
        matchAndWrite(methodDeclaration.getName().getIdentifier());
        copySpaceAndComments();

        getContext().setMethodWildcardTypes(wildcardTypes);
        writeParameterList(methodDeclaration);
        getContext().setMethodWildcardTypes(null);

        writeThrownExceptions(methodDeclaration);

        if (getContext().isWritingMethodImplementation()) {
            writeAlternateConstructorInvocation(methodDeclaration);

            copySpaceAndComments();
            writeNode(methodDeclaration.getBody());
        } else {
            if (methodIsAbstract) {
                write(" = 0");
                copySpaceAndComments();
                matchAndWrite(";");
            } else {
                skipSpaceAndComments();
                write(";");
                setPositionToEndOfNode(methodDeclaration);
            }
        }
    }

    private void writeParameterList(MethodDeclaration methodDeclaration) {
        int additionalIndent = getTargetColumn() - getSourceLogicalColumn();
        int previousAdditionalIndent = getTargetWriter().setAdditionalIndentation(additionalIndent);

        matchAndWrite("(");

        copySpaceAndComments();
        writeCommaDelimitedNodes(methodDeclaration.parameters());

        copySpaceAndComments();
        matchAndWrite(")");

        getTargetWriter().setAdditionalIndentation(previousAdditionalIndent);
    }

    private void writeThrownExceptions(MethodDeclaration methodDeclaration) {
        // If there are any checked exceptions, output them just as a comment. We don't turn them
        // into C++ checked exceptions because we don't declare runtime exceptions in the C++; since
        // we don't declare all exceptions for C++ we can't declare any since we never want
        // unexpected() to be called
        List<?> thrownExceptionTypes = methodDeclaration.thrownExceptionTypes();
        boolean first;
        if (thrownExceptionTypes.size() > 0) {
            copySpaceAndComments();

            write("/* ");
            matchAndWrite("throws");

            first = true;
            for (Object exceptionTypeObject : thrownExceptionTypes) {
                Type exceptionType = (Type) exceptionTypeObject;

                skipSpaceAndComments();
                if (first)
                    write(" ");
                else {
                    matchAndWrite(",");

                    skipSpaceAndComments();
                    write(" ");
                }

                matchAndWrite(exceptionType.toString());

                first = false;
            }
            write(" */");
        }
    }

    @SuppressWarnings("unchecked")
    private void writeAlternateConstructorInvocation(MethodDeclaration methodDeclaration) {
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

        int originalPosition = getPosition();
        setPositionToStartOfNode(superConstructorInvocation);

        // TODO: Support <expression>.super
        if (superConstructorInvocation.getExpression() != null)
            throw sourceNotSupported("<expression>.super constructor invocation syntax not currently supported");

        // TODO: Support type arguments here
        if (!superConstructorInvocation.typeArguments().isEmpty())
            throw sourceNotSupported("super constructor invocation with type arguments not currently supported");

        write(" : ");
        matchAndWrite("super");
        copySpaceAndComments();
        matchAndWrite("(");

        forEach(superConstructorInvocation.arguments(), (Expression argument, boolean first) -> {
            if (!first) {
                copySpaceAndComments();
                matchAndWrite(",");
            }

            copySpaceAndComments();
            writeNode(argument);
        });

        copySpaceAndComments();
        matchAndWrite(")");

        write(" ");

        setPosition(originalPosition);
    }
}
