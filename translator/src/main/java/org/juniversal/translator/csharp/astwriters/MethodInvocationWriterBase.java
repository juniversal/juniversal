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

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.juniversal.translator.core.Context;
import org.juniversal.translator.core.JUniversalException;

import java.util.ArrayList;
import java.util.List;

import static org.juniversal.translator.core.ASTUtil.*;

public abstract class MethodInvocationWriterBase<T extends Expression> extends CSharpASTWriter<T> {
    public MethodInvocationWriterBase(CSharpASTWriters cSharpASTWriters) {
        super(cSharpASTWriters);
    }

    protected void writeMethodInvocation(Context context, T methodInvocationNode, String methodName,
                                         List typeArguments, List arguments, IMethodBinding methodBinding) {
        ArrayList<Expression> args = new ArrayList<>();
        for (Object argument : arguments)
            args.add((Expression) argument);

        ITypeBinding declaringClass = methodBinding.getDeclaringClass();
        String objectClassQualifiedName = declaringClass.getQualifiedName();

        // If it's a standard Object method (toString, equals, etc.), handle that first
        if (writeMappedObjectMethod(context, methodInvocationNode, methodName, args, methodBinding))
            return;

        if (implementsInterface(declaringClass, "java.lang.List"))
            if (writeMappedListMethod(context, methodInvocationNode, methodName, args, methodBinding))
                return;

        if (objectClassQualifiedName.equals("java.lang.String"))
            writeMappedStringMethod(context, methodInvocationNode, methodName, args, methodBinding);
        else if (objectClassQualifiedName.equals("java.lang.StringBuilder"))
            writeMappedStringBuilderMethod(context, methodInvocationNode, methodName, args, methodBinding);
        else {
            context.matchAndWrite(methodName);

            context.copySpaceAndComments();
            writeMethodInvocationArgumentList(context, typeArguments, arguments);
        }
    }

    private boolean writeMappedObjectMethod(Context context, T methodInvocation, String methodName,
                                            ArrayList<Expression> args, IMethodBinding methodBinding) {
        if (isStatic(methodBinding))
            return false;

        switch (methodName) {
            case "equals":
                context.match(methodName);
                verifyArgCount(context, args, 1);
                writeMappedMethod(context, "Equals", args.get(0));
                context.setPositionToEndOfNode(methodInvocation);
                return true;

            case "hashCode":
                context.match(methodName);
                verifyArgCount(context, args, 0);
                writeMappedMethod(context, "GetHashCode");
                context.setPositionToEndOfNode(methodInvocation);
                return true;

            case "toString":
                context.match(methodName);
                verifyArgCount(context, args, 0);
                writeMappedMethod(context, "ToString");
                context.setPositionToEndOfNode(methodInvocation);
                return true;

            default:
                return false;
        }
    }

    private boolean writeMappedListMethod(Context context, T methodInvocation, String methodName,
                                          ArrayList<Expression> args, IMethodBinding methodBinding) {
        if (isStatic(methodBinding))
            return false;

        switch (methodName) {
            case "add":
                context.match(methodName);
                if (args.size() == 1)
                    writeMappedMethod(context, "Equals", args.get(0));
                context.setPositionToEndOfNode(methodInvocation);
                return true;

            default:
                return false;
        }
    }

    private void writeMappedStringMethod(Context context, T methodInvocation, String methodName,
                                         ArrayList<Expression> args, IMethodBinding methodBinding) {
        // Skip past the method name; we'll write a different name instead
        context.match(methodName);

        if (isStatic(methodBinding)) {
            switch (methodName) {
                default:
                    context.throwSourceNotSupported("Java static method String." + methodName + " isn't supported by the translator");
            }
        } else {
            switch (methodName) {
                case "charAt":
                    verifyArgCount(context, args, 1);
                    context.write("[");
                    context.setPositionToStartOfNode(args.get(0));
                    writeNode(context, args.get(0));
                    context.write("]");
                    break;

                case "compareTo":
                    verifyArgCount(context, args, 1);
                    writeMappedMethod(context, "CompareTo", args.get(0));
                    break;

                case "contains":
                    verifyArgCount(context, args, 1);
                    writeMappedMethod(context, "Contains", args.get(0));
                    break;

                case "endsWith":
                    verifyArgCount(context, args, 1);
                    writeMappedMethod(context, "EndsWith", args.get(0), "StringComparison.Ordinal");
                    break;

                case "getChars":
                    verifyArgCount(context, args, 4);
                    writeMappedMethod(context, "GetCharsHelper", args.get(0), args.get(1), args.get(2), args.get(3));
                    break;

                case "indexOf":
                    verifyArgCount(context, args, 1, 2);
                    if (args.size() == 1)
                        writeMappedMethod(context, "IndexOf", args.get(0));
                    else writeMappedMethod(context, "IndexOf", args.get(0), args.get(1));
                    break;

                case "lastIndexOf":
                    verifyArgCount(context, args, 1, 2);
                    if (args.size() == 1)
                        writeMappedMethod(context, "LastIndexOf", args.get(0));
                    else writeMappedMethod(context, "LastIndexOf", args.get(0), args.get(1));
                    break;

                // TODO: Handle adding parens when needed
                case "isEmpty":
                    verifyArgCount(context, args, 0);
                    context.write("Length == 0");
                    break;

                case "length":
                    verifyArgCount(context, args, 0);
                    context.write("Length");
                    break;

                case "replace":
                    verifyArgCount(context, args, 2);
                    writeMappedMethod(context, "Replace", args.get(0), args.get(1));
                    break;

                case "startsWith":
                    verifyArgCount(context, args, 1);
                    writeMappedMethod(context, "StartsWith", args.get(0), "StringComparison.Ordinal");
                    break;

                case "substring":
                    verifyArgCount(context, args, 1, 2);
                    if (args.size() == 1)
                        writeMappedMethod(context, "Substring", args.get(0));
                    else {
                        Expression arg0 = args.get(0);
                        Expression arg1 = args.get(1);

                        // TODO: Verify that arg0 and arg1 and just identifiers or constants, to keep things simple

                        context.write("Substring");
                        context.write("(");

                        context.setPositionToStartOfNode(arg0);
                        writeNode(context, arg0);

                        context.write(", ");

                        context.setPositionToStartOfNode(arg1);
                        writeNode(context, arg1);
                        context.write(" - ");
                        context.setPositionToStartOfNode(arg0);
                        writeNode(context, arg0);

                        context.write(")");
                    }
                    break;

                case "toCharArray":
                    verifyArgCount(context, args, 0);
                    writeMappedMethod(context, "ToCharArray");
                    break;

                default:
                    context.throwSourceNotSupported("Java method String." + methodName + " isn't supported by the translator");
            }
        }

        context.setPositionToEndOfNode(methodInvocation);
    }

    private void writeMappedStringBuilderMethod(Context context, T methodInvocation, String methodName,
                                                ArrayList<Expression> args, IMethodBinding methodBinding) {
        if (isStatic(methodBinding))
            context.throwSourceNotSupported("Java static method StringBuilder." + methodName + " isn't supported by the translator");

        // Skip past the method name; we'll write a different name instead
        context.match(methodName);

        switch (methodName) {
            case "charAt":
                verifyArgCount(context, args, 1);
                context.write("[");
                context.setPositionToStartOfNode(args.get(0));
                writeNode(context, args.get(0));
                context.write("]");
                break;

            case "append":
                verifyArgCount(context, args, 1, 3);
                if (args.size() == 1)
                    writeMappedMethod(context, "Append", args.get(0));
                else writeMappedMethod(context, "Append", args.get(0), args.get(1), args.get(2));
                break;

            case "length":
                verifyArgCount(context, args, 0);
                context.write("Length");
                break;

            case "toString":
                verifyArgCount(context, args, 0);
                writeMappedMethod(context, "ToString");
                break;

            default:
                context.throwSourceNotSupported("Java method StringBuilder." + methodName + " isn't supported by the translator");
        }

        context.setPositionToEndOfNode(methodInvocation);
    }

    private void verifyArgCount(Context context, ArrayList<Expression> args, int expectedArgCount) {
        if (args.size() != expectedArgCount)
            context.throwSourceNotSupported("Method call has " + args.size() +
                                            " argument(s); the translator only supports this method with " +
                                            expectedArgCount + " argument(s)");
    }

    private void verifyArgCount(Context context, ArrayList<Expression> args, int expectedArgCount1, int expectedArgCount2) {
        if (args.size() != expectedArgCount1 && args.size() != expectedArgCount2)
            context.throwSourceNotSupported("Method call has " + args.size() +
                                            " argument(s); the translator only supports this method with " +
                                            expectedArgCount1 + " or " + expectedArgCount2 + " argument(s)");
    }

    private void writeMappedMethod(Context context, String mappedMethodName, Object... args) {
        context.write(mappedMethodName);
        context.write("(");

        boolean first = true;
        for (Object arg : args) {
            if (!first)
                context.write(", ");

            if (arg instanceof Expression) {
                Expression expressionArg = (Expression) arg;
                context.setPositionToStartOfNode(expressionArg);

                writeNode(context, expressionArg);
            } else if (arg instanceof String)
                context.write((String) arg);
            else throw new JUniversalException("Unexpected arg object type in writeMappedMethod");

            first = false;
        }

        context.write(")");
    }
}
