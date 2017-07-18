#!/bin/bash

echo "NOTE: adjust paths." 

cd /opt/spark-2.2.0-bin-hadoop2.7
bin/spark-submit --class com.rhdzmota.example.StreamAnalyst --master "local[*]" /media/rhdzmota/Data/Files/github_rhdzmota/kafka-spark-streaming-state-example/target/scala-2.11/kafka-spark-streaming-state-example-assembly-0.1-SNAPSHOT.jar

