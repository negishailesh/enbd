package com.embd.flink.utils
import java.util.Properties

import com.typesafe.config.{Config, ConfigFactory}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.slf4j.LoggerFactory

class ConfigHelper(configFile: String) extends Serializable {

  val config: Properties = propsFromConfig(ConfigFactory.load(configFile))
  private val logger = LoggerFactory.getLogger(this.getClass)
  logger.info(config.toString)

  def getDefaultConsumerConfig: Properties = {
    val consumerProperties = new Properties()
    consumerProperties.setProperty(
      "bootstrap.servers",
      config.getProperty("enbd.source.kafka.bootstrap")
    )
    consumerProperties.setProperty(
      "group.id",
      config.getProperty("enbd.source.kafka.groupid")
    )


    consumerProperties.setProperty(
      "fetch.max.bytes",
      config.getProperty("enbd.source.kafka.maxfetchbytes", "524288000")
    )
    consumerProperties.setProperty(
      "max.poll.records",
      config.getProperty("enbd.source.kafka.maxpollrecords", "500")
    )
    consumerProperties.setProperty(
      "max.partition.fetch.bytes",
      config
        .getProperty("enbd.source.kafka.maxbytesperpartition", "1048576")
    )
    consumerProperties.setProperty("receive.buffer.bytes", "-1")
    consumerProperties.setProperty("send.buffer.bytes", "-1")
    consumerProperties.setProperty("check.crcs", "false")
    consumerProperties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    consumerProperties

  }

  def getDefaultProducerConfig: Properties = {
    val producerProperties = new Properties()
    producerProperties.setProperty(
      "bootstrap.servers",
      config.getProperty("enbd.sink.kafka.bootstrap")
    )
    producerProperties.setProperty(
      "group.id",
      config.getProperty("enbd.sink.kafka.groupid")
    )
    producerProperties.setProperty("compression.type", "snappy")
    producerProperties.setProperty(
      "client.id",
      config.getProperty("enbd.sink.kafka.kafkaclientid") + System
        .currentTimeMillis()
    )
    producerProperties.setProperty("receive.buffer.bytes", "-1")
    producerProperties.setProperty("send.buffer.bytes", "-1")
    producerProperties.setProperty(ProducerConfig.RETRIES_CONFIG, "3")

    producerProperties
  }

  def getProperty(key: String): String = {
    config.getProperty(key)
  }

  private def propsFromConfig(config: Config): Properties = {
    import scala.collection.JavaConversions._

    val props = new Properties()

    val map: Map[String, Object] = config
      .entrySet()
      .map({ entry => entry.getKey -> entry.getValue.unwrapped()})(collection.breakOut)

    props.putAll(map)

    props
  }

}
