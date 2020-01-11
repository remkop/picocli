package picocli.examples.scala.subcommands.SubCmdsViaClasses

import java.util.Locale
import picocli.CommandLine.{Command, Parameters}

@Command(name = "language", description = Array("Resolve ISO language code (ISO 639-1 or -2, two/three letters)"))
class SubCmd2 extends Runnable  {
  @Parameters(arity = "1..*n", paramLabel = "<language code 1> <language code 2>",
    description = Array("language code(s) to be resolved"))
  val languageCodes = new Array[String](0)

  def run(): Unit = {
    for (code <- languageCodes) {
      println(s"${code.toUpperCase()}: ".concat(new Locale(code).getDisplayLanguage))
    }
  }
}
