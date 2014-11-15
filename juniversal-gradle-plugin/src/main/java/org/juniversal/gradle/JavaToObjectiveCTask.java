package org.juniversal.gradle;

import org.gradle.api.DefaultTask;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskAction;
import org.juniversal.common.JavaToObjectiveC;
import org.juniversal.common.support.CommonFileSet;
import org.juniversal.gradle.support.GradleProject;

import java.io.File;

public class JavaToObjectiveCTask extends DefaultTask {
    /**
     * Directory for generated C# for main source.  Defaults to ${project.basedir}/c#.
     */
    private File outputDirectory = getProject().file("objective-c");

    /**
     * Directory for generated C# for test source.  Defaults to ${project.basedir}/c#-test.
     */
    private File testOutputDirectory = getProject().file("objective-c-test");

    /**
     * Directory that contains the j2objc distribution.
     */
    private File j2objcHome;

    public boolean docComments = true;

    private boolean useArc = true;

    /**
     * If true, don't do anything.  Defaults to the skipConversion global property.  Thus "mvn -DskipConversion" will
     * skip the Java -> C# conversion.
     */
    //@Parameter(defaultValue = "${skipConversion}", property = "skipConversion", required = false)
    private boolean skip;


    public File getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public File getTestOutputDirectory() {
        return testOutputDirectory;
    }

    public void setTestOutputDirectory(File testOutputDirectory) {
        this.testOutputDirectory = testOutputDirectory;
    }

    public File getJ2objcHome() {
        return j2objcHome;
    }

    public void setJ2objcHome(File j2objcHome) {
        this.j2objcHome = j2objcHome;
    }

    public boolean isDocComments() {
        return docComments;
    }

    public void setDocComments(boolean docComments) {
        this.docComments = docComments;
    }

    public boolean isUseArc() {
        return useArc;
    }

    public void setUseArc(boolean useArc) {
        this.useArc = useArc;
    }

    public boolean isSkip() {
        return skip;
    }

    public void setSkip(boolean skip) {
        this.skip = skip;
    }

    @TaskAction
    public void convert() {
        if (skip)
            return;

        if (j2objcHome == null) {
            String environmentVariable = System.getenv("J2OBJC_HOME");
            if (environmentVariable == null)
                throw new RuntimeException("Either set j2objcHome property or J2OBJC_HOME environment variable should be set, pointing to j2objc distribution");
            else j2objcHome = new File(environmentVariable);
        }

        JavaPluginConvention javaPluginConvention;
        try {
            javaPluginConvention = getProject().getConvention().getPlugin(JavaPluginConvention.class);
        } catch (IllegalStateException e) {
            throw new RuntimeException("Gradle project apparently isn't a Java project--it doesn't use the Java plugin");
        }

        GradleProject commonProject = new GradleProject(getProject());

        JavaToObjectiveC javaToObjectiveC = new JavaToObjectiveC(commonProject);
        javaToObjectiveC.setDocComments(docComments);
        javaToObjectiveC.setJ2objcHome(j2objcHome);
        javaToObjectiveC.setUseArc(useArc);

        SourceSetContainer sourceSets = javaPluginConvention.getSourceSets();
        SourceSet mainSourceSet = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME);
        SourceSet testSourceSet = sourceSets.getByName(SourceSet.TEST_SOURCE_SET_NAME);

        CommonFileSet mainSourcePath = commonProject.createFileSet(mainSourceSet.getAllJava().getSrcDirs());
        CommonFileSet testSourcePath = commonProject.createFileSet(testSourceSet.getAllJava().getSrcDirs());

        CommonFileSet mainSourceFiles = commonProject.getTranslatableSourceFiles(mainSourceSet);
        CommonFileSet testSourceFiles = commonProject.getTranslatableSourceFiles(testSourceSet);

        javaToObjectiveC.convert("j2objc-main", mainSourcePath,
                commonProject.createFileSet(mainSourceSet.getCompileClasspath()), mainSourceFiles, outputDirectory);
        if (!testSourceFiles.isEmpty())
            javaToObjectiveC.convert("j2objc-test", testSourcePath,
                    commonProject.createFileSet(testSourceSet.getCompileClasspath()), testSourceFiles, testOutputDirectory);
    }
}
