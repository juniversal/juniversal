package org.juniversal;

import groovy.lang.Closure;
import org.gradle.api.Project;
import org.gradle.api.tasks.JavaExec;
import org.slf4j.Logger;

import java.io.File;

/**
 * @author Bret Johnson
 * @since 7/10/2014 1:56 AM
 */
public class JavaToObjectiveCImpl {
    private Project project;
    private Logger logger;

    public JavaToObjectiveCImpl(Project project, Logger logger) {
        this.project = project;
        this.logger = logger;
    }

    public void convert(File sourceDirectory, File outputDirectory, File j2objcHome, boolean docComments, boolean useArc) {
        // Delete the generated source directories
        Utils.deleteChildDirectoriesExcept(outputDirectory, "Properties", "nontranslated", "Bin", "obj");

        JavaExec javaExec = new JavaExec();

        javaExec.setWorkingDir(project.getProjectDir());

        File j2objcLibDirectory = new File(j2objcHome, "lib");

        File jreEmulJar = new File(j2objcLibDirectory, "jre_emul.jar");
        javaExec.setBootstrapClasspath(project.files(jreEmulJar));

        File j2objcAnnotationsJar = new File(j2objcLibDirectory, "j2objc_annotations.jar");
        javaExec.setClasspath(project.files(j2objcAnnotationsJar));

        javaExec.args("-jar", new File(j2objcLibDirectory, "j2objc.jar").getAbsolutePath());
        javaExec.args("-d", outputDirectory.getPath());

        if (useArc)
            javaExec.args("--use-arc");
        javaExec.args("--use-arc");
        if (docComments)
            javaExec.args("--doc-comments");

        logger.debug("Executing: " + javaExec.getCommandLine());
        javaExec.execute();

        /*


        System.out.println("Starting convert...");
        Process process = Utils.exec(args, converterDirectory, logger);
        System.out.println("Done with convert");

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String inputLine;
        try {
            while ((inputLine = reader.readLine()) != null) {
                logger.info(inputLine);
            }
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException("IO exception when running Java to C# converter", e);
        }
        */
    }
}

