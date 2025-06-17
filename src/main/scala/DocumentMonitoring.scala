package com.example

import zio._
import zio.http._
import zio.json._

/**
 * Production-ready health checks and monitoring for Document Matrix.
 * Provides comprehensive system status and performance metrics.
 */
object DocumentMonitoring {

  // Health check data structures
  case class HealthStatus(
    status: String,
    timestamp: java.time.Instant,
    uptime: Duration,
    version: String,
    checks: Map[String, CheckResult]
  )

  case class CheckResult(
    status: String,
    message: String,
    duration: Duration,
    metadata: Map[String, String] = Map.empty
  )

  case class PerformanceMetrics(
    requestCount: Long,
    averageResponseTime: Double,
    errorRate: Double,
    memoryUsage: MemoryInfo,
    systemInfo: SystemInfo
  )

  case class MemoryInfo(
    used: Long,
    max: Long,
    free: Long,
    usagePercentage: Double
  )

  case class SystemInfo(
    processors: Int,
    osName: String,
    osVersion: String,
    javaVersion: String
  )

  // JSON encoders
  implicit val checkResultEncoder: JsonEncoder[CheckResult] = DeriveJsonEncoder.gen
  implicit val healthStatusEncoder: JsonEncoder[HealthStatus] = DeriveJsonEncoder.gen
  implicit val memoryInfoEncoder: JsonEncoder[MemoryInfo] = DeriveJsonEncoder.gen
  implicit val systemInfoEncoder: JsonEncoder[SystemInfo] = DeriveJsonEncoder.gen
  implicit val performanceMetricsEncoder: JsonEncoder[PerformanceMetrics] = DeriveJsonEncoder.gen

  // Application start time for uptime calculation
  private val startTime = java.time.Instant.now()

  // Performance counters
  private val requestCounter = Ref.unsafe.make(0L)(Unsafe.unsafe)
  private val totalResponseTime = Ref.unsafe.make(0.0)(Unsafe.unsafe)
  private val errorCounter = Ref.unsafe.make(0L)(Unsafe.unsafe)

  /**
   * Comprehensive health check that validates all system components.
   */
  def performHealthCheck(): ZIO[Any, Nothing, HealthStatus] = {
    for {
      uptime <- ZIO.succeed(java.time.Duration.between(startTime, java.time.Instant.now()))
      
      // Core functionality checks
      documentCheck <- checkDocumentFunctionality()
      memoryCheck <- checkMemoryStatus()
      diskCheck <- checkDiskSpace()
      performanceCheck <- checkPerformance()
      
      checks = Map(
        "document_processing" -> documentCheck,
        "memory" -> memoryCheck,
        "disk_space" -> diskCheck,
        "performance" -> performanceCheck
      )
      
      overallStatus = if (checks.values.forall(_.status == "healthy")) "healthy" else "degraded"
      
    } yield HealthStatus(
      status = overallStatus,
      timestamp = java.time.Instant.now(),
      uptime = uptime,
      version = "0.1.0",
      checks = checks
    )
  }

  /**
   * Test core document processing functionality.
   */
  private def checkDocumentFunctionality(): ZIO[Any, Nothing, CheckResult] = {
    val testDoc = Document.Vert(List(
      Document.Cell("health-check"),
      Document.Horiz(List(
        Document.Cell("test-1"),
        Document.Cell("test-2")
      ))
    ))

    ZIO.timed {
      ZIO.attempt {
        // Test traversal
        val transformed = Document.traverseM[Document.Id, String, String](_.toUpperCase)(testDoc)
        
        // Test catamorphism
        val cellCount = DocumentCata.countTotalCells(testDoc)
        
        // Test printing
        val printed = DocumentPrinter.printTree(testDoc)
        
        // Test validation
        val validated = Document.validate(testDoc)
        
        (transformed, cellCount, printed, validated)
      }.mapError(_.getMessage)
    }.map { case (duration, result) =>
      result match {
        case Right(_) => CheckResult(
          status = "healthy",
          message = "Document processing functional",
          duration = duration,
          metadata = Map("test_duration_ms" -> duration.toMillis.toString)
        )
        case Left(error) => CheckResult(
          status = "unhealthy",
          message = s"Document processing failed: $error",
          duration = duration
        )
      }
    }.catchAll { error =>
      ZIO.succeed(CheckResult(
        status = "unhealthy",
        message = s"Health check failed: ${error.getMessage}",
        duration = java.time.Duration.ZERO
      ))
    }
  }

  /**
   * Check memory usage and availability.
   */
  private def checkMemoryStatus(): ZIO[Any, Nothing, CheckResult] = {
    ZIO.attempt {
      val runtime = Runtime.getRuntime
      val maxMemory = runtime.maxMemory()
      val totalMemory = runtime.totalMemory()
      val freeMemory = runtime.freeMemory()
      val usedMemory = totalMemory - freeMemory
      val usagePercentage = (usedMemory.toDouble / maxMemory.toDouble) * 100

      val status = if (usagePercentage < 80) "healthy" 
                  else if (usagePercentage < 95) "warning"
                  else "critical"

      CheckResult(
        status = status,
        message = f"Memory usage: ${usagePercentage}%.1f%%",
        duration = java.time.Duration.ZERO,
        metadata = Map(
          "used_mb" -> (usedMemory / 1024 / 1024).toString,
          "max_mb" -> (maxMemory / 1024 / 1024).toString,
          "usage_percent" -> f"${usagePercentage}%.1f"
        )
      )
    }.catchAll { error =>
      ZIO.succeed(CheckResult(
        status = "unhealthy",
        message = s"Memory check failed: ${error.getMessage}",
        duration = java.time.Duration.ZERO
      ))
    }
  }

  /**
   * Check available disk space.
   */
  private def checkDiskSpace(): ZIO[Any, Nothing, CheckResult] = {
    ZIO.attempt {
      val file = new java.io.File(".")
      val totalSpace = file.getTotalSpace
      val freeSpace = file.getFreeSpace
      val usedSpace = totalSpace - freeSpace
      val usagePercentage = (usedSpace.toDouble / totalSpace.toDouble) * 100

      val status = if (usagePercentage < 85) "healthy"
                  else if (usagePercentage < 95) "warning"
                  else "critical"

      CheckResult(
        status = status,
        message = f"Disk usage: ${usagePercentage}%.1f%%",
        duration = Duration.ZERO,
        metadata = Map(
          "free_gb" -> (freeSpace / 1024 / 1024 / 1024).toString,
          "total_gb" -> (totalSpace / 1024 / 1024 / 1024).toString,
          "usage_percent" -> f"${usagePercentage}%.1f"
        )
      )
    }.catchAll { error =>
      ZIO.succeed(CheckResult(
        status = "unhealthy",
        message = s"Disk check failed: ${error.getMessage}",
        duration = Duration.ZERO
      ))
    }
  }

  /**
   * Check application performance metrics.
   */
  private def checkPerformance(): ZIO[Any, Nothing, CheckResult] = {
    for {
      reqCount <- requestCounter.get
      totalTime <- totalResponseTime.get
      errorCount <- errorCounter.get
      
      avgResponseTime = if (reqCount > 0) totalTime / reqCount else 0.0
      errorRate = if (reqCount > 0) (errorCount.toDouble / reqCount.toDouble) * 100 else 0.0
      
      status = if (avgResponseTime < 100 && errorRate < 1) "healthy"
              else if (avgResponseTime < 500 && errorRate < 5) "warning"
              else "critical"
      
    } yield CheckResult(
      status = status,
      message = f"Avg response: ${avgResponseTime}%.1fms, Error rate: ${errorRate}%.1f%%",
      duration = Duration.ZERO,
      metadata = Map(
        "request_count" -> reqCount.toString,
        "avg_response_ms" -> f"${avgResponseTime}%.1f",
        "error_rate_percent" -> f"${errorRate}%.1f"
      )
    )
  }

  /**
   * Get detailed performance metrics.
   */
  def getPerformanceMetrics(): ZIO[Any, Nothing, PerformanceMetrics] = {
    for {
      reqCount <- requestCounter.get
      totalTime <- totalResponseTime.get
      errorCount <- errorCounter.get
      
      runtime = Runtime.getRuntime
      maxMemory = runtime.maxMemory()
      totalMemory = runtime.totalMemory()
      freeMemory = runtime.freeMemory()
      usedMemory = totalMemory - freeMemory
      usagePercentage = (usedMemory.toDouble / maxMemory.toDouble) * 100
      
      avgResponseTime = if (reqCount > 0) totalTime / reqCount else 0.0
      errorRate = if (reqCount > 0) (errorCount.toDouble / reqCount.toDouble) * 100 else 0.0
      
      memoryInfo = MemoryInfo(
        used = usedMemory,
        max = maxMemory,
        free = freeMemory,
        usagePercentage = usagePercentage
      )
      
      systemInfo = SystemInfo(
        processors = runtime.availableProcessors(),
        osName = System.getProperty("os.name"),
        osVersion = System.getProperty("os.version"),
        javaVersion = System.getProperty("java.version")
      )
      
    } yield PerformanceMetrics(
      requestCount = reqCount,
      averageResponseTime = avgResponseTime,
      errorRate = errorRate,
      memoryUsage = memoryInfo,
      systemInfo = systemInfo
    )
  }

  /**
   * Record a request for performance tracking.
   */
  def recordRequest(responseTime: Duration, isError: Boolean = false): ZIO[Any, Nothing, Unit] = {
    for {
      _ <- requestCounter.update(_ + 1)
      _ <- totalResponseTime.update(_ + responseTime.toMillis.toDouble)
      _ <- if (isError) errorCounter.update(_ + 1) else ZIO.unit
    } yield ()
  }

  /**
   * Middleware for automatic request tracking.
   */
  def trackingMiddleware: HttpApp[Any] => HttpApp[Any] = { app =>
    Http.collectZIO[Request] { request =>
      ZIO.timed(app.runZIO(request)).flatMap { case (duration, response) =>
        val isError = response.status.code >= 400
        recordRequest(duration, isError).as(response)
      }
    }
  }

  /**
   * HTTP endpoints for monitoring.
   */
  val monitoringRoutes = Http.collectZIO[Request] {
    // Detailed health check
    case Method.GET -> Root / "health" =>
      performHealthCheck().map(health => Response.json(health.toJson))

    // Quick liveness probe
    case Method.GET -> Root / "alive" =>
      ZIO.succeed(Response.text("OK"))

    // Readiness probe
    case Method.GET -> Root / "ready" =>
      performHealthCheck().map { health =>
        if (health.status == "healthy") 
          Response.text("READY")
        else 
          Response.text("NOT_READY").withStatus(Status.ServiceUnavailable)
      }

    // Performance metrics
    case Method.GET -> Root / "metrics" =>
      getPerformanceMetrics().map(metrics => Response.json(metrics.toJson))

    // Prometheus-style metrics
    case Method.GET -> Root / "metrics" / "prometheus" =>
      getPerformanceMetrics().map { metrics =>
        val prometheusFormat = 
          s"""# HELP document_matrix_requests_total Total number of requests
             |# TYPE document_matrix_requests_total counter
             |document_matrix_requests_total ${metrics.requestCount}
             |
             |# HELP document_matrix_response_time_seconds Average response time in seconds
             |# TYPE document_matrix_response_time_seconds gauge
             |document_matrix_response_time_seconds ${metrics.averageResponseTime / 1000.0}
             |
             |# HELP document_matrix_error_rate Error rate percentage
             |# TYPE document_matrix_error_rate gauge
             |document_matrix_error_rate ${metrics.errorRate}
             |
             |# HELP document_matrix_memory_usage_bytes Memory usage in bytes
             |# TYPE document_matrix_memory_usage_bytes gauge
             |document_matrix_memory_usage_bytes ${metrics.memoryUsage.used}
             |""".stripMargin
        
        Response.text(prometheusFormat).withHeader("Content-Type", "text/plain; version=0.0.4")
      }
  }
}
