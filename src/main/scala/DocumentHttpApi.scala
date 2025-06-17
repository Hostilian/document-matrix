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

  // HTTP Routes using correct ZIO HTTP 3.x syntax
  val documentRoutes: Routes[Any, Response] = Routes(
    // Basic document endpoint
    Method.GET / "document" -> handler {
      Response.text(DocumentPrinter.printTree(sampleDoc))
    },

    // JSON representation
    Method.GET / "document" / "json" -> handler {
      Response.text(DocumentPrinter.printJson(sampleDoc))
    },

    // HTML representation
    Method.GET / "document" / "html" -> handler {
      Response.text(DocumentPrinter.printHtml(sampleDoc))
    },

    // Complex document example
    Method.GET / "document" / "complex" -> handler {
      Response.text(DocumentPrinter.printTree(complexDoc))
    },

    // Document statistics
    Method.GET / "document" / "stats" -> handler {
      Response.text(DocumentPrinter.printStats(sampleDoc))
    },

    // Transform document to uppercase (demo of traverseM)
    Method.GET / "document" / "transform" / "uppercase" -> handler {
      val uppercaseDoc = Document.traverseM[Document.Id, String, String](_.toUpperCase)(sampleDoc)
      Response.text(DocumentPrinter.printTree(uppercaseDoc))
    },

    // Validate document structure
    Method.GET / "document" / "validate" -> handler {
      Document.validate(sampleDoc) match {
        case Right(_) => 
          Response.text("✅ Document is valid")
        case Left(error) => 
          Response.text(s"❌ Validation failed: $error").withStatus(Status.BadRequest)
      }
    },

    // Health check endpoint
    Method.GET / "health" -> handler {
      Response.text("✅ Document Matrix API is healthy!")
    },

    // API documentation
    Method.GET / "api" / "docs" -> handler {
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
      Response.text(docs)
    }
  )

  // CORS middleware for web compatibility
  val corsConfig: CorsConfig = CorsConfig(
    allowedOrigins = _ => true,
    allowedMethods = Some(Set(Method.GET, Method.POST, Method.PUT, Method.DELETE, Method.OPTIONS))
  )

  // Logging middleware
  val loggingMiddleware = HandlerAspect.debug

  // Complete app with middleware
  val app: Routes[Any, Response] = documentRoutes @@ Middleware.cors(corsConfig) @@ loggingMiddleware

  // Server configuration
  val serverConfig = Server.Config.default
    .port(8080)
    .enableRequestLogging

  override def run = 
    (for {
      _ <- Console.printLine("🚀 Document Matrix API starting on http://localhost:8080")
      _ <- Console.printLine("📖 API documentation: http://localhost:8080/api/docs")
      _ <- Console.printLine("🔍 Health check: http://localhost:8080/health")
      _ <- Server.serve(app).provide(Server.defaultWith(serverConfig))
    } yield ())
}
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
          ZIO.succeed(Response.text(s"❌ Validation failed: $error").copy(status = Status.BadRequest))
      }

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

    // Health check endpoint
    case Method.GET -> Root / "health" =>
      ZIO.succeed(Response.text("""{"status": "healthy", "service": "document-matrix"}"""))

    // Root endpoint with welcome message
    case Method.GET -> Root =>
      val welcome = """
        |🌟 Document Matrix API
        |
        |A functional document processing system built with:
        |• Scala 3.4.2
        |• ZIO 2.0.21  
        |• Cats for functional programming
        |• Professional error handling
        |
        |Visit /api/docs for endpoint documentation
        |""".stripMargin
      ZIO.succeed(Response.text(welcome))
  }

  // Error handling for unknown routes
  val errorHandler = Http.collectZIO[Request] {
    case _ =>
      ZIO.succeed(
        Response.text("❌ Endpoint not found. Visit /api/docs for available endpoints.")
          .copy(status = Status.NotFound)
      )
  }

  // Combine routes with error handling
  val app = documentRoutes.orElse(errorHandler)

  // Server configuration
  val serverConfig = Server.Config.default.port(8080)

  // Application entry point
  val run = for {
    _ <- Console.printLine("🚀 Starting Document Matrix HTTP API on port 8080")
    _ <- Console.printLine("📖 Visit http://localhost:8080/api/docs for documentation")
    _ <- Server.serve(app).provide(Server.live(serverConfig))
  } yield ()
}
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
