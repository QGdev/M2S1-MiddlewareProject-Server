# First stage: build front with node
FROM node:21-alpine3.19 AS buildFront
WORKDIR /usr/app
RUN apk add git
RUN git clone https://github.com/QGdev/M2S1-MiddlewareProject-Client.git
RUN mv M2S1-MiddlewareProject-Client/* .
RUN npm install --legacy-peer-deps
RUN npm run build

# Second stage: build with maven
FROM docker.io/maven:3-eclipse-temurin-21 AS buildJar
WORKDIR /usr/app
COPY ./src /usr/app/src
COPY ./pom.xml /usr/app/pom.xml
COPY --from=buildFront /usr/app/.svelte-kit/output/prerendered/pages/ /src/main/resources/static/web
COPY --from=buildFront /usr/app/.svelte-kit/output/client/ /src/main/resources/static/web

RUN mvn package -f pom.xml
RUN jar tf target/*.jar

# Third stage: run it with java
FROM docker.io/eclipse-temurin:21-jre-jammy AS runtime

COPY --from=buildJar /usr/app/target/*.jar /usr/app.jar

CMD java -jar /usr/app.jar