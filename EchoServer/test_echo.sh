curl --location 'http://localhost:8080/echo' \
--header 'Content-Type: application/json' \
--data '{

    "text" : "Hello world",
    "whatever": "blah blah"
}'
