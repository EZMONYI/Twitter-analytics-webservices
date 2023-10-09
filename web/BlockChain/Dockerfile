FROM ubuntu:20.04

RUN apt-get update && \
    apt-get -y install openjdk-17-jre

EXPOSE 80

ADD ./target/Blockchain-1.0-SNAPSHOT.jar /

CMD ["java -jar Blockchain-1.0-SNAPSHOT.jar"]

ENTRYPOINT ["/bin/bash", "-c"]