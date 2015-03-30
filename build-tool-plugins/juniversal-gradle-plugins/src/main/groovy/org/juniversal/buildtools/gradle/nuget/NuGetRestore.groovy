package org.juniversal.buildtools.gradle.nuget

class NuGetRestore extends BaseNuGet {
    def packagesDirectory
    def solutionDirectory
    def packagesConfigFile
    def solutionFile
    def source
    def configFile
    def nocache = false

    NuGetRestore() {
        super('restore')
    }

    @Override
    void verifyCommand() {
    }

    @Override
    List<String> extraCommands() {
        def commandLineArgs = new ArrayList<String>()

        if (packagesDirectory) {
            commandLineArgs += "-PackagesDirectory"
            commandLineArgs += packagesDirectory
        }
        if (solutionDirectory) {
            commandLineArgs += "-SolutionDirectory"
            commandLineArgs += solutionDirectory
        }
        if (source) {
            commandLineArgs += "-Source"
            commandLineArgs += source
        }
        if (configFile) {
            commandLineArgs += "-ConfigFile"
            commandLineArgs += configFile
        }
        if (packagesConfigFile) {
            commandLineArgs += packagesConfigFile
        }
        if (solutionFile) {
            commandLineArgs += solutionFile
        }
        if (nocache) {
            commandLineArgs += "-NoCache"
        }

        return commandLineArgs
    }

    def getPackagesFolder() {
        // https://docs.nuget.org/consume/command-line-reference#restore-command
        // If -PackagesDirectory <packagesDirectory> is specified, <packagesDirectory> is used as the packages directory.
        if (packagesDirectory) {
            return packagesDirectory
        }

        // If -SolutionDirectory <solutionDirectory> is specified, <solutionDirectory>\packages is used as the packages directory.
        // SolutionFile can also be provided.
        // Otherwise use '.\packages'
        def solutionDir = solutionFile ? solutionFile.getParent() : solutionDirectory
        return new File(solutionDir ?: '.', 'packages').absolutePath
    }
}
