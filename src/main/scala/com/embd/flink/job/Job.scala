package com.embd.flink.job

import java.util.{Optional, Properties}
import java.util.concurrent.TimeUnit

import com.embd.flink.flow.StreamJoinFlow
import com.embd.flink.source.KafkaJsonSource
import com.embd.flink.utils.ConfigHelper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.flink.api.common.restartstrategy.RestartStrategies
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.api.common.time.Time
import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer011
import org.apache.flink.streaming.connectors.kafka.internals.KeyedSerializationSchemaWrapper
import org.apache.flink.streaming.connectors.kafka.partitioner.{FlinkFixedPartitioner, FlinkKafkaPartitioner}
import org.slf4j.LoggerFactory
import org.apache.flink.api.scala._

object Job extends TemplateJob {
  private val logger = LoggerFactory.getLogger(this.getClass)

  def main(args: Array[String]): Unit ={
    val params: ParameterTool = ParameterTool.fromArgs(args)

    val configHelper = new ConfigHelper("dev.conf")

    val env = super.createStreamEnv(configHelper, true, 5, 5)
    env.setRestartStrategy(
      RestartStrategies.failureRateRestart(
        2,
        Time.of(5, TimeUnit.MINUTES),
        Time.of(10, TimeUnit.SECONDS)
      )
    )
    val jobName = configHelper.getProperty("enbd.jobname")

    val properties = new Properties

    properties.setProperty("bootstrap.servers", configHelper.getProperty("enbd.sink.kafka.bootstrap"))

    val myProducer = new FlinkKafkaProducer011[String](
      configHelper.getProperty("enbd.sink.kafka.topicname"),
      new KeyedSerializationSchemaWrapper[String](new SimpleStringSchema),
      properties,
      Optional.of(new FlinkFixedPartitioner[String]),
      FlinkKafkaProducer011.Semantic.EXACTLY_ONCE,
      10
    )

    val downStreamParallelism: Int =
      params.getInt("downstreamParallelism", env.getParallelism)

    val consumerParallelism: Int =
      params.getInt("consumerParallelism", env.getParallelism)

    val accountConsumer = new KafkaJsonSource(configHelper).buildConsumer()

    accountConsumer.setCommitOffsetsOnCheckpoints(true)

    val loanConsumer = new KafkaJsonSource(configHelper).buildConsumer(isSecond = true)

    loanConsumer.setCommitOffsetsOnCheckpoints(true)

    val accountStream =
      env
        .addSource(accountConsumer)
        .name(jobName + "accountStream")
        .setParallelism(consumerParallelism)
        .rescale

    val loanStream =
      env
        .addSource(loanConsumer)
        .name(jobName + "loanStream")
        .setParallelism(consumerParallelism)
        .rescale

    val jsonStream = StreamJoinFlow.joinStream(
      accountStream,
      loanStream,
      configHelper
    )
    



    env.execute(jobName)
  }


}
