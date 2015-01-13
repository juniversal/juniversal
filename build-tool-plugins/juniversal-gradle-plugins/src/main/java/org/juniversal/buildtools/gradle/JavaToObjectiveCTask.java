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
import org.juniversal.buildtools.common.JavaToObjectiveC;
import org.juniversal.buildtools.gradle.support.GradleProject;

import java.io.File;

public class JavaToObjectiveCTask extends TranslatorTask {
    /**
     * Directory that contains the j2objc distribution.
     */
    public File j2objcHome;
    public boolean docComments = true;
    public boolean useArc = true;


    public JavaToObjectiveCTask() {
        super("objective-c");
    }

    @TaskAction
    public void translate() {
        if (j2objcHome == null) {
            String environmentVariable = System.getenv("J2OBJC_HOME");
            if (environmentVariable == null)
                throw new RuntimeException("Either set j2objcHome property or J2OBJC_HOME environment variable should be set, pointing to j2objc distribution");
            else j2objcHome = new File(environmentVariable);
        }

        JavaToObjectiveC javaToObjectiveC = new JavaToObjectiveC(new GradleProject(getProject()));
        initTranslator(javaToObjectiveC);
        javaToObjectiveC.setDocComments(docComments);
        javaToObjectiveC.setJ2objcHome(j2objcHome);
        javaToObjectiveC.setUseArc(useArc);

        javaToObjectiveC.translate();
    }

}
