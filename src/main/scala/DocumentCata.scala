package com.example

/**
 * Recursion schemes for Document ADT.
 * Provides catamorphisms (folds) for structural operations.
 */
object DocumentCata {
  
  /**
   * Simple fold operation for Document structure.
   * Bottom-up traversal with different functions for each case.
   */
  def fold[A, B](
    onCell: A => B,
    onHoriz: List[B] => B, 
    onVert: List[B] => B
  )(doc: Document[A]): B = doc match {
    case Document.Cell(a)     => onCell(a)
    case Document.Horiz(cs)   => onHoriz(cs.map(fold(onCell, onHoriz, onVert)))
    case Document.Vert(cs)    => onVert(cs.map(fold(onCell, onHoriz, onVert)))
  }

  // Common operations using fold
  object Algebras {
    
    /**
     * Count total number of cells in document.
     */
    def countCells[A](doc: Document[A]): Int = 
      fold[A, Int](_ => 1, _.sum, _.sum)(doc)

    /**
     * Calculate maximum depth of document tree.
     */
    def maxDepth[A](doc: Document[A]): Int = 
      fold[A, Int](_ => 1, cs => if (cs.isEmpty) 1 else cs.max + 1, cs => if (cs.isEmpty) 1 else cs.max + 1)(doc)

    /**
     * Flatten document structure to list of values.
     */
    def flatten[A](doc: Document[A]): List[A] = 
      fold[A, List[A]](List(_), _.flatten, _.flatten)(doc)
  }

  // Convenience functions for common operations
  def countTotalCells[A](doc: Document[A]): Int =
    Algebras.countCells(doc)

  def calculateMaxDepth[A](doc: Document[A]): Int =
    Algebras.maxDepth(doc)

  def flattenToList[A](doc: Document[A]): List[A] =
    Algebras.flatten(doc)
}
