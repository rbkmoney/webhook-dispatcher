FROM openjdk:13-slim
MAINTAINER k.struzhkin@rbkmoney.com

COPY @artifactId@-@version@.jar /opt/@artifactId@/@artifactId@.jar

CMD ["java", "-Xmx256m", "-jar","/opt/@artifactId@/@artifactId@.jar"]

EXPOSE @exposed.ports@

LABEL com.rbkmoney.@artifactId@.parent=openjdk-13-slim \
    com.rbkmoney.@artifactId@.commit_id=@git.commit.id@ \
    com.rbkmoney.@artifactId@.branch=@git.branch@

WORKDIR /opt/@artifactId@