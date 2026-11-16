# BUILD: compila a aplicação com o Maven Wrapper em uma imagem com o JDK 25
FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /workspace/server

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

# remove o \r (fim de linha do Windows) do mvnw
RUN sed -i 's/\r$//' mvnw
# gera o arquivo target/server-0.1.jar
RUN /bin/sh mvnw package -DskipTests

# DELIVERY: imagem final apenas com o JRE 25 (menor e sem as ferramentas de build)
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=build /workspace/server/target/server-0.1.jar server.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "server.jar"]
