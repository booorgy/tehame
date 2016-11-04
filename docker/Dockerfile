FROM jboss/wildfly:10.1.0.Final

ENV MYSQL_JDBC_VERSION 5.1.40

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
COPY tehame-0.0.1-SNAPSHOT.war $JBOSS_HOME/standalone/deployments/

USER jboss

EXPOSE 8080
