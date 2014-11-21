package org.juniversal.gradle;

import org.gradle.api.tasks.TaskAction;
import org.juniversal.common.JavaToCSharp;
import org.juniversal.gradle.support.GradleProject;

public class JavaToCSharpTask extends JUniversalTranslatorTask {
    public JavaToCSharpTask() {
        super("c#");
    }

    @TaskAction
    public void translate() {
        JavaToCSharp javaToCSharp = new JavaToCSharp(new GradleProject(getProject()));
        initTranslator(javaToCSharp);

        javaToCSharp.translate();
    }
}
