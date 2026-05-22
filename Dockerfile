FROM gradle:jdk21 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build -x test --no-daemon

FROM eclipse-temurin:21-jre
EXPOSE 8080
COPY --from=build /home/gradle/src/build/libs/ /tmp/libs/
RUN cp $(ls /tmp/libs/*.jar | grep -v plain) /chat-sauvegarde.jar
ENTRYPOINT ["java", "-jar", "/chat-sauvegarde.jar"]
