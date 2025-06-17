FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Install required tools
RUN apk add --no-cache bash curl

# Copy build configuration
COPY build.sbt ./
COPY project ./project

# Download sbt if needed
RUN curl -L -o sbt.tgz https://github.com/sbt/sbt/releases/download/v1.9.8/sbt-1.9.8.tgz && \
    tar -xzf sbt.tgz && \
    mv sbt/bin/sbt /usr/local/bin/ && \
    mv sbt/bin/sbt-launch.jar /usr/local/bin/ && \
    rm -rf sbt.tgz sbt

# Download dependencies
RUN sbt update

# Copy source code
COPY src ./src

# Build application
RUN sbt clean assembly

# Runtime image
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Install runtime dependencies
RUN apk add --no-cache curl && \
    addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Copy built JAR
COPY --from=builder /app/target/scala-3.4.2/document-matrix.jar ./app.jar

# Change ownership
RUN chown appuser:appgroup app.jar

USER appuser

EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD curl -f http://localhost:8080/health || exit 1

# Run application
CMD ["java", "-jar", "app.jar"]
