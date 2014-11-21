package org.juniversal.gradle;

import org.gradle.api.tasks.TaskAction;
import org.juniversal.common.JavaToCPlusPlus;
import org.juniversal.gradle.support.GradleProject;

public class JavaToCPlusPlusTask extends JUniversalTranslatorTask {
    public JavaToCPlusPlusTask() {
        super("c++");
    }

    @TaskAction
    public void translate() {
        JavaToCPlusPlus javaToCPlusPlus = new JavaToCPlusPlus(new GradleProject(getProject()));
        initTranslator(javaToCPlusPlus);

        javaToCPlusPlus.translate();
    }

}
