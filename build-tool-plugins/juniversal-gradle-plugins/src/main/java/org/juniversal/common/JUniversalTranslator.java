/*
 * Copyright (c) 2012-2014, Microsoft Mobile
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

package org.juniversal.common;

import org.juniversal.common.support.*;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Bret Johnson on 11/15/2014.
 */
public class JUniversalTranslator extends Translator {
    private File juniversalTranslatorJar;
    private String targetLanguage;

    public JUniversalTranslator(CommonProject project, String targetLanguage) {
        super(project);
        this.targetLanguage = targetLanguage;
    }

    public void setJuniversalTranslatorJar(File juniversalTranslatorJar) {
        this.juniversalTranslatorJar = juniversalTranslatorJar;
    }

    public void translateSourceType(SourceType sourceType, File outputDirectory) {
        // Delete the generated source directories
        Utils.deleteChildDirectoriesExcept(outputDirectory, "nontranslated");

        ArrayList<String> args = new ArrayList<String>();

        args.add("-l");
        args.add(targetLanguage);

        FileSet classpath = getProject().getClasspath(sourceType);
        FileSet sourceDirectories = getProject().getTranslatableSourceDirectories(sourceType);

        if (! classpath.isEmpty()) {
            args.add("-classpath");
            args.add(classpath.getAsPath());
        }

        args.add("-o");
        args.add(outputDirectory.getPath());

        for (File file : sourceDirectories.getFiles()) {
            args.add(file.getAbsolutePath());
        }

        // Log the arguments
        getProject().info("Calling juniversal-translator with: " + args.toString());

        String[] argsArray = new String[args.size()];
        args.toArray(argsArray);

        try {
            org.juniversal.translator.core.Translator.main(argsArray);
        } catch (Throwable e) {
            getProject().error(e.getMessage());
        }
    }
}
