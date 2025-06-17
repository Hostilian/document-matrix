package com.example

import zio._
import zio.http._
import zio.json._
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Production-ready HTTP API for Document Matrix with comprehensive endpoints.
 * Features RESTful design, proper error handling, and multiple content types.
 */
object DocumentHttpApi extends ZIOAppDefault {

  // Sample documents
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
    Document.Cell("🚀 Complex API Document"),
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

  // API Routes
  val documentRoutes = Routes(
    // Get document tree (default format)
    Method.GET / "document" -> handler {
      Response.text(DocumentPrinter.printTree(sampleDoc))
        .addHeader(Header.ContentType(MediaType.text.plain))
    },

    // Get document as JSON
    Method.GET / "document" / "json" -> handler {
      Response.text(DocumentPrinter.printJson(sampleDoc))
        .addHeader(Header.ContentType(MediaType.application.json))
    },

    // Get document as HTML table
    Method.GET / "document" / "html" -> handler {
      Response.text(DocumentPrinter.printHtml(sampleDoc))
        .addHeader(Header.ContentType(MediaType.text.html))
    },

    // Get complex document example
    Method.GET / "document" / "complex" -> handler {
      Response.text(DocumentPrinter.printFull(complexDoc))
        .addHeader(Header.ContentType(MediaType.text.plain))
    },

    // Get document statistics
    Method.GET / "document" / "stats" -> handler {
      Response.text(DocumentPrinter.printStats(sampleDoc))
        .addHeader(Header.ContentType(MediaType.text.plain))
    },

    // Transform document (demo of traverseM)
    Method.GET / "document" / "transform" / "uppercase" -> handler {
      val transformed = Document.traverseM[Document.Id, String, String](_.toUpperCase)(sampleDoc)
      Response.text(DocumentPrinter.printTree(transformed))
        .addHeader(Header.ContentType(MediaType.text.plain))
    },

    // Validate document structure
    Method.GET / "document" / "validate" -> handler {
      Document.validate(sampleDoc) match {
        case Right(_) => 
          Response.text("✅ Document is valid")
            .addHeader(Header.ContentType(MediaType.text.plain))
        case Left(error) => 
          Response.text(s"❌ Validation failed: $error")
            .addHeader(Header.ContentType(MediaType.text.plain))
            .copy(status = Status.BadRequest)
      }
    },

    // API documentation
    Method.GET / "api" / "docs" -> handler {
      val docs = 
        s"""
        |# Document Matrix API Documentation
        |
        |## Endpoints
        |
        |### Core Document Operations
        |- `GET /document` - Get document tree (plain text)
        |- `GET /document/json` - Get document as JSON
        |- `GET /document/html` - Get document as HTML table
        |- `GET /document/complex` - Get complex document example
        |- `GET /document/stats` - Get document statistics
        |
        |### Transformations
        |- `GET /document/transform/uppercase` - Transform to uppercase
        |- `GET /document/validate` - Validate document structure
        |
        |### System
        |- `GET /health` - Health check
        |- `GET /api/docs` - This documentation
        |
        |## Response Formats
        |- Plain text with ANSI colors
        |- JSON structured data
        |- HTML tables
        |
        |Generated at: ${LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}
        |""".stripMargin

      Response.text(docs)
        .addHeader(Header.ContentType(MediaType.text.plain))
    },

    // Health check endpoint
    Method.GET / "health" -> handler {
      val health = Map(
        "status" -> "healthy",
        "timestamp" -> LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
        "service" -> "document-matrix",
        "version" -> "0.1.0"
      )
      Response.text(health.toString)
        .addHeader(Header.ContentType(MediaType.application.json))
    },

    // Root endpoint with API information
    Method.GET / Root -> handler {
      val welcome = 
        s"""
        |🌟 Document Matrix API
        |=====================
        |
        |A functional document processing system built with Scala 3, ZIO, and Cats.
        |
        |📋 Try these endpoints:
        |• GET /document - View sample document
        |• GET /document/complex - View complex example  
        |• GET /document/json - JSON format
        |• GET /document/html - HTML table format
        |• GET /api/docs - Full API documentation
        |• GET /health - Service health
        |
        |🚀 Built with modern functional programming patterns!
        |""".stripMargin

      Response.text(welcome)
        .addHeader(Header.ContentType(MediaType.text.plain))
    }
  )

  // Error handling middleware
  val errorHandler = Routes(
    Method.ANY / trailing -> handler { (path: Path, request: Request) =>
      Response.text(s"❌ Endpoint not found: ${request.method} ${path}")
        .copy(status = Status.NotFound)
        .addHeader(Header.ContentType(MediaType.text.plain))
    }
  )

  // Logging middleware
  val loggingMiddleware = HandlerAspect.requestLogging(
    level = LogLevel.Info,
    logRequestBody = false,
    logResponseBody = false
  )

  // CORS middleware for browser compatibility
  val corsMiddleware = HandlerAspect.cors(
    CorsConfig(
      allowedOrigins = _ => true,
      allowedMethods = Some(Set(Method.GET, Method.POST, Method.OPTIONS))
    )
  )

  // Combined application with middleware
  val app = (documentRoutes ++ errorHandler)
    .toHttpApp @@ loggingMiddleware @@ corsMiddleware

  // Server configuration
  val serverConfig = Server.Config.default
    .port(8080)
    .enableRequestStreaming

  val run = 
    for {
      _ <- Console.printLine("🚀 Starting Document Matrix HTTP API...")
      _ <- Console.printLine("📡 Server will be available at: http://localhost:8080")
      _ <- Console.printLine("📋 Try: curl http://localhost:8080/document")
      _ <- Console.printLine("📚 API docs: curl http://localhost:8080/api/docs")
      _ <- Server.serve(app).provide(Server.live(serverConfig))
    } yield ()
}
