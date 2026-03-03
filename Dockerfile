FROM maven:3-amazoncorretto-25
WORKDIR /root/
COPY ./pom.xml .
RUN mvn dependency:copy-dependencies -DoutputDirectory=lib
COPY ./src ./src
RUN mvn install

FROM debian:13
WORKDIR /root/
RUN apt-get update && apt-get -y install curl gpg
RUN curl https://apt.corretto.aws/corretto.key | gpg --dearmor -o /usr/share/keyrings/corretto-keyring.gpg && echo "deb [signed-by=/usr/share/keyrings/corretto-keyring.gpg] https://apt.corretto.aws stable main" | tee /etc/apt/sources.list.d/corretto.list
RUN apt-get update && apt-get install -y java-25-amazon-corretto-jdk
COPY --from=0 /root/target/VVTalk-0.0.1-SNAPSHOT.jar ./VVTalk-0.0.1-SNAPSHOT.jar
COPY --from=0 /root/lib ./
CMD ["java","--enable-native-access=ALL-UNNAMED","-jar","VVTalk-0.0.1-SNAPSHOT.jar"]
