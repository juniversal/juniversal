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

package org.juniversal.translator.cplusplus;

import org.eclipse.jdt.core.dom.*;
import org.juniversal.translator.core.Context;
import org.juniversal.translator.core.JUniversalException;
import org.juniversal.translator.core.SourceFile;
import org.juniversal.translator.core.SourceFileWriter;
import org.juniversal.translator.csharp.CSharpASTNodeWriter;

import java.io.Writer;
import java.util.List;


public class CPlusPlusSourceFileWriter extends SourceFileWriter {
    private CPlusPlusTranslator cPlusPlusTranslator;
    private Context context;
    private OutputType outputType;

    public CPlusPlusSourceFileWriter(CPlusPlusTranslator cPlusPlusTranslator, SourceFile sourceFile, Writer writer,
                                     OutputType outputType) {
        super(cPlusPlusTranslator, sourceFile, writer);

        this.cPlusPlusTranslator = cPlusPlusTranslator;
        this.outputType = outputType;
        this.context = new Context();

        addDeclarationWriters();
        addStatementWriters();
        addExpressionWriters();

        // Simple name
        addWriter(SimpleName.class, new CPlusPlusASTNodeWriter<SimpleName>(this) {
            @Override
            public void write(SimpleName simpleName) {
                matchAndWrite(simpleName.getIdentifier());
            }
        });
    }

    @Override
    public CPlusPlusTranslator getTranslator() {
        return cPlusPlusTranslator;
    }

    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public OutputType getOutputType() {
        return outputType;
    }

    /**
     * Add visitors for class, method, field, and type declarations.
     */
    private void addDeclarationWriters() {
        // Compilation unit
        addWriter(CompilationUnit.class, new CompilationUnitWriter(this));

        // Type (class/interface) declaration
        addWriter(TypeDeclaration.class, new TypeDeclarationWriter(this));

        // Method declaration (which includes implementation)
        addWriter(MethodDeclaration.class, new MethodDeclarationWriter(this));

        // Field declaration
        addWriter(FieldDeclaration.class, new FieldDeclarationWriter(this));

        // Variable declaration fragment
        addWriter(VariableDeclarationFragment.class, new CPlusPlusASTNodeWriter<VariableDeclarationFragment>(this) {
            @Override
            public void write(VariableDeclarationFragment variableDeclarationFragment) {
                // TODO: Handle syntax with extra dimensions on array
                if (variableDeclarationFragment.getExtraDimensions() > 0)
                    throw sourceNotSupported("\"int foo[]\" syntax not currently supported; use \"int[] foo\" instead");

                if (getContext().isWritingVariableDeclarationNeedingStar())
                    write("*");

                writeNode(variableDeclarationFragment.getName());

                Expression initializer = variableDeclarationFragment.getInitializer();
                if (initializer != null) {
                    copySpaceAndComments();
                    matchAndWrite("=");

                    copySpaceAndComments();
                    writeNode(initializer);
                }
            }
        });

        // Single variable declaration (used in parameter list, catch clauses, and enhanced for statements)
        addWriter(SingleVariableDeclaration.class, new CPlusPlusASTNodeWriter<SingleVariableDeclaration>(this) {
            @Override
            public void write(SingleVariableDeclaration singleVariableDeclaration) {
                // TODO: Handle syntax with extra dimensions on array
                if (singleVariableDeclaration.getExtraDimensions() > 0)
                    throw sourceNotSupported("\"int foo[]\" syntax not currently supported; use \"int[] foo\" instead");

                List<?> modifiers = singleVariableDeclaration.modifiers();
                ensureModifiersJustFinalOrAnnotations(modifiers);
                skipModifiers(modifiers);

                // TODO: Handle final & varargs

                Type type = singleVariableDeclaration.getType();

                if (singleVariableDeclaration.isVarargs()) {
                    writeArrayOfType(type);

                    copySpaceAndComments();
                    match("...");
                } else writeType(type, true);

                copySpaceAndComments();
                writeNode(singleVariableDeclaration.getName());

                if (singleVariableDeclaration.getInitializer() != null)
                    throw new JUniversalException("Unexpected initializer present for SingleVariableDeclaration");
            }
        });

        // Simple type
        addWriter(SimpleType.class, new CPlusPlusASTNodeWriter<SimpleType>(this) {
            @Override
            public void write(SimpleType simpleType) {
                Name name = simpleType.getName();
                if (name instanceof QualifiedName) {
                    QualifiedName qualifiedName = (QualifiedName) name;

                    write(getNamespaceNameForPackageName(qualifiedName.getQualifier()));
                    setPositionToEndOfNode(qualifiedName.getQualifier());

                    copySpaceAndComments();
                    matchAndWrite(".", "::");
                    matchAndWrite(qualifiedName.getName().getIdentifier());
                } else {
                    SimpleName simpleName = (SimpleName) name;

                    matchAndWrite(simpleName.getIdentifier());
                }
            }
        });

        // Parameterized type
        addWriter(ParameterizedType.class, new CPlusPlusASTNodeWriter<ParameterizedType>(this) {
            @Override
            public void write(ParameterizedType parameterizedType) {
                writeNode(parameterizedType.getType());

                copySpaceAndComments();
                matchAndWrite("<");

                boolean first = true;
                List<?> typeArguments = parameterizedType.typeArguments();
                for (Object typeArgumentObject : typeArguments) {
                    Type typeArgument = (Type) typeArgumentObject;

                    if (!first) {
                        copySpaceAndComments();
                        matchAndWrite(",");
                    }

                    copySpaceAndComments();
                    writeNode(typeArgument);

                    first = false;
                }

                matchAndWrite(">");
            }
        });

        // Array type
        addWriter(ArrayType.class, new CPlusPlusASTNodeWriter<ArrayType>(this) {
            @Override
            public void write(ArrayType arrayType) {
                writeArrayOfType(arrayType.getElementType());

                skipSpaceAndComments();
                match("[");

                skipSpaceAndComments();
                match("]");
            }
        });

        // Primitive type
        addWriter(PrimitiveType.class, new CPlusPlusASTNodeWriter<PrimitiveType>(this) {
            @Override
            public void write(PrimitiveType primitiveType) {
                CPPProfile profile = getCPPProfile();

                PrimitiveType.Code code = primitiveType.getPrimitiveTypeCode();
                if (code == PrimitiveType.BYTE)
                    matchAndWrite("byte", profile.getInt8Type());
                else if (code == PrimitiveType.SHORT)
                    matchAndWrite("short", profile.getInt16Type());
                else if (code == PrimitiveType.CHAR)
                    matchAndWrite("char", "unichar");
                else if (code == PrimitiveType.INT)
                    matchAndWrite("int", profile.getInt32Type());
                else if (code == PrimitiveType.LONG) {
                    matchAndWrite("long", profile.getInt64Type());
                } else if (code == PrimitiveType.FLOAT)
                    matchAndWrite("float", profile.getFloat32Type());
                else if (code == PrimitiveType.DOUBLE)
                    matchAndWrite("double", profile.getFloat64Type());
                else if (code == PrimitiveType.BOOLEAN)
                    matchAndWrite("boolean", "bool");
                else if (code == PrimitiveType.VOID)
                    matchAndWrite("void", "void");
                else
                    throw invalidAST("Unknown primitive type: " + code);
            }
        });
    }

    /**
     * Add visitors for the different kinds of statements.
     */
    private void addStatementWriters() {
        // Block
        addWriter(Block.class, new CPlusPlusASTNodeWriter<Block>(this) {
            @SuppressWarnings("unchecked")
            @Override
            public void write(Block block) {
                matchAndWrite("{");

                boolean firstStatement = true;
                for (Statement statement : (List<Statement>) block.statements()) {
                    // If the first statement is a super constructor invocation, we skip it since
                    // it's included as part of the method declaration in C++. If a super
                    // constructor invocation is a statement other than the first, which it should
                    // never be, we let that error out since writeNode won't find a match for it.
                    if (firstStatement && statement instanceof SuperConstructorInvocation)
                        setPositionToEndOfNodeSpaceAndComments(statement);
                    else {
                        copySpaceAndComments();
                        writeNode(statement);
                    }

                    firstStatement = false;
                }

                copySpaceAndComments();
                matchAndWrite("}");
            }
        });

        // Empty statement (";")
        addWriter(EmptyStatement.class, new CPlusPlusASTNodeWriter<EmptyStatement>(this) {
            @Override
            public void write(EmptyStatement emptyStatement) {
                matchAndWrite(";");
            }
        });

        // Expression statement
        addWriter(ExpressionStatement.class, new CPlusPlusASTNodeWriter<ExpressionStatement>(this) {
            @Override
            public void write(ExpressionStatement expressionStatement) {
                writeNode(expressionStatement.getExpression());

                copySpaceAndComments();
                matchAndWrite(";");
            }
        });

        // If statement
        addWriter(IfStatement.class, new CPlusPlusASTNodeWriter<IfStatement>(this) {
            @Override
            public void write(IfStatement ifStatement) {
                matchAndWrite("if");
                copySpaceAndComments();

                matchAndWrite("(");
                copySpaceAndComments();

                writeNode(ifStatement.getExpression());
                copySpaceAndComments();

                matchAndWrite(")");
                copySpaceAndComments();

                writeNode(ifStatement.getThenStatement());

                Statement elseStatement = ifStatement.getElseStatement();
                if (elseStatement != null) {
                    copySpaceAndComments();

                    matchAndWrite("else");
                    copySpaceAndComments();

                    writeNode(elseStatement);
                }
            }
        });

        // While statement
        addWriter(WhileStatement.class, new CPlusPlusASTNodeWriter<WhileStatement>(this) {
            @Override
            public void write(WhileStatement whileStatement) {
                matchAndWrite("while");

                copySpaceAndComments();
                matchAndWrite("(");

                copySpaceAndComments();
                writeNode(whileStatement.getExpression());

                copySpaceAndComments();
                matchAndWrite(")");

                copySpaceAndComments();
                writeNode(whileStatement.getBody());
            }
        });

        // Do while statement
        addWriter(DoStatement.class, new CPlusPlusASTNodeWriter<DoStatement>(this) {
            @Override
            public void write(DoStatement doStatement) {
                matchAndWrite("do");

                copySpaceAndComments();
                writeNode(doStatement.getBody());

                copySpaceAndComments();
                matchAndWrite("while");

                copySpaceAndComments();
                matchAndWrite("(");

                copySpaceAndComments();
                writeNode(doStatement.getExpression());

                copySpaceAndComments();
                matchAndWrite(")");

                copySpaceAndComments();
                matchAndWrite(";");
            }
        });

        // Continue statement
        addWriter(ContinueStatement.class, new CPlusPlusASTNodeWriter<ContinueStatement>(this) {
            @Override
            public void write(ContinueStatement continueStatement) {
                if (continueStatement.getLabel() != null)
                    throw sourceNotSupported("continue statement with a label isn't supported as that construct doesn't exist in C++; change the code to not use a label");

                matchAndWrite("continue");

                copySpaceAndComments();
                matchAndWrite(";");
            }
        });

        // Break statement
        addWriter(BreakStatement.class, new CPlusPlusASTNodeWriter<BreakStatement>(this) {
            @Override
            public void write(BreakStatement breakStatement) {
                if (breakStatement.getLabel() != null)
                    throw sourceNotSupported("break statement with a label isn't supported as that construct doesn't exist in C++; change the code to not use a label");

                matchAndWrite("break");

                copySpaceAndComments();
                matchAndWrite(";");
            }
        });

        // For statement
        addWriter(ForStatement.class, new ForStatementWriter(this));

        // Return statement
        addWriter(ReturnStatement.class, new CPlusPlusASTNodeWriter<ReturnStatement>(this) {
            @Override
            public void write(ReturnStatement returnStatement) {
                matchAndWrite("return");

                Expression expression = returnStatement.getExpression();
                if (expression != null) {
                    copySpaceAndComments();
                    writeNode(returnStatement.getExpression());
                }

                copySpaceAndComments();
                matchAndWrite(";");
            }
        });

        // Local variable declaration statement
        addWriter(VariableDeclarationStatement.class, new VariableDeclarationWriter(this));

        // Throw statement
        addWriter(ThrowStatement.class, new CPlusPlusASTNodeWriter<ThrowStatement>(this) {
            @Override
            public void write(ThrowStatement throwStatement) {
                matchAndWrite("throw");

                copySpaceAndComments();
                writeNode(throwStatement.getExpression());

                copySpaceAndComments();
                matchAndWrite(";");
            }
        });

        // Delegating constructor invocation
        addWriter(ConstructorInvocation.class, new CPlusPlusASTNodeWriter(this) {
            @Override
            public void write(ASTNode node) {
                throw sourceNotSupported("Delegating constructors aren't currently supported; for now you have to change the code to not use them (e.g. by adding an init method)");
            }
        });
    }

    /**
     * Add visitors for the different kinds of expressions.
     */
    private void addExpressionWriters() {

        // Assignment expression
        addWriter(Assignment.class, new AssignmentWriter(this));

        // Method invocation
        addWriter(MethodInvocation.class, new MethodInvocationWriter(this));

        // Super Method invocation
        addWriter(SuperMethodInvocation.class, new MethodInvocationWriter(this));

        // Class instance creation
        addWriter(ClassInstanceCreation.class, new ClassInstanceCreationWriter(this));

        // Array creation
        addWriter(ArrayCreation.class, new ArrayCreationWriter(this));

        // Variable declaration expression (used in a for statement)
        addWriter(VariableDeclarationExpression.class, new VariableDeclarationWriter(this));

        // Infix expression
        addWriter(InfixExpression.class, new InfixExpressionWriter(this));

        // Prefix expression
        addWriter(PrefixExpression.class, new CPlusPlusASTNodeWriter<PrefixExpression>(this) {
            @Override
            public void write(PrefixExpression prefixExpression) {
                PrefixExpression.Operator operator = prefixExpression.getOperator();
                if (operator == PrefixExpression.Operator.INCREMENT)
                    matchAndWrite("++");
                else if (operator == PrefixExpression.Operator.DECREMENT)
                    matchAndWrite("--");
                else if (operator == PrefixExpression.Operator.PLUS)
                    matchAndWrite("+");
                else if (operator == PrefixExpression.Operator.MINUS)
                    matchAndWrite("-");
                else if (operator == PrefixExpression.Operator.COMPLEMENT)
                    matchAndWrite("~");
                else if (operator == PrefixExpression.Operator.NOT)
                    matchAndWrite("!");
                else throw invalidAST("Unknown prefix operator type: " + operator);
                copySpaceAndComments();

                writeNode(prefixExpression.getOperand());
            }
        });

        // Postfix expression
        addWriter(PostfixExpression.class, new CPlusPlusASTNodeWriter<PostfixExpression>(this) {
            @Override
            public void write(PostfixExpression postfixExpression) {
                writeNode(postfixExpression.getOperand());
                copySpaceAndComments();

                PostfixExpression.Operator operator = postfixExpression.getOperator();
                if (operator == PostfixExpression.Operator.INCREMENT)
                    matchAndWrite("++");
                else if (operator == PostfixExpression.Operator.DECREMENT)
                    matchAndWrite("--");
                else throw invalidAST("Unknown postfix operator type: " + operator);
            }
        });

        // instanceof expression
        addWriter(InstanceofExpression.class, new CPlusPlusASTNodeWriter<InstanceofExpression>(this) {
            @Override
            public void write(InstanceofExpression instanceofExpression) {
                write("INSTANCEOF(");

                Expression expression = instanceofExpression.getLeftOperand();
                writeNode(expression);

                skipSpaceAndComments();
                match("instanceof");

                skipSpaceAndComments();
                Type type = instanceofExpression.getRightOperand();
                writeNode(type);

                write(")");
            }
        });

        // conditional expression
        addWriter(ConditionalExpression.class, new CPlusPlusASTNodeWriter<ConditionalExpression>(this) {
            @Override
            public void write(ConditionalExpression conditionalExpression) {
                writeNode(conditionalExpression.getExpression());

                copySpaceAndComments();
                matchAndWrite("?");

                copySpaceAndComments();
                writeNode(conditionalExpression.getThenExpression());

                copySpaceAndComments();
                matchAndWrite(":");

                copySpaceAndComments();
                writeNode(conditionalExpression.getElseExpression());
            }
        });

        // this
        addWriter(ThisExpression.class, new CPlusPlusASTNodeWriter<ThisExpression>(this) {
            @Override
            public void write(ThisExpression thisExpression) {
                // TODO: Handle qualified this expressions; probably need to do from parent invoking
                // node & disallow qualified this accesses if not field reference / method
                // invocation; it's allowed otherwise in Java but I don't think it does anything
                // MyClass.this.   -->   this->MyClass::
                if (thisExpression.getQualifier() != null)
                    throw new JUniversalException("Qualified this expression isn't supported yet");

                matchAndWrite("this");
            }
        });

        // Field access
        addWriter(FieldAccess.class, new CPlusPlusASTNodeWriter<FieldAccess>(this) {
            @Override
            public void write(FieldAccess fieldAccess) {
                writeNode(fieldAccess.getExpression());
                copySpaceAndComments();

                matchAndWrite(".", "->");

                writeNode(fieldAccess.getName());
            }
        });

        // Array access
        addWriter(ArrayAccess.class, new CPlusPlusASTNodeWriter<ArrayAccess>(this) {
            @Override
            public void write(ArrayAccess arrayAccess) {
                writeNode(arrayAccess.getArray());
                copySpaceAndComments();

                matchAndWrite("[");
                copySpaceAndComments();

                writeNode(arrayAccess.getIndex());
                copySpaceAndComments();

                matchAndWrite("]");
            }
        });

        // Qualified name
        addWriter(QualifiedName.class, new CPlusPlusASTNodeWriter<QualifiedName>(this) {
            @Override
            public void write(QualifiedName qualifiedName) {
                // TODO: Figure out the other cases where this can occur & make them all correct

                // Here assume that a QualifiedName refers to field access; if it refers to a type,
                // the caller should catch that case itself and ensure it never gets here

                writeNode(qualifiedName.getQualifier());
                copySpaceAndComments();

                matchAndWrite(".", "->");

                writeNode(qualifiedName.getName());
            }
        });

        // Parenthesized expression
        addWriter(ParenthesizedExpression.class, new CPlusPlusASTNodeWriter<ParenthesizedExpression>(this) {
            @Override
            public void write(ParenthesizedExpression parenthesizedExpression) {
                matchAndWrite("(");

                copySpaceAndComments();
                writeNode(parenthesizedExpression.getExpression());

                copySpaceAndComments();
                matchAndWrite(")");
            }
        });

        // Cast expression
        addWriter(CastExpression.class, new CPlusPlusASTNodeWriter<CastExpression>(this) {
            @Override
            public void write(CastExpression castExpression) {
                matchAndWrite("(", "static_cast<");

                copySpaceAndComments();
                writeNode(castExpression.getType());

                copySpaceAndComments();
                matchAndWrite(")", ">");

                // Skip just whitespace as that's not normally present here in C++ unlike Java, but
                // if there's a newline or comment, preserve that
                skipSpaceAndComments();
                copySpaceAndComments();

                // Write out the parentheses unless by chance the casted expression already includes
                // them
                boolean needParentheses = !(castExpression.getExpression() instanceof ParenthesizedExpression);
                if (needParentheses)
                    write("(");
                writeNode(castExpression.getExpression());
                if (needParentheses)
                    write(")");
            }
        });

        // Number literal
        addWriter(NumberLiteral.class, new CPlusPlusASTNodeWriter<NumberLiteral>(this) {
            @Override
            public void write(NumberLiteral numberLiteral) {
                matchAndWrite(numberLiteral.getToken());
            }
        });

        // Boolean literal
        addWriter(BooleanLiteral.class, new CPlusPlusASTNodeWriter<BooleanLiteral>(this) {
            @Override
            public void write(BooleanLiteral booleanLiteral) {
                matchAndWrite(booleanLiteral.booleanValue() ? "true" : "false");
            }
        });

        // Character literal
        addWriter(CharacterLiteral.class, new CPlusPlusASTNodeWriter<CharacterLiteral>(this) {
            @Override
            public void write(CharacterLiteral characterLiteral) {
                // TODO: Map character escape sequences
                matchAndWrite(characterLiteral.getEscapedValue());
            }
        });

        // Null literal
        addWriter(NullLiteral.class, new CPlusPlusASTNodeWriter<NullLiteral>(this) {
            @Override
            public void write(NullLiteral nullLiteral) {
                matchAndWrite("null", "NULL");
            }
        });

        // String literal
        addWriter(StringLiteral.class, new CPlusPlusASTNodeWriter<StringLiteral>(this) {
            @Override
            public void write(StringLiteral stringLiteral) {
                write("new String(" + stringLiteral.getEscapedValue() + "L)");
                match(stringLiteral.getEscapedValue());
            }
        });
    }
}
