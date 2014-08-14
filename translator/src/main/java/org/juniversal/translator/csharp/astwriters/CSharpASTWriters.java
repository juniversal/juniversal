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

import org.eclipse.jdt.core.dom.*;
import org.juniversal.translator.core.ASTWriter;
import org.juniversal.translator.core.ASTWriters;
import org.juniversal.translator.core.Context;
import org.juniversal.translator.core.JUniversalException;
import org.juniversal.translator.swift.astwriters.ASTWriterUtil;

import java.util.List;


public class CSharpASTWriters extends ASTWriters {
    public CSharpASTWriters() {
        addDeclarationWriters();

        addStatementWriters();

        addExpressionWriters();

        // TODO: Implement this
        // Simple name
        addWriter(SimpleName.class, new ASTWriter() {
            @Override
            public void write(ASTNode node, Context context) {
                SimpleName simpleName = (SimpleName) node;

                context.matchAndWrite(simpleName.getIdentifier());
            }
        });
    }

    /**
     * Add visitors for class, method, field, and type declarations.
     */
    private void addDeclarationWriters() {
        // TODO: Implement this
        // Compilation unit
        addWriter(CompilationUnit.class, new CompilationUnitWriter(this));

        // TODO: Implement this
        // Type (class/interface) declaration
        addWriter(TypeDeclaration.class, new TypeDeclarationWriter(this));

        // TODO: Implement this
        // Method declaration (which includes implementation)
        addWriter(MethodDeclaration.class, new MethodDeclarationWriter(this));

        // TODO: Implement this
        // Field declaration
        //addWriter(FieldDeclaration.class, new FieldDeclarationWriter(this));

        // TODO: Implement this
        // Variable declaration fragment
        addWriter(VariableDeclarationFragment.class, new ASTWriter() {
            @Override
            public void write(ASTNode node, Context context) {
                VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) node;

                // TODO: Handle syntax with extra dimensions on array
                if (variableDeclarationFragment.getExtraDimensions() > 0)
                    context.throwSourceNotSupported("\"int foo[]\" syntax not currently supported; use \"int[] foo\" instead");

                if (context.isWritingVariableDeclarationNeedingStar())
                    context.write("*");

                writeNode(variableDeclarationFragment.getName(), context);

                Expression initializer = variableDeclarationFragment.getInitializer();
                if (initializer != null) {
                    context.copySpaceAndComments();
                    context.matchAndWrite("=");

                    context.copySpaceAndComments();
                    writeNode(initializer, context);
                }
            }
        });

        // TODO: Implement this
        // Single variable declaration (used in parameter list & catch clauses)
        addWriter(SingleVariableDeclaration.class, new ASTWriter() {
            @Override
            public void write(ASTNode node, Context context) {
                SingleVariableDeclaration singleVariableDeclaration = (SingleVariableDeclaration) node;

                // TODO: Handle syntax with extra dimensions on array
                if (singleVariableDeclaration.getExtraDimensions() > 0)
                    context.throwSourceNotSupported("\"int foo[]\" syntax not currently supported; use \"int[] foo\" instead");

                // TODO: Handle final & varargs

                SimpleName name = singleVariableDeclaration.getName();
                context.setPositionToStartOfNode(name);
                writeNode(name, context);
                context.write(": ");

                int endOfNamePosition = context.getPosition();

                Type type = singleVariableDeclaration.getType();
                context.setPositionToStartOfNode(type);
                writeNode(type, context);

                context.setPosition(endOfNamePosition);

                // TODO: Handle initializer
                Expression initializer = singleVariableDeclaration.getInitializer();
                if (initializer != null)
                    throw new JUniversalException("Unexpected initializer present for SingleVariableDeclaration");
            }
        });

        // TODO: Implement this
        // Simple type
        addWriter(SimpleType.class, new ASTWriter() {
            @Override
            public void write(ASTNode node, Context context) {
                SimpleType simpleType = (SimpleType) node;

                Name name = simpleType.getName();
                if (name instanceof QualifiedName) {
                    QualifiedName qualifiedName = (QualifiedName) name;

                    context.write(ASTWriterUtil.getNamespaceNameForPackageName(qualifiedName.getQualifier()));
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

        // TODO: Implement this
        // Parameterized type
        addWriter(ParameterizedType.class, new ASTWriter() {
            @Override
            public void write(ASTNode node, Context context) {
                ParameterizedType parameterizedType = (ParameterizedType) node;

                writeNode(parameterizedType.getType(), context);

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
                    writeNode(typeArgument, context);

                    first = false;
                }

                context.matchAndWrite(">");
            }
        });

        // TODO: Implement this
        // Array type
        addWriter(ArrayType.class, new ASTWriter() {
            @Override
            public void write(ASTNode node, Context context) {
                ArrayType arrayType = (ArrayType) node;

                context.write("Array<");
                writeNode(arrayType.getElementType(), context);
                context.skipSpaceAndComments();
                context.write(">");

                context.match("[");
                context.skipSpaceAndComments();

                context.match("]");
            }
        });

        // Primitive type
        addWriter(PrimitiveType.class, new ASTWriter() {
            @Override
            public void write(ASTNode node, Context context) {
                PrimitiveType primitiveType = (PrimitiveType) node;

                PrimitiveType.Code code = primitiveType.getPrimitiveTypeCode();
                if (code == PrimitiveType.BYTE)
                    context.matchAndWrite("byte", "Int8");
                else if (code == PrimitiveType.SHORT)
                    context.matchAndWrite("short", "Int16");
                else if (code == PrimitiveType.CHAR)
                    context.matchAndWrite("char", "Character");
                    // TODO: For now, map 32 bit Java int to Int type in Swift, which can be 32 or 64 bits.
                    // Later probably add @PreserveJavaIntSemantics annotation to force 32 bit + no overflow checking
                else if (code == PrimitiveType.INT)
                    context.matchAndWrite("int", "Int");
                else if (code == PrimitiveType.LONG) {
                    context.matchAndWrite("long", "Int64");
                } else if (code == PrimitiveType.FLOAT)
                    context.matchAndWrite("float", "Float");
                else if (code == PrimitiveType.DOUBLE)
                    context.matchAndWrite("double", "Double");
                else if (code == PrimitiveType.BOOLEAN)
                    context.matchAndWrite("boolean", "Bool");
                else if (code == PrimitiveType.VOID)
                    throw new JUniversalException("Should detect void return types before it gets here");
                else
                    context.throwInvalidAST("Unknown primitive type: " + code);
            }
        });
    }

    /**
     * Add visitors for the different kinds of statements.
     */
    private void addStatementWriters() {
        // TODO: Implement this
        // Block
        addWriter(Block.class, new ASTWriter() {
            @SuppressWarnings("unchecked")
            @Override
            public void write(ASTNode node, Context context) {
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
                        writeNode(statement, context);
                    }

                    firstStatement = false;
                }

                context.copySpaceAndComments();
                context.matchAndWrite("}");
            }
        });

        // TODO: Implement this
        // Empty statement (";")
        addWriter(EmptyStatement.class, new ASTWriter() {
            @Override
            public void write(ASTNode node, Context context) {
                context.matchAndWrite(";");
            }
        });

        // TODO: Implement this
        // Expression statement
        addWriter(ExpressionStatement.class, new ASTWriter() {
            @Override
            public void write(ASTNode node, Context context) {
                ExpressionStatement expressionStatement = (ExpressionStatement) node;

                writeNode(expressionStatement.getExpression(), context);

                context.copySpaceAndComments();
                context.matchAndWrite(";");
            }
        });

        // TODO: Implement this
        // If statement
        addWriter(IfStatement.class, new ASTWriter() {
            @Override
            public void write(ASTNode node, Context context) {
                IfStatement ifStatement = (IfStatement) node;

                int ifColumn = context.getTargetColumn();
                context.matchAndWrite("if");

                context.copySpaceAndCommentsEnsuringDelimiter();

                writeConditionNoParens(ifStatement.getExpression(), context);

                writeStatementEnsuringBraces(ifColumn, false, ifStatement.getThenStatement(), context);

                Statement elseStatement = ifStatement.getElseStatement();
                if (elseStatement != null) {
                    context.copySpaceAndComments();

                    context.matchAndWrite("else");
                    writeStatementEnsuringBraces(ifColumn, true, ifStatement.getElseStatement(), context);
                }
            }
        });

        // TODO: Implement this
        // While statement
        addWriter(WhileStatement.class, new ASTWriter() {
            @Override
            public void write(ASTNode node, Context context) {
                WhileStatement whileStatement = (WhileStatement) node;

                int whileColumn = context.getTargetColumn();
                context.matchAndWrite("while");

                context.copySpaceAndCommentsEnsuringDelimiter();

                writeConditionNoParens(whileStatement.getExpression(), context);

                writeStatementEnsuringBraces(whileColumn, false, whileStatement.getBody(), context);
            }
        });

        // TODO: Implement this
        // Do while statement
        addWriter(DoStatement.class, new ASTWriter() {
            @Override
            public void write(ASTNode node, Context context) {
                DoStatement doStatement = (DoStatement) node;

                int doColumn = context.getTargetColumn();
                context.matchAndWrite("do");

                writeStatementEnsuringBraces(doColumn, false, doStatement.getBody(), context);

                context.copySpaceAndComments();
                context.matchAndWrite("while");

                context.copySpaceAndComments();
                writeConditionNoParens(doStatement.getExpression(), context);

                context.copySpaceAndComments();
                context.matchAndWrite(";");
            }
        });

        // TODO: Implement this
        // Continue statement
        addWriter(ContinueStatement.class, new ASTWriter() {
            @Override
            public void write(ASTNode node, Context context) {
                ContinueStatement continueStatement = (ContinueStatement) node;

                if (continueStatement.getLabel() != null)
                    context.throwSourceNotSupported("continue statement with a label isn't supported as that construct doesn't exist in C++; change the code to not use a label");

                context.matchAndWrite("continue");

                context.copySpaceAndComments();
                context.matchAndWrite(";");
            }
        });

        // TODO: Implement this
        // Break statement
        addWriter(BreakStatement.class, new ASTWriter() {
            @Override
            public void write(ASTNode node, Context context) {
                BreakStatement breakStatement = (BreakStatement) node;

                if (breakStatement.getLabel() != null)
                    context.throwSourceNotSupported("break statement with a label isn't supported as that construct doesn't exist in C++; change the code to not use a label");

                context.matchAndWrite("break");

                context.copySpaceAndComments();
                context.matchAndWrite(";");
            }
        });

        // TODO: Implement this
        // For statement
/*
        addWriter(ForStatement.class, new ForStatementWriter(this));
*/

        // TODO: Implement this
        // Return statement
        addWriter(ReturnStatement.class, new ASTWriter() {
            @Override
            public void write(ASTNode node, Context context) {
                ReturnStatement returnStatement = (ReturnStatement) node;

                context.matchAndWrite("return");

                Expression expression = returnStatement.getExpression();
                if (expression != null) {
                    context.copySpaceAndComments();
                    writeNode(returnStatement.getExpression(), context);
                }

                context.copySpaceAndComments();
                context.matchAndWrite(";");
            }
        });

        // TODO: Implement this
        // Local variable declaration statement
/*
        addWriter(VariableDeclarationStatement.class, new VariableDeclarationWriter(this));
*/

        // TODO: Implement this
        // Throw statement
        addWriter(ThrowStatement.class, new ASTWriter() {
            @Override
            public void write(ASTNode node, Context context) {
                ThrowStatement throwStatement = (ThrowStatement) node;

                context.matchAndWrite("throw");
                context.copySpaceAndComments();

                writeNode(throwStatement.getExpression(), context);
                context.copySpaceAndComments();

                context.matchAndWrite(";");
            }
        });

        // TODO: Implement this
        // Delegating constructor invocation
        addWriter(ConstructorInvocation.class, new ASTWriter() {
            @Override
            public void write(ASTNode node, Context context) {
                context.throwSourceNotSupported("Delegating constructors aren't currently supported; for now you have to change the code to not use them (e.g. by adding an init method)");
            }
        });
    }

    /**
     * Add visitors for the different kinds of expressions.
     */
    private void addExpressionWriters() {

        // TODO: Implement this
        // Assignment expression
/*
        addWriter(Assignment.class, new AssignmentWriter(this));
*/

        // TODO: Implement this
        // Method invocation
/*
        addWriter(MethodInvocation.class, new MethodInvocationWriter(this));
*/

        // TODO: Implement this
        // Super Method invocation
/*
        addWriter(SuperMethodInvocation.class, new MethodInvocationWriter(this));
*/

        // TODO: Implement this
        // Class instance creation
/*
        addWriter(ClassInstanceCreation.class, new ClassInstanceCreationWriter(this));
*/

        // TODO: Implement this
        // Array creation
/*
        addWriter(ArrayCreation.class, new ArrayCreationWriter(this));
*/

        // TODO: Implement this
        // Variable declaration expression (used in a for statement)
/*
        addWriter(VariableDeclarationExpression.class, new VariableDeclarationWriter(this));
*/

        // TODO: Implement this
        // Infix expression
/*
        addWriter(InfixExpression.class, new InfixExpressionWriter(this));
*/

        // Prefix expression
        addWriter(PrefixExpression.class, new ASTWriter() {
            @Override
            public void write(ASTNode node, Context context) {
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

                // In Swift there can't be any whitespace or comments between a unary prefix operator & its operand, so
                // strip it, not copying anything here
                context.skipSpaceAndComments();

                writeNode(prefixExpression.getOperand(), context);
            }
        });

        // Postfix expression
        addWriter(PostfixExpression.class, new ASTWriter() {
            @Override
            public void write(ASTNode node, Context context) {
                PostfixExpression postfixExpression = (PostfixExpression) node;

                writeNode(postfixExpression.getOperand(), context);

                // In Swift there can't be any whitespace or comments between a postfix operator & its operand, so
                // strip it, not copying anything here
                context.skipSpaceAndComments();

                PostfixExpression.Operator operator = postfixExpression.getOperator();
                if (operator == PostfixExpression.Operator.INCREMENT)
                    context.matchAndWrite("++");
                else if (operator == PostfixExpression.Operator.DECREMENT)
                    context.matchAndWrite("--");
                else context.throwInvalidAST("Unknown postfix operator type: " + operator);
            }
        });

        // TODO: Implement this
        // instanceof expression
        addWriter(InstanceofExpression.class, new ASTWriter() {
            @Override
            public void write(ASTNode node, Context context) {
                InstanceofExpression instanceofExpression = (InstanceofExpression) node;

                context.write("INSTANCEOF(");

                Expression expression = instanceofExpression.getLeftOperand();
                writeNode(expression, context);

                context.skipSpaceAndComments();
                context.match("instanceof");

                context.skipSpaceAndComments();
                Type type = instanceofExpression.getRightOperand();
                writeNode(type, context);

                context.write(")");
            }
        });

        // conditional expression
        addWriter(ConditionalExpression.class, new ASTWriter() {
            @Override
            public void write(ASTNode node, Context context) {
                ConditionalExpression conditionalExpression = (ConditionalExpression) node;

                writeNode(conditionalExpression.getExpression(), context);

                context.copySpaceAndComments();
                context.matchAndWrite("?");

                context.copySpaceAndComments();
                writeNode(conditionalExpression.getThenExpression(), context);

                context.copySpaceAndComments();
                context.matchAndWrite(":");

                context.copySpaceAndComments();
                writeNode(conditionalExpression.getElseExpression(), context);
            }
        });

        // TODO: Implement this
        // this
        addWriter(ThisExpression.class, new ASTWriter() {
            @Override
            public void write(ASTNode node, Context context) {
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

        // TODO: Implement this
        // Field access
        addWriter(FieldAccess.class, new ASTWriter() {
            @Override
            public void write(ASTNode node, Context context) {
                FieldAccess fieldAccess = (FieldAccess) node;

                writeNode(fieldAccess.getExpression(), context);
                context.copySpaceAndComments();

                context.matchAndWrite(".", "->");

                writeNode(fieldAccess.getName(), context);
            }
        });

        // TODO: Implement this
        // Array access
        addWriter(ArrayAccess.class, new ASTWriter() {
            @Override
            public void write(ASTNode node, Context context) {
                ArrayAccess arrayAccess = (ArrayAccess) node;

                writeNode(arrayAccess.getArray(), context);
                context.copySpaceAndComments();

                context.matchAndWrite("[");
                context.copySpaceAndComments();

                writeNode(arrayAccess.getIndex(), context);
                context.copySpaceAndComments();

                context.matchAndWrite("]");
            }
        });

        // TODO: Implement this
        // Qualified name
        addWriter(QualifiedName.class, new ASTWriter() {
            @Override
            public void write(ASTNode node, Context context) {
                QualifiedName qualifiedName = (QualifiedName) node;

                // TODO: Figure out the other cases where this can occur & make them all correct

                // Here assume that a QualifiedName refers to field access; if it refers to a type,
                // the caller should catch that case itself and ensure it never gets here

                writeNode(qualifiedName.getQualifier(), context);
                context.copySpaceAndComments();

                context.matchAndWrite(".", "->");

                writeNode(qualifiedName.getName(), context);
            }
        });

        // TODO: Implement this
        // Parenthesized expression
        addWriter(ParenthesizedExpression.class, new ASTWriter() {
            @Override
            public void write(ASTNode node, Context context) {
                ParenthesizedExpression parenthesizedExpression = (ParenthesizedExpression) node;

                context.matchAndWrite("(");
                context.copySpaceAndComments();

                writeNode(parenthesizedExpression.getExpression(), context);
                context.copySpaceAndComments();

                context.matchAndWrite(")");
            }
        });

        // TODO: Implement this
        // Cast expression
        addWriter(CastExpression.class, new ASTWriter() {
            @Override
            public void write(ASTNode node, Context context) {
                CastExpression castExpression = (CastExpression) node;

                context.matchAndWrite("(", "static_cast<");

                context.copySpaceAndComments();
                writeNode(castExpression.getType(), context);

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
                writeNode(castExpression.getExpression(), context);
                if (needParentheses)
                    context.write(")");
            }
        });

        // Number literal
        addWriter(NumberLiteral.class, new ASTWriter() {
            @Override
            public void write(ASTNode node, Context context) {
                NumberLiteral numberLiteral = (NumberLiteral) node;
                String token = numberLiteral.getToken();

                // Strip out any _ separators in the number, as those aren't supported in C# (at least not until the
                // new C# 6 comes out)
                String convertedToken = token.replace("_", "");

                // TODO: Support binary (and octal) literals by converting to hex
                if (token.length() >= 2 && token.startsWith("0")) {
                    char secondChar = token.charAt(1);
                    if (secondChar >= '0' && secondChar <= '7') {
                        context.throwSourceNotSupported("Octal literals aren't currently supported; change the source to use hex instead");
                    } else if ((secondChar == 'b') || (secondChar == 'B')) {
                        context.throwSourceNotSupported("Binary literals aren't currently supported; change the source to use hex instead");
                    }
                }

                context.write(convertedToken);
                context.match(token);
            }
        });

        // TODO: Implement this
        // Boolean literal
        addWriter(BooleanLiteral.class, new ASTWriter() {
            @Override
            public void write(ASTNode node, Context context) {
                BooleanLiteral booleanLiteral = (BooleanLiteral) node;
                context.matchAndWrite(booleanLiteral.booleanValue() ? "true" : "false");
            }
        });

        // TODO: Implement this
        // Character literal
        addWriter(CharacterLiteral.class, new ASTWriter() {
            @Override
            public void write(ASTNode node, Context context) {
                CharacterLiteral characterLiteral = (CharacterLiteral) node;

                // TODO: Map character escape sequences
                // TODO: Handle conversion of characters to integers where that's normally implicit in Java

                context.write("Character(\"" + characterLiteral.charValue() + "\")");
                context.match(characterLiteral.getEscapedValue());
            }
        });

        // TODO: Implement this
        // Null literal
        addWriter(NullLiteral.class, new ASTWriter() {
            @Override
            public void write(ASTNode node, Context context) {
                context.matchAndWrite("null");
            }
        });

        // TODO: Implement this
        // String literal
        addWriter(StringLiteral.class, new ASTWriter() {
            @Override
            public void write(ASTNode node, Context context) {
                StringLiteral stringLiteral = (StringLiteral) node;

                context.write("new String(" + stringLiteral.getEscapedValue() + "L)");
                context.match(stringLiteral.getEscapedValue());
            }
        });
    }

    private void writeConditionNoParens(Expression expression, Context context) {
        context.match("(");
        context.skipSpaceAndComments();

        writeNode(expression, context);

        context.skipSpaceAndComments();
        context.match(")");
    }

    private void writeStatementEnsuringBraces(int blockStartColumn, boolean forceSeparateLine,
                                              Statement statement, Context context) {
        if (statement instanceof Block) {
            context.copySpaceAndComments();
            writeNode(statement, context);
        } else {
            if (context.startsOnSameLine(statement)) {
                if (forceSeparateLine) {
                    context.write(" {\n");

                    context.writeSpacesUntilColumn(blockStartColumn);
                    context.writeSpaces(context.getPreferredIndent());
                    context.skipSpacesAndTabs();

                    context.copySpaceAndComments();

                    writeNode(statement, context);

                    context.copySpaceAndCommentsUntilEOL();
                    context.setKnowinglyProcessedTrailingSpaceAndComments(true);
                    context.writeln();

                    context.writeSpacesUntilColumn(blockStartColumn);
                    context.write("}");
                } else {
                    context.copySpaceAndCommentsEnsuringDelimiter();

                    context.write("{ ");
                    writeNode(statement, context);
                    context.write(" }");
                }
            } else {
                context.write(" {");

                context.copySpaceAndComments();

                writeNode(statement, context);

                context.copySpaceAndCommentsUntilEOL();
                context.setKnowinglyProcessedTrailingSpaceAndComments(true);
                context.writeln();

                context.writeSpacesUntilColumn(blockStartColumn);
                context.write("}");
            }
        }
    }

    /**
     * Write out a type, when it's used (as opposed to defined).
     *
     * @param type    type to write
     * @param context context
     */
    public void writeType(Type type, Context context, boolean useRawPointer) {
        boolean referenceType = !type.isPrimitiveType();

        if (!referenceType)
            writeNode(type, context);
        else {
            if (useRawPointer) {
                writeNode(type, context);
                context.write("*");
            } else {
                context.write("ptr< ");
                writeNode(type, context);
                context.write(" >");
            }
        }
    }
}
