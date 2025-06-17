FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY . .
RUN ./sbt assembly
CMD ["java", "-jar", "./target/scala-3.4.2/document-matrix-assembly-0.1.0.jar"]
