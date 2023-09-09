mvn clean package
kill $(lsof -t -i :9001)
java -jar target/RoundRobinServer-0.0.1-SNAPSHOT.jar --server.port=9001 &

