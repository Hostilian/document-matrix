package com.example

import zio.test._
import zio.test.Assertion._
import cats.Id
import cats.instances.option._

object DocumentSpec extends ZIOSpecDefault {
  def spec = suite("Document")(
    test("traverseM[Id] identity is identity") {
      val doc = Document.Vert(List(
        Document.Cell(1),
        Document.Horiz(List(Document.Cell(2), Document.Cell(3)))
      ))
      assert(Document.traverseM[Id, Int, Int](identity)(doc))(equalTo(doc))
    },
    test("traverseM[Option] with Some") {
      val doc = Document.Horiz(List(
        Document.Cell(5),
        Document.Cell(10)
      ))
      val f: Int => Option[Int] = x => Some(x * 2)
      val expected = Some(Document.Horiz(List(
        Document.Cell(10),
        Document.Cell(20)
      )))
      assert(Document.traverseM[Option, Int, Int](f)(doc))(equalTo(expected))
    }
  )
}
