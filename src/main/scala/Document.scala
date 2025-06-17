package com.example

import cats.{Monad, Traverse}
import cats.syntax.all._
import zio.json._

/**
 * Document represents a document that can be subdivided horizontally or vertically
 * into 1 or more cells that can be further subdivided, or can hold a value of type A.
 * 
 * This ADT enables compositional document construction with type safety.
 */
sealed trait Document[+A] derives JsonEncoder, JsonDecoder
object Document {
  case class Cell[A](value: A) extends Document[A]
  case class Horiz[A](children: List[Document[A]]) extends Document[A] 
  case class Vert[A](children: List[Document[A]]) extends Document[A]

  // Identity type for lawful traversal
  type Id[A] = A
  implicit val idMonad: Monad[Id] = cats.Id.catsInstancesForId

  /**
   * Monadic traversal function that satisfies:
   * - f[Id](identity) = identity
   * - f[Option](Some(_)) = Some(_)
   * 
   * This enables effect-preserving transformations over document structure.
   */
  def traverseM[M[_]: Monad, A, B](f: A => M[B])(doc: Document[A]): M[Document[B]] =
    doc match {
      case Cell(a)      => f(a).map(Cell(_))
      case Horiz(list)  => list.traverse(traverseM(f)).map(Horiz(_))
      case Vert(list)   => list.traverse(traverseM(f)).map(Vert(_))
    }

  // Cats Traverse instance for Document
  implicit val documentTraverse: Traverse[Document] = new Traverse[Document] {
    def traverse[G[_]: cats.Applicative, A, B](fa: Document[A])(f: A => G[B]): G[Document[B]] =
      fa match {
        case Cell(a)      => f(a).map(Cell(_))
        case Horiz(list)  => list.traverse(traverse(_)(f)).map(Horiz(_))
        case Vert(list)   => list.traverse(traverse(_)(f)).map(Vert(_))
      }

    def foldLeft[A, B](fa: Document[A], b: B)(f: (B, A) => B): B =
      fa match {
        case Cell(a)     => f(b, a)
        case Horiz(list) => list.foldLeft(b)((acc, doc) => foldLeft(doc, acc)(f))
        case Vert(list)  => list.foldLeft(b)((acc, doc) => foldLeft(doc, acc)(f))
      }

    def foldRight[A, B](fa: Document[A], lb: cats.Eval[B])(f: (A, cats.Eval[B]) => cats.Eval[B]): cats.Eval[B] =
      fa match {
        case Cell(a)     => f(a, lb)
        case Horiz(list) => list.foldRight(lb)((doc, acc) => foldRight(doc, acc)(f))
        case Vert(list)  => list.foldRight(lb)((doc, acc) => foldRight(doc, acc)(f))
      }
  }

  // Smart constructors for better API
  def cell[A](value: A): Document[A] = Cell(value)
  def horiz[A](docs: Document[A]*): Document[A] = Horiz(docs.toList)
  def vert[A](docs: Document[A]*): Document[A] = Vert(docs.toList)

  // Validation
  def validate[A](doc: Document[A]): Either[String, Document[A]] =
    doc match {
      case Cell(_) => Right(doc)
      case Horiz(Nil) => Left("Horizontal container cannot be empty")
      case Vert(Nil) => Left("Vertical container cannot be empty") 
      case Horiz(children) => children.traverse(validate).map(Horiz(_))
      case Vert(children) => children.traverse(validate).map(Vert(_))
    }
}
