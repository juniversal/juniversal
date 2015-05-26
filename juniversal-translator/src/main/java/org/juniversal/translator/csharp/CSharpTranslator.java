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

package org.juniversal.translator.csharp;

import org.eclipse.jdt.core.dom.*;
import org.jetbrains.annotations.Nullable;
import org.juniversal.translator.core.JUniversalException;
import org.juniversal.translator.core.JavaSourceFile;
import org.juniversal.translator.core.Translator;
import org.xuniversal.translator.core.TargetProfile;
import org.xuniversal.translator.csharp.CSharpTargetProfile;
import org.xuniversal.translator.csharp.CSharpTargetWriter;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.juniversal.translator.core.ASTUtil.forEach;
import static org.juniversal.translator.core.ASTUtil.isArrayLengthField;

public class CSharpTranslator extends Translator {
    private CSharpTargetProfile targetProfile = new CSharpTargetProfile();
    private CSharpContext context;
    private HashMap<String, String> annotationMap = new HashMap<>();
    private HashSet<String> cSharpReservedWords;

    public CSharpTranslator() {
        annotationMap.put("org.junit.Test", "NUnit.Framework.Test");

        addDeclarationWriters();
        addStatementWriters();
        addExpressionWriters();

        // TODO: Implement this
        // Simple name
        addWriter(SimpleName.class, new CSharpASTNodeWriter<SimpleName>(this) {
            @Override
            public void write(SimpleName simpleName) {
                String identifier = simpleName.getIdentifier();

                // Escape identifier names which are also reserved words in C#
                if (getCSharpReservedWords().contains(identifier))
                    matchAndWrite(identifier, "@" + identifier);
                else matchAndWrite(identifier);
            }
        });
    }

    @Override public TargetProfile getTargetProfile() {
        return targetProfile;
    }

    @Override public CSharpContext getContext() {
        return context;
    }

    @Override protected CSharpTargetWriter createTargetWriter(Writer writer) {
        return new CSharpTargetWriter(writer, targetProfile);
    }

    public Map<String, String> getAnnotationMap() {
        return annotationMap;
    }

    @Override public void translateFile(JavaSourceFile sourceFile) {
        try (CSharpTargetWriter targetWriter = createTargetWriter(createTargetFileWriter(sourceFile, ".cs"))) {
            context = new CSharpContext(sourceFile, targetWriter);
            writeRootNode(sourceFile.getCompilationUnit());
        }
    }

    @Override public String translateNode(JavaSourceFile sourceFile, ASTNode astNode) {
        try (StringWriter writer = new StringWriter()) {
            context = new CSharpContext(sourceFile, createTargetWriter(writer));

            // Set the type declaration part of the context
            AbstractTypeDeclaration typeDeclaration = (AbstractTypeDeclaration) sourceFile.getCompilationUnit().types().get(0);
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
        // Javadoc comment
        addWriter(Javadoc.class, new JavadocCommentWriter(this));

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

        // Simple type
        addWriter(SimpleType.class, new SimpleTypeWriter(this));

        // TODO: Implement this
        // Variable declaration fragment
        addWriter(VariableDeclarationFragment.class, new CSharpASTNodeWriter<VariableDeclarationFragment>(this) {
            @Override
            public void write(VariableDeclarationFragment variableDeclarationFragment) {
                String name = variableDeclarationFragment.getName().getIdentifier();
                if (getContext().getTypeMethodNames().contains(name))
                    throw sourceNotSupported("Class also contains a method with name '" + name + "'; in C#, unlike Java, a class can't have a method and a variable with the same name, so rename one of them");

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
        addWriter(SingleVariableDeclaration.class, new CSharpASTNodeWriter<SingleVariableDeclaration>(this) {
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
                writeNode(singleVariableDeclaration.getName());

                // TODO: Handle initializer
                if (singleVariableDeclaration.getInitializer() != null)
                    throw new JUniversalException("Unexpected initializer present for SingleVariableDeclaration");
            }
        });

        // Array type
        addWriter(ArrayType.class, new CSharpASTNodeWriter<ArrayType>(this) {
            @Override
            public void write(ArrayType arrayType) {
                writeNode(arrayType.getElementType());

                forEach(arrayType.dimensions(), (Dimension dimension) -> {
                    copySpaceAndComments();
                    matchAndWrite("[");

                    copySpaceAndComments();
                    matchAndWrite("]");
                });
            }
        });

        // Array initializer
        addWriter(ArrayInitializer.class, new CSharpASTNodeWriter<ArrayInitializer>(this) {
            @Override
            public void write(ArrayInitializer arrayInitializer) {
                // TODO: Test more cases here
                if (arrayInitializer.getParent() instanceof ArrayInitializer) {
                    write("new[] ");
/*
                    throw sourceNotSupported(
                            "Nested array initializers, without a 'new' specified, aren't supported in C#.   Change the " +
                            "Java source to include a new, a syntax supported by both Java and C#.  For instance, " +
                            "change { {1, 2, 3}, {10, 11, 12} ) => { new int[] {1, 2, 3}, new int[] {10, 11, 12} }");
*/
                }

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
        addWriter(Block.class, new CSharpASTNodeWriter<Block>(this) {
            @Override
            public void write(Block block) {
                matchAndWrite("{");

                writeNodes(block.statements());

                copySpaceAndComments();
                matchAndWrite("}");
            }
        });

        addWriter(EnhancedForStatement.class, new CSharpASTNodeWriter<EnhancedForStatement>(this) {
            @Override
            public void write(EnhancedForStatement enhancedForStatement) {
                matchAndWrite("for", "foreach");

                copySpaceAndComments();
                matchAndWrite("(");

                copySpaceAndComments();
                writeNode(enhancedForStatement.getParameter());

                copySpaceAndComments();
                // TODO: Ensure spaces around "in"
                matchAndWrite(":", "in");

                copySpaceAndComments();
                writeNode(enhancedForStatement.getExpression());

                copySpaceAndComments();
                matchAndWrite(")");

                copySpaceAndComments();
                writeNode(enhancedForStatement.getBody());
            }
        });

        // Local variable declaration statement
        addWriter(VariableDeclarationStatement.class, new CSharpASTNodeWriter<VariableDeclarationStatement>(this) {
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
        // Delegating constructor invocation
        addWriter(ConstructorInvocation.class, new CSharpASTNodeWriter(this) {
            @Override
            public void write(ASTNode node) {
                throw sourceNotSupported("Delegating constructors aren't currently supported; for now you have to change the code to not use them (e.g. by adding an init method)");
            }
        });

        addWriter(AssertStatement.class, new CSharpASTNodeWriter<AssertStatement>(this) {
            @Override
            public void write(AssertStatement assertStatement) {
                matchAndWrite("assert", nativeReference("System.Diagnostics", "Debug.Assert"));
                write("(");

                skipSpaceAndComments();
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

        // Synchronized statement
        addWriter(SynchronizedStatement.class, new CSharpASTNodeWriter<SynchronizedStatement>(this) {
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

        // Class instance creation
        addWriter(ClassInstanceCreation.class, new CSharpClassInstanceCreationWriter(this));

        // TODO: Implement this
        // Array creation
        addWriter(ArrayCreation.class, new ArrayCreationWriter(this));

        // TODO: Implement this
        // Variable declaration expression (used in a for statement)
        addWriter(VariableDeclarationExpression.class, new CSharpASTNodeWriter<VariableDeclarationExpression>(this) {
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

        // instanceof expression
        addWriter(InstanceofExpression.class, new CSharpASTNodeWriter<InstanceofExpression>(this) {
            @Override
            public void write(InstanceofExpression instanceofExpression) {
                Expression expression = instanceofExpression.getLeftOperand();
                writeNode(expression);

                copySpaceAndComments();
                matchAndWrite("instanceof", "is");

                copySpaceAndComments();
                Type type = instanceofExpression.getRightOperand();
                writeNode(type);
            }
        });

        // TODO: Implement this
        // this
        addWriter(ThisExpression.class, new CSharpASTNodeWriter<ThisExpression>(this) {
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
        addWriter(FieldAccess.class, new CSharpASTNodeWriter<FieldAccess>(this) {
            @Override
            public void write(FieldAccess fieldAccess) {
                writeNode(fieldAccess.getExpression());

                copySpaceAndComments();
                matchAndWrite(".");

                copySpaceAndComments();
                if (isArrayLengthField(fieldAccess))
                    matchAndWrite("length", "Length");
                else writeNode(fieldAccess.getName());
            }
        });

        // Qualified name
        addWriter(QualifiedName.class, new CSharpASTNodeWriter<QualifiedName>(this) {
            @Override
            public void write(QualifiedName qualifiedName) {
                // TODO: Figure out the other cases where this can occur & make them all correct

                writeNode(qualifiedName.getQualifier());

                copySpaceAndComments();
                matchAndWrite(".");

                copySpaceAndComments();
                if (isArrayLengthField(qualifiedName))
                    matchAndWrite("length", "Length");
                else writeNode(qualifiedName.getName());
            }
        });

        // TODO: Implement this
        // Cast expression
        addWriter(CastExpression.class, new CastExpressionWriter(this));

        // Number literal
        addWriter(NumberLiteral.class, new NumberLiteralWriter(this));

        // Boolean literal
        addWriter(BooleanLiteral.class, new CSharpASTNodeWriter<BooleanLiteral>(this) {
            @Override
            public void write(BooleanLiteral booleanLiteral) {
                matchAndWrite(booleanLiteral.booleanValue() ? "true" : "false");
            }
        });

        // Character literal
        addWriter(CharacterLiteral.class, new CSharpASTNodeWriter<CharacterLiteral>(this) {
            @Override
            public void write(CharacterLiteral characterLiteral) {
                // TODO: Map character escape sequences
                // TODO: Add cast to {byte) or other types if needed, to handle for instance:
                // byte[] binaryData = new byte[]{'1', '2', '3'};
                // Maybe should do that for all expressions, not just literals (do more testing to see)
                matchAndWrite(characterLiteral.getEscapedValue());
            }
        });

        // Null literal
        addWriter(NullLiteral.class, new CSharpASTNodeWriter(this) {
            @Override
            public void write(ASTNode node) {
                matchAndWrite("null");
            }
        });

        // TODO: Implement this
        // String literal
        addWriter(StringLiteral.class, new CSharpASTNodeWriter<StringLiteral>(this) {
            @Override
            public void write(StringLiteral stringLiteral) {
                matchAndWrite(stringLiteral.getEscapedValue());
            }
        });

        addWriter(TypeLiteral.class, new CSharpASTNodeWriter<TypeLiteral>(this) {
            @Override
            public void write(TypeLiteral typeLiteral) {
                throw sourceNotSupported("Type literals (<type>.class) aren't supported by JUniversal, partially to ease idiomatic translation to C++.  For HashMaps of types, consider using string keys instead.");
            }
        });
    }

    public HashSet<String> getCSharpReservedWords() {
        if (cSharpReservedWords == null)
            cSharpReservedWords = createCSharpReservedWords();
        return cSharpReservedWords;
    }

    /**
     * Create a set containing all reserved keywords in C# that aren't reserved words in Java.   These keywords are the
     * ones that should be escaped if used in Java source as an identifier.
     *
     * @return hash set containing set of C#-only reserved words
     */
    public HashSet<String> createCSharpReservedWords() {
        HashSet<String> reservedWords = new HashSet<>();

        reservedWords.add("as");
        reservedWords.add("base");
        reservedWords.add("bool");
        reservedWords.add("checked");
        reservedWords.add("decimal");
        reservedWords.add("delegate");
        reservedWords.add("event");
        reservedWords.add("explicit");
        reservedWords.add("extern");
        reservedWords.add("fixed");
        reservedWords.add("foreach");
        reservedWords.add("implicit");
        reservedWords.add("in");
        reservedWords.add("internal");
        reservedWords.add("is");
        reservedWords.add("lock");
        reservedWords.add("long");
        reservedWords.add("namespace");
        reservedWords.add("object");
        reservedWords.add("operator");
        reservedWords.add("out");
        reservedWords.add("override");
        reservedWords.add("params");
        reservedWords.add("readonly");
        reservedWords.add("ref");
        reservedWords.add("sbyte");
        reservedWords.add("sealed");
        reservedWords.add("short");
        reservedWords.add("sizeof");
        reservedWords.add("stackalloc");
        reservedWords.add("string");
        reservedWords.add("struct");
        reservedWords.add("typeof");
        reservedWords.add("uint");
        reservedWords.add("ulong");
        reservedWords.add("unchecked");
        reservedWords.add("unsafe");
        reservedWords.add("ushort");
        reservedWords.add("using");
        reservedWords.add("virtual");

        return reservedWords;
    }
}
