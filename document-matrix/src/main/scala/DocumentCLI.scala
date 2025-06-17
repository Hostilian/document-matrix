package com.example

import zio._

object DocumentCLI extends ZIOAppDefault {
  val doc: Document[String] = Document.Vert(List(
    Document.Cell("Header"),
    Document.Horiz(List(
      Document.Cell("Left cell"),
      Document.Cell("Right cell")
    )),
    Document.Cell("Footer")
  ))

  val run =
    for {
      _ <- Console.printLine(fansi.Color.Green("Document Tree:"))
      _ <- Console.printLine(DocumentPrinter.printTree(doc))
    } yield ()
}
