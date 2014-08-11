package org.gradle

import org.junit.Test
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.api.Project
import org.juniversal.MSBuildTask

import static org.junit.Assert.*

class MSBuildTaskTest {
    @Test
    public void canAddTaskToProject() {
        Project project = ProjectBuilder.builder().build()
        def task = project.task('msbuild', type: MSBuildTask)
        assertTrue(task instanceof MSBuildTask)
    }
}
