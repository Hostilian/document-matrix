package com.example

import zio._
import zio.http._

object DocumentHttpApi extends ZIOAppDefault {
  val doc: Document[String] = Document.Vert(List(
    Document.Cell("API Header"),
    Document.Horiz(List(
      Document.Cell("Left cell"),
      Document.Cell("Right cell")
    )),
    Document.Cell("Footer")
  ))

  val app = Http.collect[Request] {
    case Method.GET -> Root / "document" =>
      Response.text(DocumentPrinter.printTree(doc))
  }

  val run = Server.serve(app).provide(Server.default)
}
