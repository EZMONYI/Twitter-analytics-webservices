FROM ubuntu:20.04

RUN apt-get update && \
    apt-get -y install openjdk-17-jre

EXPOSE 80

ADD ./target/twitter-0.0.1-SNAPSHOT.jar /

CMD ["java -jar twitter-0.0.1-SNAPSHOT.jar"]

ENTRYPOINT ["/bin/bash", "-c"]