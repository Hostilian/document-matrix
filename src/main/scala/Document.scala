package com.example

import cats.Monad
import cats.syntax.all._

// Document can be a single value, or split horizontally/vertically into a list of sub-documents
sealed trait Document[+A]
object Document {
  case class Cell[A](value: A) extends Document[A]
  case class Horiz[A](children: List[Document[A]]) extends Document[A]
  case class Vert[A](children: List[Document[A]]) extends Document[A]

  // General transformation with Monad M
  def traverseM[M[_]: Monad, A, B](f: A => M[B])(doc: Document[A]): M[Document[B]] =
    doc match {
      case Cell(a)      => f(a).map(Cell(_))
      case Horiz(list)  => list.traverse(traverseM(f)).map(Horiz(_))
      case Vert(list)   => list.traverse(traverseM(f)).map(Vert(_))
    }

  // Required: f[Id](identity) = identity
  type Id[A] = A
}
