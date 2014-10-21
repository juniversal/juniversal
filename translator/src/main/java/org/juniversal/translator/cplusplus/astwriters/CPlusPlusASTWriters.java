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

package org.juniversal.translator.cplusplus.astwriters;

import org.juniversal.translator.core.*;
import org.juniversal.translator.cplusplus.CPPProfile;
import org.eclipse.jdt.core.dom.*;
import org.juniversal.translator.cplusplus.CPlusPlusTranslator;

import java.util.List;


public class CPlusPlusASTWriters extends ASTWriters {
    private CPlusPlusTranslator cPlusPlusTranslator;

    public CPlusPlusASTWriters(CPlusPlusTranslator cPlusPlusTranslator) {
        this.cPlusPlusTranslator = cPlusPlusTranslator;

        addDeclarationWriters();

        addStatementWriters();

        addExpressionWriters();

        // Simple name
        addWriter(SimpleName.class, new CPlusPlusASTWriter(this) {
            @Override
            public void write(Context context, ASTNode node) {
                SimpleName simpleName = (SimpleName) node;

                context.matchAndWrite(simpleName.getIdentifier());
            }
        });
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
        addWriter(VariableDeclarationFragment.class, new CPlusPlusASTWriter(this) {
            @Override
            public void write(Context context, ASTNode node) {
                VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) node;

                // TODO: Handle syntax with extra dimensions on array
                if (variableDeclarationFragment.getExtraDimensions() > 0)
                    context.throwSourceNotSupported("\"int foo[]\" syntax not currently supported; use \"int[] foo\" instead");

                if (context.isWritingVariableDeclarationNeedingStar())
                    context.write("*");

                writeNode(context, variableDeclarationFragment.getName());

                Expression initializer = variableDeclarationFragment.getInitializer();
                if (initializer != null) {
                    context.copySpaceAndComments();
                    context.matchAndWrite("=");

                    context.copySpaceAndComments();
                    writeNode(context, initializer);
                }
            }
        });

        // Single variable declaration (used in parameter list & catch clauses)
        addWriter(SingleVariableDeclaration.class, new CPlusPlusASTWriter(this) {
            @Override
            public void write(Context context, ASTNode node) {
                SingleVariableDeclaration singleVariableDeclaration = (SingleVariableDeclaration) node;

                // TODO: Handle syntax with extra dimensions on array
                if (singleVariableDeclaration.getExtraDimensions() > 0)
                    context.throwSourceNotSupported("\"int foo[]\" syntax not currently supported; use \"int[] foo\" instead");

                // TODO: Handle final & varargs

                Type type = singleVariableDeclaration.getType();
                writeType(type, context, true);

                context.copySpaceAndComments();

                writeNode(context, singleVariableDeclaration.getName());

                context.copySpaceAndComments();

                Expression initializer = singleVariableDeclaration.getInitializer();
                if (initializer != null)
                    throw new JUniversalException("Unexpected initializer present for SingleVariableDeclaration");
            }
        });

        // Simple type
        addWriter(SimpleType.class, new CPlusPlusASTWriter(this) {
            @Override
            public void write(Context context, ASTNode node) {
                SimpleType simpleType = (SimpleType) node;

                Name name = simpleType.getName();
                if (name instanceof QualifiedName) {
                    QualifiedName qualifiedName = (QualifiedName) name;

                    context.write(getNamespaceNameForPackageName(qualifiedName.getQualifier()));
                    context.setPositionToEndOfNode(qualifiedName.getQualifier());

                    context.copySpaceAndComments();
                    context.matchAndWrite(".", "::");
                    context.matchAndWrite(qualifiedName.getName().getIdentifier());
                } else {
                    SimpleName simpleName = (SimpleName) name;

                    context.matchAndWrite(simpleName.getIdentifier());
                }
            }
        });

        // Parameterized type
        addWriter(ParameterizedType.class, new CPlusPlusASTWriter(this) {
            @Override
            public void write(Context context, ASTNode node) {
                ParameterizedType parameterizedType = (ParameterizedType) node;

                writeNode(context, parameterizedType.getType());

                context.copySpaceAndComments();
                context.matchAndWrite("<");

                boolean first = true;
                List<?> typeArguments = parameterizedType.typeArguments();
                for (Object typeArgumentObject : typeArguments) {
                    Type typeArgument = (Type) typeArgumentObject;

                    if (!first) {
                        context.copySpaceAndComments();
                        context.matchAndWrite(",");
                    }

                    context.copySpaceAndComments();
                    writeNode(context, typeArgument);

                    first = false;
                }

                context.matchAndWrite(">");
            }
        });

        // Array type
        addWriter(ArrayType.class, new CPlusPlusASTWriter(this) {
            @Override
            public void write(Context context, ASTNode node) {
                ArrayType arrayType = (ArrayType) node;

                context.write("Array<");
                writeNode(context, arrayType.getElementType());
                context.skipSpaceAndComments();
                context.write(">");

                context.match("[");
                context.skipSpaceAndComments();

                context.match("]");
            }
        });

        // Primitive type
        addWriter(PrimitiveType.class, new CPlusPlusASTWriter(this) {
            @Override
            public void write(Context context, ASTNode node) {
                PrimitiveType primitiveType = (PrimitiveType) node;

                CPPProfile profile = context.getCPPProfile();

                PrimitiveType.Code code = primitiveType.getPrimitiveTypeCode();
                if (code == PrimitiveType.BYTE)
                    context.matchAndWrite("byte", profile.getInt8Type());
                else if (code == PrimitiveType.SHORT)
                    context.matchAndWrite("short", profile.getInt16Type());
                else if (code == PrimitiveType.CHAR)
                    context.matchAndWrite("char", "unichar");
                else if (code == PrimitiveType.INT)
                    context.matchAndWrite("int", profile.getInt32Type());
                else if (code == PrimitiveType.LONG) {
                    context.matchAndWrite("long", profile.getInt64Type());
                } else if (code == PrimitiveType.FLOAT)
                    context.matchAndWrite("float", profile.getFloat32Type());
                else if (code == PrimitiveType.DOUBLE)
                    context.matchAndWrite("double", profile.getFloat64Type());
                else if (code == PrimitiveType.BOOLEAN)
                    context.matchAndWrite("boolean", "bool");
                else if (code == PrimitiveType.VOID)
                    context.matchAndWrite("void", "void");
                else
                    context.throwInvalidAST("Unknown primitive type: " + code);
            }
        });
    }

    /**
     * Add visitors for the different kinds of statements.
     */
    private void addStatementWriters() {
        // Block
        addWriter(Block.class, new CPlusPlusASTWriter(this) {
            @SuppressWarnings("unchecked")
            @Override
            public void write(Context context, ASTNode node) {
                Block block = (Block) node;

                context.matchAndWrite("{");

                boolean firstStatement = true;
                for (Statement statement : (List<Statement>) block.statements()) {
                    // If the first statement is a super constructor invocation, we skip it since
                    // it's included as part of the method declaration in C++. If a super
                    // constructor invocation is a statement other than the first, which it should
                    // never be, we let that error out since writeNode won't find a match for it.
                    if (firstStatement && statement instanceof SuperConstructorInvocation)
                        context.setPositionToEndOfNodeSpaceAndComments(statement);
                    else {
                        context.copySpaceAndComments();
                        writeNode(context, statement);
                    }

                    firstStatement = false;
                }

                context.copySpaceAndComments();
                context.matchAndWrite("}");
            }
        });

        // Empty statement (";")
        addWriter(EmptyStatement.class, new CPlusPlusASTWriter(this) {
            @Override
            public void write(Context context, ASTNode node) {
                context.matchAndWrite(";");
            }
        });

        // Expression statement
        addWriter(ExpressionStatement.class, new CPlusPlusASTWriter(this) {
            @Override
            public void write(Context context, ASTNode node) {
                ExpressionStatement expressionStatement = (ExpressionStatement) node;

                writeNode(context, expressionStatement.getExpression());

                context.copySpaceAndComments();
                context.matchAndWrite(";");
            }
        });

        // If statement
        addWriter(IfStatement.class, new CPlusPlusASTWriter(this) {
            @Override
            public void write(Context context, ASTNode node) {
                IfStatement ifStatement = (IfStatement) node;

                context.matchAndWrite("if");
                context.copySpaceAndComments();

                context.matchAndWrite("(");
                context.copySpaceAndComments();

                writeNode(context, ifStatement.getExpression());
                context.copySpaceAndComments();

                context.matchAndWrite(")");
                context.copySpaceAndComments();

                writeNode(context, ifStatement.getThenStatement());

                Statement elseStatement = ifStatement.getElseStatement();
                if (elseStatement != null) {
                    context.copySpaceAndComments();

                    context.matchAndWrite("else");
                    context.copySpaceAndComments();

                    writeNode(context, elseStatement);
                }
            }
        });

        // While statement
        addWriter(WhileStatement.class, new CPlusPlusASTWriter(this) {
            @Override
            public void write(Context context, ASTNode node) {
                WhileStatement whileStatement = (WhileStatement) node;

                context.matchAndWrite("while");

                context.copySpaceAndComments();
                context.matchAndWrite("(");

                context.copySpaceAndComments();
                writeNode(context, whileStatement.getExpression());

                context.copySpaceAndComments();
                context.matchAndWrite(")");

                context.copySpaceAndComments();
                writeNode(context, whileStatement.getBody());
            }
        });

        // Do while statement
        addWriter(DoStatement.class, new CPlusPlusASTWriter(this) {
            @Override
            public void write(Context context, ASTNode node) {
                DoStatement doStatement = (DoStatement) node;

                context.matchAndWrite("do");

                context.copySpaceAndComments();
                writeNode(context, doStatement.getBody());

                context.copySpaceAndComments();
                context.matchAndWrite("while");

                context.copySpaceAndComments();
                context.matchAndWrite("(");

                context.copySpaceAndComments();
                writeNode(context, doStatement.getExpression());

                context.copySpaceAndComments();
                context.matchAndWrite(")");

                context.copySpaceAndComments();
                context.matchAndWrite(";");
            }
        });

        // Continue statement
        addWriter(ContinueStatement.class, new CPlusPlusASTWriter(this) {
            @Override
            public void write(Context context, ASTNode node) {
                ContinueStatement continueStatement = (ContinueStatement) node;

                if (continueStatement.getLabel() != null)
                    context.throwSourceNotSupported("continue statement with a label isn't supported as that construct doesn't exist in C++; change the code to not use a label");

                context.matchAndWrite("continue");

                context.copySpaceAndComments();
                context.matchAndWrite(";");
            }
        });

        // Break statement
        addWriter(BreakStatement.class, new CPlusPlusASTWriter(this) {
            @Override
            public void write(Context context, ASTNode node) {
                BreakStatement breakStatement = (BreakStatement) node;

                if (breakStatement.getLabel() != null)
                    context.throwSourceNotSupported("break statement with a label isn't supported as that construct doesn't exist in C++; change the code to not use a label");

                context.matchAndWrite("break");

                context.copySpaceAndComments();
                context.matchAndWrite(";");
            }
        });

        // For statement
        addWriter(ForStatement.class, new ForStatementWriter(this));

        // Return statement
        addWriter(ReturnStatement.class, new CPlusPlusASTWriter(this) {
            @Override
            public void write(Context context, ASTNode node) {
                ReturnStatement returnStatement = (ReturnStatement) node;

                context.matchAndWrite("return");

                Expression expression = returnStatement.getExpression();
                if (expression != null) {
                    context.copySpaceAndComments();
                    writeNode(context, returnStatement.getExpression());
                }

                context.copySpaceAndComments();
                context.matchAndWrite(";");
            }
        });

        // Local variable declaration statement
        addWriter(VariableDeclarationStatement.class, new VariableDeclarationWriter(this));

        // Throw statement
        addWriter(ThrowStatement.class, new CPlusPlusASTWriter(this) {
            @Override
            public void write(Context context, ASTNode node) {
                ThrowStatement throwStatement = (ThrowStatement) node;

                context.matchAndWrite("throw");
                context.copySpaceAndComments();

                writeNode(context, throwStatement.getExpression());
                context.copySpaceAndComments();

                context.matchAndWrite(";");
            }
        });

        // Delegating constructor invocation
        addWriter(ConstructorInvocation.class, new CPlusPlusASTWriter(this) {
            @Override
            public void write(Context context, ASTNode node) {
                context.throwSourceNotSupported("Delegating constructors aren't currently supported; for now you have to change the code to not use them (e.g. by adding an init method)");
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
        addWriter(PrefixExpression.class, new CPlusPlusASTWriter(this) {
            @Override
            public void write(Context context, ASTNode node) {
                PrefixExpression prefixExpression = (PrefixExpression) node;

                PrefixExpression.Operator operator = prefixExpression.getOperator();
                if (operator == PrefixExpression.Operator.INCREMENT)
                    context.matchAndWrite("++");
                else if (operator == PrefixExpression.Operator.DECREMENT)
                    context.matchAndWrite("--");
                else if (operator == PrefixExpression.Operator.PLUS)
                    context.matchAndWrite("+");
                else if (operator == PrefixExpression.Operator.MINUS)
                    context.matchAndWrite("-");
                else if (operator == PrefixExpression.Operator.COMPLEMENT)
                    context.matchAndWrite("~");
                else if (operator == PrefixExpression.Operator.NOT)
                    context.matchAndWrite("!");
                else context.throwInvalidAST("Unknown prefix operator type: " + operator);
                context.copySpaceAndComments();

                writeNode(context, prefixExpression.getOperand());
            }
        });

        // Postfix expression
        addWriter(PostfixExpression.class, new CPlusPlusASTWriter(this) {
            @Override
            public void write(Context context, ASTNode node) {
                PostfixExpression postfixExpression = (PostfixExpression) node;

                writeNode(context, postfixExpression.getOperand());
                context.copySpaceAndComments();

                PostfixExpression.Operator operator = postfixExpression.getOperator();
                if (operator == PostfixExpression.Operator.INCREMENT)
                    context.matchAndWrite("++");
                else if (operator == PostfixExpression.Operator.DECREMENT)
                    context.matchAndWrite("--");
                else context.throwInvalidAST("Unknown postfix operator type: " + operator);
            }
        });

        // instanceof expression
        addWriter(InstanceofExpression.class, new CPlusPlusASTWriter(this) {
            @Override
            public void write(Context context, ASTNode node) {
                InstanceofExpression instanceofExpression = (InstanceofExpression) node;

                context.write("INSTANCEOF(");

                Expression expression = instanceofExpression.getLeftOperand();
                writeNode(context, expression);

                context.skipSpaceAndComments();
                context.match("instanceof");

                context.skipSpaceAndComments();
                Type type = instanceofExpression.getRightOperand();
                writeNode(context, type);

                context.write(")");
            }
        });

        // conditional expression
        addWriter(ConditionalExpression.class, new CPlusPlusASTWriter(this) {
            @Override
            public void write(Context context, ASTNode node) {
                ConditionalExpression conditionalExpression = (ConditionalExpression) node;

                writeNode(context, conditionalExpression.getExpression());

                context.copySpaceAndComments();
                context.matchAndWrite("?");

                context.copySpaceAndComments();
                writeNode(context, conditionalExpression.getThenExpression());

                context.copySpaceAndComments();
                context.matchAndWrite(":");

                context.copySpaceAndComments();
                writeNode(context, conditionalExpression.getElseExpression());
            }
        });

        // this
        addWriter(ThisExpression.class, new CPlusPlusASTWriter(this) {
            @Override
            public void write(Context context, ASTNode node) {
                ThisExpression thisExpression = (ThisExpression) node;

                // TODO: Handle qualified this expressions; probably need to do from parent invoking
                // node & disallow qualified this accesses if not field reference / method
                // invocation; it's allowed otherwise in Java but I don't think it does anything
                // MyClass.this.   -->   this->MyClass::
                if (thisExpression.getQualifier() != null)
                    throw new JUniversalException("Qualified this expression isn't supported yet");

                context.matchAndWrite("this");
            }
        });

        // Field access
        addWriter(FieldAccess.class, new CPlusPlusASTWriter(this) {
            @Override
            public void write(Context context, ASTNode node) {
                FieldAccess fieldAccess = (FieldAccess) node;

                writeNode(context, fieldAccess.getExpression());
                context.copySpaceAndComments();

                context.matchAndWrite(".", "->");

                writeNode(context, fieldAccess.getName());
            }
        });

        // Array access
        addWriter(ArrayAccess.class, new CPlusPlusASTWriter(this) {
            @Override
            public void write(Context context, ASTNode node) {
                ArrayAccess arrayAccess = (ArrayAccess) node;

                writeNode(context, arrayAccess.getArray());
                context.copySpaceAndComments();

                context.matchAndWrite("[");
                context.copySpaceAndComments();

                writeNode(context, arrayAccess.getIndex());
                context.copySpaceAndComments();

                context.matchAndWrite("]");
            }
        });

        // Qualfied name
        addWriter(QualifiedName.class, new CPlusPlusASTWriter(this) {
            @Override
            public void write(Context context, ASTNode node) {
                QualifiedName qualifiedName = (QualifiedName) node;

                // TODO: Figure out the other cases where this can occur & make them all correct

                // Here assume that a QualifiedName refers to field access; if it refers to a type,
                // the caller should catch that case itself and ensure it never gets here

                writeNode(context, qualifiedName.getQualifier());
                context.copySpaceAndComments();

                context.matchAndWrite(".", "->");

                writeNode(context, qualifiedName.getName());
            }
        });

        // Parenthesized expression
        addWriter(ParenthesizedExpression.class, new CPlusPlusASTWriter(this) {
            @Override
            public void write(Context context, ASTNode node) {
                ParenthesizedExpression parenthesizedExpression = (ParenthesizedExpression) node;

                context.matchAndWrite("(");
                context.copySpaceAndComments();

                writeNode(context, parenthesizedExpression.getExpression());
                context.copySpaceAndComments();

                context.matchAndWrite(")");
            }
        });

        // Cast expression
        addWriter(CastExpression.class, new CPlusPlusASTWriter(this) {
            @Override
            public void write(Context context, ASTNode node) {
                CastExpression castExpression = (CastExpression) node;

                context.matchAndWrite("(", "static_cast<");

                context.copySpaceAndComments();
                writeNode(context, castExpression.getType());

                context.copySpaceAndComments();
                context.matchAndWrite(")", ">");

                // Skip just whitespace as that's not normally present here in C++ unlike Java, but
                // if there's a newline or comment, preserve that
                context.skipSpaceAndComments();
                context.copySpaceAndComments();

                // Write out the parentheses unless by chance the casted expression already includes
                // them
                boolean needParentheses = !(castExpression.getExpression() instanceof ParenthesizedExpression);
                if (needParentheses)
                    context.write("(");
                writeNode(context, castExpression.getExpression());
                if (needParentheses)
                    context.write(")");
            }
        });

        // Number literal
        addWriter(NumberLiteral.class, new CPlusPlusASTWriter(this) {
            @Override
            public void write(Context context, ASTNode node) {
                NumberLiteral numberLiteral = (NumberLiteral) node;
                context.matchAndWrite(numberLiteral.getToken());
            }
        });

        // Boolean literal
        addWriter(BooleanLiteral.class, new CPlusPlusASTWriter(this) {
            @Override
            public void write(Context context, ASTNode node) {
                BooleanLiteral booleanLiteral = (BooleanLiteral) node;
                context.matchAndWrite(booleanLiteral.booleanValue() ? "true" : "false");
            }
        });

        // Character literal
        addWriter(CharacterLiteral.class, new CPlusPlusASTWriter(this) {
            @Override
            public void write(Context context, ASTNode node) {
                CharacterLiteral characterLiteral = (CharacterLiteral) node;

                // TODO: Map character escape sequences
                context.matchAndWrite(characterLiteral.getEscapedValue());
            }
        });

        // Null literal
        addWriter(NullLiteral.class, new CPlusPlusASTWriter(this) {
            @Override
            public void write(Context context, ASTNode node) {
                context.matchAndWrite("null", "NULL");
            }
        });

        // String literal
        addWriter(StringLiteral.class, new CPlusPlusASTWriter(this) {
            @Override
            public void write(Context context, ASTNode node) {
                StringLiteral stringLiteral = (StringLiteral) node;

                context.write("new String(" + stringLiteral.getEscapedValue() + "L)");
                context.match(stringLiteral.getEscapedValue());
            }
        });
    }

    @Override
    public CPlusPlusTranslator getTranslator() {
        return cPlusPlusTranslator;
    }
}
