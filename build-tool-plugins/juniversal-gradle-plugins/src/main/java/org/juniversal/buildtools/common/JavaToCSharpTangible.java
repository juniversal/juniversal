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

package org.juniversal.buildtools.common;

import org.juniversal.buildtools.common.support.CommonProject;
import org.juniversal.buildtools.common.support.SourceType;
import org.juniversal.buildtools.common.support.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * @author Bret Johnson
 * @since 7/10/2014 1:56 AM
 */
public class JavaToCSharpTangible extends Translator {
    private File converterDirectory;
    private File converterSettings;
    private boolean runMinimized;

    public JavaToCSharpTangible(CommonProject project) {
        super(project);
    }

    public void setConverterDirectory(File converterDirectory) {
        this.converterDirectory = converterDirectory;
    }

    public void setConverterSettings(File converterSettings) {
        this.converterSettings = converterSettings;
    }

    public void setRunMinimized(boolean runMinimized) {
        this.runMinimized = runMinimized;
    }

    // TODO: Check into why converterSettings not used
    protected void translateSourceType(SourceType sourceType, File outputDirectory) {
        // Delete the generated source directories
        Utils.deleteChildDirectoriesExcept(outputDirectory, "Properties", "nontranslated", "Bin", "obj");

        ArrayList<String> args = new ArrayList<String>();

        if (runMinimized) {
            args.add("cmd.exe");
            args.add("/c");
            args.add("start");
            args.add("/min");
            args.add("/b");
            args.add("C# Converter");
        }

        args.add(new File(converterDirectory, "Java to C# Converter.exe").getPath());

        args.add(getProject().getTranslatableSourceDirectories(sourceType).getSingleElement().getAbsolutePath());
        args.add(outputDirectory.getAbsolutePath());
        args.add(converterDirectory.getAbsolutePath());

        Process process = getProject().exec(args, converterDirectory);

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String inputLine;
        try {
            while ((inputLine = reader.readLine()) != null) {
                getProject().info(inputLine);
            }
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException("IO exception when running Java to C# converter", e);
        }
    }
}

