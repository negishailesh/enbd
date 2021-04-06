package com.embd.flink.source

import com.embd.flink.utils.ConfigHelper
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011


class KafkaJsonSource(config: ConfigHelper, fromLatest: Boolean = true) {

  def buildConsumer(isSecond : Boolean = false): FlinkKafkaConsumer011[ObjectNode] = {

    val topic = if(!isSecond)config.getProperty("enbd.source.kafka.topicname")
    else config.getProperty("enbd.source.kafka.secondtopicname")

    val kafkaJsonConsumer = new FlinkKafkaConsumer011[ObjectNode](
      topic,
      new CustomJSONDeserializationSchema(),
      config.getDefaultConsumerConfig
    )
    if(fromLatest){
      kafkaJsonConsumer.setStartFromLatest()
    }
    kafkaJsonConsumer
  }
}