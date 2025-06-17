package com.example.examples

import com.example.{Document, DocumentCata, DocumentPrinter}
import zio._
import zio.json._
import cats.{Id, Monad}
import cats.instances.option._
import cats.instances.list._
import cats.syntax.all._

// ZIO Monad instance for use in traverseM
given Monad[({type L[X] = ZIO[Any, Nothing, X]})#L] = new Monad[({type L[X] = ZIO[Any, Nothing, X]})#L] {
  def pure[A](x: A): ZIO[Any, Nothing, A] = ZIO.succeed(x)
  def flatMap[A, B](fa: ZIO[Any, Nothing, A])(f: A => ZIO[Any, Nothing, B]): ZIO[Any, Nothing, B] = fa.flatMap(f)
  def tailRecM[A, B](a: A)(f: A => ZIO[Any, Nothing, Either[A, B]]): ZIO[Any, Nothing, B] =
    ZIO.succeed(a).flatMap(f).flatMap {
      case Left(nextA) => tailRecM(nextA)(f)
      case Right(b) => ZIO.succeed(b)
    }
}

/**
 * Comprehensive examples showcasing Document Matrix capabilities.
 * Demonstrates real-world usage patterns and advanced functional programming.
 */
object DocumentExamples extends ZIOAppDefault {

  // Example 1: Basic Document Construction
  val basicExample: Document[String] = {
    import Document._
    
    vert(
      cell("📋 Invoice Header"),
      horiz(
        cell("👤 Customer: John Doe"),
        cell("📅 Date: 2025-06-17")
      ),
      vert(
        cell("📦 Items:"),
        horiz(
          cell("🍎 Apples"),
          cell("💰 $5.00")
        ),
        horiz(
          cell("🍌 Bananas"), 
          cell("💰 $3.00")
        )
      ),
      cell("💵 Total: $8.00")
    )
  }

  // Example 2: Monadic Traversal with Validation
  def validateAndTransform(doc: Document[String]): Either[String, Document[String]] = {
    val validateCell: String => Either[String, String] = value =>
      if (value.trim.nonEmpty) Right(value.toUpperCase)
      else Left(s"Empty cell value: '$value'")
    
    Document.traverseM[({type L[X] = Either[String, X]})#L, String, String](validateCell)(doc)
  }

  // Example 3: Async Processing with ZIO
  def processDocumentAsync(doc: Document[String]): ZIO[Any, Throwable, Document[String]] = {
    val enrichCell: String => ZIO[Any, Throwable, String] = value =>
      ZIO.attempt {
        Thread.sleep(10) // Simulate async processing
        s"🚀 Processed: $value"
      }
    
    Document.traverseM[({type L[X] = ZIO[Any, Throwable, X]})#L, String, String](enrichCell)(doc)
  }

  // Example 4: Complex Business Logic - Financial Report
  case class FinancialData(account: String, amount: BigDecimal, currency: String)
  
  val financialReport: Document[FinancialData] = {
    import Document._
    
    vert(
      cell(FinancialData("Header", BigDecimal("0"), "USD")),
      horiz(
        vert(
          cell(FinancialData("Revenue", BigDecimal("100000"), "USD")),
          cell(FinancialData("Costs", BigDecimal("60000"), "USD"))
        ),
        vert(
          cell(FinancialData("Assets", BigDecimal("500000"), "USD")),
          cell(FinancialData("Liabilities", BigDecimal("200000"), "USD"))
        )
      ),
      cell(FinancialData("Net Profit", BigDecimal("40000"), "USD"))
    )
  }

  // Example 5: Currency Conversion
  def convertCurrency(
    exchangeRate: BigDecimal
  )(data: FinancialData): ZIO[Any, Nothing, FinancialData] =
    ZIO.succeed(data.copy(
      amount = data.amount * exchangeRate,
      currency = "EUR"
    ))

  // Example 6: Document Analytics
  def analyzeDocument[A](doc: Document[A]): DocumentAnalytics = {
    val cellCount = DocumentCata.countTotalCells(doc)
    val maxDepth = DocumentCata.calculateMaxDepth(doc)
    val values = DocumentCata.flattenToList(doc)
    
    DocumentAnalytics(
      totalCells = cellCount,
      maxDepth = maxDepth,
      uniqueValues = values.distinct.length,
      structure = analyzeStructure(doc)
    )
  }

  case class DocumentAnalytics(
    totalCells: Int,
    maxDepth: Int,
    uniqueValues: Int,
    structure: StructureAnalysis
  )

  case class StructureAnalysis(
    hasHorizontalDivisions: Boolean,
    hasVerticalDivisions: Boolean,
    isBalanced: Boolean,
    complexity: String
  )

  def analyzeStructure[A](doc: Document[A]): StructureAnalysis = {
    def hasHoriz(d: Document[A]): Boolean = d match {
      case Document.Cell(_) => false
      case Document.Horiz(_) => true
      case Document.Vert(children) => children.exists(hasHoriz)
    }
    
    def hasVert(d: Document[A]): Boolean = d match {
      case Document.Cell(_) => false
      case Document.Horiz(children) => children.exists(hasVert)
      case Document.Vert(_) => true
    }
    
    val cellCount = DocumentCata.countTotalCells(doc)
    val complexity = cellCount match {
      case n if n <= 3 => "Simple"
      case n if n <= 10 => "Moderate"
      case n if n <= 20 => "Complex"
      case _ => "Very Complex"
    }
    
    StructureAnalysis(
      hasHorizontalDivisions = hasHoriz(doc),
      hasVerticalDivisions = hasVert(doc),
      isBalanced = isDocumentBalanced(doc),
      complexity = complexity
    )
  }

  def isDocumentBalanced[A](doc: Document[A]): Boolean = {
    def childrenSizes(d: Document[A]): List[Int] = d match {
      case Document.Cell(_) => List(1)
      case Document.Horiz(children) => children.map(DocumentCata.countTotalCells)
      case Document.Vert(children) => children.map(DocumentCata.countTotalCells)
    }
    
    val sizes = childrenSizes(doc)
    if (sizes.length <= 1) true
    else {
      val max = sizes.max
      val min = sizes.min
      max <= min * 2 // Balanced if largest is at most 2x smallest
    }
  }

  // Example 7: Document Transformation Pipeline
  def transformationPipeline(doc: Document[String]): ZIO[Any, String, Document[String]] = {
    for {
      // Step 1: Validate
      validated <- ZIO.fromEither(validateAndTransform(doc))
      
      // Step 2: Async enrichment
      enriched <- processDocumentAsync(validated).mapError(_.getMessage)
      
      // Step 3: Additional processing
      finalDoc = Document.traverseM[Id, String, String](
        value => s"✨ Final: ${value.take(50)}..."
      )(enriched)
      
    } yield finalDoc
  }

  // Example 8: JSON Serialization/Deserialization
  def serializeToJson[A: JsonEncoder](doc: Document[A]): String =
    doc.toJson

  def deserializeFromJson[A: JsonDecoder](json: String): Either[String, Document[A]] =
    json.fromJson[Document[A]]

  // Example 9: Performance Benchmarking
  def benchmarkOperations(doc: Document[String]): ZIO[Any, Nothing, BenchmarkResults] = {
    for {
      start1 <- Clock.nanoTime
      _ <- ZIO.succeed(Document.traverseM[Id, String, String](_.reverse)(doc))
      end1 <- Clock.nanoTime
      traversalTime = (end1 - start1) / 1000000 // Convert to milliseconds
      
      start2 <- Clock.nanoTime
      _ <- ZIO.succeed(DocumentCata.countTotalCells(doc))
      end2 <- Clock.nanoTime
      cataTime = (end2 - start2) / 1000000
      
      start3 <- Clock.nanoTime
      _ <- ZIO.succeed(DocumentPrinter.printTree(doc))
      end3 <- Clock.nanoTime
      printTime = (end3 - start3) / 1000000
      
    } yield BenchmarkResults(traversalTime, cataTime, printTime)
  }

  case class BenchmarkResults(
    traversalTimeMs: Long,
    catamorphismTimeMs: Long,
    printingTimeMs: Long
  )

  // Main demonstration program
  def run = for {
    _ <- Console.printLine("🚀 Document Matrix Examples")
    _ <- Console.printLine("=" * 50)
    
    // Example 1: Basic usage
    _ <- Console.printLine("\n📋 Basic Document:")
    _ <- Console.printLine(DocumentPrinter.printTree(basicExample))
    
    // Example 2: Analytics
    _ <- Console.printLine("\n📊 Document Analytics:")
    analytics = analyzeDocument(basicExample)
    _ <- Console.printLine(s"📈 Cells: ${analytics.totalCells}, Depth: ${analytics.maxDepth}")
    _ <- Console.printLine(s"🏗️  Structure: ${analytics.structure.complexity}")
    
    // Example 3: Transformation
    _ <- Console.printLine("\n🔄 Transformation Pipeline:")
    transformed <- transformationPipeline(basicExample).catchAll(error => 
      ZIO.succeed(Document.cell(s"❌ Error: $error"))
    )
    _ <- Console.printLine(DocumentPrinter.printCompact(transformed))
    
    // Example 4: Financial processing
    _ <- Console.printLine("\n💰 Financial Report:")
    _ <- Console.printLine(DocumentPrinter.printTree(financialReport.map(_.toString)))
    
    convertedReport <- Document.traverseM[({type L[X] = ZIO[Any, Nothing, X]})#L, FinancialData, FinancialData](
      convertCurrency(BigDecimal("0.85"))
    )(financialReport)
    _ <- Console.printLine("\n💶 Converted to EUR:")
    _ <- Console.printLine(DocumentPrinter.printCompact(convertedReport.map(_.toString)))
    
    // Example 5: Performance benchmarking
    _ <- Console.printLine("\n⚡ Performance Benchmark:")
    benchmark <- benchmarkOperations(basicExample)
    _ <- Console.printLine(s"🏃 Traversal: ${benchmark.traversalTimeMs}ms")
    _ <- Console.printLine(s"📐 Catamorphism: ${benchmark.catamorphismTimeMs}ms")
    _ <- Console.printLine(s"🖨️  Printing: ${benchmark.printingTimeMs}ms")
    
    // Example 6: JSON serialization
    _ <- Console.printLine("\n📄 JSON Serialization:")
    json = serializeToJson(basicExample)
    _ <- Console.printLine(json.take(100) + "...")
    
    _ <- Console.printLine("\n✅ Examples completed successfully!")
    
  } yield ()
}
