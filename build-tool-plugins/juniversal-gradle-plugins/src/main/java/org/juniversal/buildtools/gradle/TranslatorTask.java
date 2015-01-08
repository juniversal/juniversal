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

package org.juniversal.buildtools.gradle;

import org.gradle.api.DefaultTask;
import org.juniversal.buildtools.common.Translator;

import java.io.File;

public class TranslatorTask extends DefaultTask {
    /**
     * If true, don't do anything.  Defaults to the skipConversion global property.  Thus "mvn -DskipConversion" will
     * skip the Java to C# conversion.
     */
    //@Parameter(defaultValue = "${skipConversion}", property = "skipConversion", required = false)
    public boolean skip;

    /**
     * Directory for generated C# for main source.  Defaults to ${project.basedir}/c#.
     */
    //@Parameter(defaultValue = "${project.basedir}/c#", property = "outputDirectory", required = false)
    public File outputDirectory;

    /**
     * Directory for generated C# for test source.  Defaults to ${project.basedir}/c#-test.
     */
    //@Parameter(defaultValue = "${project.basedir}/c#-test", property = "testOutputDirectory", required = false)
    public File testOutputDirectory;


    public TranslatorTask(String defaultOutputDirectoryName) {
        outputDirectory = getProject().file(defaultOutputDirectoryName);
        testOutputDirectory = getProject().file(defaultOutputDirectoryName + "-test");
    }

    protected void initTranslator(Translator translator) {
        translator.setSkip(skip);
        translator.setOutputDirectory(outputDirectory);
        translator.setTestOutputDirectory(testOutputDirectory);
    }
}
