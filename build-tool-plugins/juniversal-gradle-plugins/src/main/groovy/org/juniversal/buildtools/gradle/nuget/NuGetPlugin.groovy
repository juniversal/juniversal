package org.juniversal.buildtools.gradle.nuget

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.BasePlugin

class NuGetPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.apply plugin: 'base'
        def nuget = project.task('nugetPack', type: NuGetPack)
        nuget.group = BasePlugin.BUILD_GROUP
        nuget.description = 'Executes nuget pack command.'

        nuget = project.task('nugetPush', type: NuGetPush)
        nuget.group = BasePlugin.UPLOAD_GROUP
        nuget.description = 'Executes nuget push command.'

        nuget = project.task('nugetRestore', type: NuGetRestore)
        nuget.group = BasePlugin.BUILD_GROUP
        nuget.description = 'Executes nuget package restore command.'
        nuget.outputs.dir { nuget.getPackagesFolder() }
        project.tasks.clean.dependsOn project.tasks.cleanNugetRestore
    }
}

