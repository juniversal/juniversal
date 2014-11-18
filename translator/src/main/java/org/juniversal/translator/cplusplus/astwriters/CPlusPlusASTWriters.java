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

    public CPlusPlusASTWriters(Context context, CPlusPlusTranslator cPlusPlusTranslator) {
        super(context);

        this.cPlusPlusTranslator = cPlusPlusTranslator;

        addDeclarationWriters();

        addStatementWriters();

        addExpressionWriters();

        // Simple name
        addWriter(SimpleName.class, new CPlusPlusASTWriter(this) {
            @Override
            public void write(ASTNode node) {
                SimpleName simpleName = (SimpleName) node;

                matchAndWrite(simpleName.getIdentifier());
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
            public void write(ASTNode node) {
                VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) node;

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

        // Single variable declaration (used in parameter list & catch clauses)
        addWriter(SingleVariableDeclaration.class, new CPlusPlusASTWriter(this) {
            @Override
            public void write(ASTNode node) {
                SingleVariableDeclaration singleVariableDeclaration = (SingleVariableDeclaration) node;

                // TODO: Handle syntax with extra dimensions on array
                if (singleVariableDeclaration.getExtraDimensions() > 0)
                    throw sourceNotSupported("\"int foo[]\" syntax not currently supported; use \"int[] foo\" instead");

                // TODO: Handle final & varargs

                Type type = singleVariableDeclaration.getType();
                writeType(type, true);

                copySpaceAndComments();
                writeNode(singleVariableDeclaration.getName());

                copySpaceAndComments();
                Expression initializer = singleVariableDeclaration.getInitializer();
                if (initializer != null)
                    throw new JUniversalException("Unexpected initializer present for SingleVariableDeclaration");
            }
        });

        // Simple type
        addWriter(SimpleType.class, new CPlusPlusASTWriter(this) {
            @Override
            public void write(ASTNode node) {
                SimpleType simpleType = (SimpleType) node;

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
        addWriter(ParameterizedType.class, new CPlusPlusASTWriter(this) {
            @Override
            public void write(ASTNode node) {
                ParameterizedType parameterizedType = (ParameterizedType) node;

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
        addWriter(ArrayType.class, new CPlusPlusASTWriter(this) {
            @Override
            public void write(ASTNode node) {
                ArrayType arrayType = (ArrayType) node;

                write("Array<");
                writeNode(arrayType.getElementType());
                skipSpaceAndComments();
                write(">");

                match("[");
                skipSpaceAndComments();

                match("]");
            }
        });

        // Primitive type
        addWriter(PrimitiveType.class, new CPlusPlusASTWriter(this) {
            @Override
            public void write(ASTNode node) {
                PrimitiveType primitiveType = (PrimitiveType) node;

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
        addWriter(Block.class, new CPlusPlusASTWriter(this) {
            @SuppressWarnings("unchecked")
            @Override
            public void write(ASTNode node) {
                Block block = (Block) node;

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
        addWriter(EmptyStatement.class, new CPlusPlusASTWriter(this) {
            @Override
            public void write(ASTNode node) {
                matchAndWrite(";");
            }
        });

        // Expression statement
        addWriter(ExpressionStatement.class, new CPlusPlusASTWriter(this) {
            @Override
            public void write(ASTNode node) {
                ExpressionStatement expressionStatement = (ExpressionStatement) node;

                writeNode(expressionStatement.getExpression());

                copySpaceAndComments();
                matchAndWrite(";");
            }
        });

        // If statement
        addWriter(IfStatement.class, new CPlusPlusASTWriter(this) {
            @Override
            public void write(ASTNode node) {
                IfStatement ifStatement = (IfStatement) node;

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
        addWriter(WhileStatement.class, new CPlusPlusASTWriter(this) {
            @Override
            public void write(ASTNode node) {
                WhileStatement whileStatement = (WhileStatement) node;

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
        addWriter(DoStatement.class, new CPlusPlusASTWriter(this) {
            @Override
            public void write(ASTNode node) {
                DoStatement doStatement = (DoStatement) node;

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
        addWriter(ContinueStatement.class, new CPlusPlusASTWriter(this) {
            @Override
            public void write(ASTNode node) {
                ContinueStatement continueStatement = (ContinueStatement) node;

                if (continueStatement.getLabel() != null)
                    throw sourceNotSupported("continue statement with a label isn't supported as that construct doesn't exist in C++; change the code to not use a label");

                matchAndWrite("continue");

                copySpaceAndComments();
                matchAndWrite(";");
            }
        });

        // Break statement
        addWriter(BreakStatement.class, new CPlusPlusASTWriter(this) {
            @Override
            public void write(ASTNode node) {
                BreakStatement breakStatement = (BreakStatement) node;

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
        addWriter(ReturnStatement.class, new CPlusPlusASTWriter(this) {
            @Override
            public void write(ASTNode node) {
                ReturnStatement returnStatement = (ReturnStatement) node;

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
        addWriter(ThrowStatement.class, new CPlusPlusASTWriter(this) {
            @Override
            public void write(ASTNode node) {
                ThrowStatement throwStatement = (ThrowStatement) node;

                matchAndWrite("throw");
                copySpaceAndComments();

                writeNode(throwStatement.getExpression());
                copySpaceAndComments();

                matchAndWrite(";");
            }
        });

        // Delegating constructor invocation
        addWriter(ConstructorInvocation.class, new CPlusPlusASTWriter(this) {
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
        addWriter(PrefixExpression.class, new CPlusPlusASTWriter(this) {
            @Override
            public void write(ASTNode node) {
                PrefixExpression prefixExpression = (PrefixExpression) node;

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
        addWriter(PostfixExpression.class, new CPlusPlusASTWriter(this) {
            @Override
            public void write(ASTNode node) {
                PostfixExpression postfixExpression = (PostfixExpression) node;

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
        addWriter(InstanceofExpression.class, new CPlusPlusASTWriter(this) {
            @Override
            public void write(ASTNode node) {
                InstanceofExpression instanceofExpression = (InstanceofExpression) node;

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
        addWriter(ConditionalExpression.class, new CPlusPlusASTWriter(this) {
            @Override
            public void write(ASTNode node) {
                ConditionalExpression conditionalExpression = (ConditionalExpression) node;

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
        addWriter(ThisExpression.class, new CPlusPlusASTWriter(this) {
            @Override
            public void write(ASTNode node) {
                ThisExpression thisExpression = (ThisExpression) node;

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
        addWriter(FieldAccess.class, new CPlusPlusASTWriter(this) {
            @Override
            public void write(ASTNode node) {
                FieldAccess fieldAccess = (FieldAccess) node;

                writeNode(fieldAccess.getExpression());
                copySpaceAndComments();

                matchAndWrite(".", "->");

                writeNode(fieldAccess.getName());
            }
        });

        // Array access
        addWriter(ArrayAccess.class, new CPlusPlusASTWriter(this) {
            @Override
            public void write(ASTNode node) {
                ArrayAccess arrayAccess = (ArrayAccess) node;

                writeNode(arrayAccess.getArray());
                copySpaceAndComments();

                matchAndWrite("[");
                copySpaceAndComments();

                writeNode(arrayAccess.getIndex());
                copySpaceAndComments();

                matchAndWrite("]");
            }
        });

        // Qualfied name
        addWriter(QualifiedName.class, new CPlusPlusASTWriter(this) {
            @Override
            public void write(ASTNode node) {
                QualifiedName qualifiedName = (QualifiedName) node;

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
        addWriter(ParenthesizedExpression.class, new CPlusPlusASTWriter(this) {
            @Override
            public void write(ASTNode node) {
                ParenthesizedExpression parenthesizedExpression = (ParenthesizedExpression) node;

                matchAndWrite("(");
                copySpaceAndComments();

                writeNode(parenthesizedExpression.getExpression());
                copySpaceAndComments();

                matchAndWrite(")");
            }
        });

        // Cast expression
        addWriter(CastExpression.class, new CPlusPlusASTWriter(this) {
            @Override
            public void write(ASTNode node) {
                CastExpression castExpression = (CastExpression) node;

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
        addWriter(NumberLiteral.class, new CPlusPlusASTWriter(this) {
            @Override
            public void write(ASTNode node) {
                NumberLiteral numberLiteral = (NumberLiteral) node;
                matchAndWrite(numberLiteral.getToken());
            }
        });

        // Boolean literal
        addWriter(BooleanLiteral.class, new CPlusPlusASTWriter(this) {
            @Override
            public void write(ASTNode node) {
                BooleanLiteral booleanLiteral = (BooleanLiteral) node;
                matchAndWrite(booleanLiteral.booleanValue() ? "true" : "false");
            }
        });

        // Character literal
        addWriter(CharacterLiteral.class, new CPlusPlusASTWriter(this) {
            @Override
            public void write(ASTNode node) {
                CharacterLiteral characterLiteral = (CharacterLiteral) node;

                // TODO: Map character escape sequences
                matchAndWrite(characterLiteral.getEscapedValue());
            }
        });

        // Null literal
        addWriter(NullLiteral.class, new CPlusPlusASTWriter(this) {
            @Override
            public void write(ASTNode node) {
                matchAndWrite("null", "NULL");
            }
        });

        // String literal
        addWriter(StringLiteral.class, new CPlusPlusASTWriter(this) {
            @Override
            public void write(ASTNode node) {
                StringLiteral stringLiteral = (StringLiteral) node;

                write("new String(" + stringLiteral.getEscapedValue() + "L)");
                match(stringLiteral.getEscapedValue());
            }
        });
    }

    @Override public CPlusPlusTranslator getTranslator() {
        return cPlusPlusTranslator;
    }
}
