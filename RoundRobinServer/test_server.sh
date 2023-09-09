curl --location 'http://localhost:9001/roundrobin' \
--header 'Content-Type: application/json' \
--data '{

    "text" : "Hello world",
    "whatever": "blah blah"
}'

echo ""
echo "-------------------"

curl --location 'http://localhost:9001/roundrobin' \
--header 'Content-Type: application/json' \
--data '{

    "text" : "Hello world",
    "broken-text": "blah blah
}'

echo ""
echo "-------------------"

curl --location 'http://localhost:9001/roundrobin' \
--header 'Content-Type: application/json' \
--data '{

    "foo" : "Foo",
    "bar": "Bar"
}'

echo ""
echo "-------------------"

# huge payload
curl --location 'http://localhost:9001/roundrobin' \
--header 'Content-Type: application/json' \
--data '{
    "foo": "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Integer nec odio. Praesent libero. Sed cursus ante dapibus diam. Sed nisi. Nulla quis sem at nibh elementum imperdiet. Duis sagittis ipsum. Praesent mauris.",
    "bar": "Fusce nec tellus sed augue semper porta. Mauris massa. Vestibulum lacinia arcu eget nulla. Class aptent taciti sociosqu ad litora",
    "baz": "Donec nec justo eget felis facilisis fermentum. Aliquam porttitor mauris sit amet orci. Aenean dignissim pellentesque felis.",
    "qux": "Morbi in sem quis dui placerat ornare. Pellentesque odio nisi, euismod in, pharetra a, ultricies in, diam.",
    "quux": "Sed arcu. Cras consequat. Praesent dapibus, neque id cursus faucibus, tortor neque egestas augue, eu vulputate magna",
    "quuz": "Cras convallis tellus et elit aliquet, vitae dignissim tellus elementum. Maecenas at aliquet eros, in sollicitudin orci. Aliquam erat volutpat. Donec vitae tincidunt eros."
}'

echo ""
echo "-------------------"

# no payload
curl --location 'http://localhost:9001/roundrobin' \
--header 'Content-Type: application/json' \
--data '{}'
