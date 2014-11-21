package org.juniversal.gradle;

import org.gradle.api.tasks.TaskAction;
import org.juniversal.common.JavaToObjectiveC;
import org.juniversal.gradle.support.GradleProject;

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
