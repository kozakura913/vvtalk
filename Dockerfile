FROM maven:3-amazoncorretto-21
WORKDIR /root/
COPY ./pom.xml .
RUN mvn dependency:copy-dependencies -DoutputDirectory=lib
COPY ./src ./src
RUN mvn install

FROM amazoncorretto:21
WORKDIR /root/
COPY --from=0 /root/target/VVTalk-0.0.1-SNAPSHOT.jar ./VVTalk-0.0.1-SNAPSHOT.jar
COPY --from=0 /root/lib ./lib
CMD ["java","-cp","./lib/*:.","-jar","VVTalk-0.0.1-SNAPSHOT.jar"]

