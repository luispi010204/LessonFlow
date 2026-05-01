FROM eclipse-temurin:25-jdk-noble

RUN apt-get update && apt-get install -y supervisor curl \
    && curl -sL https://deb.nodesource.com/setup_22.x | bash - \
    && apt-get install -y nodejs \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /usr/src/app

COPY mvnw mvnw
COPY .mvn .mvn
COPY pom.xml pom.xml
COPY src src
COPY frontend frontend

RUN cd frontend && npm ci && npm run build

RUN sed -i 's/\r$//' mvnw && chmod +x mvnw

RUN ./mvnw package -DskipTests

# Use supervisor to start frontend and backend
RUN cat > /etc/supervisor/conf.d/supervisord.conf <<'EOF'
[supervisord]
nodaemon=true
user=root

[program:backend]
command=java -jar /usr/src/app/target/lessonflow-0.0.1-SNAPSHOT.jar
directory=/usr/src/app
autostart=true
autorestart=true
stdout_logfile=/dev/stdout
stdout_logfile_maxbytes=0
stderr_logfile=/dev/stderr
stderr_logfile_maxbytes=0

[program:frontend]
directory=/usr/src/app/frontend
command=sh -c "node build"
autostart=true
autorestart=true
stdout_logfile=/dev/stdout
stdout_logfile_maxbytes=0
stderr_logfile=/dev/stderr
stderr_logfile_maxbytes=0
EOF

EXPOSE 3000 8080

ENV NODE_ENV=production

CMD ["/usr/bin/supervisord", "-c", "/etc/supervisor/conf.d/supervisord.conf"]