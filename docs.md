Entrypoint:         http://localhost:49999/

Order Service:      http://localhost:8081/swagger-ui.html
Kitchen Service:    http://localhost:8082/swagger-ui.html
Menu Service:       http://localhost:8083/swagger-ui.html
Inventory Service:  http://localhost:8084/swagger-ui.html
Billing Service:    http://localhost:8085/swagger-ui.html

Apps:               http://localhost:8761/eureka/apps



Config Server: http://localhost:8888



Kafka UI: http://localhost:8090

docker exec kafka-1 kafka-topics --list --bootstrap-server kafka-1:9092

docker exec kafka-1 kafka-console-consumer --bootstrap-server kafka-1:9092 --topic restaurant.orders.created --from-beginning



3 брокера

kafka-1:29092, kafka-2:29093, kafka-3:29094

Репликация

KAFKA_DEFAULT_REPLICATION_FACTOR: 2
KAFKA_MIN_INSYNC_REPLICAS: 2
KAFKA_NUM_PARTITIONS: 3



docker exec kafka-1 kafka-metadata --snapshot /var/lib/kafka/data/__cluster_metadata-0/00000000000000000000.log --command "broker-list"

Детали топика

docker exec kafka-1 kafka-topics --describe --topic restaurant.orders.created --bootstrap-server kafka-1:9092



MinIO: http://localhost:9001

minioadmin

minioadmin123



config-server
./gradlew bootRun

eureka-server
./gradlew bootRun

order-service && ./gradlew bootRun
