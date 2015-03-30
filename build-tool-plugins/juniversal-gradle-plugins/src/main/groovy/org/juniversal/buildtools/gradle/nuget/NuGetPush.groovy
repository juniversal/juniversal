package org.juniversal.buildtools.gradle.nuget

class NuGetPush extends BaseNuGet {
	def nupkgFile
    def version
	def Symbols
	String serverUrl
	String apiKey
	String timeout

    NuGetPush() {
		super('push')
    }

	@Override
	void verifyCommand() {
		// TODO verify push, how ?
	}

    @Override
    List<String> extraCommands() {
        def nupkgFileValue = nupkgFile

        def versionValue = version
        if (versionValue == null) {
            versionValue = project.version
        }

        if (nupkgFileValue == null) {
            def nupkgFileName = project.hasProperty('nugetId') ? project.nugetId : project.name;
            nupkgFileValue = new File(project.convention.plugins.base.distsDir, nupkgFileName).toString() + "." + versionValue + ".nupkg";
            logger.info(nupkgFileValue)
        }

        def commandLineArgs = [nupkgFileValue]
		if (serverUrl)
		{
			commandLineArgs += "-Source"
			commandLineArgs += serverUrl
		}
		if (apiKey)
		{
			commandLineArgs += "-ApiKey"
			commandLineArgs += apiKey
		}
		if (timeout)
		{
			commandLineArgs += "-Timeout"
			commandLineArgs += timeout
		}
		return commandLineArgs
    }
}
