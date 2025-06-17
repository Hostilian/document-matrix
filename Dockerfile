FROM eclipse-temurin:17-jdk-alpine as builder

WORKDIR /app

# Copy build files
COPY build.sbt ./
COPY project ./project

# Download dependencies
RUN apk add --no-cache bash
COPY sbt-launch.jar /usr/local/bin/sbt-launch.jar
RUN echo '#!/bin/bash\njava -jar /usr/local/bin/sbt-launch.jar "$@"' > /usr/local/bin/sbt && chmod +x /usr/local/bin/sbt

# Copy source code
COPY src ./src

# Build application
RUN sbt clean assembly

# Runtime image
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy built JAR
COPY --from=builder /app/target/scala-3.4.2/document-matrix-assembly.jar ./app.jar

# Create non-root user
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

USER appuser

EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/document || exit 1

CMD ["java", "-jar", "app.jar"]
