# ============================================================================
# Corn COBOL-to-Java Compiler - Docker Image
# License: Corn Evaluation License - See LICENSE
# ============================================================================

FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /build

COPY pom.xml .
COPY modules/ir/pom.xml modules/ir/
COPY modules/runtime-java/pom.xml modules/runtime-java/
COPY modules/lexer-parser/pom.xml modules/lexer-parser/
COPY modules/codegen-java/pom.xml modules/codegen-java/
COPY modules/cli/pom.xml modules/cli/
COPY modules/server/pom.xml modules/server/

RUN mvn dependency:go-offline -B

COPY modules/ir/src modules/ir/src
COPY modules/runtime-java/src modules/runtime-java/src
COPY modules/lexer-parser/src modules/lexer-parser/src
COPY modules/codegen-java/src modules/codegen-java/src
COPY modules/cli/src modules/cli/src
COPY modules/server/src modules/server/src

RUN mvn clean package -DskipTests -B

FROM eclipse-temurin:21-jre-jammy

LABEL maintainer="Sekacorn"
LABEL description="Corn COBOL-to-Java Compiler and demo web UI"
LABEL version="1.0.0-SNAPSHOT"
LABEL org.opencontainers.image.title="Corn COBOL-to-Java"
LABEL org.opencontainers.image.description="Deterministic COBOL-to-Java compiler with CLI and demo UI"
LABEL org.opencontainers.image.licenses="Corn Evaluation License"

WORKDIR /app

COPY --from=builder /build/modules/cli/target/corn-cobol-to-java.jar /app/corn-cobol-to-java.jar
COPY --from=builder /build/modules/server/target/corn-demo-server.jar /app/corn-demo-server.jar
COPY demo-ui /app/demo-ui

RUN mkdir -p /app/input /app/output /app/config

ENV JAVA_OPTS="-Xmx2g -Xms512m"
ENV CORN_HOME="/app"
ENV PORT="8085"

RUN groupadd -r corn && useradd -r -g corn corn
RUN chown -R corn:corn /app
USER corn

EXPOSE 8085

HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD java -version || exit 1

ENTRYPOINT ["sh", "-c", "case \"${1:-server}\" in server|gui|web) shift; exec java $JAVA_OPTS -jar /app/corn-demo-server.jar --port \"${PORT:-8085}\" --static /app/demo-ui \"$@\" ;; cli) shift; exec java $JAVA_OPTS -jar /app/corn-cobol-to-java.jar \"$@\" ;; *) exec java $JAVA_OPTS -jar /app/corn-cobol-to-java.jar \"$@\" ;; esac", "--"]
CMD ["server"]
