FROM java:8-alpine
MAINTAINER Your Name <you@example.com>

ADD target/uberjar/rainmote.jar /rainmote/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/rainmote/app.jar"]
