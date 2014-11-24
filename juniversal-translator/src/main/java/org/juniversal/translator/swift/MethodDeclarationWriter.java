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

package org.juniversal.translator.swift;

import org.eclipse.jdt.core.dom.*;
import org.jetbrains.annotations.Nullable;
import org.juniversal.translator.core.Context;

import java.util.List;


public class MethodDeclarationWriter extends SwiftASTNodeWriter {
    private SwiftSourceFileWriter swiftASTWriters;

    public MethodDeclarationWriter(SwiftSourceFileWriter swiftASTWriters) {
        super(swiftASTWriters);
    }

    @Override
    public void write(ASTNode node) {
        MethodDeclaration methodDeclaration = (MethodDeclaration) node;

        // TODO: Implement this as appropriate
/*
        // If we're writing the implementation of a generic method, include the "template<...>" prefix
        @SuppressWarnings("unchecked")
        List<TypeParameter> typeParameters = (List<TypeParameter>) context.getTypeDeclaration().typeParameters();

        boolean isGeneric = !typeParameters.isEmpty();
        if (isGeneric && context.isWritingMethodImplementation()) {
            write("template ");
            ASTWriterUtil.writeTypeParameters(typeParameters, true, context);
            writeln();
        }
*/

        // TODO: Handle modifiers
        // Write static & virtual modifiers, in the class definition
/*
        if (ASTUtil.containsStatic(methodDeclaration.modifiers()))
            write("static ");
        else {
            boolean isFinal = ASTUtil.containsFinal(typeDeclaration.modifiers())
                    || ASTUtil.containsFinal(methodDeclaration.modifiers());

            if (! isFinal)
                write("virtual ");
        }
*/
        skipModifiers(methodDeclaration.modifiers());
        copySpaceAndComments();

        write("func ");

        // Get return type if present
        @Nullable Type returnType = null;
        if (!methodDeclaration.isConstructor())
            returnType = methodDeclaration.getReturnType2();

        // If void return, set to null
        if (returnType instanceof PrimitiveType && ((PrimitiveType) returnType).getPrimitiveTypeCode() == PrimitiveType.VOID)
            returnType = null;

        SimpleName name = methodDeclaration.getName();
        setPositionToStartOfNode(name);
        matchAndWrite(name.getIdentifier());
        copySpaceAndComments();

        // TODO: Implement this
/*
        if (isGeneric)
            ASTWriterUtil.writeTypeParameters(typeParameters, false, context);
*/

        writeParameterList(methodDeclaration);
        //writeThrownExceptions(methodDeclaration, context);

        if (returnType != null) {
            int originalPosition = getPosition();
            setPositionToStartOfNode(returnType);

            write(" -> ");
            writeType(returnType, false);

            setPosition(originalPosition);
        }

        copySpaceAndComments();

        // TODO: Implement this
        //writeSuperConstructorInvocation(methodDeclaration, context);

        swiftASTWriters.writeNode(methodDeclaration.getBody());
    }

    private void writeParameterList(MethodDeclaration methodDeclaration) {
        matchAndWrite("(");
        copySpaceAndComments();

        List<?> parameters = methodDeclaration.parameters();

        boolean first = true;
        for (Object object : parameters) {
            SingleVariableDeclaration singleVariableDeclaration = (SingleVariableDeclaration) object;

            if (!first) {
                matchAndWrite(",");
                copySpaceAndComments();
            }

            swiftASTWriters.writeNode(singleVariableDeclaration);
            copySpaceAndComments();

            first = false;
        }

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
            for (Object exceptionNameObject : thrownExceptions) {
                Name exceptionName = (Name) exceptionNameObject;

                skipSpaceAndComments();
                if (first)
                    write(" ");
                else {
                    matchAndWrite(",");

                    skipSpaceAndComments();
                    write(" ");
                }

                matchAndWrite(exceptionName.toString());

                first = false;
            }
            write(" */");
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

        List<?> arguments = superConstructorInvocation.arguments();

        boolean first = true;
        for (Expression argument : (List<Expression>) arguments) {
            if (!first) {
                copySpaceAndComments();
                matchAndWrite(",");
            }

            copySpaceAndComments();
            swiftASTWriters.writeNode(argument);

            first = false;
        }

        copySpaceAndComments();
        matchAndWrite(")");

        write(" ");

        setPosition(originalPosition);
    }
}
