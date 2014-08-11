package org.gradle

import org.junit.Test
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.api.Project
import org.juniversal.JavaToCSharpTask

import static org.junit.Assert.*

class JavaToCSharpPluginTest {
    @Test
    public void pluginAddsTaskToProject() {
        Project project = ProjectBuilder.builder().build()
        project.apply plugin: 'javaToCSharp'

        assertTrue(project.tasks.javaToCSharp != null)
    }
}
