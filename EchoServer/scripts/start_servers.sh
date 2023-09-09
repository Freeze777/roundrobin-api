#!/bin/zsh
sh ./kill_servers.sh
cd .. &&
mvn clean package
java -jar target/EchoServer-0.0.1-SNAPSHOT.jar --server.port=8080 &
java -jar target/EchoServer-0.0.1-SNAPSHOT.jar --server.port=8081 &
java -jar target/EchoServer-0.0.1-SNAPSHOT.jar --server.port=8082 &

sh ./test_servers.sh
