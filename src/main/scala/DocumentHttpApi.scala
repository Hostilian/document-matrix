package com.example

import zio._
import zio.http._

/**
 * Production-ready HTTP API for Document Matrix.
 * Demonstrates ZIO HTTP with proper error handling and multiple endpoints.
 */
object DocumentHttpApi extends ZIOAppDefault {

  // Sample documents for demonstration
  val sampleDoc: Document[String] = Document.Vert(List(
    Document.Cell("🌟 API Response Header"),
    Document.Horiz(List(
      Document.Cell("📊 Left Data Panel"),
      Document.Vert(List(
        Document.Cell("🔝 Top Right Section"),
        Document.Cell("🔽 Bottom Right Section")
      ))
    )),
    Document.Cell("📄 Response Footer")
  ))

  val complexDoc: Document[String] = Document.Vert(List(
    Document.Cell("🚀 Complex Document"),
    Document.Horiz(List(
      Document.Vert(List(
        Document.Cell("📈 Analytics"),
        Document.Cell("📊 Metrics")
      )),
      Document.Vert(List(
        Document.Cell("📉 Reports"),
        Document.Horiz(List(
          Document.Cell("💰 Revenue"),
          Document.Cell("👥 Users")
        ))
      )),
      Document.Cell("📋 Summary")
    ))
  ))

  // HTTP Routes using ZIO HTTP 3.x syntax
  val app = Http.collectZIO[Request] {
    // Basic document endpoint
    case Method.GET -> Root / "document" =>
      ZIO.succeed(Response.text(DocumentPrinter.printTree(sampleDoc)))

    // JSON representation
    case Method.GET -> Root / "document" / "json" =>
      ZIO.succeed(Response.text(DocumentPrinter.printJson(sampleDoc)))

    // HTML representation
    case Method.GET -> Root / "document" / "html" =>
      ZIO.succeed(Response.text(DocumentPrinter.printHtml(sampleDoc)))

    // Complex document example
    case Method.GET -> Root / "document" / "complex" =>
      ZIO.succeed(Response.text(DocumentPrinter.printTree(complexDoc)))

    // Document statistics
    case Method.GET -> Root / "document" / "stats" =>
      ZIO.succeed(Response.text(DocumentPrinter.printStats(sampleDoc)))

    // Transform document to uppercase (demo of traverseM)
    case Method.GET -> Root / "document" / "transform" / "uppercase" =>
      val uppercaseDoc = Document.traverseM[Document.Id, String, String](_.toUpperCase)(sampleDoc)
      ZIO.succeed(Response.text(DocumentPrinter.printTree(uppercaseDoc)))

    // Validate document structure
    case Method.GET -> Root / "document" / "validate" =>
      Document.validate(sampleDoc) match {
        case Right(_) => 
          ZIO.succeed(Response.text("✅ Document is valid"))
        case Left(error) => 
          ZIO.succeed(Response.text(s"❌ Validation failed: $error").withStatus(Status.BadRequest))
      }

    // Health check endpoint
    case Method.GET -> Root / "health" =>
      ZIO.succeed(Response.text("✅ Document Matrix API is healthy!"))

    // API documentation
    case Method.GET -> Root / "api" / "docs" =>
      val docs = """
        |# Document Matrix API
        |
        |## Endpoints:
        |- GET /document          - Basic document tree
        |- GET /document/json     - JSON format
        |- GET /document/html     - HTML format  
        |- GET /document/complex  - Complex example
        |- GET /document/stats    - Document statistics
        |- GET /document/transform/uppercase - Transform to uppercase
        |- GET /document/validate - Validate structure
        |- GET /health           - Health check
        |- GET /api/docs         - This documentation
        |
        |## Features:
        |- Algebraic Data Types for type safety
        |- Monadic traversals with cats
        |- ZIO effects for async operations
        |- Professional error handling
        |""".stripMargin
      ZIO.succeed(Response.text(docs))

    // Root endpoint with welcome message
    case Method.GET -> Root =>
      ZIO.succeed(Response.text("🌟 Welcome to Document Matrix API! Visit /api/docs for documentation."))

    // Catch-all for unknown routes
    case _ =>
      ZIO.succeed(Response.text("❌ Route not found. Visit /api/docs for available endpoints.").withStatus(Status.NotFound))
  }

  override def run = 
    Server.serve(app ++ DocumentMonitoring.monitoringRoutes)
      .provide(Server.defaultWithPort(8080)) *>
    Console.printLine("🚀 Document Matrix API started on http://localhost:8080") *>
    Console.printLine("📊 Health check: http://localhost:8080/health") *>
    Console.printLine("📈 Metrics: http://localhost:8080/metrics")
}
