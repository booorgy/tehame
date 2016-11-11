FROM jboss/wildfly:10.1.0.Final

ENV MYSQL_JDBC_VERSION 5.1.40

ARG RDS_DB_NAME=tehame
ARG RDS_USERNAME=root
ARG RDS_PASSWORD=gude
ARG RDS_HOSTNAME=localhost
ARG RDS_PORT=3306

ENV RDS_DB_NAME=$RDS_DB_NAME
ENV RDS_USERNAME=$RDS_USERNAME
ENV RDS_PASSWORD=$RDS_PASSWORD
ENV RDS_HOSTNAME=$RDS_HOSTNAME
ENV RDS_PORT=$RDS_PORT

USER root

RUN yum -y install wget \
    && yum clean all \
    && wget -O /tmp/connector.tar.gz https://dev.mysql.com/get/Downloads/Connector-J/mysql-connector-java-$MYSQL_JDBC_VERSION.tar.gz \
    && tar xf /tmp/connector.tar.gz -C /tmp/ \
    && rm /tmp/connector.tar.gz \
    && mkdir -p $JBOSS_HOME/modules/com/mysql/main/ \
    && cp /tmp/mysql-connector-java-$MYSQL_JDBC_VERSION/mysql-connector-java-$MYSQL_JDBC_VERSION-bin.jar $JBOSS_HOME/modules/com/mysql/main/mysql-connector-java-$MYSQL_JDBC_VERSION-bin.jar

COPY module.xml $JBOSS_HOME/modules/com/mysql/main/
COPY standalone.xml $JBOSS_HOME/standalone/configuration/
COPY tehame.war $JBOSS_HOME/standalone/deployments/

USER jboss

EXPOSE 8080
