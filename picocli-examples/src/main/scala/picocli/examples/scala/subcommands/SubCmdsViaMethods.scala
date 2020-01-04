package picocli.examples.scala.subcommands

import java.util.Locale

import picocli.CommandLine
import picocli.CommandLine.{Command, HelpCommand, Parameters}

@Command(name = "ISOCodeResolve", version = Array("4.1.4"),
  mixinStandardHelpOptions = true, subcommands = Array(classOf[HelpCommand]),
  description = Array("Resolve ISO country codes (ISO-3166-1) or language codes (ISO 639-1 or -2)"))
class SubCmdsViaMethods extends Runnable {
  @CommandLine.Spec
  val spec: CommandLine.Model.CommandSpec = null

  @Command(name = "country", description = Array("Resolve ISO country code (ISO-3166-1, Alpha-2 code)"))
  def country(@Parameters(arity = "1..*n", paramLabel = "<country code1> <country code2>",
    description = Array("country code(s) to be resolved"))
              countryCodes: Array[String]): Unit = {
    for (code <- countryCodes) {
      println(s"${code.toUpperCase()}: ".concat(new Locale("", code).getDisplayCountry))
    }
  }

  @Command(name = "language", description = Array("Resolve ISO language code (ISO 639-1 or -2, two/three letters)"))
  def language(@Parameters(arity = "1..*n", paramLabel = "<language code 1> <language code 2>",
    description = Array("language code(s) to be resolved"))
               languageCodes: Array[String]): Unit = {
    for (code <- languageCodes) {
      println(s"${code.toUpperCase()}: ".concat(new Locale(code).getDisplayLanguage))
    }
  }

  def run(): Unit = {
    throw new CommandLine.ParameterException(spec.commandLine(), "Specify a subcommand")
  }
}

object SubCmdsViaMethods {
  def main(args: Array[String]): Unit = {
    System.exit(new CommandLine(new SubCmdsViaMethods()).execute(args: _*))
  }
}
