# STAGE 1: BUilding the application means compiling it into a .jar
FROM maven:3.9-eclipse-temurin-21 AS build

# Typically means cd, Workdir -> cd, all the comments run here.
WORKDIR /app

# Means copying the Maven config first fro caching it. If the dependencies don't change, Docker won't download them again
COPY pom.xml .

# Pre-downloading dependencies so builds are faster and more reliable
RUN mvn -q -DskipTests dependency:go-offline

# Copying the actual Java code
COPY src ./src

# Building the project and producing a jar inside target/
RUN mvn -q -DskipTests clean package


# STAGE 2: Running the application (small, clean, runtime image)
FROM eclipse-temurin:21-jre
# A samller image thta only has Java runtime(no maven): This makes the final image smaller and ssafer

# Cd app
WORKDIR /app

# Copying the built jar from the Stage 1 into this step and naming it app.jar
COPY --from=build /app/target/*.jar app.jar

# This container listens on 8080. Not required to run, bu important for tooling and humans
EXPOSE 8080

# Meaning: when the container starts, run java -jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]


# docker build -t cas-backend . The dot here means that telling the system to use the current directory as build context


