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

package org.juniversal.buildtools.gradle;

import org.gradle.api.tasks.TaskAction;
import org.juniversal.buildtools.common.JavaToCSharpTangible;
import org.juniversal.buildtools.gradle.support.GradleProject;

import java.io.File;

public class JavaToCSharpTangibleTask extends TranslatorTask {
    /**
     * Directory that contains the converter.  Java to C# Converter.exe must be in this directory.
     */
    public File converterDirectory;

    /**
     * Path to converter settings file (which normally has a .dat extension).
     */
    public File converterSettings;

    /**
     * If true (the default), run the converter in a minimized window via "cmd /c start /min".  That avoids the
     * converter window flashing for an instant.
     */
    public boolean runMinimized = true;


    public JavaToCSharpTangibleTask() {
        super("c#");
    }

    @TaskAction
    public void convert() {
        GradleProject commonProject = new GradleProject(getProject());

        JavaToCSharpTangible javaToCSharpTangible = new JavaToCSharpTangible(commonProject);
        initTranslator(javaToCSharpTangible);
        javaToCSharpTangible.setConverterDirectory(converterDirectory);
        javaToCSharpTangible.setConverterSettings(converterSettings);
        javaToCSharpTangible.setRunMinimized(runMinimized);

        javaToCSharpTangible.translate();
    }

}
