package org.juniversal.buildtools.gradle.nuget

import org.gradle.api.internal.ConventionTask
import org.gradle.api.tasks.TaskAction

public class BaseNuGet extends ConventionTask {
    private static final String NUGET_EXE = 'NuGet.exe'

    String verbosity
    String command

    public BaseNuGet() {
    }

    protected BaseNuGet(String command) {
        this.command = command
    }

    protected List<String> extraCommands() { [] }

    protected void verifyCommand() {}

    @TaskAction
    def build() {
        // NuGet bootstapper executable was downloaded from http://nuget.codeplex.com/downloads/get/412077#
        // We could download it from there instead of storing it in the jar

        File nugetExe = new File(temporaryDir, NUGET_EXE)

        // because of http://issues.gradle.org/browse/GRADLE-2001
        // ( http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6207022 )
        // we need to skip cache ?

        def url = BaseNuGet.class.getResource(NUGET_EXE)
        def conn = url.openConnection()
        conn.useCaches = false

        nugetExe.setBytes(conn.getInputStream().getBytes())
        nugetExe.setExecutable(true)

        def commandLineArgs = [nugetExe]

        commandLineArgs += getCommand()
        commandLineArgs += extraCommands()

        String verb = verbosity
        if (!verb) {
            if (logger.debugEnabled) {
                verb = 'detailed'
            } else if (logger.infoEnabled) {
                verb = 'normal'
            } else {
                verb = 'quiet'
            }
        }
        if (verb) {
            commandLineArgs += '-Verbosity'
            commandLineArgs += verb
        }

        commandLineArgs += '-NonInteractive'

        project.exec {
            commandLine = commandLineArgs
        }

        // NuGet return code is always 0 .... great ....
        verifyCommand()
    }
}
