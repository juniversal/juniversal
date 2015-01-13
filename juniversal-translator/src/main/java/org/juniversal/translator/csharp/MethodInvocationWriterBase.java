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

public abstract class MethodInvocationWriterBase<T extends Expression> extends CSharpASTNodeWriter<T> {
    public MethodInvocationWriterBase(CSharpSourceFileWriter cSharpASTWriters) {
        super(cSharpASTWriters);
    }

    protected void writeMethodInvocation(T methodInvocationNode, @Nullable Expression expression, SimpleName methodName,
                                         List typeArguments, List arguments, IMethodBinding methodBinding) {
        String methodNameString = methodName.getIdentifier();

        ArrayList<Expression> args = new ArrayList<>();
        for (Object argument : arguments)
            args.add((Expression) argument);

        ITypeBinding objectType;
        if (expression != null)
            objectType = expression.resolveTypeBinding();
        else objectType = methodBinding.getDeclaringClass();

        //TODO: Detect when precedence allows skkpping parens
        boolean addParentheses = false;
        if (isType(objectType, "java.lang.String") && methodNameString.equals("isEmpty"))
            addParentheses = true;

        if (addParentheses)
            write("(");

        if (expression != null) {
            writeNode(expression);

            copySpaceAndComments();

            // If the Java method is mapped to an overloaded operator in C# (e.g. charAt -> []) then handle that case
            // here, with no "." getting written
            if (writeMethodMappedToOperatorOverload(methodNameString, args, methodBinding))
                return;

            // A functional interface method becomes a delegate.   So in Java a call of the form
            // "funcInterface.funcMethod(...)" becomes in C# "funcInterface(...)".   Handle that case here
            @Nullable ITypeBinding expressionTypeBinding = expression.resolveTypeBinding();
            if (expressionTypeBinding != null && isFunctionalInterface(expressionTypeBinding)) {
                match(".");
                skipSpaceAndComments();
                match(methodNameString);
                skipSpaceAndComments();

                writeMethodInvocationArgumentList(arguments);
                return;
            }

            matchAndWrite(".");
            copySpaceAndComments();
        }

        // If it's a standard Object method (toString, equals, etc.), handle that first
        if (writeMappedObjectMethod(methodInvocationNode, methodNameString, args, methodBinding))
            ;
        else if (implementsInterface(objectType, "java.lang.List") &&
                 writeMappedListMethod(methodInvocationNode, methodNameString, args, methodBinding))
            ;
        else if (isType(objectType, "java.lang.String"))
            writeMappedStringMethod(methodInvocationNode, methodNameString, args, methodBinding);
        else if (isType(objectType, "java.lang.StringBuilder"))
            writeMappedStringBuilderMethod(methodInvocationNode, methodNameString, args, methodBinding);
        else {
            // In C# type arguments for methods come after the method name, not before ("foo.<String>bar(3)" in Java is
            // "foo.bar<String>(3)" in C#).   So change the order around here.
            if (typeArguments != null && !typeArguments.isEmpty()) {
                writeNodeAtDifferentPosition(methodName);

                matchAndWrite("<");

                writeCommaDelimitedNodes(typeArguments, (Type type) -> {
                    copySpaceAndComments();
                    writeNode(type);
                });

                copySpaceAndComments();
                matchAndWrite(">");

                setPositionToEndOfNode(methodName);
            } else matchAndWrite(methodNameString);

            copySpaceAndComments();
            writeMethodInvocationArgumentList(arguments);
        }

        if (addParentheses)
            write(")");
    }

    /**
     * Some Java methods are mapped to overloaded operators in C# (just String.charAt -> [] currently).  Handle those
     * here.
     *
     * @param methodName    method name
     * @param args          method arguments
     * @param methodBinding IMethodBinding object
     * @return true iff the method call was handled, mapped to an operator
     */
    private boolean writeMethodMappedToOperatorOverload(String methodName, ArrayList<Expression> args,
                                                        IMethodBinding methodBinding) {
        if (isStatic(methodBinding))
            return false;

        ITypeBinding objectType = methodBinding.getDeclaringClass();

        if (isType(objectType, "java.lang.String") || isType(objectType, "java.lang.AbstractStringBuilder")) {
            switch (methodName) {
                case "charAt":
                    verifyArgCount(args, 1);
                    match(".");

                    copySpaceAndComments();
                    match("charAt");

                    copySpaceAndComments();
                    matchAndWrite("(", "[");

                    copySpaceAndComments();
                    writeNode(args.get(0));

                    copySpaceAndComments();
                    matchAndWrite(")", "]");
                    return true;

                default:
                    return false;
            }
        } else return false;
    }

    private boolean writeMappedObjectMethod(T methodInvocation, String methodName,
                                            ArrayList<Expression> args, IMethodBinding methodBinding) {
        if (isStatic(methodBinding))
            return false;

        switch (methodName) {
            case "equals":
                match(methodName);
                verifyArgCount(args, 1);
                writeMappedMethod("Equals", args.get(0));
                setPositionToEndOfNode(methodInvocation);
                return true;

            case "hashCode":
                match(methodName);
                verifyArgCount(args, 0);
                writeMappedMethod("GetHashCode");
                setPositionToEndOfNode(methodInvocation);
                return true;

            case "toString":
                match(methodName);
                verifyArgCount(args, 0);
                writeMappedMethod("ToString");
                setPositionToEndOfNode(methodInvocation);
                return true;

            default:
                return false;
        }
    }

    private boolean writeMappedListMethod(T methodInvocation, String methodName,
                                          ArrayList<Expression> args, IMethodBinding methodBinding) {
        if (isStatic(methodBinding))
            return false;

        switch (methodName) {
            case "add":
                match(methodName);
                if (args.size() == 1)
                    writeMappedMethod("Equals", args.get(0));
                setPositionToEndOfNode(methodInvocation);
                return true;

            default:
                return false;
        }
    }

    private void writeMappedStringMethod(T methodInvocation, String methodName,
                                         ArrayList<Expression> args, IMethodBinding methodBinding) {
        // Skip past the method name; we'll write a different name instead
        match(methodName);

        if (isStatic(methodBinding)) {
            switch (methodName) {
                default:
                    throw sourceNotSupported("Java static method String." + methodName + " isn't supported by the translator");
            }
        } else {
            switch (methodName) {
                case "compareTo":
                    verifyArgCount(args, 1);
                    writeMappedMethod("CompareTo", args.get(0));
                    break;

                case "contains":
                    verifyArgCount(args, 1);
                    writeMappedMethod("Contains", args.get(0));
                    break;

                case "endsWith":
                    verifyArgCount(args, 1);
                    writeMappedMethod("EndsWith", args.get(0), nativeReference("System", "StringComparison.Ordinal"));
                    break;

                case "indexOf":
                    verifyArgCount(args, 1, 2);
                    if (args.size() == 1)
                        writeMappedMethod("IndexOf", args.get(0));
                    else writeMappedMethod("IndexOf", args.get(0), args.get(1));
                    break;

                case "lastIndexOf":
                    verifyArgCount(args, 1, 2);
                    if (args.size() == 1)
                        writeMappedMethod("LastIndexOf", args.get(0));
                    else writeMappedMethod("LastIndexOf", args.get(0), args.get(1));
                    break;

                // TODO: Handle adding parens when needed
                case "isEmpty":
                    verifyArgCount(args, 0);
                    write("Length == 0");
                    break;

                case "length":
                    verifyArgCount(args, 0);
                    write("Length");
                    break;

                case "replace":
                    verifyArgCount(args, 2);
                    writeMappedMethod("Replace", args.get(0), args.get(1));
                    break;

                case "startsWith":
                    verifyArgCount(args, 1);

                    writeMappedMethod("StartsWith", args.get(0), nativeReference("System", "StringComparison.Ordinal"));
                    break;

                case "substring":
                    verifyArgCount(args, 1, 2);
                    if (args.size() == 1)
                        writeMappedMethod("Substring", args.get(0));
                    else {
                        Expression arg0 = args.get(0);
                        Expression arg1 = args.get(1);

                        // TODO: Verify that arg0 and arg1 and just identifiers or constants, to keep things simple

                        write("Substring");
                        write("(");

                        setPositionToStartOfNode(arg0);
                        writeNode(arg0);

                        write(", ");

                        setPositionToStartOfNode(arg1);
                        writeNode(arg1);
                        write(" - ");
                        setPositionToStartOfNode(arg0);
                        writeNode(arg0);

                        write(")");
                    }
                    break;

                // TODO: Remove this
                case "split":
                    verifyArgCount(args, 1, 1);
                    writeMappedMethod("xxxxx");
                    break;

                case "toCharArray":
                    verifyArgCount(args, 0);
                    writeMappedMethod("ToCharArray");
                    break;

                case "trim":
                    verifyArgCount(args, 0);
                    writeMappedMethod("Trim");
                    break;

                default:
                    throw sourceNotSupported("Java method String." + methodName + " isn't supported by the translator");
            }
        }

        setPositionToEndOfNode(methodInvocation);
    }

    private void writeMappedStringBuilderMethod(T methodInvocation, String methodName,
                                                ArrayList<Expression> args, IMethodBinding methodBinding) {
        if (isStatic(methodBinding))
            throw sourceNotSupported("Java static method StringBuilder." + methodName + " isn't supported by the translator");

        // Skip past the method name; we'll write a different name instead
        match(methodName);

        switch (methodName) {
            case "append":
                verifyArgCount(args, 1, 3);
                if (args.size() == 1)
                    writeMappedMethod("Append", args.get(0));
                else writeMappedMethod("Append", args.get(0), args.get(1), args.get(2));
                break;

            case "length":
                verifyArgCount(args, 0);
                write("Length");
                break;

            case "toString":
                verifyArgCount(args, 0);
                writeMappedMethod("ToString");
                break;

            default:
                throw sourceNotSupported("Java method StringBuilder." + methodName + " isn't supported by the translator");
        }

        setPositionToEndOfNode(methodInvocation);
    }

    private void verifyArgCount(ArrayList<Expression> args, int expectedArgCount) {
        if (args.size() != expectedArgCount)
            throw sourceNotSupported("Method call has " + args.size() +
                                     " argument(s); the translator only supports this method with " +
                                     expectedArgCount + " argument(s)");
    }

    private void verifyArgCount(ArrayList<Expression> args, int expectedArgCount1, int expectedArgCount2) {
        if (args.size() != expectedArgCount1 && args.size() != expectedArgCount2)
            throw sourceNotSupported("Method call has " + args.size() +
                                     " argument(s); the translator only supports this method with " +
                                     expectedArgCount1 + " or " + expectedArgCount2 + " argument(s)");
    }

    private void writeMappedMethod(String mappedMethodName, Object... args) {
        write(mappedMethodName);
        write("(");

        boolean first = true;
        for (Object arg : args) {
            if (!first)
                write(", ");

            if (arg instanceof Expression) {
                Expression expressionArg = (Expression) arg;
                setPositionToStartOfNode(expressionArg);

                writeNode(expressionArg);
            } else if (arg instanceof String)
                write((String) arg);
            else throw new JUniversalException("Unexpected arg object type in writeMappedMethod");

            first = false;
        }

        write(")");
    }
}
