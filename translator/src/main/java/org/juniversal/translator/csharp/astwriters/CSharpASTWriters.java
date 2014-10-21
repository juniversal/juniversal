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
import org.jetbrains.annotations.Nullable;
import org.juniversal.translator.core.ASTWriters;
import org.juniversal.translator.core.Context;
import org.juniversal.translator.core.JUniversalException;
import org.juniversal.translator.csharp.CSharpTranslator;

import java.util.ArrayList;
import java.util.List;


public class CSharpASTWriters extends ASTWriters {
    private CSharpTranslator cSharpTranslator;

    public CSharpASTWriters(Context context, CSharpTranslator cSharpTranslator) {
        super(context);

        this.cSharpTranslator = cSharpTranslator;

        addDeclarationWriters();
        addStatementWriters();
        addExpressionWriters();

        // TODO: Implement this
        // Simple name
        addWriter(SimpleName.class, new CSharpASTWriter<SimpleName>(this) {
            @Override
            public void write(Context context, SimpleName simpleName) {
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
        // Type (class/interface) declaration
        addWriter(EnumDeclaration.class, new EnumDeclarationWriter(this));

        // TODO: Implement this
        // Method declaration (which includes implementation)
        addWriter(MethodDeclaration.class, new MethodDeclarationWriter(this));

        // Field declaration
        addWriter(FieldDeclaration.class, new FieldDeclarationWriter(this));

        // TODO: Implement this
        // Variable declaration fragment
        addWriter(VariableDeclarationFragment.class, new CSharpASTWriter<VariableDeclarationFragment>(this) {
            @Override
            public void write(Context context, VariableDeclarationFragment variableDeclarationFragment) {
                // TODO: Handle syntax with extra dimensions on array
                if (variableDeclarationFragment.getExtraDimensions() > 0)
                    context.throwSourceNotSupported("\"int foo[]\" array type syntax not currently supported; use \"int[] foo\" instead");

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

        // TODO: Implement this
        // Single variable declaration (used in parameter list, catch clauses, and enhanced for statements)
        addWriter(SingleVariableDeclaration.class, new CSharpASTWriter<SingleVariableDeclaration>(this) {
            @Override
            public void write(Context context, SingleVariableDeclaration singleVariableDeclaration) {
                // TODO: Handle syntax with extra dimensions on array
                if (singleVariableDeclaration.getExtraDimensions() > 0)
                    context.throwSourceNotSupported("\"int foo[]\" array type syntax not currently supported; use \"int[] foo\" instead");

                List<?> modifiers = singleVariableDeclaration.modifiers();
                context.ensureModifiersJustFinalOrAnnotations(modifiers);
                context.skipModifiers(modifiers);

                Type type = singleVariableDeclaration.getType();

                if (singleVariableDeclaration.isVarargs()) {
                    context.write("params ");
                    writeNode(context, type);

                    context.copySpaceAndComments();
                    // TODO: Think through & handle all cases where a regular type is converted to an array here
                    context.matchAndWrite("...", "[]");
                } else writeNode(context, type);

                context.copySpaceAndComments();
                SimpleName name = singleVariableDeclaration.getName();
                writeNode(context, name);

                // TODO: Handle initializer
                Expression initializer = singleVariableDeclaration.getInitializer();
                if (initializer != null)
                    throw new JUniversalException("Unexpected initializer present for SingleVariableDeclaration");
            }
        });

        // TODO: Implement this
        // Simple type
        addWriter(SimpleType.class, new CSharpASTWriter<SimpleType>(this) {
            @Override
            public void write(Context context, SimpleType simpleType) {
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

        // TODO: Implement this
        // Parameterized type
        addWriter(ParameterizedType.class, new CSharpASTWriter<ParameterizedType>(this) {
            @Override
            public void write(Context context, ParameterizedType parameterizedType) {
                writeNode(context, parameterizedType.getType());

                context.copySpaceAndComments();
                context.matchAndWrite("<");

                writeCommaDelimitedNodes(context, parameterizedType.typeArguments());

                context.copySpaceAndComments();
                context.matchAndWrite(">");
            }
        });

        addWriter(WildcardType.class, new CSharpASTWriter<WildcardType>(this) {
            @Override
            public void write(Context context, WildcardType wildcardType) {
                ArrayList<WildcardType> wildcardTypes = context.getMethodWildcardTypes();
                if (wildcardTypes == null)
                    context.throwSourceNotSupported("Wildcard types (that is, ?) only supported in method parameters and return types.  You may want to change the Java source to use an explicitly named generic type instead of a wildcard here.");

                int index = wildcardTypes.indexOf(wildcardType);
                if (index == -1)
                    throw new JUniversalException("Wildcard type not found in list");

                context.write("TWildCard" + (index + 1));
                context.setPositionToEndOfNode(wildcardType);
            }
        });

        // Array type
        addWriter(ArrayType.class, new CSharpASTWriter<ArrayType>(this) {
            @Override
            public void write(Context context, ArrayType arrayType) {
                writeNode(context, arrayType.getElementType());

                for (Object dimensionObject : arrayType.dimensions()) {
                    Dimension dimension = (Dimension) dimensionObject;

                    context.copySpaceAndComments();
                    context.match("[");

                    context.copySpaceAndComments();
                    context.match("]");
                }
            }
        });

        // TODO: Implement this
        // Primitive type
        addWriter(PrimitiveType.class, new CSharpASTWriter<PrimitiveType>(this) {
            @Override
            public void write(Context context, PrimitiveType primitiveType) {
                PrimitiveType.Code code = primitiveType.getPrimitiveTypeCode();
                if (code == PrimitiveType.BYTE)
                    context.matchAndWrite("byte", "sbyte");
                else if (code == PrimitiveType.SHORT)
                    context.matchAndWrite("short");
                else if (code == PrimitiveType.CHAR)
                    context.matchAndWrite("char");
                else if (code == PrimitiveType.INT)
                    context.matchAndWrite("int");
                else if (code == PrimitiveType.LONG)
                    context.matchAndWrite("long");
                else if (code == PrimitiveType.FLOAT)
                    context.matchAndWrite("float");
                else if (code == PrimitiveType.DOUBLE)
                    context.matchAndWrite("double");
                else if (code == PrimitiveType.BOOLEAN)
                    context.matchAndWrite("boolean", "bool");
                else if (code == PrimitiveType.VOID)
                    context.matchAndWrite("void", "void");
                else
                    context.throwInvalidAST("Unknown primitive type: " + code);
            }
        });

        // Array initializer
        addWriter(ArrayInitializer.class, new CSharpASTWriter<ArrayInitializer>(this) {
            @Override
            public void write(Context context, ArrayInitializer arrayInitializer) {
                context.matchAndWrite("{");

                // TODO: Check that number of expressions matches array size (I think, as I think C# requires exact number and Java allows less)
                writeCommaDelimitedNodes(context, arrayInitializer.expressions());
                // TODO: Skip extra trailing commas here

                context.copySpaceAndComments();
                context.matchAndWrite("}");
            }
        });
    }

    /**
     * Add visitors for the different kinds of statements.
     */
    private void addStatementWriters() {
        // TODO: Implement this
        // Block
        addWriter(Block.class, new CSharpASTWriter<Block>(this) {
            @Override
            public void write(Context context, Block block) {
                context.matchAndWrite("{");

                writeNodes(context, block.statements());

                context.copySpaceAndComments();
                context.matchAndWrite("}");
            }
        });

        // TODO: Implement this
        // Empty statement (";")
        addWriter(EmptyStatement.class, new CSharpASTWriter<EmptyStatement>(this) {
            @Override
            public void write(Context context, EmptyStatement emptyStatement) {
                context.matchAndWrite(";");
            }
        });

        // TODO: Implement this
        // Expression statement
        addWriter(ExpressionStatement.class, new CSharpASTWriter<ExpressionStatement>(this) {
            @Override
            public void write(Context context, ExpressionStatement expressionStatement) {
                writeNode(context, expressionStatement.getExpression());

                context.copySpaceAndComments();
                context.matchAndWrite(";");
            }
        });

        // If statement
        addWriter(IfStatement.class, new CSharpASTWriter<IfStatement>(this) {
            @Override
            public void write(Context context, IfStatement ifStatement) {
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
        addWriter(WhileStatement.class, new CSharpASTWriter<WhileStatement>(this) {
            @Override
            public void write(Context context, WhileStatement whileStatement) {
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
        addWriter(DoStatement.class, new CSharpASTWriter<DoStatement>(this) {
            @Override
            public void write(Context context, DoStatement doStatement) {
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

        // TODO: Implement this
        // Continue statement
        addWriter(ContinueStatement.class, new CSharpASTWriter<ContinueStatement>(this) {
            @Override
            public void write(Context context, ContinueStatement continueStatement) {
                if (continueStatement.getLabel() != null)
                    context.throwSourceNotSupported("continue statement with a label isn't supported as that construct doesn't exist in C++; change the code to not use a label");

                context.matchAndWrite("continue");

                context.copySpaceAndComments();
                context.matchAndWrite(";");
            }
        });

        // TODO: Implement this
        // Break statement
        addWriter(BreakStatement.class, new CSharpASTWriter<BreakStatement>(this) {
            @Override
            public void write(Context context, BreakStatement breakStatement) {
                if (breakStatement.getLabel() != null)
                    context.throwSourceNotSupported("break statement with a label isn't supported as that construct doesn't exist in C++; change the code to not use a label");

                context.matchAndWrite("break");

                context.copySpaceAndComments();
                context.matchAndWrite(";");
            }
        });

        // TODO: Implement this
        // For statement
        addWriter(ForStatement.class, new ForStatementWriter(this));

        addWriter(EnhancedForStatement.class, new CSharpASTWriter<EnhancedForStatement>(this) {
            @Override
            public void write(Context context, EnhancedForStatement enhancedForStatement) {
                context.matchAndWrite("for", "foreach");

                context.copySpaceAndComments();
                context.matchAndWrite("(");

                context.copySpaceAndComments();
                writeNode(context, enhancedForStatement.getParameter());

                context.copySpaceAndComments();
                context.matchAndWrite(":");

                context.copySpaceAndComments();
                writeNode(context, enhancedForStatement.getExpression());

                context.copySpaceAndComments();
                context.matchAndWrite(")");

                context.copySpaceAndComments();
                writeNode(context, enhancedForStatement.getBody());
            }
        });

        // TODO: Implement this
        // Switch statement
        addWriter(SwitchStatement.class, new SwitchStatementWriter(this));

        // TODO: Implement this
        // Return statement
        addWriter(ReturnStatement.class, new CSharpASTWriter<ReturnStatement>(this) {
            @Override
            public void write(Context context, ReturnStatement returnStatement) {
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
        addWriter(VariableDeclarationStatement.class, new CSharpASTWriter<VariableDeclarationStatement>(this) {
            @Override
            public void write(Context context, VariableDeclarationStatement variableDeclarationStatement) {
                writeVariableDeclaration(context, variableDeclarationStatement.modifiers(),
                        variableDeclarationStatement.getType(),
                        variableDeclarationStatement.fragments());

                context.copySpaceAndComments();
                context.matchAndWrite(";");
            }
        });

        // Try statement
        addWriter(TryStatement.class, new TryStatementWriterTemp(this));

        // TODO: Implement this
        // Throw statement
        addWriter(ThrowStatement.class, new CSharpASTWriter<ThrowStatement>(this) {
            @Override
            public void write(Context context, ThrowStatement throwStatement) {
                context.matchAndWrite("throw");
                context.copySpaceAndComments();

                writeNode(context, throwStatement.getExpression());
                context.copySpaceAndComments();

                context.matchAndWrite(";");
            }
        });

        // TODO: Implement this
        // Delegating constructor invocation
        addWriter(ConstructorInvocation.class, new CSharpASTWriter(this) {
            @Override
            public void write(Context context, ASTNode node) {
                context.throwSourceNotSupported("Delegating constructors aren't currently supported; for now you have to change the code to not use them (e.g. by adding an init method)");
            }
        });

        addWriter(AssertStatement.class, new CSharpASTWriter<AssertStatement>(this) {
            @Override
            public void write(Context context, AssertStatement assertStatement) {
                context.matchAndWrite("assert", "Debug.Assert(");

                context.copySpaceAndComments();
                writeNode(context, assertStatement.getExpression());

                @Nullable Expression message = assertStatement.getMessage();
                if (message != null) {
                    context.skipSpaceAndComments();
                    context.matchAndWrite(":", ",");

                    context.copySpaceAndComments();
                    writeNode(context, message);

                    context.copySpaceAndComments();
                    context.matchAndWrite(";",  ");");
                }
            }
        });
    }

    /**
     * Add visitors for the different kinds of expressions.
     */
    private void addExpressionWriters() {

        // TODO: Implement this
        // Assignment expression
        addWriter(Assignment.class, new AssignmentWriter(this));

        // TODO: Implement this
        // Method invocation
        addWriter(MethodInvocation.class, new MethodInvocationWriter(this));

        // TODO: Implement this
        // Super Method invocation
        addWriter(SuperMethodInvocation.class, new SuperMethodInvocationWriter(this));

        // TODO: Implement this
        // Class instance creation
        addWriter(ClassInstanceCreation.class, new ClassInstanceCreationWriter(this));

        // TODO: Implement this
        // Array creation
        addWriter(ArrayCreation.class, new ArrayCreationWriter(this));

        // TODO: Implement this
        // Variable declaration expression (used in a for statement)
        addWriter(VariableDeclarationExpression.class, new CSharpASTWriter<VariableDeclarationExpression>(this) {
            @Override
            public void write(Context context, VariableDeclarationExpression variableDeclarationExpression) {
                writeVariableDeclaration(context, variableDeclarationExpression.modifiers(),
                        variableDeclarationExpression.getType(),
                        variableDeclarationExpression.fragments());
            }
        });

        // TODO: Implement this
        // Infix expression
        addWriter(InfixExpression.class, new InfixExpressionWriter(this));

        // Prefix expression
        addWriter(PrefixExpression.class, new CSharpASTWriter<PrefixExpression>(this) {
            @Override
            public void write(Context context, PrefixExpression prefixExpression) {
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

                writeNode(context, prefixExpression.getOperand());
            }
        });

        // Postfix expression
        addWriter(PostfixExpression.class, new CSharpASTWriter<PostfixExpression>(this) {
            @Override
            public void write(Context context, PostfixExpression postfixExpression) {
                writeNode(context, postfixExpression.getOperand());

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
        addWriter(InstanceofExpression.class, new CSharpASTWriter<InstanceofExpression>(this) {
            @Override
            public void write(Context context, InstanceofExpression instanceofExpression) {
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
        addWriter(ConditionalExpression.class, new CSharpASTWriter<ConditionalExpression>(this) {
            @Override
            public void write(Context context, ConditionalExpression conditionalExpression) {
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

        // TODO: Implement this
        // this
        addWriter(ThisExpression.class, new CSharpASTWriter<ThisExpression>(this) {
            @Override
            public void write(Context context, ThisExpression thisExpression) {
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
        addWriter(FieldAccess.class, new CSharpASTWriter<FieldAccess>(this) {
            @Override
            public void write(Context context, FieldAccess fieldAccess) {
                writeNode(context, fieldAccess.getExpression());

                context.copySpaceAndComments();
                context.matchAndWrite(".");

                context.copySpaceAndComments();
                writeNode(context, fieldAccess.getName());
            }
        });

        // Array access
        addWriter(ArrayAccess.class, new CSharpASTWriter<ArrayAccess>(this) {
            @Override
            public void write(Context context, ArrayAccess arrayAccess) {
                writeNode(context, arrayAccess.getArray());

                context.copySpaceAndComments();
                context.matchAndWrite("[");

                context.copySpaceAndComments();
                writeNode(context, arrayAccess.getIndex());

                context.copySpaceAndComments();
                context.matchAndWrite("]");
            }
        });

        // Qualified name
        addWriter(QualifiedName.class, new CSharpASTWriter<QualifiedName>(this) {
            @Override
            public void write(Context context, QualifiedName qualifiedName) {
                // TODO: Figure out the other cases where this can occur & make them all correct

                writeNode(context, qualifiedName.getQualifier());

                context.copySpaceAndComments();
                context.matchAndWrite(".");

                context.copySpaceAndComments();
                writeNode(context, qualifiedName.getName());
            }
        });

        // TODO: Implement this
        // Parenthesized expression
        addWriter(ParenthesizedExpression.class, new CSharpASTWriter<ParenthesizedExpression>(this) {
            @Override
            public void write(Context context, ParenthesizedExpression parenthesizedExpression) {
                context.matchAndWrite("(");
                context.copySpaceAndComments();

                writeNode(context, parenthesizedExpression.getExpression());
                context.copySpaceAndComments();

                context.matchAndWrite(")");
            }
        });

        // TODO: Implement this
        // Cast expression
        addWriter(CastExpression.class, new CSharpASTWriter<CastExpression>(this) {
            @Override
            public void write(Context context, CastExpression castExpression) {
                context.matchAndWrite("(");

                context.copySpaceAndComments();
                writeNode(context, castExpression.getType());

                context.copySpaceAndComments();
                context.matchAndWrite(")");

                context.copySpaceAndComments();
                writeNode(context, castExpression.getExpression());
            }
        });

        // Number literal
        addWriter(NumberLiteral.class, new CSharpASTWriter<NumberLiteral>(this) {
            @Override
            public void write(Context context, NumberLiteral numberLiteral) {
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

        // Boolean literal
        addWriter(BooleanLiteral.class, new CSharpASTWriter<BooleanLiteral>(this) {
            @Override
            public void write(Context context, BooleanLiteral booleanLiteral) {
                context.matchAndWrite(booleanLiteral.booleanValue() ? "true" : "false");
            }
        });

        // Character literal
        addWriter(CharacterLiteral.class, new CSharpASTWriter<CharacterLiteral>(this) {
            @Override
            public void write(Context context, CharacterLiteral characterLiteral) {
                // TODO: Map character escape sequences
                context.matchAndWrite(characterLiteral.getEscapedValue());
            }
        });

        // Null literal
        addWriter(NullLiteral.class, new CSharpASTWriter(this) {
            @Override
            public void write(Context context, ASTNode node) {
                context.matchAndWrite("null");
            }
        });

        // TODO: Implement this
        // String literal
        addWriter(StringLiteral.class, new CSharpASTWriter<StringLiteral>(this) {
            @Override
            public void write(Context context, StringLiteral stringLiteral) {
                context.matchAndWrite(stringLiteral.getEscapedValue());
            }
        });
    }

    @Override
    public CSharpTranslator getTranslator() {
        return cSharpTranslator;
    }
}
