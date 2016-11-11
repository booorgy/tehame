#!/bin/bash

clear

if [ ! -f Dockerfile ]
then 
	echo "Dockerfile nicht gefunden, falsches Verzeichnis?"
    exit 1
fi

mkdir -p logs
rm tehame.war 2> /dev/null

cd ..

echo "Bitte warten ... Maven baut Projekt ..."
mvn clean package > docker/logs/mvn.log
tail -n 7 docker/logs/mvn.log
cp target/tehame.war docker/tehame.war
cd docker

echo "Bitte warten ... Docker baut Image ..."
docker build . -t tehame/tehame:latest > logs/docker.log
echo "---------------------------------------------"
tail -n 1 logs/docker.log

echo "---------------------------------------------"
echo "Bitte warten ... Docker startet Container ..."
docker run tehame/tehame:latest > logs/wfly.log &
sleep 30s
tail -n 5 logs/wfly.log

echo "---------------------------------------------"
echo "IP Adresse des Containers:"
docker network inspect bridge | grep IPv4
echo "Laufende Docker Instanzen:"
docker ps

echo "---------------------------------------------"
echo "Befehl um Container zu beenden: docker stop <Container Name oder ID>"