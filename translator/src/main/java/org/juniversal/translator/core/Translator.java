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

package org.juniversal.translator.core;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;
import org.jetbrains.annotations.Nullable;
import org.juniversal.translator.cplusplus.CPPProfile;
import org.juniversal.translator.cplusplus.CPlusPlusTranslator;
import org.juniversal.translator.csharp.CSharpTranslator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

public abstract class Translator {
    private CPPProfile cppProfile = new CPPProfile();
    private List<File> javaProjectDirectories;
    private File outputDirectory;
    private int preferredIndent = 4;
    private int sourceTabStop = 4;
    private String[] classpath;

    public static void main(String[] args) {
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
            throw new UserViewableException("No target language specified; must specify -l <language> param");

        Translator translator;
        if (targetLanguage.equals("c++"))
            translator = new CPlusPlusTranslator();
        else if (targetLanguage.equals("c#"))
            translator = new CSharpTranslator();
        else {
            throw new UserViewableException("'" + targetLanguage + "' is not a valid target language");
        }

        translator.init(args);
        translator.translate();
    }

    public Translator() {
    }

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

                    this.outputDirectory = validateAndNormalizeDirectoryArgument(arg);
                } else if (arg.equals("-l")) {
                    // Skip target language arg, as that was already captured
                    ++i;
                } else if (arg.equals("-cp") || arg.equals("-classpath")) {
                    ++i;
                    if (i >= args.length)
                        usageError();
                    arg = args[i];

                    classpath = arg.split(Pattern.quote(File.pathSeparator));
                } else
                    usageError();
            } else
                this.javaProjectDirectories.add(validateAndNormalizeDirectoryArgument(arg));
        }

        // Ensure that there's at least one input directory & the output directory is specified
        if (this.javaProjectDirectories.size() == 0 || this.outputDirectory == null)
            usageError();
    }

    public static void usageError() {
        System.err.println("Usage: jtrans -o <output-directory> -l <target-language> <java-project-directories>...");
        System.exit(1);
    }

    private static File validateAndNormalizeDirectoryArgument(String path) {
        File file = new File(path);
        try {
            file = file.getCanonicalFile();
        } catch (IOException e) {
            throw new UserViewableException("IOException when turning directory argument into canonical path: " + file.getPath());
        }

        if (!file.exists())
            throw new UserViewableException("Directory doesn't exist: " + file.getPath());

        return file;
    }

    public File getOutputDirectory() {
        return outputDirectory;
    }

    public void translate() {
        ASTParser parser = ASTParser.newParser(AST.JLS8);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        //parser.setEnvironment(new String[0], new String[0], null, false);
        //TODO: Set classpath & sourcepath differently probably; this just uses the current VM (I think), but I can
        //see that it doesn't resolve everything for some reason
        parser.setEnvironment(classpath, null, null, true);
        parser.setResolveBindings(true);

        Map options = JavaCore.getOptions();
        JavaCore.setComplianceOptions(JavaCore.VERSION_1_7, options);
        parser.setCompilerOptions(options);

        FileASTRequestor astRequestor = new FileASTRequestor() {
            public void acceptAST(String sourceFilePath, CompilationUnit compilationUnit) {
                SourceFile sourceFile = new SourceFile(compilationUnit, sourceFilePath, sourceTabStop);

                //boolean outputErrorForFile = false;
                for (IProblem problem : compilationUnit.getProblems()) {
                    if (problem.isError()) {
                        System.err.println("Error: " + problem.getMessage());
                        System.err.println(sourceFile.getPositionDescription(problem.getSourceStart()));
                    }
                }

                System.out.println("Called to accept AST for " + sourceFilePath);
                translateAST(sourceFile);
            }
        };

        parser.createASTs(getJavaFiles(), null, new String[0], astRequestor, null);

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
		 * profile, cppWriter, OutputType.SOURCE);
		 *
		 * context.setPosition(typeDeclaration.getStartPosition());
		 *
		 * ASTWriters astWriters = new ASTWriters();
		 *
		 * try { context.setPosition(typeDeclaration.getStartPosition());
		 * context.skipSpaceAndComments();
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

    protected abstract void translateAST(SourceFile sourceFile);

    public abstract ASTWriters getASTWriters();

    public int getSourceTabStop() {
        return sourceTabStop;
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
}
