---
sidebar_position: 1
---

# Introduction

Welcome to **Document Matrix** - a production-ready, type-safe document processing system showcasing advanced **Scala 3**, **ZIO**, and **Cats** functional programming patterns.

## What is Document Matrix?

Document Matrix implements the core requirement from advanced Scala developer assessments:

> Define a data type `D` that represents a document subdivided horizontally or vertically into 1 or more cells, with a monadic traversal function `f[M[_]: Monad, A, B]: (A => M[B]) => D[A] => M[D[B]]` satisfying identity and option laws.

## Key Features

🏗️ **Algebraic Data Types**: Type-safe document representation with `Cell`, `Horiz`, and `Vert`  
🔄 **Monadic Traversal**: `traverseM[M[_]: Monad, A, B]` with proven laws  
📐 **Recursion Schemes**: Catamorphisms, anamorphisms, and hylomorphisms  
⚡ **ZIO Effects**: Async I/O, error handling, and resource management  
🌐 **HTTP API**: RESTful endpoints with multiple output formats  
💻 **Interactive CLI**: Rich ANSI formatting and multiple commands  
🐳 **Docker Ready**: Multi-stage builds with health checks  
🧪 **Comprehensive Testing**: Property-based tests and law verification  
📊 **CI/CD Pipeline**: Automated testing, coverage, and deployment  

## Quick Start

### Prerequisites
- Java 17+
- SBT 1.9+ or Maven 3.8+
- Docker (optional)

### 🏃‍♂️ Run Locally

```bash
# Clone repository
git clone https://github.com/username/document-matrix.git
cd document-matrix

# 🧪 Run tests
sbt test

# 💻 Start interactive CLI
sbt "runMain com.example.DocumentCLI"

# 🌐 Start HTTP API
sbt "runMain com.example.DocumentHttpApi"
# Visit: http://localhost:8080
```

### 🐳 Docker Deployment

```bash
# 🏗️ Build image
docker build -t document-matrix .

# 🚀 Run container
docker run -p 8080:8080 document-matrix

# 🧪 Test endpoints
curl http://localhost:8080/document
curl http://localhost:8080/api/docs
curl http://localhost:8080/health
```

## Core Implementation

### Document ADT
```scala
sealed trait Document[+A]
object Document {
  case class Cell[A](value: A) extends Document[A]
  case class Horiz[A](children: List[Document[A]]) extends Document[A] 
  case class Vert[A](children: List[Document[A]]) extends Document[A]
}
```

### Monadic Traversal
```scala
def traverseM[M[_]: Monad, A, B](f: A => M[B])(doc: Document[A]): M[Document[B]] =
  doc match {
    case Cell(a)      => f(a).map(Cell(_))
    case Horiz(list)  => list.traverse(traverseM(f)).map(Horiz(_))
    case Vert(list)   => list.traverse(traverseM(f)).map(Vert(_))
  }
```

### Law Verification
```scala
// ✅ Identity Law
f[Id](identity) = identity

// ✅ Option Law  
f[Option](Some(_)) = Some(_)
```

## Next Steps

- [Getting Started](./getting-started) - Detailed setup and first steps
- [API Reference](./api) - Complete API documentation
- [Architecture](./architecture) - System design and patterns
- [Examples](./examples) - Real-world usage examples
- [Contributing](./contributing) - How to contribute to the project
