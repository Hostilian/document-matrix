package com.example

import fansi._
import zio.json._

/**
 * Advanced ANSI printer for Document with tree-style rendering.
 * Supports multiple output formats and customizable styling.
 */
object DocumentPrinter {

  // ANSI styling utilities
  object Style {
    def cell(str: String): String = fansi.Color.Green(fansi.Bold.On(str)).toString
    def container(str: String): String = fansi.Color.Blue(fansi.Bold.On(str)).toString
    def stats(str: String): String = fansi.Color.Yellow(str).toString
    def separator(str: String): String = fansi.Color.Magenta(str).toString
    def error(str: String): String = fansi.Color.Red(fansi.Bold.On(str)).toString
    def success(str: String): String = fansi.Color.Green(fansi.Bold.On(str)).toString
    def info(str: String): String = fansi.Color.Cyan(str).toString
  }

  /**
   * Tree-style rendering with ANSI colors and Unicode box-drawing characters.
   */
  def printTree[A](doc: Document[A], depth: Int = 0, isLast: Boolean = true, prefix: String = ""): String = {
    val connector = if (isLast) "└── " else "├── "
    val childPrefix = prefix + (if (isLast) "    " else "│   ")
    
    doc match {
      case Document.Cell(a) => 
        prefix + connector + Style.cell(s"📄 ${a.toString}")
        
      case Document.Horiz(cs) =>
        val header = prefix + connector + Style.container("📊 Horizontal")
        val children = cs.zipWithIndex.map { case (child, i) =>
          printTree(child, depth + 1, i == cs.length - 1, childPrefix)
        }
        (header :: children).mkString("\n")
        
      case Document.Vert(cs) =>
        val header = prefix + connector + Style.container("📋 Vertical")
        val children = cs.zipWithIndex.map { case (child, i) =>
          printTree(child, depth + 1, i == cs.length - 1, childPrefix)
        }
        (header :: children).mkString("\n")
    }
  }

  /**
   * Compact single-line representation.
   */
  def printCompact[A](doc: Document[A]): String = doc match {
    case Document.Cell(a)     => a.toString
    case Document.Horiz(cs)   => cs.map(printCompact).mkString("[", " | ", "]")
    case Document.Vert(cs)    => cs.map(printCompact).mkString("{", " / ", "}")
  }

  /**
   * JSON-style pretty printing.
   */
  def printJson[A](doc: Document[A], indent: Int = 0): String = {
    val spaces = "  " * indent
    doc match {
      case Document.Cell(a) => 
        s"""${spaces}{"type": "cell", "value": "${a.toString}"}"""
        
      case Document.Horiz(cs) =>
        val children = cs.map(printJson(_, indent + 1)).mkString(",\n")
        s"""${spaces}{"type": "horiz", "children": [\n${children}\n${spaces}]}"""
        
      case Document.Vert(cs) =>
        val children = cs.map(printJson(_, indent + 1)).mkString(",\n")
        s"""${spaces}{"type": "vert", "children": [\n${children}\n${spaces}]}"""
    }
  }

  /**
   * HTML table representation.
   */
  def printHtml[A](doc: Document[A]): String = {
    def renderTable(doc: Document[A]): String = doc match {
      case Document.Cell(a) => 
        s"""<td style="border: 1px solid #ccc; padding: 8px;">${a.toString}</td>"""
        
      case Document.Horiz(cs) =>
        cs.map(renderTable).mkString("")
        
      case Document.Vert(cs) =>
        cs.map(c => s"<tr>${renderTable(c)}</tr>").mkString("")
    }
    
    s"""<table style="border-collapse: collapse; font-family: monospace;">
       |${renderTable(doc)}
       |</table>""".stripMargin
  }

  /**
   * Statistics about document structure.
   */
  def printStats[A](doc: Document[A]): String = {
    val cellCount = DocumentCata.cata(DocumentCata.Algebras.countCells)(doc)(_ => 1)
    val maxDepth = DocumentCata.cata(DocumentCata.Algebras.maxDepth)(doc)(_ => 1)
    val values = DocumentCata.cata(DocumentCata.Algebras.flatten)(doc)(List(_))
    
    val stats = List(
      s"📊 Total cells: ${cellCount}",
      s"📏 Max depth: ${maxDepth}",
      s"📝 Unique values: ${values.distinct.length}",
      s"🔢 Total values: ${values.length}"
    )
    
    Style.stats(stats.mkString("\n"))
  }

  /**
   * Combined output with tree view and statistics.
   */
  def printFull[A](doc: Document[A]): String = {
    val tree = printTree(doc)
    val stats = printStats(doc)
    s"${tree}\n\n${Style.separator("═" * 50)}\n${stats}"
  }

  // Styling utilities
  object Style {
    def cell(str: String): String = fansi.Color.Green(fansi.Bold.On(str)).toString
    def container(str: String): String = fansi.Color.Blue(fansi.Bold.On(str)).toString
    def stats(str: String): String = fansi.Color.Yellow(str).toString
    def separator(str: String): String = fansi.Color.Magenta(str).toString
    def error(str: String): String = fansi.Color.Red(fansi.Bold.On(str)).toString
    def success(str: String): String = fansi.Color.Green(fansi.Bold.On(str)).toString
    def info(str: String): String = fansi.Color.Cyan(str).toString
  }

  // Output format enum
  sealed trait OutputFormat
  object OutputFormat {
    case object Tree extends OutputFormat
    case object Compact extends OutputFormat  
    case object Json extends OutputFormat
    case object Html extends OutputFormat
    case object Stats extends OutputFormat
    case object Full extends OutputFormat
  }

  /**
   * Main rendering function with format selection.
   */
  def render[A](doc: Document[A], format: OutputFormat = OutputFormat.Tree): String = 
    format match {
      case OutputFormat.Tree    => printTree(doc)
      case OutputFormat.Compact => printCompact(doc)
      case OutputFormat.Json    => printJson(doc)
      case OutputFormat.Html    => printHtml(doc)
      case OutputFormat.Stats   => printStats(doc)
      case OutputFormat.Full    => printFull(doc)
    }
}
