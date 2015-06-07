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

package org.juniversal.translator.cplusplus;

import org.eclipse.jdt.core.dom.*;
import org.jetbrains.annotations.Nullable;
import org.juniversal.translator.core.*;
import org.xuniversal.translator.core.TypeName;
import org.xuniversal.translator.cplusplus.CPlusPlusTargetProfile;
import org.xuniversal.translator.cplusplus.CPlusPlusTargetWriter;
import org.xuniversal.translator.cplusplus.ReferenceKind;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import static org.juniversal.translator.core.ASTUtil.forEach;

public class CPlusPlusTranslator extends Translator {
    private CPlusPlusTargetProfile targetProfile = new CPlusPlusTargetProfile();
    private CPlusPlusContext context;

    public CPlusPlusTranslator() {
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

    @Override public CPlusPlusTargetProfile getTargetProfile() {
        return targetProfile;
    }

    @Override public CPlusPlusContext getContext() {
        return context;
    }

    @Override protected CPlusPlusTargetWriter createTargetWriter(Writer writer) {
        return new CPlusPlusTargetWriter(writer, targetProfile);
    }

    @Override public void translateFile(JavaSourceFile sourceFile) {
        writeFile(sourceFile, OutputType.HEADER_FILE);
        writeFile(sourceFile, OutputType.SOURCE_FILE);
    }

    private void writeFile(JavaSourceFile sourceFile, OutputType outputType) {
        String targetFileExtension = outputType == OutputType.HEADER_FILE ? ".h" : ".cpp";
        try (CPlusPlusTargetWriter targetWriter = createTargetWriter(createTargetFileWriter(sourceFile, targetFileExtension))) {
            context = new CPlusPlusContext(sourceFile, targetWriter, outputType);
            writeRootNode(sourceFile.getCompilationUnit());
        }
    }

    @Override public String translateNode(JavaSourceFile sourceFile, ASTNode astNode) {
        try (StringWriter writer = new StringWriter()) {
            AbstractTypeDeclaration typeDeclaration = (AbstractTypeDeclaration) sourceFile.getCompilationUnit().types().get(0);

            context = new CPlusPlusContext(sourceFile, createTargetWriter(writer), OutputType.SOURCE_FILE);
            // Set the type declaration part of the context
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
                    //addNameNeedingImport(getTargetProfile().getSharedPtrType());
                    //addNameNeedingImport(getTargetProfile().getArrayType());

                    write("std::shared_ptr< xuniv::Array<");
                    writeTypeReference(type, ReferenceKind.SharedPtr);
                    write("> >");

                    copySpaceAndComments();
                    match("...");
                } else writeTypeReference(type, ReferenceKind.SharedPtr);

                copySpaceAndComments();
                writeNode(singleVariableDeclaration.getName());

                if (singleVariableDeclaration.getInitializer() != null)
                    throw new JUniversalException("Unexpected initializer present for SingleVariableDeclaration");
            }
        });

        // Parameterized type
        replaceWriter(ParameterizedType.class, new CPlusPlusASTNodeWriter<ParameterizedType>(this) {
            @Override
            public void write(ParameterizedType parameterizedType) {
                writeNode(parameterizedType.getType());

                copySpaceAndComments();
                matchAndWrite("<");

                forEach(parameterizedType.typeArguments(), (Type type, boolean first) -> {
                    if (!first) {
                        copySpaceAndComments();
                        matchAndWrite(",");
                    }

                    copySpaceAndComments();
                    writeTypeReference(type, ReferenceKind.SharedPtr);
                });

                copySpaceAndComments();
                matchAndWrite(">");
            }
        });

        // Simple type
        addWriter(SimpleType.class, new CPlusPlusASTNodeWriter<SimpleType>(this) {
            @Override
            public void write(SimpleType simpleType) {
                Name name = simpleType.getName();

                @Nullable ITypeBinding typeBinding = simpleType.resolveBinding();
                if (typeBinding == null) {
                    write(name.getFullyQualifiedName());
                    setPositionToEndOfNode(name);
                } else if (typeBinding.isTypeVariable())
                    matchAndWrite(((SimpleName) name).getIdentifier());
                else {
                    TypeName typeName = getTargetType(typeBinding);

                    getContext().addReferencedTargetType(typeName);

                    if (name instanceof QualifiedName ||
                        !typeName.inSamePackageAs(getContext().getOutermostTypeName())) {
                        write(typeName.toString("::"));
                        setPositionToEndOfNode(name);
                    } else {
                        SimpleName simpleName = (SimpleName) name;
                        matchAndWrite(simpleName.getIdentifier(), typeName.getType().getLastComponent());
                    }
                }
            }
        });

        // Array type
        addWriter(ArrayType.class, new CPlusPlusASTNodeWriter<ArrayType>(this) {
            @Override
            public void write(ArrayType arrayType) {
                int dimensions = arrayType.getDimensions();

                //getContext().getReferencedTypes().add(getTargetProfile().getArrayType());
                for (int i = 0; i < dimensions; i++)
                    write("xuniv::Array<");

                writeTypeReference(arrayType.getElementType(), ReferenceKind.SharedPtr);

                for (int i = 0; i < dimensions; i++) {
                    skipSpaceAndComments();
                    match("[");

                    skipSpaceAndComments();
                    match("]");
                }

                for (int i = 0; i < dimensions; i++)
                    write(">");
            }
        });

        // TODO: Test the different cases here
        // Array initializer
        addWriter(ArrayInitializer.class, new CPlusPlusASTNodeWriter<ArrayInitializer>(this) {
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

                // TODO: Check that number of expressions matches array length (I think, as I think C# requires exact number and Java allows less)
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
        // Block
        replaceWriter(Block.class, new CPlusPlusASTNodeWriter<Block>(this) {
            @SuppressWarnings("unchecked")
            @Override
            public void write(Block block) {
                matchAndWrite("{");

                boolean firstStatement = true;
                for (Statement statement : (List<Statement>) block.statements()) {
                    // If the first statement is a super or delegating constructor invocation, we skip it since
                    // it's included as part of the method declaration in C++. If a super/delegating
                    // constructor invocation is a statement other than the first, which it should
                    // never be, we let that error out since writeNode won't find a match for it.
                    if (firstStatement &&
                        (statement instanceof SuperConstructorInvocation || statement instanceof ConstructorInvocation))
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

        addWriter(EnhancedForStatement.class, new CPlusPlusASTNodeWriter<EnhancedForStatement>(this) {
            @Override
            public void write(EnhancedForStatement enhancedForStatement) {
                matchAndWrite("for");

                copySpaceAndComments();
                matchAndWrite("(");

                copySpaceAndComments();
                writeNode(enhancedForStatement.getParameter());

                copySpaceAndComments();
                // TODO: Ensure spaces around "in"
                matchAndWrite(":");

                copySpaceAndComments();
                writeNode(enhancedForStatement.getExpression());

                copySpaceAndComments();
                matchAndWrite(")");

                copySpaceAndComments();
                writeNode(enhancedForStatement.getBody());
            }
        });

        // Try statement
        addWriter(TryStatement.class, new TryStatementWriter(this));

        // Local variable declaration statement
        addWriter(VariableDeclarationStatement.class, new VariableDeclarationWriter(this));
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
        addWriter(ClassInstanceCreation.class, new CPlusPlusClassInstanceCreationWriter(this));

        // Array creation
        addWriter(ArrayCreation.class, new ArrayCreationWriter(this));

        // Variable declaration expression (used in a for statement)
        addWriter(VariableDeclarationExpression.class, new VariableDeclarationWriter(this));

        // Infix expression
        addWriter(InfixExpression.class, new InfixExpressionWriter(this));

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

                // If accessing array "length" property, turn that into a method call in C++
                if (fieldAccess.getExpression().resolveTypeBinding().isArray() &&
                    fieldAccess.getName().getIdentifier().equals("length")) {
                    write("length()");
                    setPositionToEndOfNode(fieldAccess.getName());
                } else writeNode(fieldAccess.getName());
            }
        });

        // Qualified name
        addWriter(QualifiedName.class, new CPlusPlusASTNodeWriter<QualifiedName>(this) {
            @Override
            public void write(QualifiedName qualifiedName) {
                // TODO: Figure out the other cases where this can occur & make them all correct

                // Here assume that a QualifiedName refers to field access; if it refers to a type,
                // the caller should catch that case itself and ensure it never gets here

                IBinding binding = qualifiedName.resolveBinding();

                boolean wroteQualifier = false;
                @Nullable ITypeBinding typeBinding = qualifiedName.getQualifier().resolveTypeBinding();
                if (typeBinding != null) {
                    TypeName typeName = getTargetType(typeBinding);
                    getContext().addReferencedTargetType(typeName);

                    if (!typeName.inSamePackageAs(getContext().getOutermostTypeName())) {
                        write(typeName.toString("::"));
                        setPositionToEndOfNode(qualifiedName.getQualifier());
                        wroteQualifier = true;

                    }
                }

                if (! wroteQualifier)
                    writeNode(qualifiedName.getQualifier());

                copySpaceAndComments();
                if (binding.getKind() == IBinding.VARIABLE && (binding.getModifiers() & Modifier.STATIC) != 0)
                    matchAndWrite(".", "::");
                else matchAndWrite(".", "->");

                // If accessing array "length" property (which can either look like a FieldAccess or QualifiedName),
                // turn that into a method call in C++
                if (qualifiedName.getQualifier().resolveTypeBinding().isArray() &&
                    qualifiedName.getName().getIdentifier().equals("length")) {
                    write("length()");
                    setPositionToEndOfNode(qualifiedName);
                } else writeNode(qualifiedName.getName());
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

        replaceWriter(ArrayAccess.class, new CommonASTNodeWriter<ArrayAccess>(this) {
            @Override
            public void write(ArrayAccess arrayAccess) {
                writeNode(arrayAccess.getArray());

                copySpaceAndComments();
                matchAndWrite("[", "->at(");

                copySpaceAndComments();
                writeNode(arrayAccess.getIndex());

                copySpaceAndComments();
                matchAndWrite("]", ")");
            }
        });

        // Number literal
        addWriter(NumberLiteral.class, new CPlusPlusASTNodeWriter<NumberLiteral>(this) {
            @Override
            public void write(NumberLiteral numberLiteral) {
                String literal = numberLiteral.getToken();
                if (literal.endsWith("L"))
                    literal = literal.replace("L", "LL");
                else if (literal.endsWith("l"))
                    literal = literal.replace("l", "LL");

                // TODO: Error out if floating point literal ends in f or F and doesn't contain a "." or "e"/"E" (that is, "12f" is a valid floating point literal in Java but you need to say 12.0f in C++).
                matchAndWrite(numberLiteral.getToken(), literal);
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
                matchAndWrite("null", "nullptr");
            }
        });

        // String literal
        addWriter(StringLiteral.class, new CPlusPlusASTNodeWriter<StringLiteral>(this) {
            @Override
            public void write(StringLiteral stringLiteral) {
                //addNameNeedingImport(getTargetProfile().getMakeSharedFunction());
                write("xuniv::String::make(u" + stringLiteral.getEscapedValue() + ")");
                match(stringLiteral.getEscapedValue());
            }
        });
    }

    /**
     * Write an ASTNode corresponding to an expression whose results are boxed.   The default implementation just writes
     * out the node like any other, but for some languages (namely C++) it's necessary to add an explicit conversion for
     * the boxing.
     *
     * @param expression expression ASTNode in question
     * @param visitor    ASTNodeWriter for the expression node
     */
    @Override protected void writeBoxedExpressionNode(Expression expression, ASTNodeWriter visitor) {
        ITypeBinding typeBinding = expression.resolveTypeBinding();
        String boxedTargetTypeName = primitiveTypeNameToTargetName(typeBinding.getName());

        //getContext().addNameNeedingImport(getTargetProfile().getMakeSharedFunction());
        getContext().addReferencedTargetType(getTargetProfile().getBoxType());
        getContext().write("xuniv::Box<" + boxedTargetTypeName + ">::make(");
        visitor.write(expression);
        getContext().write(")");
    }

    /**
     * Write an ASTNode corresponding to an expression whose results are unboxed.   The default implementation just
     * writes out the node like any other, but for some languages (namely C++) it's necessary to add an explicit
     * conversion back, to do the unboxing.
     *
     * @param expression expression ASTNode in question
     * @param visitor    ASTNodeWriter for the expression node
     */
    @Override protected void writeUnboxedExpressionNode(Expression expression, ASTNodeWriter visitor) {
        super.writeUnboxedExpressionNode(expression, visitor);
    }
}
