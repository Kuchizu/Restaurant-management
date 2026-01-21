#!/bin/bash

echo "Status"
docker exec kafka-1 kafka-topics --bootstrap-server kafka-1:9092 --describe --topic restaurant.orders.created

echo ""
echo "Stop 1"
docker stop kafka-1
sleep 5

echo ""
echo "Status after stop"
docker exec kafka-2 kafka-topics --bootstrap-server kafka-2:9092 --describe --topic restaurant.orders.created

echo ""
echo "Restore 1"
docker start kafka-1
sleep 10

echo ""
echo "Final 1"
docker exec kafka-1 kafka-topics --bootstrap-server kafka-1:9092 --describe --topic restaurant.orders.created
