FROM openjdk:8-jdk 
ADD opt /opt/
ADD authz-service-1.0.1-SNAPSHOT.jar /opt/app/aaf/authz-service/lib/authz-service-1.0.1-SNAPSHOT.jar
ADD startup.sh /startup.sh
RUN chmod 777 /startup.sh
RUN chmod -R 777 /opt/app/aaf/authz-service/etc
ENTRYPOINT ./startup.sh


