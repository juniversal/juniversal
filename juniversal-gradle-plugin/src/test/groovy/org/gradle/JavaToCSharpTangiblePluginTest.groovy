package org.gradle

import org.junit.Test
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.api.Project

import static org.junit.Assert.*

class JavaToCSharpTangiblePluginTest {
    @Test
    public void pluginAddsTaskToProject() {
        Project project = ProjectBuilder.builder().build()
        project.apply plugin: 'javaToCSharpTangible'

        assertTrue(project.tasks.javaToCSharpTangible != null)
    }
}
