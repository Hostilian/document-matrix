package com.example

import zio.test._
import zio.test.Assertion._
import cats.Id
import cats.instances.option._
import cats.syntax.all._

/**
 * Comprehensive test suite for Document Matrix.
 * Tests core functionality, laws, and edge cases.
 */
object DocumentSpec extends ZIOSpecDefault {

  def spec = suite("Document Matrix Test Suite")(
    suite("Document ADT")(
      test("should create cells correctly") {
        val cell = Document.Cell("test")
        assert(cell.value)(equalTo("test"))
      },
      
      test("should create horizontal containers") {
        val horiz = Document.Horiz(List(
          Document.Cell("a"), 
          Document.Cell("b")
        ))
        assert(horiz.children.length)(equalTo(2))
      },
      
      test("should create vertical containers") {
        val vert = Document.Vert(List(
          Document.Cell("x"),
          Document.Cell("y"),
          Document.Cell("z")
        ))
        assert(vert.children.length)(equalTo(3))
      }
    ),

    suite("traverseM Laws")(
      test("Identity Law: f[Id](identity) = identity") {
        val doc = Document.Vert(List(
          Document.Cell(1),
          Document.Horiz(List(
            Document.Cell(2), 
            Document.Cell(3)
          )),
          Document.Cell(4)
        ))
        
        val result = Document.traverseM[Id, Int, Int](identity)(doc)
        assert(result)(equalTo(doc))
      },

      test("Option Law: f[Option](Some(_)) preserves structure") {
        val doc = Document.Horiz(List(
          Document.Cell(5),
          Document.Cell(10)
        ))
        
        val f: Int => Option[Int] = x => Some(x * 2)
        val result = Document.traverseM[Option, Int, Int](f)(doc)
        val expected = Some(Document.Horiz(List(
          Document.Cell(10),
          Document.Cell(20)
        )))
        
        assert(result)(equalTo(expected))
      },

      test("Option with None should propagate failure") {
        val doc = Document.Vert(List(
          Document.Cell(1),
          Document.Cell(2)
        ))
        
        val f: Int => Option[String] = x => if (x > 1) None else Some(x.toString)
        val result = Document.traverseM[Option, Int, String](f)(doc)
        
        assert(result)(isNone)
      },

      test("Composition law") {
        val doc = Document.Cell(5)
        val f: Int => Option[Int] = x => Some(x + 1)
        val g: Int => Option[String] = x => Some(x.toString)
        
        val composed = Document.traverseM[Option, Int, String](x => f(x).flatMap(g))(doc)
        val sequential = Document.traverseM[Option, Int, Int](f)(doc)
          .flatMap(Document.traverseM[Option, Int, String](g))
        
        assert(composed)(equalTo(sequential))
      }
    ),

    suite("Recursion Schemes")(
      test("Catamorphism should count cells correctly") {
        val doc = Document.Vert(List(
          Document.Cell("a"),
          Document.Horiz(List(
            Document.Cell("b"),
            Document.Cell("c")
          ))
        ))
        
        val count = DocumentCata.countTotalCells(doc)
        assert(count)(equalTo(3))
      },

      test("Catamorphism should calculate max depth") {
        val doc = Document.Vert(List(
          Document.Cell("a"),
          Document.Horiz(List(
            Document.Vert(List(Document.Cell("deep"))),
            Document.Cell("b")
          ))
        ))
        
        val depth = DocumentCata.calculateMaxDepth(doc)
        assert(depth)(equalTo(4)) // Root -> Vert -> Horiz -> Vert -> Cell
      },

      test("Catamorphism should flatten correctly") {
        val doc = Document.Horiz(List(
          Document.Cell("x"),
          Document.Vert(List(
            Document.Cell("y"),
            Document.Cell("z")
          ))
        ))
        
        val flattened = DocumentCata.flattenToList(doc)
        assert(flattened.toSet)(equalTo(Set("x", "y", "z")))
      }
    ),

    suite("Document Validation")(
      test("Valid documents should pass validation") {
        val doc = Document.Vert(List(
          Document.Cell("valid"),
          Document.Horiz(List(Document.Cell("also"), Document.Cell("valid")))
        ))
        
        val result = Document.validate(doc)
        assert(result)(isRight)
      },

      test("Empty horizontal containers should fail validation") {
        val doc = Document.Horiz(Nil)
        val result = Document.validate(doc)
        assert(result)(isLeft)
      },

      test("Empty vertical containers should fail validation") {
        val doc = Document.Vert(Nil)
        val result = Document.validate(doc)
        assert(result)(isLeft)
      }
    ),

    suite("Smart Constructors")(
      test("cell constructor should work") {
        val doc = Document.cell("test")
        assert(doc)(equalTo(Document.Cell("test")))
      },

      test("horiz constructor should work with varargs") {
        val doc = Document.horiz(
          Document.cell("a"),
          Document.cell("b"),
          Document.cell("c")
        )
        assert(doc.children.length)(equalTo(3))
      },

      test("vert constructor should work with varargs") {
        val doc = Document.vert(
          Document.cell("x"),
          Document.cell("y")
        )
        assert(doc.children.length)(equalTo(2))
      }
    ),

    suite("Document Transformation")(
      test("Transform with string operations") {
        val doc = Document.Horiz(List(
          Document.Cell("hello"),
          Document.Cell("world")
        ))
        
        val uppercase = Document.traverseM[Id, String, String](_.toUpperCase)(doc)
        val expected = Document.Horiz(List(
          Document.Cell("HELLO"),
          Document.Cell("WORLD")
        ))
        
        assert(uppercase)(equalTo(expected))
      },

      test("Transform with numeric operations") {
        val doc = Document.Vert(List(
          Document.Cell(1),
          Document.Cell(2),
          Document.Cell(3)
        ))
        
        val doubled = Document.traverseM[Id, Int, Int](_ * 2)(doc)
        val expected = Document.Vert(List(
          Document.Cell(2),
          Document.Cell(4),
          Document.Cell(6)
        ))
        
        assert(doubled)(equalTo(expected))
      }
    )
  )
}
