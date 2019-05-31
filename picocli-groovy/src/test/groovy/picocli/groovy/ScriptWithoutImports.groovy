package picocli.groovy

@picocli.CommandLine.Command(name = 'cmd', description = 'my description')

@picocli.groovy.PicocliScript

@picocli.CommandLine.Option(names = ["-h", "--help"], usageHelp = true)
@groovy.transform.Field boolean usageHelpRequested = false

["hi"]