package com.example

/**
 * Recursion schemes for Document ADT.
 * Provides catamorphisms (folds) for structural operations.
 */
object DocumentCata {
  
  /**
   * Catamorphism (fold) for Document structure.
   * Bottom-up traversal that applies algebra to each level.
   */
  def cata[A, B](alg: Document[B] => B)(doc: Document[A])(leaf: A => B): B =
    doc match {
      case Document.Cell(a)    => leaf(a)
      case Document.Horiz(cs)  => 
        val results = cs.map(c => cata(alg)(c)(leaf))
        alg(Document.Horiz(results))
      case Document.Vert(cs)   => 
        val results = cs.map(c => cata(alg)(c)(leaf))
        alg(Document.Vert(results))
    }

  // Common algebras
  object Algebras {
    
    /**
     * Count total number of cells in document.
     */
    def countCells: Document[Int] => Int = {
      case Document.Cell(count)  => count
      case Document.Horiz(cs)    => cs.sum
      case Document.Vert(cs)     => cs.sum
    }

    /**
     * Calculate maximum depth of document tree.
     */
    def maxDepth: Document[Int] => Int = {
      case Document.Cell(depth)  => depth
      case Document.Horiz(cs)    => if (cs.isEmpty) 0 else cs.max + 1
      case Document.Vert(cs)     => if (cs.isEmpty) 0 else cs.max + 1
    }

    /**
     * Flatten document structure to list of values.
     */
    def flatten[A]: Document[List[A]] => List[A] = {
      case Document.Cell(as)     => as
      case Document.Horiz(cs)    => cs.flatten
      case Document.Vert(cs)     => cs.flatten
    }

    /**
     * Transform each cell value with a function.
     */
    def mapCells[A, B]: (A => B) => Document[A] => Document[B] = f => {
      case Document.Cell(a)      => Document.Cell(f(a))
      case Document.Horiz(cs)    => Document.Horiz(cs.map(doc => mapCells(f)(doc)))
      case Document.Vert(cs)     => Document.Vert(cs.map(doc => mapCells(f)(doc)))
    }
  }

  // Convenience functions for common operations
  def countTotalCells[A](doc: Document[A]): Int =
    cata(Algebras.countCells)(doc)(_ => 1)

  def calculateMaxDepth[A](doc: Document[A]): Int =
    cata(Algebras.maxDepth)(doc)(_ => 1)

  def flattenToList[A](doc: Document[A]): List[A] =
    cata(Algebras.flatten)(doc)(List(_))
}
