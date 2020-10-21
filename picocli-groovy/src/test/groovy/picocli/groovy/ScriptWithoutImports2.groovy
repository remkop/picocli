package picocli.groovy

@picocli.CommandLine.Command(name = 'cmd', description = 'my description')

@picocli.groovy.PicocliScript2

@picocli.CommandLine.Option(names = ["-h", "--help"], usageHelp = true)
@groovy.transform.Field boolean usageHelpRequested = false

["hi"]