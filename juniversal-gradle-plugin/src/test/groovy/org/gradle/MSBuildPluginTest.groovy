package org.gradle

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

import static org.junit.Assert.assertTrue

/**
 * @author Bret Johnson
 * @since 7/7/2014 6:00 PM
 */
class MSBuildPluginTest {
    @Test public void pluginAddsTaskToProject() {
        Project project = ProjectBuilder.builder().build()
        project.apply plugin: 'msbuild'

        assertTrue(project.tasks.msbuild != null)
    }
}
