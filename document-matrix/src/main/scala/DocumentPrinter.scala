package com.example

import fansi._

// Simple ANSI pretty-printer for Document
object DocumentPrinter {
  def printTree[A](doc: Document[A], depth: Int = 0): String = doc match {
    case Document.Cell(a)   => "  " * depth + Style.Bold("Cell: ") + a.toString
    case Document.Horiz(cs) =>
      "  " * depth + Style.Underlined("Horiz") + "\n" +
        cs.map(printTree(_, depth + 1)).mkString("\n")
    case Document.Vert(cs)  =>
      "  " * depth + Style.Underlined("Vert") + "\n" +
        cs.map(printTree(_, depth + 1)).mkString("\n")
  }
  object Style {
    def Bold(str: String) = fansi.Bold.On(str)
    def Underlined(str: String) = fansi.Underlined.On(str)
  }
}
