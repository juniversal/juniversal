package org.juniversal

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileVisitDetails
import org.gradle.api.file.FileVisitor
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskAction

public class JavaToObjectiveCTask extends DefaultTask {
    /**
     * Directory for generated C# for main source.  Defaults to ${project.basedir}/c#.
     */
    //@Parameter(defaultValue = "${project.basedir}/c#", property = "outputDirectory", required = false)
    File outputDirectory = getProject().file("objc");

    /**
     * Directory for generated C# for test source.  Defaults to ${project.basedir}/c#-test.
     */
    //@Parameter(defaultValue = "${project.basedir}/c#-test", property = "testOutputDirectory", required = false)
    File testOutputDirectory = getProject().file("objc-test");

    /**
     * Directory that contains the j2objc distribution.
     */
    //@Parameter(property = "j2objcHome", required = true)
    File j2objcHome;

    boolean docComments = true;

    boolean useArc = true;

    /**
     * If true, don't do anything.  Defaults to the skipConversion global property.  Thus "mvn -DskipConversion" will
     * skip the Java -> C# conversion.
     */
    //@Parameter(defaultValue = "${skipConversion}", property = "skipConversion", required = false)
    boolean skip;

    @TaskAction
    public void convert() {
        if (skip)
            return;

        if (j2objcHome == null)
            throw new RuntimeException("j2objcHome property not set");

        doConvert(false);

        boolean testSourceExists = false
        project.sourceSets['test'].getAllJava().getSrcDirs().each {
            if (it.exists())
                testSourceExists = true;
        }

        if (testSourceExists) {
            doConvert(true);
        }
    }

    private void doConvert(boolean isTestSource) {
        File objCOutputDirectory = isTestSource ? testOutputDirectory : outputDirectory;

        // Delete the generated source directories
        Utils.deleteChildDirectoriesExcept(objCOutputDirectory, "Properties", "nontranslated", "Bin", "obj");

        Logger myLogger = getLogger();
        File j2objcLibDirectory = new File(j2objcHome, "lib");

        SourceSet sourceSet = project.sourceSets[isTestSource ? 'test' : 'main']

        ConfigurableFileCollection sourcepath = project.files(sourceSet.getAllJava().getSrcDirs())
        if (isTestSource) {
            SourceSet mainSourceSet = project.sourceSets['main']
            sourcepath.add(project.files(mainSourceSet.getAllJava().getSrcDirs()))
        }

        // Get all the Java source files, from the source directory set defined for Java.   Skip source directories
        // whose name ends with "-nontranslated"
        ArrayList<File> sourceFiles = new ArrayList<>();
        sourceSet.java.visit(new FileVisitor() {
            @Override
            void visitDir(FileVisitDetails fileVisitDetails) {
                File parentFile = fileVisitDetails.file.getParentFile()

                if (parentFile.getName().endsWith("-nontranslated"))
                    fileVisitDetails.stopVisiting()
            }

            @Override
            void visitFile(FileVisitDetails fileVisitDetails) {
                File parentFile = fileVisitDetails.file.getParentFile()
                if (parentFile.getName().endsWith("-nontranslated"))
                    fileVisitDetails.stopVisiting()
                else sourceFiles.add(fileVisitDetails.file)
            }
        })

        ConfigurableFileCollection classpath = project.files(
                new File(j2objcLibDirectory, "j2objc_annotations.jar"), sourceSet.compileClasspath);

        String j2objcCommandLine;
        project.javaexec {
            workingDir = project.getProjectDir()

            // Because JavaExec doesn't have proper support for exec'ing runnable jars, use this hack,
            // saying the main class is "-jar", to make it work
            main = "-jar"
            args(new File(j2objcLibDirectory, "j2objc.jar").getAbsolutePath());

            args("-Xbootclasspath:" + project.files(new File(j2objcLibDirectory, "jre_emul.jar")).getAsPath());

            args("-classpath", classpath.getAsPath());

            if (! sourcepath.isEmpty())
                args("-sourcepath", sourcepath.getAsPath())

            args("-d", objCOutputDirectory.getPath())

            if (useArc)
                args("-use-arc");
            if (docComments)
                args("--doc-comments");

            for (File file : sourceFiles) {
                args(file.getAbsolutePath());
            }

            System.out.println("Executingx: " + commandLine);

            myLogger.info("Executing: " + commandLine);
        }

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
