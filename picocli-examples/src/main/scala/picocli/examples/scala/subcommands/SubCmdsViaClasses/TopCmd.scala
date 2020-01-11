package picocli.examples.scala.subcommands.SubCmdsViaClasses

import picocli.CommandLine
import picocli.CommandLine.{Command, HelpCommand}

@Command(name = "ISOCodeResolve", version = Array("4.1.4"),
  mixinStandardHelpOptions = true,
  subcommands = Array(classOf[SubCmd1], classOf[SubCmd2], classOf[HelpCommand]),
  description = Array("Resolve ISO country codes (ISO-3166-1) or language codes (ISO 639-1 or -2)"))
class TopCmd extends Runnable {
  @CommandLine.Spec
  val spec: CommandLine.Model.CommandSpec = null

  def run(): Unit = {
    throw new CommandLine.ParameterException(spec.commandLine(), "Specify a subcommand")
  }
}

object TopCmd {
  def main(args: Array[String]): Unit = {
    System.exit(new CommandLine(new TopCmd()).execute(args: _*))
  }
}
