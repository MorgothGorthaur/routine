FROM openjdk:17-oracle
COPY target/routine-0.0.1-SNAPSHOT.jar /demo.jar
CMD ["java", "-jar", "demo.jar"]