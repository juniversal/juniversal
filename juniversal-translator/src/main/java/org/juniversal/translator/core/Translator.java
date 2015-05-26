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

package org.juniversal.translator.core;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.*;
import org.jetbrains.annotations.Nullable;
import org.juniversal.translator.cplusplus.CPlusPlusTranslator;
import org.juniversal.translator.csharp.CSharpTranslator;
import org.xuniversal.translator.core.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.juniversal.translator.core.ASTUtil.getFirstTypeDeclaration;

public abstract class Translator {
    private List<File> javaProjectDirectories;
    private File outputDirectory;
    private int sourceTabStop = 4;
    private int destTabStop = -1;
    private String[] classpath;
    private String[] sourcepath;
    private HashMap<Class<? extends ASTNode>, ASTNodeWriter> visitors = new HashMap<>();

    public static void main(String[] args) {
        try {
            if (!translate(args))
                System.exit(1);
        } catch (Throwable t) {
            t.printStackTrace(System.err);
            System.exit(2);
        }
    }

    public static boolean translate(String[] args) {
        try {
            @Nullable String targetLanguage = null;

            for (int i = 0; i < args.length; ++i) {
                String arg = args[i];

                if (arg.equals("-l")) {
                    ++i;
                    if (i < args.length) {
                        targetLanguage = args[i];
                        break;
                    }
                }
            }

            if (targetLanguage == null)
                usageError("No target language specified; must specify -l <language> param");

            Translator translator;
            if (targetLanguage.equals("c++"))
                translator = new CPlusPlusTranslator();
            else if (targetLanguage.equals("c#"))
                translator = new CSharpTranslator();
            else {
                throw new UserViewableException("'" + targetLanguage + "' is not a valid target language");
            }

            translator.init(args);
            return translator.translate();
        } catch (UserViewableException e) {
            System.err.println("Error: " + e.getMessage());
            return false;
        }
    }

    public Translator() {
        addDeclarationWriters();
        addStatementWriters();
        addExpressionWriters();
    }

    protected abstract TargetWriter createTargetWriter(Writer writer);

    public void init(String[] args) {
        javaProjectDirectories = new ArrayList<>();

        for (int i = 0; i < args.length; ++i) {
            String arg = args[i];

            if (arg.startsWith("-")) {
                if (arg.equals("-o")) {
                    ++i;
                    if (i >= args.length)
                        usageError();
                    arg = args[i];

                    this.outputDirectory = validateAndNormalizeDirectoryArgument(arg, false);
                } else if (arg.equals("-l")) {
                    // Skip target language arg, as that was already captured
                    ++i;
                } else if (arg.equals("-cp") || arg.equals("-classpath")) {
                    ++i;
                    if (i >= args.length)
                        usageError();
                    arg = args[i];

                    classpath = getPathArgument(arg, "-classpath");
                } else if (arg.equals("-sourcepath")) {
                    ++i;
                    if (i >= args.length)
                        usageError();
                    arg = args[i];

                    sourcepath = getPathArgument(arg, "-sourcepath");
                } else
                    usageError();
            } else
                this.javaProjectDirectories.add(validateAndNormalizeDirectoryArgument(arg, true));
        }

        // Ensure that there's at least one input directory & the output directory is specified
        if (this.javaProjectDirectories.size() == 0 || this.outputDirectory == null)
            usageError();
    }

    private String[] getPathArgument(String arg, String pathType) {
        ArrayList<String> pathEntries = new ArrayList<>();
        for (String pathEntry : arg.split(Pattern.quote(File.pathSeparator))) {
            File pathEntryFile = new File(pathEntry);

            if (!pathEntryFile.exists())
                System.err.println("Warning: " + pathType + " path entry " + pathEntry + " does not exist; ignoring");
            else pathEntries.add(pathEntry);
        }

        String[] pathEntriesArray = new String[pathEntries.size()];
        pathEntries.toArray(pathEntriesArray);
        return pathEntriesArray;
    }

    public static void usageError() {
        usageError(null);
    }

    public static void usageError(@Nullable String message) {
        if (message != null)
            System.err.println(message);
        System.err.println("Usage: -o <output-directory> -l <target-language> [-classpath <classpath>] [-sourcepath <sourcepath>] <java-project-directories-to-translate>...");
        System.exit(1);
    }

    private static File validateAndNormalizeDirectoryArgument(String path, boolean ensureExists) {
        File file = new File(path);
        try {
            file = file.getCanonicalFile();
        } catch (IOException e) {
            throw new UserViewableException("IOException when turning directory argument into canonical path: " + file.getPath());
        }

        if (ensureExists && !file.exists())
            throw new UserViewableException("Directory doesn't exist: " + file.getPath());

        return file;
    }

    public File getOutputDirectory() {
        return outputDirectory;
    }

    public TargetWriter createTargetWriter(JavaSourceFile sourceFile, String fileExtension) {
        CompilationUnit compilationUnit = sourceFile.getCompilationUnit();
        AbstractTypeDeclaration mainTypeDeclaration = getFirstTypeDeclaration(compilationUnit);

        String typeName = mainTypeDeclaration.getName().getIdentifier();
        String fileName = typeName + fileExtension;

        File file = new File(getPackageDirectory(mainTypeDeclaration), fileName);
        try {
            return createTargetWriter(new FileWriter(file));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected FileWriter createTargetFileWriter(JavaSourceFile sourceFile, String targetFileExtension) {
        CompilationUnit compilationUnit = sourceFile.getCompilationUnit();
        AbstractTypeDeclaration mainTypeDeclaration = getFirstTypeDeclaration(compilationUnit);

        String typeName = mainTypeDeclaration.getName().getIdentifier();
        String fileName = typeName + targetFileExtension;

        File file = new File(getPackageDirectory(mainTypeDeclaration), fileName);
        try {
            return new FileWriter(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public File getPackageDirectory(AbstractTypeDeclaration abstractTypeDeclaration) {
        String[] packageNameComponents = abstractTypeDeclaration.resolveBinding().getPackage().getNameComponents();

        File directory = outputDirectory;
        for (String packageNameComponent : packageNameComponents) {
            directory = new File(directory, packageNameComponent);
        }

        if (!directory.exists()) {
            if (!directory.mkdirs())
                throw new JUniversalException("Unable to create directory for path: " + directory);
        }

        return directory;
    }

    /**
     * Translate all source files configured for the translator.   If a user errors occurs during translation for a file
     * (e.g. a SourceNotSupported exception is thrown), an error message is output for that file, the translation
     * continues on with remaining files, and false is eventually returned from this method as the translate failed.  If
     * an internal occurs during translation (e.g. the translator has a bug), an exception is thrown.
     *
     * @return true if all files were translated without error, false if some failed
     */
    public boolean translate() {
        ASTParser parser = ASTParser.newParser(AST.JLS8);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        //parser.setEnvironment(new String[0], new String[0], null, false);
        //TODO: Set classpath & sourcepath differently probably; this just uses the current VM (I think), but I can
        //see that it doesn't resolve everything for some reason
        parser.setEnvironment(classpath, sourcepath, null, true);
        parser.setResolveBindings(true);

        Map options = JavaCore.getOptions();
        JavaCore.setComplianceOptions(JavaCore.VERSION_1_7, options);
        parser.setCompilerOptions(options);

        Var<Boolean> failed = new Var<>(false);

        FileASTRequestor astRequestor = new FileASTRequestor() {
            public void acceptAST(String sourceFilePath, CompilationUnit compilationUnit) {
                JavaSourceFile sourceFile = new JavaSourceFile(compilationUnit, new File(sourceFilePath), sourceTabStop);

                //boolean outputErrorForFile = false;
                for (IProblem problem : compilationUnit.getProblems()) {
                    if (problem.isError()) {
                        System.err.println("Error: " + problem.getMessage());
                        System.err.println(sourceFile.getPositionDescription(problem.getSourceStart()));
                    }
                }

                // Translate each file as it's returned; if a user error occurs while translating (e.g. a
                // SourceNotSupported exception is thrown), print the message for that, note the failure, and continue
                // on
                System.out.println("Translating " + sourceFilePath);
                try {
                    translateFile(sourceFile);
                } catch (UserViewableException e) {
                    System.err.println("Error: " + e.getMessage());
                    failed.set(true);
                }
            }
        };

        parser.createASTs(getJavaFiles(), null, new String[0], astRequestor, null);

        return !failed.value();

		/*
         * String source = readFile(jUniversal.getJavaProjectDirectories().get(0).getPath());
		 * parser.setSource(source.toCharArray());
		 *
		 * CompilationUnit compilationUnit = (CompilationUnit) parser.createAST(null);
		 *
		 *
		 * TypeDeclaration typeDeclaration = ASTUtil.getFirstTypeDeclaration(compilationUnit);
		 *
		 * FileWriter writer; try { writer = new FileWriter(jUniversal.getOutputDirectory()); }
		 * catch (IOException e) { throw new RuntimeException(e);   }
		 *
		 * CPPProfile profile = new CPPProfile(); // profile.setTabStop(4);
		 *
		 * CPPWriter cppWriter = new CPPWriter(writer, profile);
		 *
		 * Context context = new Context((CompilationUnit) compilationUnit.getRoot(), source, 8,
		 * profile, cppWriter, OutputType.SOURCE_FILE);
		 *
		 * context.setPosition(typeDeclaration.getStartPosition());
		 *
		 * ASTWriters astWriters = new ASTWriters();
		 *
		 * try { context.setPosition(typeDeclaration.getStartPosition());
		 * skipSpaceAndComments();
		 *
		 * astWriters.writeNode(typeDeclaration, context); } catch (UserViewableException e) {
		 * System.err.println(e.getMessage()); System.exit(1); } catch (RuntimeException e) { if (e
		 * instanceof ContextPositionMismatchException) throw e; else throw new
		 * JUniversalException(e.getMessage() + "\nError occurred with context at position\n" +
		 * context.getPositionDescription(context.getPosition()), e); }
		 *
		 * try { writer.close(); } catch (IOException e) { throw new RuntimeException(e); }
		 */
    }

    public abstract void translateFile(JavaSourceFile sourceFile);

    /**
     * Translate a single node in the AST.   This method is normally just used for testing (unit tests); for production
     * use whole files are always translated.
     *
     * @param sourceFile SourceFile containing node
     * @param astNode    node to translate
     * @return translated source for the node
     */
    public abstract String translateNode(JavaSourceFile sourceFile, ASTNode astNode);

    public abstract JavaSourceContext getContext();

    public abstract TargetProfile getTargetProfile();

    public int getSourceTabStop() {
        return sourceTabStop;
    }

    public void setSourceTabStop(int sourceTabStop) {
        this.sourceTabStop = sourceTabStop;
    }

    public int getDestTabStop() {
        return destTabStop;
    }

    public void setDestTabStop(int destTabStop) {
        this.destTabStop = destTabStop;
    }

    /**
     * Get all the source files in the specified Java project directories.
     *
     * @return list of all files in the project directories, in project directory order specified on command line
     */
    private String[] getJavaFiles() {
        ArrayList<File> files = new ArrayList<File>();

        for (File directory : javaProjectDirectories) {
            System.out.println(directory);
            try {
                Util.getFilesRecursive(directory, ".java", files);
            } catch (FileNotFoundException e) {
                throw new UserViewableException("Java project directory " + directory + " not found or not an accessible directory");
            }
        }

        int length = files.size();
        String[] filePathsArray = new String[length];
        for (int i = 0; i < length; ++i)
            filePathsArray[i] = files.get(i).getPath();

        return filePathsArray;
    }

    public boolean isCSharp() {
        return getTargetProfile().isCSharp();
    }

    public boolean isCPlusPlus() {
        return getTargetProfile().isCPlusPlus();
    }

    public boolean isSwift() {
        return getTargetProfile().isSwift();
    }

    protected void addWriter(Class<? extends ASTNode> clazz, ASTNodeWriter visitor) {
        if (visitors.get(clazz) != null)
            throw new JUniversalException("Writer for class " + clazz + " already added to ASTWriters");
        visitors.put(clazz, visitor);
    }

    protected void replaceWriter(Class<? extends ASTNode> clazz, ASTNodeWriter visitor) {
        if (visitors.get(clazz) == null)
            throw new JUniversalException("Writer for class " + clazz + " doesn't currently exist so can't replace it; call addWriter to add a new writer");
        visitors.put(clazz, visitor);
    }

    public ASTNodeWriter getVisitor(Class clazz) {
        ASTNodeWriter visitor = visitors.get(clazz);
        if (visitor == null)
            throw new JUniversalException("No visitor found for class " + clazz.getName());
        return visitor;
    }

    private static final boolean VALIDATE_CONTEXT_POSITION = true;

    public void writeNode(ASTNode node) {
        JavaSourceContext context = getContext();

        int nodeStartPosition;
        if (VALIDATE_CONTEXT_POSITION) {
            nodeStartPosition = node.getStartPosition();

            SourceCopier sourceCopier = getSourceCopier();

            // If the node starts with Javadoc (true for method declarations with Javadoc before
            // them), then skip past it; the caller is always expected to handle any comments,
            // Javadoc or not, coming before the start of code proper for the node. The exception to
            // that rule is the CompilationUnit; as outermost node it handles any comments at the
            // beginning itself.
            if (sourceCopier.getSourceCharAt(nodeStartPosition) == '/'
                && sourceCopier.getSourceCharAt(nodeStartPosition + 1) == '*'
                && sourceCopier.getSourceCharAt(nodeStartPosition + 2) == '*'
                && !(node instanceof CompilationUnit || node instanceof Javadoc))
                context.assertPositionIs(sourceCopier.skipSpaceAndComments(nodeStartPosition, false));
            else context.assertPositionIs(nodeStartPosition);
        }

        ASTNodeWriter visitor = getVisitor(node.getClass());

        Expression expression = null;
        if (node instanceof Expression)
            expression = (Expression) node;

        if (expression != null && expression.resolveBoxing() &&
            ! (expression instanceof SimpleName && expression.getParent() instanceof QualifiedName))
            writeBoxedExpressionNode(expression, visitor);
        else if (expression != null && expression.resolveUnboxing())
            writeUnboxedExpressionNode(expression, visitor);
        else visitor.write(node);

        if (VALIDATE_CONTEXT_POSITION) {
            if (getContext().getKnowinglyProcessedTrailingSpaceAndComments())
                context.assertPositionIsAtLeast(nodeStartPosition + node.getLength());
            else context.assertPositionIs(nodeStartPosition + node.getLength());
        }

        context.setKnowinglyProcessedTrailingSpaceAndComments(false);
    }

    /**
     * Write an ASTNode corresponding to an expression whose results are boxed.   The default implementation just writes
     * out the node like any other, but for some languages (namely C++) it's necessary to add an explicit conversion for
     * the boxing.
     *
     * @param expression expression ASTNode in question
     * @param visitor    ASTNodeWriter for the expression node
     */
    protected void writeBoxedExpressionNode(Expression expression, ASTNodeWriter visitor) {
        visitor.write(expression);
    }

    /**
     * Write an ASTNode corresponding to an expression whose results are unboxed.   The default implementation just
     * writes out the node like any other, but for some languages (namely C++) it's necessary to add an explicit
     * conversion back, to do the unboxing.
     *
     * @param expression expression ASTNode in question
     * @param visitor    ASTNodeWriter for the expression node
     */
    protected void writeUnboxedExpressionNode(Expression expression, ASTNodeWriter visitor) {
        visitor.write(expression);
    }

    public void writeRootNode(ASTNode node) {
        try {
            getContext().setPosition(node.getStartPosition());
            writeNode(node);
        } catch (UserViewableException e) {
            throw e;
        } catch (RuntimeException e) {
            if (e instanceof ContextPositionMismatchException)
                throw e;
            else
                throw new JUniversalException(e.getMessage() + "\nError occurred with context at position\n"
                                              + getContext().getCurrentPositionDescription(), e);
        }
    }

    public int getPreferredIndent() {
        return getTargetProfile().getPreferredIndent();
    }

    public SourceCopier getSourceCopier() {
        return getContext().getSourceCopier();
    }

    public ITypeBinding resolveTypeBinding(Type type) {
        ITypeBinding typeBinding = type.resolveBinding();
        if (typeBinding == null)
            throw getContext().sourceNotSupported("Type binding could not be resolved; perhaps this Java code doesn't compile or there's a missing JAR dependency?");
        return typeBinding;
    }

    public String primitiveTypeNameToTargetName(String primitiveTypeName) {
        TargetProfile targetProfile = getTargetProfile();

        switch (primitiveTypeName) {
            case "boolean":
                return targetProfile.getBooleanType();
            case "byte":
                return targetProfile.getInt8Type();
            case "char":
                return targetProfile.getCharType();
            case "short":
                return targetProfile.getInt16Type();
            case "int":
                return targetProfile.getInt32Type();
            case "long":
                return targetProfile.getInt64Type();
            case "float":
                return targetProfile.getFloat32Type();
            case "double":
                return targetProfile.getFloat64Type();
            case "void":
                return targetProfile.getVoidType();
            default:
                throw new JUniversalException("Name is not a primitive type: " + primitiveTypeName);
        }
    }

    private void addDeclarationWriters() {
        // Parameterized type
        addWriter(ParameterizedType.class, new CommonASTNodeWriter<ParameterizedType>(this) {
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

        addWriter(WildcardType.class, new CommonASTNodeWriter<WildcardType>(this) {
            @Override
            public void write(WildcardType wildcardType) {
                ArrayList<WildcardType> wildcardTypes = getContext().getMethodWildcardTypes();
                if (wildcardTypes == null)
                    throw sourceNotSupported("Wildcard types (that is, ?) only supported in method parameters and return types.  You may want to change the Java source to use an explicitly named generic type instead of a wildcard here.");

                writeWildcardTypeSyntheticName(wildcardTypes, wildcardType);
                setPositionToEndOfNode(wildcardType);
            }
        });

        // Primitive type
        addWriter(PrimitiveType.class, new CommonASTNodeWriter<PrimitiveType>(this) {
            @Override
            public void write(PrimitiveType primitiveType) {
                TargetProfile profile = getTargetProfile();

                PrimitiveType.Code code = primitiveType.getPrimitiveTypeCode();
                if (code == PrimitiveType.BYTE)
                    matchAndWrite("byte", profile.getInt8Type());
                else if (code == PrimitiveType.SHORT)
                    matchAndWrite("short", profile.getInt16Type());
                else if (code == PrimitiveType.CHAR)
                    matchAndWrite("char", profile.getCharType());
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

    private void addStatementWriters() {
        // TODO: Implement this
        // Block
        addWriter(Block.class, new CommonASTNodeWriter<Block>(this) {
            @Override
            public void write(Block block) {
                matchAndWrite("{");

                writeNodes(block.statements());

                copySpaceAndComments();
                matchAndWrite("}");
            }
        });

        // Empty statement (";")
        addWriter(EmptyStatement.class, new CommonASTNodeWriter<EmptyStatement>(this) {
            @Override
            public void write(EmptyStatement emptyStatement) {
                matchAndWrite(";");
            }
        });

        // Expression statement
        addWriter(ExpressionStatement.class, new CommonASTNodeWriter<ExpressionStatement>(this) {
            @Override
            public void write(ExpressionStatement expressionStatement) {
                writeNode(expressionStatement.getExpression());

                copySpaceAndComments();
                matchAndWrite(";");
            }
        });

        // If statement
        addWriter(IfStatement.class, new CommonASTNodeWriter<IfStatement>(this) {
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
        addWriter(WhileStatement.class, new CommonASTNodeWriter<WhileStatement>(this) {
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
        addWriter(DoStatement.class, new CommonASTNodeWriter<DoStatement>(this) {
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

        // For statement
        addWriter(ForStatement.class, new CommonASTNodeWriter<ForStatement>(this) {
            @Override public void write(ForStatement forStatement) {
                matchAndWrite("for");

                copySpaceAndComments();
                matchAndWrite("(");

                writeCommaDelimitedNodes(forStatement.initializers());

                copySpaceAndComments();
                matchAndWrite(";");

                Expression forExpression = forStatement.getExpression();
                if (forExpression != null) {
                    copySpaceAndComments();
                    writeNode(forStatement.getExpression());
                }

                copySpaceAndComments();
                matchAndWrite(";");

                writeCommaDelimitedNodes(forStatement.updaters());

                copySpaceAndComments();
                matchAndWrite(")");

                copySpaceAndComments();
                writeNode(forStatement.getBody());
            }
        });

        addWriter(SwitchStatement.class, new SwitchStatementWriter(this));

        // Continue statement
        addWriter(ContinueStatement.class, new CommonASTNodeWriter<ContinueStatement>(this) {
            @Override
            public void write(ContinueStatement continueStatement) {
                if (continueStatement.getLabel() != null)
                    throw sourceNotSupported("continue statement with a label isn't supported; change the code to not use a label");

                matchAndWrite("continue");

                copySpaceAndComments();
                matchAndWrite(";");
            }
        });

        // Break statement
        addWriter(BreakStatement.class, new CommonASTNodeWriter<BreakStatement>(this) {
            @Override
            public void write(BreakStatement breakStatement) {
                if (breakStatement.getLabel() != null)
                    throw sourceNotSupported("break statement with a label isn't supported; change the code to not use a label");

                matchAndWrite("break");

                copySpaceAndComments();
                matchAndWrite(";");
            }
        });

        // Return statement
        addWriter(ReturnStatement.class, new CommonASTNodeWriter<ReturnStatement>(this) {
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

        // Throw statement
        addWriter(ThrowStatement.class, new CommonASTNodeWriter<ThrowStatement>(this) {
            @Override
            public void write(ThrowStatement throwStatement) {
                matchAndWrite("throw");

                copySpaceAndComments();
                writeNode(throwStatement.getExpression());

                copySpaceAndComments();
                matchAndWrite(";");
            }
        });

        // Static initializer
        addWriter(Initializer.class, new CommonASTNodeWriter<Initializer>(this) {
            @Override
            public void write(Initializer initializer) {
                throw sourceNotSupported("Static initializers aren't supported (for one thing, their order of execution isn't fully deterministic); use a static method that initializes on demand instead");
            }
        });
    }

    /**
     * Add visitors for the different kinds of expressions.
     */
    private void addExpressionWriters() {
        // Prefix expression
        addWriter(PrefixExpression.class, new CommonASTNodeWriter<PrefixExpression>(this) {
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
                // strip it, not copying anything here; otherwise copy
                if (isSwift())
                    skipSpaceAndComments();
                else copySpaceAndComments();

                writeNode(prefixExpression.getOperand());
            }
        });

        // Postfix expression
        addWriter(PostfixExpression.class, new CommonASTNodeWriter<PostfixExpression>(this) {
            @Override
            public void write(PostfixExpression postfixExpression) {
                writeNode(postfixExpression.getOperand());

                // In Swift there can't be any whitespace or comments between a postfix operator & its operand, so
                // strip it; otherwise copy
                if (isSwift())
                    skipSpaceAndComments();
                else copySpaceAndComments();

                PostfixExpression.Operator operator = postfixExpression.getOperator();
                if (operator == PostfixExpression.Operator.INCREMENT)
                    matchAndWrite("++");
                else if (operator == PostfixExpression.Operator.DECREMENT)
                    matchAndWrite("--");
                else throw invalidAST("Unknown postfix operator type: " + operator);
            }
        });

        // Conditional expression
        addWriter(ConditionalExpression.class, new CommonASTNodeWriter<ConditionalExpression>(this) {
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

        // Array access
        addWriter(ArrayAccess.class, new CommonASTNodeWriter<ArrayAccess>(this) {
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

        // Parenthesized expression
        addWriter(ParenthesizedExpression.class, new CommonASTNodeWriter<ParenthesizedExpression>(this) {
            @Override
            public void write(ParenthesizedExpression parenthesizedExpression) {
                matchAndWrite("(");

                copySpaceAndComments();
                writeNode(parenthesizedExpression.getExpression());

                copySpaceAndComments();
                matchAndWrite(")");
            }
        });
    }
}
