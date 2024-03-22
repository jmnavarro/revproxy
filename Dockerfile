FROM gradle:jdk21

EXPOSE 8080 9001
ENTRYPOINT [ "java","-jar","/home/gradle/revproxy/build/revproxy.jar" ]

WORKDIR /home/gradle/revproxy

COPY . /home/gradle/revproxy
RUN gradle build --no-daemon
