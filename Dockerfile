# First stage: build front with node
FROM node:21-alpine3.19 AS buildFront
WORKDIR /usr/app
RUN apk add git
RUN git clone https://github.com/QGdev/M2S1-MiddlewareProject-Client -b main --depth=1
RUN mv M2S1-MiddlewareProject-Client/* .
RUN npm install --legacy-peer-deps
RUN npm run build

# Second stage: build with maven
FROM docker.io/maven:3-eclipse-temurin-21 AS buildJar
WORKDIR /usr/app
COPY ./src /usr/app/src
COPY ./pom.xml /usr/app/pom.xml
COPY --from=buildFront /usr/app/.svelte-kit/output/prerendered/pages/ /usr/app/src/main/resources/static/web
COPY --from=buildFront /usr/app/.svelte-kit/output/client/ /usr/app/src/main/resources/static/web

RUN mvn package -f pom.xml

# Third stage: run it with java
FROM docker.io/eclipse-temurin:21-jre-jammy AS runtime
WORKDIR /usr/app

COPY --from=buildJar /usr/app/target/*.jar /usr/app/app.jar

CMD java -jar app.jar