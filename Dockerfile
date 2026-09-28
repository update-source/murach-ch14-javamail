# ---------- Stage 1: build the WAR with Maven ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q package

# ---------- Stage 2: run on Tomcat 9 (javax.servlet, same as the book) ----------
FROM tomcat:9.0-jre17-temurin

# Remove default apps and deploy ours as the root context ("/")
RUN rm -rf /usr/local/tomcat/webapps/* \
    && useradd --system --no-create-home --shell /usr/sbin/nologin app
COPY --from=build /app/target/ch14-email.war /usr/local/tomcat/webapps/ROOT.war
COPY docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh
RUN chmod +x /usr/local/bin/docker-entrypoint.sh \
    && chown -R app:app /usr/local/tomcat

USER app

# Render injects PORT (default 10000); locally we fall back to 8080.
ENV PORT=8080 \
    MAIL_MODE=log \
    JAVA_OPTS="-XX:MaxRAMPercentage=75 -Djava.security.egd=file:/dev/./urandom"
EXPOSE 8080

ENTRYPOINT ["/usr/local/bin/docker-entrypoint.sh"]
