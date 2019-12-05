FROM openjdk:12

VOLUME /tmp

ADD ./target/st-microservice-workspaces-0.0.1-SNAPSHOT.jar st-microservice-workspaces.jar

EXPOSE 8080

ENTRYPOINT java -jar /st-microservice-workspaces.jar