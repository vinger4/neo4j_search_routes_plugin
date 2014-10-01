# README #

### What is this repository for? ###

* There is plugin for neo4j database for search routes on graph
* We write version number in pom.xml

### How do I get set up? ###

* We use follow command for make *.jar: *mvn clean package*. Than we copy *.jar into neo4j/plugins directory
* Dependencies: please show pom.xml

### Who do I talk to? ###

* Please feel free to send a email at vinger4 at gmail.com

### Example ###

request:


```
#!bash

curl -X POST http://localhost:7474/db/data/ext/FindShortestPath/graphdb/get_shortest_path -H "Content-Type: application/json" -d '{"node_from": "/node/4238", "node_to": "/node/4316", "relationship_types": ["TRANSPORT_WALK", "TRANSPORT_TRANSPORT_WAIT", "TRANSPORT_TRAIN"], "relationship_costs": [1000.0, 1000.0, 10.0], "only_one_route": true, "soft_timeout": 5000, "max_cost": 1000000.0}'
```

response: 

```
#!bash

"[[{\"country_code\":\"RU\",\"city_name_google_locality_political\":\"Moscow\",\"gtype\":1,\"several_airport_nodes\":\"yes\",\"lng\":37.61713,\"transfer_node\":true,\"bbox\":[55.750883,37.61713,55.750883,37.61713],\"name\":\"Москва\",\"type\":\"area\",\"always_from_area\":true,\"lat\":55.750883,\"coords\":[37.61713,55.750883]},{\"country_code\":\"RU\",\"gtype\":1,\"lng\":37.65446791239999,\"transfer_node\":true,\"bbox\":[55.7777597986,37.65446791239999,55.7777597986,37.65446791239999],\"station_id\":2006004,\"name\":\"Moskva Oktiabrskaia\",\"timezoneId\":\"Europe/Moscow\",\"type\":\"railway_station\",\"lat\":55.7777597986,\"coords\":[37.65446791239999,55.7777597986]},{\"station_id\":2006004,\"name\":\"Moskva Oktiabrskaia\",\"source_type\":\"mobiticket\",\"type\":\"railway_node\"},{\"station_id\":2004001,\"name\":\"SANKT-PETERBURG-GLAVN.\",\"source_type\":\"mobiticket\",\"type\":\"railway_node\"},{\"country_code\":\"RU\",\"gtype\":1,\"lng\":30.3626,\"transfer_node\":true,\"bbox\":[59.928799,30.3626,59.928799,30.3626],\"station_id\":2004001,\"name\":\"Санкт-Петербург-Главн.\",\"timezoneId\":\"Europe/Moscow\",\"type\":\"railway_station\",\"lat\":59.928799,\"coords\":[30.3626,59.928799]},{\"country_code\":\"RU\",\"city_name_google_locality_political\":\"Saint Petersburg\",\"gtype\":1,\"several_airport_nodes\":\"yes\",\"lng\":30.328,\"transfer_node\":true,\"bbox\":[59.938,30.328,59.938,30.328],\"name\":\"RU:Санкт-Петербург\",\"type\":\"area\",\"lat\":59.938,\"coords\":[30.328,59.938]}]]"
```

### Future ###

* I plan to store timetable on the graph and include function to plugin for it.