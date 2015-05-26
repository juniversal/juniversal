/*
 * Copyright (c) 2012-2015, Microsoft Mobile
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
import org.juniversal.translator.core.*;
import org.xuniversal.translator.swift.SwiftTargetProfile;
import org.xuniversal.translator.swift.SwiftTargetWriter;

import java.io.*;

public class SwiftTranslator extends Translator {
    private SwiftTargetProfile targetProfile = new SwiftTargetProfile();
    private SwiftContext context;

    public SwiftTranslator() {
        addDeclarationWriters();
        addStatementWriters();
        addExpressionWriters();

        // Simple name
        addWriter(SimpleName.class, new SwiftASTNodeWriter(this) {
            @Override
            public void write(ASTNode node) {
                SimpleName simpleName = (SimpleName) node;

                matchAndWrite(simpleName.getIdentifier());
            }
        });
    }

    @Override public SwiftTargetProfile getTargetProfile() {
        return targetProfile;
    }

    @Override public SwiftContext getContext() {
        return context;
    }

    @Override protected SwiftTargetWriter createTargetWriter(Writer writer) {
        return new SwiftTargetWriter(writer, targetProfile);
    }

    @Override public void translateFile(JavaSourceFile sourceFile) {
        try (SwiftTargetWriter targetWriter = createTargetWriter(createTargetFileWriter(sourceFile, ".swift"))) {
            context = new SwiftContext(sourceFile, targetWriter);
            writeRootNode(sourceFile.getCompilationUnit());
        }
    }

    @Override public String translateNode(JavaSourceFile sourceFile, ASTNode astNode) {
        try (StringWriter writer = new StringWriter()) {
            context = new SwiftContext(sourceFile, createTargetWriter(writer));

            // Set the type declaration part of the context
            AbstractTypeDeclaration typeDeclaration = ASTUtil.getFirstTypeDeclaration(sourceFile.getCompilationUnit());
            context.setTypeDeclaration(typeDeclaration);

            writeRootNode(astNode);

            return writer.getBuffer().toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
/*
        addWriter(FieldDeclaration.class, new FieldDeclarationWriter(this));
*/

        // TODO: Implement this
        // Variable declaration fragment
        addWriter(VariableDeclarationFragment.class, new SwiftASTNodeWriter(this) {
            @Override
            public void write(ASTNode node) {
                VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) node;

                // TODO: Handle syntax with extra dimensions on array
                if (variableDeclarationFragment.getExtraDimensions() > 0)
                    throw sourceNotSupported("\"int foo[]\" syntax not currently supported; use \"int[] foo\" instead");

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
        // Single variable declaration (used in parameter list & catch clauses)
        addWriter(SingleVariableDeclaration.class, new SwiftASTNodeWriter(this) {
            @Override
            public void write(ASTNode node) {
                SingleVariableDeclaration singleVariableDeclaration = (SingleVariableDeclaration) node;

                // TODO: Handle syntax with extra dimensions on array
                if (singleVariableDeclaration.getExtraDimensions() > 0)
                    throw sourceNotSupported("\"int foo[]\" syntax not currently supported; use \"int[] foo\" instead");

                // TODO: Handle final & varargs

                SimpleName name = singleVariableDeclaration.getName();
                setPositionToStartOfNode(name);
                writeNode(name);
                write(": ");

                int endOfNamePosition = getPosition();

                Type type = singleVariableDeclaration.getType();
                setPositionToStartOfNode(type);
                writeNode(type);

                setPosition(endOfNamePosition);

                // TODO: Handle initializer
                Expression initializer = singleVariableDeclaration.getInitializer();
                if (initializer != null)
                    throw new JUniversalException("Unexpected initializer present for SingleVariableDeclaration");
            }
        });

        // TODO: Implement this
        // Simple type
        addWriter(SimpleType.class, new SwiftASTNodeWriter(this) {
            @Override
            public void write(ASTNode node) {
                SimpleType simpleType = (SimpleType) node;

                Name name = simpleType.getName();
                if (name instanceof QualifiedName) {
                    QualifiedName qualifiedName = (QualifiedName) name;

                    write(ASTWriterUtil.getNamespaceNameForPackageName(qualifiedName.getQualifier()));
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
        // Array type
        addWriter(ArrayType.class, new SwiftASTNodeWriter(this) {
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
    }

    /**
     * Add visitors for the different kinds of statements.
     */
    private void addStatementWriters() {
        // If statement
        replaceWriter(IfStatement.class, new SwiftASTNodeWriter(this) {
            @Override
            public void write(ASTNode node) {
                IfStatement ifStatement = (IfStatement) node;

                int ifColumn = getTargetColumn();
                matchAndWrite("if");

                copySpaceAndCommentsEnsuringDelimiter();

                writeConditionNoParens(ifStatement.getExpression());

                writeStatementEnsuringBraces(ifStatement.getThenStatement(), ifColumn, false);

                Statement elseStatement = ifStatement.getElseStatement();
                if (elseStatement != null) {
                    copySpaceAndComments();

                    matchAndWrite("else");
                    writeStatementEnsuringBraces(ifStatement.getElseStatement(), ifColumn, true);
                }
            }
        });

        // While statement
        replaceWriter(WhileStatement.class, new SwiftASTNodeWriter(this) {
            @Override
            public void write(ASTNode node) {
                WhileStatement whileStatement = (WhileStatement) node;

                int whileColumn = getTargetColumn();
                matchAndWrite("while");

                copySpaceAndCommentsEnsuringDelimiter();

                writeConditionNoParens(whileStatement.getExpression());

                writeStatementEnsuringBraces(whileStatement.getBody(), whileColumn, false);
            }
        });

        // Do while statement
        replaceWriter(DoStatement.class, new SwiftASTNodeWriter(this) {
            @Override
            public void write(ASTNode node) {
                DoStatement doStatement = (DoStatement) node;

                int doColumn = getTargetColumn();
                matchAndWrite("do");

                writeStatementEnsuringBraces(doStatement.getBody(), doColumn, false);

                copySpaceAndComments();
                matchAndWrite("while");

                copySpaceAndComments();
                writeConditionNoParens(doStatement.getExpression());

                copySpaceAndComments();
                matchAndWrite(";");
            }
        });

        // TODO: Implement this
        // For statement
/*
        addWriter(ForStatement.class, new ForStatementWriter(this));
*/

        // TODO: Implement this
        // Local variable declaration statement
        addWriter(VariableDeclarationStatement.class, new VariableDeclarationWriter(this));

        // TODO: Implement this
        // Throw statement
        replaceWriter(ThrowStatement.class, new SwiftASTNodeWriter(this) {
            @Override
            public void write(ASTNode node) {
                throw sourceNotSupported("throw isn't supported in Swift (unfortunately) currently;");
            }
        });

        // TODO: Implement this
        // Delegating constructor invocation
        addWriter(ConstructorInvocation.class, new SwiftASTNodeWriter(this) {
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

        // TODO: Implement this
        // Assignment expression
/*
        addWriter(Assignment.class, new AssignmentWriter(this));
*/

        // TODO: Implement this
        // Method invocation
        addWriter(MethodInvocation.class, new MethodInvocationWriter(this));

        // TODO: Implement this
        // Super Method invocation
        addWriter(SuperMethodInvocation.class, new MethodInvocationWriter(this));

        // TODO: Implement this
        // Class instance creation
        addWriter(ClassInstanceCreation.class, new ClassInstanceCreationWriter(this));

        // TODO: Implement this
        // Array creation
/*
        addWriter(ArrayCreation.class, new ArrayCreationWriter(this));
*/

        // TODO: Implement this
        // Variable declaration expression (used in a for statement)
        addWriter(VariableDeclarationExpression.class, new VariableDeclarationWriter(this));

        // Infix expression
        addWriter(InfixExpression.class, new InfixExpressionWriter(this));

        // TODO: Implement this
        // instanceof expression
        addWriter(InstanceofExpression.class, new SwiftASTNodeWriter(this) {
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

        // TODO: Implement this
        // this
        addWriter(ThisExpression.class, new SwiftASTNodeWriter(this) {
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

        // TODO: Implement this
        // Field access
        addWriter(FieldAccess.class, new SwiftASTNodeWriter(this) {
            @Override
            public void write(ASTNode node) {
                FieldAccess fieldAccess = (FieldAccess) node;

                writeNode(fieldAccess.getExpression());
                copySpaceAndComments();

                matchAndWrite(".", "->");

                writeNode(fieldAccess.getName());
            }
        });

        // TODO: Implement this
        // Qualified name
        addWriter(QualifiedName.class, new SwiftASTNodeWriter(this) {
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

        // TODO: Implement this
        // Cast expression
        addWriter(CastExpression.class, new SwiftASTNodeWriter(this) {
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
        addWriter(NumberLiteral.class, new SwiftASTNodeWriter(this) {
            @Override
            public void write(ASTNode node) {
                NumberLiteral numberLiteral = (NumberLiteral) node;
                String token = numberLiteral.getToken();

                boolean isOctal = false;
                if (token.length() >= 2 && token.startsWith("0")) {
                    char secondChar = token.charAt(1);
                    if (secondChar >= '0' && secondChar <= '7') {
                        isOctal = true;
                    }
                }

                // Swift prefixes octal with 0o whereas Java just uses a leading 0
                if (isOctal) {
                    write("0o");
                    write(token.substring(1));
                } else write(token);

                match(token);
            }
        });

        // Boolean literal
        addWriter(BooleanLiteral.class, new SwiftASTNodeWriter(this) {
            @Override
            public void write(ASTNode node) {
                BooleanLiteral booleanLiteral = (BooleanLiteral) node;
                matchAndWrite(booleanLiteral.booleanValue() ? "true" : "false");
            }
        });

        // Character literal
        addWriter(CharacterLiteral.class, new SwiftASTNodeWriter(this) {
            @Override
            public void write(ASTNode node) {
                CharacterLiteral characterLiteral = (CharacterLiteral) node;

                // TODO: Map character escape sequences
                // TODO: Handle conversion of characters to integers where that's normally implicit in Java

                write("Character(\"" + characterLiteral.charValue() + "\")");
                match(characterLiteral.getEscapedValue());
            }
        });

        // Null literal
        addWriter(NullLiteral.class, new SwiftASTNodeWriter(this) {
            @Override
            public void write(ASTNode node) {
                matchAndWrite("null", "nil");
            }
        });

        // TODO: Implement this
        // String literal
        addWriter(StringLiteral.class, new SwiftASTNodeWriter(this) {
            @Override
            public void write(ASTNode node) {
                StringLiteral stringLiteral = (StringLiteral) node;

                write("new String(" + stringLiteral.getEscapedValue() + "L)");
                match(stringLiteral.getEscapedValue());
            }
        });
    }
}
