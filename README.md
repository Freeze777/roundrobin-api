## HTTP Round Robin API
This repo is a monorepo containing 2 projects:
- EchoServer : A simple echo server that returns the same JSON request body.
- RoundRobinServer : A simple proxy server that forwards requests to a list of echo servers in a round-robin fashion.

### EchoServer
- simple spring boot application
- returns a 200 OK with the same JSON request body if the request body is a valid JSON
- returns a 400 BAD REQUEST if the request body is not a valid JSON

### RoundRobinServer
- simple spring boot application
- forward requests to a list of echo servers in a round-robin fashion
- the list of echo servers is defined in the `application.properties` file
- returns a 502 BAD GATEWAY when it cannot reach the echo servers.
- returns same response as the echo servers

#### Reliability
- timeouts can be set in the `application.properties` file
- exponential backoff retry settings can be set in the `application.properties` file. 
- upstream server success rates are tracked using moving averages. SMA and EMA can be configured in the `application.properties` file.


