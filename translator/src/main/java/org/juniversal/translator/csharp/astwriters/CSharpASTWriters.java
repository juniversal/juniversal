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
            public void write(SimpleName simpleName) {
                matchAndWrite(simpleName.getIdentifier());
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
            public void write(VariableDeclarationFragment variableDeclarationFragment) {
                // TODO: Handle syntax with extra dimensions on array
                if (variableDeclarationFragment.getExtraDimensions() > 0)
                    throw sourceNotSupported("\"int foo[]\" array type syntax not currently supported; use \"int[] foo\" instead");

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

        // TODO: Implement this
        // Single variable declaration (used in parameter list, catch clauses, and enhanced for statements)
        addWriter(SingleVariableDeclaration.class, new CSharpASTWriter<SingleVariableDeclaration>(this) {
            @Override
            public void write(SingleVariableDeclaration singleVariableDeclaration) {
                // TODO: Handle syntax with extra dimensions on array
                if (singleVariableDeclaration.getExtraDimensions() > 0)
                    throw sourceNotSupported("\"int foo[]\" array type syntax not currently supported; use \"int[] foo\" instead");

                List<?> modifiers = singleVariableDeclaration.modifiers();
                ensureModifiersJustFinalOrAnnotations(modifiers);
                skipModifiers(modifiers);

                Type type = singleVariableDeclaration.getType();

                if (singleVariableDeclaration.isVarargs()) {
                    write("params ");
                    writeNode(type);

                    copySpaceAndComments();
                    // TODO: Think through & handle all cases where a regular type is converted to an array here
                    matchAndWrite("...", "[]");
                } else writeNode(type);

                copySpaceAndComments();
                SimpleName name = singleVariableDeclaration.getName();
                writeNode(name);

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

        // TODO: Implement this
        // Parameterized type
        addWriter(ParameterizedType.class, new CSharpASTWriter<ParameterizedType>(this) {
            @Override
            public void write(ParameterizedType parameterizedType) {
                writeNode(parameterizedType.getType());

                copySpaceAndComments();
                matchAndWrite("<");

                writeCommaDelimitedNodes(parameterizedType.typeArguments());

                copySpaceAndComments();
                matchAndWrite(">");
            }
        });

        addWriter(WildcardType.class, new CSharpASTWriter<WildcardType>(this) {
            @Override
            public void write(WildcardType wildcardType) {
                ArrayList<WildcardType> wildcardTypes = getContext().getMethodWildcardTypes();
                if (wildcardTypes == null)
                    throw sourceNotSupported("Wildcard types (that is, ?) only supported in method parameters and return types.  You may want to change the Java source to use an explicitly named generic type instead of a wildcard here.");

                int index = wildcardTypes.indexOf(wildcardType);
                if (index == -1)
                    throw new JUniversalException("Wildcard type not found in list");

                write("TWildCard" + (index + 1));
                setPositionToEndOfNode(wildcardType);
            }
        });

        // Array type
        addWriter(ArrayType.class, new CSharpASTWriter<ArrayType>(this) {
            @Override
            public void write(ArrayType arrayType) {
                writeNode(arrayType.getElementType());

                for (Object dimensionObject : arrayType.dimensions()) {
                    Dimension dimension = (Dimension) dimensionObject;

                    copySpaceAndComments();
                    match("[");

                    copySpaceAndComments();
                    match("]");
                }
            }
        });

        // TODO: Implement this
        // Primitive type
        addWriter(PrimitiveType.class, new CSharpASTWriter<PrimitiveType>(this) {
            @Override
            public void write(PrimitiveType primitiveType) {
                PrimitiveType.Code code = primitiveType.getPrimitiveTypeCode();
                if (code == PrimitiveType.BYTE)
                    matchAndWrite("byte", "sbyte");
                else if (code == PrimitiveType.SHORT)
                    matchAndWrite("short");
                else if (code == PrimitiveType.CHAR)
                    matchAndWrite("char");
                else if (code == PrimitiveType.INT)
                    matchAndWrite("int");
                else if (code == PrimitiveType.LONG)
                    matchAndWrite("long");
                else if (code == PrimitiveType.FLOAT)
                    matchAndWrite("float");
                else if (code == PrimitiveType.DOUBLE)
                    matchAndWrite("double");
                else if (code == PrimitiveType.BOOLEAN)
                    matchAndWrite("boolean", "bool");
                else if (code == PrimitiveType.VOID)
                    matchAndWrite("void", "void");
                else
                    throw invalidAST("Unknown primitive type: " + code);
            }
        });

        // Array initializer
        addWriter(ArrayInitializer.class, new CSharpASTWriter<ArrayInitializer>(this) {
            @Override
            public void write(ArrayInitializer arrayInitializer) {
                matchAndWrite("{");

                // TODO: Check that number of expressions matches array size (I think, as I think C# requires exact number and Java allows less)
                writeCommaDelimitedNodes(arrayInitializer.expressions());
                // TODO: Skip extra trailing commas here

                copySpaceAndComments();
                matchAndWrite("}");
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
            public void write(Block block) {
                matchAndWrite("{");

                writeNodes(block.statements());

                copySpaceAndComments();
                matchAndWrite("}");
            }
        });

        // TODO: Implement this
        // Empty statement (";")
        addWriter(EmptyStatement.class, new CSharpASTWriter<EmptyStatement>(this) {
            @Override
            public void write(EmptyStatement emptyStatement) {
                matchAndWrite(";");
            }
        });

        // TODO: Implement this
        // Expression statement
        addWriter(ExpressionStatement.class, new CSharpASTWriter<ExpressionStatement>(this) {
            @Override
            public void write(ExpressionStatement expressionStatement) {
                writeNode(expressionStatement.getExpression());

                copySpaceAndComments();
                matchAndWrite(";");
            }
        });

        // If statement
        addWriter(IfStatement.class, new CSharpASTWriter<IfStatement>(this) {
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
        addWriter(WhileStatement.class, new CSharpASTWriter<WhileStatement>(this) {
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
        addWriter(DoStatement.class, new CSharpASTWriter<DoStatement>(this) {
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

        // TODO: Implement this
        // Continue statement
        addWriter(ContinueStatement.class, new CSharpASTWriter<ContinueStatement>(this) {
            @Override
            public void write(ContinueStatement continueStatement) {
                if (continueStatement.getLabel() != null)
                    throw sourceNotSupported("continue statement with a label isn't supported as that construct doesn't exist in C++; change the code to not use a label");

                matchAndWrite("continue");

                copySpaceAndComments();
                matchAndWrite(";");
            }
        });

        // TODO: Implement this
        // Break statement
        addWriter(BreakStatement.class, new CSharpASTWriter<BreakStatement>(this) {
            @Override
            public void write(BreakStatement breakStatement) {
                if (breakStatement.getLabel() != null)
                    throw sourceNotSupported("break statement with a label isn't supported as that construct doesn't exist in C++; change the code to not use a label");

                matchAndWrite("break");

                copySpaceAndComments();
                matchAndWrite(";");
            }
        });

        // TODO: Implement this
        // For statement
        addWriter(ForStatement.class, new ForStatementWriter(this));

        addWriter(EnhancedForStatement.class, new CSharpASTWriter<EnhancedForStatement>(this) {
            @Override
            public void write(EnhancedForStatement enhancedForStatement) {
                matchAndWrite("for", "foreach");

                copySpaceAndComments();
                matchAndWrite("(");

                copySpaceAndComments();
                writeNode(enhancedForStatement.getParameter());

                copySpaceAndComments();
                matchAndWrite(":");

                copySpaceAndComments();
                writeNode(enhancedForStatement.getExpression());

                copySpaceAndComments();
                matchAndWrite(")");

                copySpaceAndComments();
                writeNode(enhancedForStatement.getBody());
            }
        });

        // TODO: Implement this
        // Switch statement
        addWriter(SwitchStatement.class, new SwitchStatementWriter(this));

        // TODO: Implement this
        // Return statement
        addWriter(ReturnStatement.class, new CSharpASTWriter<ReturnStatement>(this) {
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
        addWriter(VariableDeclarationStatement.class, new CSharpASTWriter<VariableDeclarationStatement>(this) {
            @Override
            public void write(VariableDeclarationStatement variableDeclarationStatement) {
                writeVariableDeclaration(variableDeclarationStatement.modifiers(),
                        variableDeclarationStatement.getType(),
                        variableDeclarationStatement.fragments());

                copySpaceAndComments();
                matchAndWrite(";");
            }
        });

        // Try statement
        addWriter(TryStatement.class, new TryStatementWriter(this));

        // TODO: Implement this
        // Throw statement
        addWriter(ThrowStatement.class, new CSharpASTWriter<ThrowStatement>(this) {
            @Override
            public void write(ThrowStatement throwStatement) {
                matchAndWrite("throw");

                copySpaceAndComments();
                writeNode(throwStatement.getExpression());

                copySpaceAndComments();
                matchAndWrite(";");
            }
        });

        // TODO: Implement this
        // Delegating constructor invocation
        addWriter(ConstructorInvocation.class, new CSharpASTWriter(this) {
            @Override
            public void write(ASTNode node) {
                throw sourceNotSupported("Delegating constructors aren't currently supported; for now you have to change the code to not use them (e.g. by adding an init method)");
            }
        });

        addWriter(AssertStatement.class, new CSharpASTWriter<AssertStatement>(this) {
            @Override
            public void write(AssertStatement assertStatement) {
                matchAndWrite("assert", "Debug.Assert(");

                copySpaceAndComments();
                writeNode(assertStatement.getExpression());

                @Nullable Expression message = assertStatement.getMessage();
                if (message != null) {
                    skipSpaceAndComments();
                    matchAndWrite(":", ",");

                    copySpaceAndComments();
                    writeNode(message);

                    copySpaceAndComments();
                    matchAndWrite(";", ");");
                }
            }
        });

        // TODO: Implement this
        // Throw statement
        addWriter(SynchronizedStatement.class, new CSharpASTWriter<SynchronizedStatement>(this) {
            @Override
            public void write(SynchronizedStatement synchronizedStatement) {
                matchAndWrite("synchronized", "lock");

                copySpaceAndComments();
                matchAndWrite("(");

                writeNode(synchronizedStatement.getExpression());

                copySpaceAndComments();
                matchAndWrite(")");

                copySpaceAndComments();
                writeNode(synchronizedStatement.getBody());
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
            public void write(VariableDeclarationExpression variableDeclarationExpression) {
                writeVariableDeclaration(variableDeclarationExpression.modifiers(),
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

                // In Swift there can't be any whitespace or comments between a unary prefix operator & its operand, so
                // strip it, not copying anything here
                skipSpaceAndComments();

                writeNode(prefixExpression.getOperand());
            }
        });

        // Postfix expression
        addWriter(PostfixExpression.class, new CSharpASTWriter<PostfixExpression>(this) {
            @Override
            public void write(PostfixExpression postfixExpression) {
                writeNode(postfixExpression.getOperand());

                // In Swift there can't be any whitespace or comments between a postfix operator & its operand, so
                // strip it, not copying anything here
                skipSpaceAndComments();

                PostfixExpression.Operator operator = postfixExpression.getOperator();
                if (operator == PostfixExpression.Operator.INCREMENT)
                    matchAndWrite("++");
                else if (operator == PostfixExpression.Operator.DECREMENT)
                    matchAndWrite("--");
                else throw invalidAST("Unknown postfix operator type: " + operator);
            }
        });

        // TODO: Implement this
        // instanceof expression
        addWriter(InstanceofExpression.class, new CSharpASTWriter<InstanceofExpression>(this) {
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
        addWriter(ConditionalExpression.class, new CSharpASTWriter<ConditionalExpression>(this) {
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

        // TODO: Implement this
        // this
        addWriter(ThisExpression.class, new CSharpASTWriter<ThisExpression>(this) {
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
        addWriter(FieldAccess.class, new CSharpASTWriter<FieldAccess>(this) {
            @Override
            public void write(FieldAccess fieldAccess) {
                writeNode(fieldAccess.getExpression());

                copySpaceAndComments();
                matchAndWrite(".");

                copySpaceAndComments();
                writeNode(fieldAccess.getName());
            }
        });

        // Array access
        addWriter(ArrayAccess.class, new CSharpASTWriter<ArrayAccess>(this) {
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
        addWriter(QualifiedName.class, new CSharpASTWriter<QualifiedName>(this) {
            @Override
            public void write(QualifiedName qualifiedName) {
                // TODO: Figure out the other cases where this can occur & make them all correct

                writeNode(qualifiedName.getQualifier());

                copySpaceAndComments();
                matchAndWrite(".");

                copySpaceAndComments();
                writeNode(qualifiedName.getName());
            }
        });

        // TODO: Implement this
        // Parenthesized expression
        addWriter(ParenthesizedExpression.class, new CSharpASTWriter<ParenthesizedExpression>(this) {
            @Override
            public void write(ParenthesizedExpression parenthesizedExpression) {
                matchAndWrite("(");

                copySpaceAndComments();
                writeNode(parenthesizedExpression.getExpression());

                copySpaceAndComments();
                matchAndWrite(")");
            }
        });

        // TODO: Implement this
        // Cast expression
        addWriter(CastExpression.class, new CSharpASTWriter<CastExpression>(this) {
            @Override
            public void write(CastExpression castExpression) {
                matchAndWrite("(");

                copySpaceAndComments();
                writeNode(castExpression.getType());

                copySpaceAndComments();
                matchAndWrite(")");

                copySpaceAndComments();
                writeNode(castExpression.getExpression());
            }
        });

        // Number literal
        addWriter(NumberLiteral.class, new CSharpASTWriter<NumberLiteral>(this) {
            @Override
            public void write(NumberLiteral numberLiteral) {
                String token = numberLiteral.getToken();

                // Strip out any _ separators in the number, as those aren't supported in C# (at least not until the
                // new C# 6 comes out)
                String convertedToken = token.replace("_", "");

                // TODO: Support binary (and octal) literals by converting to hex
                if (token.length() >= 2 && token.startsWith("0")) {
                    char secondChar = token.charAt(1);
                    if (secondChar >= '0' && secondChar <= '7') {
                        throw sourceNotSupported("Octal literals aren't currently supported; change the source to use hex instead");
                    } else if ((secondChar == 'b') || (secondChar == 'B')) {
                        throw sourceNotSupported("Binary literals aren't currently supported; change the source to use hex instead");
                    }
                }

                write(convertedToken);
                match(token);
            }
        });

        // Boolean literal
        addWriter(BooleanLiteral.class, new CSharpASTWriter<BooleanLiteral>(this) {
            @Override
            public void write(BooleanLiteral booleanLiteral) {
                matchAndWrite(booleanLiteral.booleanValue() ? "true" : "false");
            }
        });

        // Character literal
        addWriter(CharacterLiteral.class, new CSharpASTWriter<CharacterLiteral>(this) {
            @Override
            public void write(CharacterLiteral characterLiteral) {
                // TODO: Map character escape sequences
                matchAndWrite(characterLiteral.getEscapedValue());
            }
        });

        // Null literal
        addWriter(NullLiteral.class, new CSharpASTWriter(this) {
            @Override
            public void write(ASTNode node) {
                matchAndWrite("null");
            }
        });

        // TODO: Implement this
        // String literal
        addWriter(StringLiteral.class, new CSharpASTWriter<StringLiteral>(this) {
            @Override
            public void write(StringLiteral stringLiteral) {
                matchAndWrite(stringLiteral.getEscapedValue());
            }
        });
    }

    @Override public CSharpTranslator getTranslator() {
        return cSharpTranslator;
    }
}
