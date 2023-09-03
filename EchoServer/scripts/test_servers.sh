echo "server 8080:"
curl --location 'http://localhost:8080/echo' \
--header 'Content-Type: application/json' \
--data '{

    "text" : "Hello world!",
    "whatever": "blah blah"
}'

echo ""
echo "server 8081:"
curl --location 'http://localhost:8081/echo' \
--header 'Content-Type: application/json' \
--data '{

    "text" : "Hello world!",
    "whatever": "blah blah"
}'

echo ""
echo "server 8082:"
curl --location 'http://localhost:8082/echo' \
--header 'Content-Type: application/json' \
--data '{

    "text" : "Hello world!",
    "whatever": "blah blah"
}'
