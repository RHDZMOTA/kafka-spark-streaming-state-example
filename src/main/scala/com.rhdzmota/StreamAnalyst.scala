package com.rhdzmota.example

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.{StringDeserializer, LongDeserializer }
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.{SparkContext, SparkConf}
import org.apache.spark.SparkContext._
import org.apache.spark.streaming.{Seconds, StreamingContext, Time, State, StateSpec}
import org.apache.kafka.common.TopicPartition
import org.apache.spark.storage._
import scala.collection.mutable


object StreamAnalyst {

  // Spark config and streaming context
  val sparkConf = new SparkConf().setAppName("StreamAnalyst")
  val ssc = new StreamingContext(sparkConf, Seconds(10))


  def trackStateFunc(batchTime: Time, key: String,
                     value: Option[String],
                     state: State[List[String]]
                    ): Option[(String, List[String])] = {
    val newVal = value match {
      case Some(v) => v :: state.getOption.getOrElse(List[String]())
      case None => state.getOption.getOrElse(List[String]())
    }
    val output = (key, newVal)
    state.update(newVal)
    Some(output)
  }

  def main(args: Array[String]) {

    ssc.checkpoint("/tmp/spark-streaming-checkpoint")

    // Kafka Configs.
    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> "localhost:9092",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer ],
      "group.id" -> "spark_streaming",
      "auto.offset.reset" -> "latest",
      "enable.auto.commit" -> (false: java.lang.Boolean)
    )

    // Listen to topics: adapt according to your needs
    val topics = Array("Test")


    // Get direct stream from Kafka
    val kafkaStream = KafkaUtils.createDirectStream[String, String](
    ssc,
    PreferConsistent,
    Subscribe[String, String](topics, kafkaParams)
    )

    // Create the initial datastructure for the State.
    val initialize = ssc.sparkContext.parallelize(List[(String, List[String])]())

    // Define state specifications
    val stateSpec = StateSpec.function(trackStateFunc _)
                             .initialState(initialize)
                             .numPartitions(1)
                             .timeout(Seconds(30))


    // KafkaDirectStream to DirectStream and separate vals into a pair.
    val stream = kafkaStream.map(x => (x.value.split(" ")(0), x.value.split(" ")(1)))

    // Update state
    val stateStream = stream.mapWithState(stateSpec)

    // print val
    stateStream.print()


    // Start streaming
    ssc.start
    ssc.awaitTermination()
  }
}
