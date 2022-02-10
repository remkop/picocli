package picocli.examples.scala

import java.io.File
import java.util.ArrayList
import java.util.concurrent.Callable
import java.math.BigInteger
import java.nio.file.{Files, Paths}
import java.security.MessageDigest
import java.util

import picocli.CommandLine
import picocli.CommandLine.{Command, HelpCommand, Option, Parameters}

@Command(name = "checksum", version = Array("checksum 4.1.4"),
  mixinStandardHelpOptions = true, subcommands = Array(classOf[HelpCommand]),
  description = Array("Prints the checksum (SHA-1 by default) of file(s) to STDOUT."))
class Checksum extends Callable[Int] {

  @Parameters(index = "0..*", arity = "1..*", paramLabel = "<file 1> <file 2>",
    description = Array("The file(s) whose checksum to calculate."))
  private val files = new util.ArrayList[File]

  @Option(names = Array("-a", "--algorithm"),
    description = Array("MD5, SHA-1, SHA-256, ..."))
  private var algorithm = "SHA-1"

  def call(): Int = {
    files.forEach {
      file =>
        if (Files.isRegularFile(Paths.get(file.getPath))) {
          val fileContents = Files.readAllBytes(file.toPath)
          val digest = MessageDigest.getInstance(algorithm).digest(fileContents)
          println(("%s: %0" + digest.size * 2 + "x").format(file.getName, new BigInteger(1, digest)))
        }
        else {
          println("File '%s' does not exist!".format(file.getPath))
        }
    }
    0
  }
}

object Checksum {
  def main(args: Array[String]): Unit = {
    System.exit(new CommandLine(new Checksum()).execute(args: _*))
  }
}
