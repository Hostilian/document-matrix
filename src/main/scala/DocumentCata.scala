package com.example

object DocumentCata {
  // Catamorphism for Document (fold)
  def cata[A, B](alg: Document[B] => B)(doc: Document[A])(leaf: A => B): B =
    doc match {
      case Document.Cell(a)    => leaf(a)
      case Document.Horiz(cs)  => alg(Document.Horiz(cs.map(c => Document.Cell(cata(alg)(c)(leaf))))) 
      case Document.Vert(cs)   => alg(Document.Vert(cs.map(c => Document.Cell(cata(alg)(c)(leaf)))))
    }
}
