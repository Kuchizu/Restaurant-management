Entrypoint:         http://localhost:49999/

Order Service:      http://localhost:8081/swagger-ui.html
Kitchen Service:    http://localhost:8082/swagger-ui.html
Menu Service:       http://localhost:8083/swagger-ui.html
Inventory Service:  http://localhost:8084/swagger-ui.html
Billing Service:    http://localhost:8085/swagger-ui.html

Apps:               http://localhost:8761/eureka/apps



Config Server: http://localhost:8888



Kafka UI: http://localhost:8090



MinIO: http://localhost:9001

minioadmin

minioadmin123



config-server
./gradlew bootRun

eureka-server
./gradlew bootRun

order-service && ./gradlew bootRun


