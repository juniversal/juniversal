package org.juniversal.translator.swift.astwriters;

import org.eclipse.jdt.core.dom.*;
import org.jetbrains.annotations.Nullable;
import org.juniversal.translator.core.Context;

import java.util.List;


public class MethodDeclarationWriter extends ASTWriter {
    public MethodDeclarationWriter(ASTWriters astWriters) {
        super(astWriters);
    }

    @Override
    public void write(ASTNode node, Context context) {
        MethodDeclaration methodDeclaration = (MethodDeclaration) node;

        // TODO: Implement this as appropriate
/*
        // If we're writing the implementation of a generic method, include the "template<...>" prefix
        @SuppressWarnings("unchecked")
        List<TypeParameter> typeParameters = (List<TypeParameter>) context.getTypeDeclaration().typeParameters();

        boolean isGeneric = !typeParameters.isEmpty();
        if (isGeneric && context.isWritingMethodImplementation()) {
            context.write("template ");
            ASTWriterUtil.writeTypeParameters(typeParameters, true, context);
            context.writeln();
        }
*/

        // TODO: Handle modifiers
        // Write static & virtual modifiers, in the class definition
/*
        if (ASTUtil.containsStatic(methodDeclaration.modifiers()))
            context.write("static ");
        else {
            boolean isFinal = ASTUtil.containsFinal(typeDeclaration.modifiers())
                    || ASTUtil.containsFinal(methodDeclaration.modifiers());

            if (! isFinal)
                context.write("virtual ");
        }
*/
        context.skipModifiers(methodDeclaration.modifiers());
        context.copySpaceAndComments();

        context.write("func ");

        // Get return type if present
        @Nullable Type returnType = null;
        if (!methodDeclaration.isConstructor())
            returnType = methodDeclaration.getReturnType2();

        // If void return, set to null
        if (returnType instanceof PrimitiveType && ((PrimitiveType) returnType).getPrimitiveTypeCode() == PrimitiveType.VOID)
            returnType = null;

        SimpleName name = methodDeclaration.getName();
        context.setPositionToStartOfNode(name);
        context.matchAndWrite(name.getIdentifier());
        context.copySpaceAndComments();

        // TODO: Implement this
/*
        if (isGeneric)
            ASTWriterUtil.writeTypeParameters(typeParameters, false, context);
*/

        writeParameterList(methodDeclaration, context);
        //writeThrownExceptions(methodDeclaration, context);

        if (returnType != null) {
            int originalPosition = context.getPosition();
            context.setPositionToStartOfNode(returnType);

            context.write(" -> ");
            getASTWriters().writeType(returnType, context, false);

            context.setPosition(originalPosition);
        }

        context.copySpaceAndComments();

        // TODO: Implement this
        //writeSuperConstructorInvocation(methodDeclaration, context);

        getASTWriters().writeNode(methodDeclaration.getBody(), context);
    }

    private void writeParameterList(MethodDeclaration methodDeclaration, Context context) {
        context.matchAndWrite("(");
        context.copySpaceAndComments();

        List<?> parameters = methodDeclaration.parameters();

        boolean first = true;
        for (Object object : parameters) {
            SingleVariableDeclaration singleVariableDeclaration = (SingleVariableDeclaration) object;

            if (!first) {
                context.matchAndWrite(",");
                context.copySpaceAndComments();
            }

            getASTWriters().writeNode(singleVariableDeclaration, context);
            context.copySpaceAndComments();

            first = false;
        }

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
            for (Object exceptionNameObject : thrownExceptions) {
                Name exceptionName = (Name) exceptionNameObject;

                context.skipSpaceAndComments();
                if (first)
                    context.write(" ");
                else {
                    context.matchAndWrite(",");

                    context.skipSpaceAndComments();
                    context.write(" ");
                }

                context.matchAndWrite(exceptionName.toString());

                first = false;
            }
            context.write(" */");
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

        int originalPosition = context.getPosition();
        context.setPositionToStartOfNode(superConstructorInvocation);

        // TODO: Support <expression>.super
        if (superConstructorInvocation.getExpression() != null)
            context.throwSourceNotSupported("<expression>.super constructor invocation syntax not currently supported");

        // TODO: Support type arguments here
        if (!superConstructorInvocation.typeArguments().isEmpty())
            context.throwSourceNotSupported("super constructor invocation with type arguments not currently supported");

        context.write(" : ");
        context.matchAndWrite("super");
        context.copySpaceAndComments();
        context.matchAndWrite("(");

        List<?> arguments = superConstructorInvocation.arguments();

        boolean first = true;
        for (Expression argument : (List<Expression>) arguments) {
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

        context.write(" ");

        context.setPosition(originalPosition);
    }
}
