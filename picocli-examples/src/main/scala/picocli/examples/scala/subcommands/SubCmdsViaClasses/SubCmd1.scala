package picocli.examples.scala.subcommands.SubCmdsViaClasses

import java.util.Locale
import picocli.CommandLine.{Command, HelpCommand, Parameters}

@Command(name = "country", description = Array("Resolve ISO country code (ISO-3166-1, Alpha-2 code)"))
class SubCmd1 extends Runnable  {
  @Parameters(arity = "1..*n", paramLabel = "<country code1> <country code2>",
    description = Array("country code(s) to be resolved"))
  val countryCodes = new Array[String](0)

  def run(): Unit = {
    for (code <- countryCodes) {
      println(s"${code.toUpperCase()}: ".concat(new Locale("", code).getDisplayCountry))
    }
  }
}
