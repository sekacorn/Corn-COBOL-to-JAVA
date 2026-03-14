# ============================================================================
# Corn COBOL-to-Java Compiler - Docker Image
# Author: Sekacorn
# Created: 2025-01-10
# License: Commercial - See LICENSE
# ============================================================================

# Build stage
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /build

# Copy POM files first for better layer caching
COPY pom.xml .
COPY modules/ir/pom.xml modules/ir/
COPY modules/runtime-java/pom.xml modules/runtime-java/
COPY modules/cli/pom.xml modules/cli/

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -B

# Copy source code
COPY modules/ir/src modules/ir/src
COPY modules/runtime-java/src modules/runtime-java/src
COPY modules/cli/src modules/cli/src

# Build the application
RUN mvn clean package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:21-jre-jammy

LABEL maintainer="Sekacorn"
LABEL description="Corn COBOL-to-Java Compiler - Enterprise-grade COBOL modernization tool"
LABEL version="1.0.0-SNAPSHOT"

WORKDIR /app

# Copy the JAR from builder
COPY --from=builder /build/modules/cli/target/corn-cobol-to-java.jar /app/corn-cobol-to-java.jar

# Create directories for input/output
RUN mkdir -p /app/input /app/output /app/config

# Set environment variables
ENV JAVA_OPTS="-Xmx2g -Xms512m"
ENV CORN_HOME="/app"

# Create a non-root user
RUN groupadd -r corn && useradd -r -g corn corn
RUN chown -R corn:corn /app
USER corn

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD java -version || exit 1

# Default command shows help
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/corn-cobol-to-java.jar \"$@\"", "--"]
CMD ["--help"]
