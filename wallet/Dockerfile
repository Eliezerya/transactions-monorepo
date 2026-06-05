# Stage 1: Build the application
FROM eclipse-temurin:26-jdk AS build
WORKDIR /app

# Copy Maven wrapper and POM files first to cache dependencies
COPY mvnw pom.xml ./
COPY .mvn/ .mvn/

# Mark Maven wrapper as executable
RUN chmod +x mvnw

# Resolve dependencies (offline mode) to optimize caching
RUN ./mvnw dependency:go-offline -B

# Copy the rest of the application source code
COPY src/ src/

# Build package
RUN ./mvnw clean package -DskipTests

# Stage 2: Create runtime image
FROM eclipse-temurin:26-jre
WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port
EXPOSE 8081

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
