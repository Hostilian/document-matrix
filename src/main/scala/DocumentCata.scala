package com.example

import cats.syntax.all._

/**
 * Recursion schemes for Document ADT.
 * Provides catamorphisms (folds) and anamorphisms (unfolds).
 */
object DocumentCata {
  
  /**
   * Catamorphism (fold) for Document structure.
   * Bottom-up traversal that applies algebra to each level.
   */
  def cata[A, B](alg: Document[B] => B)(doc: Document[A])(leaf: A => B): B =
    doc match {
      case Document.Cell(a)    => leaf(a)
      case Document.Horiz(cs)  => alg(Document.Horiz(cs.map(cata(alg)(_)(leaf))))
      case Document.Vert(cs)   => alg(Document.Vert(cs.map(cata(alg)(_)(leaf))))
    }

  /**
   * Anamorphism (unfold) for Document structure.
   * Top-down generation from a seed value.
   */
  def ana[A, B](coalg: A => Document[A], stop: A => Option[B])(seed: A): Document[B] =
    stop(seed) match {
      case Some(b) => Document.Cell(b)
      case None    => coalg(seed) match {
        case Document.Cell(_)    => Document.Cell(seed.asInstanceOf[B]) // Should not happen
        case Document.Horiz(cs)  => Document.Horiz(cs.map(ana(coalg, stop)))
        case Document.Vert(cs)   => Document.Vert(cs.map(ana(coalg, stop)))
      }
    }

  /**
   * Hylomorphism: unfold then fold in one pass.
   */
  def hylo[A, B, C](alg: Document[C] => C, coalg: A => Document[A], stop: A => Option[B])(
    seed: A, leaf: B => C
  ): C = {
    def go(a: A): C =
      stop(a) match {
        case Some(b) => leaf(b)
        case None    => coalg(a) match {
          case Document.Cell(_)    => leaf(a.asInstanceOf[B])
          case Document.Horiz(cs)  => alg(Document.Horiz(cs.map(go)))
          case Document.Vert(cs)   => alg(Document.Vert(cs.map(go)))
        }
      }
    go(seed)
  }

  /**
   * Paramorphism: catamorphism with access to original structure.
   */
  def para[A, B](alg: Document[(Document[A], B)] => B)(doc: Document[A])(leaf: A => B): B =
    doc match {
      case Document.Cell(a)    => leaf(a)
      case Document.Horiz(cs)  => 
        val pairs = cs.map(c => (c, para(alg)(c)(leaf)))
        alg(Document.Horiz(pairs))
      case Document.Vert(cs)   => 
        val pairs = cs.map(c => (c, para(alg)(c)(leaf)))
        alg(Document.Vert(pairs))
    }

  // Common algebras
  object Algebras {
    
    /**
     * Count total number of cells in document.
     */
    def countCells[A]: Document[Int] => Int = {
      case Document.Cell(_)     => 1
      case Document.Horiz(cs)   => cs.sum
      case Document.Vert(cs)    => cs.sum
    }

    /**
     * Calculate maximum depth of document tree.
     */
    def maxDepth[A]: Document[Int] => Int = {
      case Document.Cell(_)     => 1
      case Document.Horiz(cs)   => if (cs.isEmpty) 0 else cs.max + 1
      case Document.Vert(cs)    => if (cs.isEmpty) 0 else cs.max + 1
    }

    /**
     * Flatten document structure to list of values.
     */
    def flatten[A]: Document[List[A]] => List[A] = {
      case Document.Cell(a)     => List(a)
      case Document.Horiz(cs)   => cs.flatten
      case Document.Vert(cs)    => cs.flatten
    }
  }
}
