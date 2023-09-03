mvn clean package
java -jar target/SimpleServer-0.0.1-SNAPSHOT.jar --server.port=8080 &
java -jar target/SimpleServer-0.0.1-SNAPSHOT.jar --server.port=8081 &
java -jar target/SimpleServer-0.0.1-SNAPSHOT.jar --server.port=8082 &
