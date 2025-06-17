# Document Matrix

A functional document processing system showcasing advanced Scala 3, ZIO, and Cats patterns.

## Architecture Overview

### Core Data Structure
```scala
sealed trait Document[+A]
object Document {
  case class Cell[A](value: A) extends Document[A]
  case class Horiz[A](children: List[Document[A]]) extends Document[A]
  case class Vert[A](children: List[Document[A]]) extends Document[A]
}
```

### Key Features
- **Algebraic Data Types**: Type-safe document representation
- **Monadic Traversal**: `traverseM[M[_]: Monad, A, B]: (A => M[B]) => Document[A] => M[Document[B]]`
- **Recursion Schemes**: Catamorphisms for document folding
- **ZIO Effects**: Async I/O and error handling
- **HTTP API**: RESTful document services
- **CLI Interface**: ANSI-colored tree visualization

## Quick Start

### Prerequisites
- Java 17+
- SBT 1.9+
- Docker (optional)

### Local Development
```bash
# Clone and build
git clone <repository-url>
cd document-matrix
sbt clean compile

# Run CLI
sbt "runMain com.example.DocumentCLI"

# Run HTTP API
sbt "runMain com.example.DocumentHttpApi"
# Visit: http://localhost:8080/document

# Run tests
sbt test
```

### Docker Deployment
```bash
# Build image
docker build -t document-matrix .

# Run container
docker run -p 8080:8080 document-matrix

# Test endpoint
curl http://localhost:8080/document
```

## API Reference

### HTTP Endpoints
- `GET /document` - Returns formatted document tree

### CLI Commands
```bash
java -jar target/scala-3.4.2/document-matrix-assembly.jar
```

## Development

### Project Structure
```
src/
├── main/scala/
│   ├── Document.scala         # Core ADT with traverseM
│   ├── DocumentCata.scala     # Catamorphism implementation
│   ├── DocumentPrinter.scala  # ANSI pretty-printer
│   ├── DocumentCLI.scala      # Command-line interface
│   └── DocumentHttpApi.scala  # ZIO HTTP server
└── test/scala/
    └── DocumentSpec.scala     # ZIO Test specifications
```

### Key Dependencies
- **Scala 3.4.2**: Latest stable Scala
- **ZIO 2.0.21**: Effect system
- **ZIO HTTP 3.0.0-RC2**: HTTP server
- **Cats 2.12.0**: Functional abstractions
- **ZIO Interop Cats**: Bridge between ZIO and Cats

## Functional Programming Patterns

### Monadic Traversal
The core requirement `f[M[_]: Monad, A, B]: (A => M[B]) => Document[A] => M[Document[B]]` enables:
- Effect-preserving transformations
- Compositional document processing
- Type-safe error handling

### Identity Law
```scala
f[Id](identity) = identity  // where Id[A] = A
```

### Option Example
```scala
f[Option](Some(_)) = Some(_)
```

## CI/CD Pipeline

### GitHub Actions
- Automated testing on push/PR
- Docker image building
- GitHub Pages documentation deployment
- Multi-environment testing

### Quality Gates
- Unit test coverage
- Integration test validation
- Docker health checks
- Performance benchmarks

## Deployment Options

### Local Development
```bash
sbt run
```

### Production Docker
```bash
docker-compose up -d
```

### Cloud Deployment
- Kubernetes manifests included
- Health check endpoints
- Graceful shutdown handling
- Resource limits configured

## Contributing

1. Fork the repository
2. Create feature branch
3. Add tests for new functionality
4. Ensure CI pipeline passes
5. Submit pull request

## License

MIT License - see LICENSE file for details