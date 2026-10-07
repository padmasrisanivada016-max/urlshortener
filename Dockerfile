# ---------- Stage 1: BUILD (the kitchen) ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy only pom.xml first so Docker can remember the "download libraries" step.
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Now copy our code and build the jar file
COPY src ./src
RUN mvn -B package -DskipTests

# ---------- Stage 2: RUN (the lunchbox) ----------
FROM eclipse-temurin:17-jre
WORKDIR /app

# Do not run as the all-powerful "root" user
RUN useradd --system --create-home appuser
USER appuser

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
