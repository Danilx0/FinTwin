# Imagem do FinTwin para hospedagem (ex.: Render).
# Variaveis obrigatorias no servidor: DB_URL, DB_USER, DB_PASSWORD e JWT_SECRET.

# ---------- 1. Compilacao ----------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Baixa as dependencias primeiro, para o cache do Docker reaproveitar quando so o codigo mudar.
COPY demo/.mvn .mvn
COPY demo/mvnw demo/pom.xml ./
RUN sh ./mvnw -q -B dependency:go-offline

COPY demo/src src
RUN sh ./mvnw -q -B package -DskipTests && cp target/*.jar app.jar

# ---------- 2. Execucao ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# Roda sem privilegios de root.
RUN useradd --system --create-home fintwin
USER fintwin

COPY --from=build /app/app.jar app.jar

# Usa ate 75% da memoria do servidor (o plano gratuito do Render tem 512 MB).
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
