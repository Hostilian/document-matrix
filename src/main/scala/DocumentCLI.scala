package com.example

import zio._
import zio.Console._
import org.jline.terminal.{Terminal, TerminalBuilder}
import org.jline.reader.{LineReader, LineReaderBuilder}

/**
 * Interactive CLI for Document Matrix with advanced features.
 * Supports multiple commands, interactive mode, and rich formatting.
 */
object DocumentCLI extends ZIOAppDefault {

  // Sample documents for demonstration
  val sampleDoc: Document[String] = Document.Vert(List(
    Document.Cell("📋 Document Header"),
    Document.Horiz(List(
      Document.Cell("📊 Left Panel"),
      Document.Vert(List(
        Document.Cell("🔝 Top Right"),
        Document.Cell("🔽 Bottom Right")
      ))
    )),
    Document.Cell("📄 Document Footer")
  ))

  val complexDoc: Document[String] = Document.Vert(List(
    Document.Cell("🌟 Complex Document"),
    Document.Horiz(List(
      Document.Vert(List(
        Document.Cell("📈 Chart 1"),
        Document.Cell("📊 Data 1")
      )),
      Document.Vert(List(
        Document.Cell("📉 Chart 2"), 
        Document.Horiz(List(
          Document.Cell("🔢 Metrics A"),
          Document.Cell("📏 Metrics B")
        ))
      )),
      Document.Cell("📝 Summary")
    ))
  ))

  // CLI Commands
  sealed trait Command
  object Command {
    case object ShowSample extends Command
    case object ShowComplex extends Command
    case object ShowStats extends Command
    case object ShowJson extends Command
    case object ShowHtml extends Command
    case object Transform extends Command
    case object Validate extends Command
    case object Help extends Command
    case object Exit extends Command
    
    def parse(input: String): Command = input.trim.toLowerCase match {
      case "sample" | "s" => ShowSample
      case "complex" | "c" => ShowComplex
      case "stats" | "st" => ShowStats
      case "json" | "j" => ShowJson
      case "html" | "h" => ShowHtml
      case "transform" | "t" => Transform
      case "validate" | "v" => Validate
      case "help" | "?" => Help
      case "exit" | "quit" | "q" => Exit
      case _ => Help
    }
  }

  def printBanner(): UIO[Unit] = {
    val banner = 
      s"""
      |${DocumentPrinter.Style.success("╔═══════════════════════════════════════╗")}
      |${DocumentPrinter.Style.success("║")}     ${DocumentPrinter.Style.info("📋 Document Matrix CLI")}        ${DocumentPrinter.Style.success("║")}
      |${DocumentPrinter.Style.success("║")}   ${DocumentPrinter.Style.info("Functional Document Processing")}   ${DocumentPrinter.Style.success("║")}
      |${DocumentPrinter.Style.success("╚═══════════════════════════════════════╝")}
      |""".stripMargin
    
    printLine(banner)
  }

  def printHelp(): UIO[Unit] = {
    val help = 
      s"""
      |${DocumentPrinter.Style.info("Available Commands:")}
      |  ${DocumentPrinter.Style.success("sample")}    (s) - Show sample document tree
      |  ${DocumentPrinter.Style.success("complex")}   (c) - Show complex document example
      |  ${DocumentPrinter.Style.success("stats")}     (st)- Show document statistics
      |  ${DocumentPrinter.Style.success("json")}      (j) - Export as JSON
      |  ${DocumentPrinter.Style.success("html")}      (h) - Export as HTML table
      |  ${DocumentPrinter.Style.success("transform")} (t) - Transform document values
      |  ${DocumentPrinter.Style.success("validate")}  (v) - Validate document structure
      |  ${DocumentPrinter.Style.success("help")}      (?) - Show this help
      |  ${DocumentPrinter.Style.success("exit")}      (q) - Exit application
      |""".stripMargin
    
    printLine(help)
  }

  def handleCommand(command: Command): UIO[Boolean] = command match {
    case Command.ShowSample =>
      for {
        _ <- printLine(DocumentPrinter.Style.info("\n🌟 Sample Document:"))
        _ <- printLine(DocumentPrinter.printTree(sampleDoc))
      } yield true

    case Command.ShowComplex =>
      for {
        _ <- printLine(DocumentPrinter.Style.info("\n🚀 Complex Document:"))
        _ <- printLine(DocumentPrinter.printFull(complexDoc))
      } yield true

    case Command.ShowStats =>
      for {
        _ <- printLine(DocumentPrinter.Style.info("\n📊 Document Statistics:"))
        _ <- printLine(DocumentPrinter.printStats(sampleDoc))
      } yield true

    case Command.ShowJson =>
      for {
        _ <- printLine(DocumentPrinter.Style.info("\n📄 JSON Export:"))
        _ <- printLine(DocumentPrinter.printJson(sampleDoc))
      } yield true

    case Command.ShowHtml =>
      for {
        _ <- printLine(DocumentPrinter.Style.info("\n🌐 HTML Export:"))
        _ <- printLine(DocumentPrinter.printHtml(sampleDoc))
      } yield true

    case Command.Transform =>
      for {
        _ <- printLine(DocumentPrinter.Style.info("\n🔄 Document Transformation (uppercase):"))
        transformed = Document.traverseM[Document.Id, String, String](_.toUpperCase)(sampleDoc)
        _ <- printLine(DocumentPrinter.printTree(transformed))
      } yield true

    case Command.Validate =>
      for {
        _ <- printLine(DocumentPrinter.Style.info("\n✅ Document Validation:"))
        result = Document.validate(sampleDoc)
        _ <- result match {
          case Right(_) => printLine(DocumentPrinter.Style.success("✓ Document is valid"))
          case Left(error) => printLine(DocumentPrinter.Style.error(s"✗ Validation failed: $error"))
        }
      } yield true

    case Command.Help =>
      printHelp().as(true)

    case Command.Exit =>
      printLine(DocumentPrinter.Style.success("👋 Goodbye!")).as(false)
  }

  def interactiveMode(): ZIO[Any, Throwable, Unit] = {
    def loop(): ZIO[Any, Throwable, Unit] = 
      for {
        _ <- print(DocumentPrinter.Style.info("\n📋 > "))
        input <- readLine
        command = Command.parse(input)
        continue <- handleCommand(command)
        _ <- if (continue) loop() else ZIO.unit
      } yield ()

    for {
      _ <- printBanner()
      _ <- printHelp()
      _ <- loop()
    } yield ()
  }

  def batchMode(args: List[String]): UIO[Unit] = {
    val command = args.headOption.map(Command.parse).getOrElse(Command.ShowSample)
    handleCommand(command).unit
  }

  val run: ZIO[Any, Throwable, Unit] = 
    for {
      args <- getArgs
      _ <- if (args.isEmpty) interactiveMode() else batchMode(args.toList)
    } yield ()
}
