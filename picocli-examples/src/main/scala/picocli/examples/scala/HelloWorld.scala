package picocli.examples.scala

import java.util.concurrent.Callable

import picocli.CommandLine
import picocli.CommandLine.{Command, Option}

@Command(name = "MyApp", version = Array("Scala picocli demo v4.1"),
  mixinStandardHelpOptions = true, // add --help and --version options
  description = Array("@|bold Scala|@ @|underline picocli|@ example"))
class HelloWorld extends Callable[Int] {

  @Option(names = Array("-c", "--count"), paramLabel = "COUNT",
    description = Array("the count"))
  private var count: Int = 1

  def call(): Int = {
    for (i <- 0 until count) {
      println(s"Hello world $i...")
    }
    0
  }
}

object HelloWorld {
  def main(args: Array[String]): Unit = {
    System.exit(new CommandLine(new HelloWorld()).execute(args: _*))
  }
}