FROM openjdk:8-alpine

RUN apk update && apk add bash

RUN mkdir -p /opt/app

ENV PROJECT_HOME /opt/app

COPY target/campsite-0.0.1-SNAPSHOT.jar $PROJECT_HOME/campsite.jar
COPY bin/wait-for-it.sh $PROJECT_HOME/bin/wait-for-it.sh

WORKDIR $PROJECT_HOME

CMD ["./bin/wait-for-it.sh", "mysql:3306", "-t", "30", "--", "java", "-jar", "./campsite.jar"]