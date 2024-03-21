FROM gradle:jdk21

RUN mkdir /home/gradle/revproxy
COPY . /home/gradle/revproxy
WORKDIR /home/gradle/revproxy
RUN gradle build --no-daemon
ENTRYPOINT [ "java","-jar","/home/gradle/revproxy/build/revproxy.jar" ]
# RUN java -jar /home/gradle/proxyServer/build/libs/proxyserver.jar
EXPOSE 8080