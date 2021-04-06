package com.embd.flink.job

import com.embd.flink.utils.ConfigHelper
import org.slf4j.LoggerFactory
import org.apache.flink.contrib.streaming.state.RocksDBStateBackend
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.{CheckpointingMode, TimeCharacteristic}


abstract class TemplateJob {

  private val logger = LoggerFactory.getLogger(this.getClass)

  def createStreamEnv(configHelper: ConfigHelper,
                      enableCheckpointing: Boolean,
                      checkPointDurationMins: Int = 15,
                      minPauseBetweenCheckpointMins: Int = 15) = {

    logger.info("Creating flink environment...")
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)

    addRocksDBStateBackend(env,configHelper)
    if (enableCheckpointing) {
      env.enableCheckpointing(checkPointDurationMins * 60 * 1000L, CheckpointingMode.EXACTLY_ONCE)
      env.getCheckpointConfig.enableExternalizedCheckpoints(
        ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION
      )
      env.getCheckpointConfig.setMinPauseBetweenCheckpoints(minPauseBetweenCheckpointMins * 60 * 1000L)
    }
    env
  }

  def addRocksDBStateBackend(
                              env: StreamExecutionEnvironment,
                              configHelper: ConfigHelper
                            ): StreamExecutionEnvironment = {
    val rocksDBStateBackend = new RocksDBStateBackend(
      configHelper.getProperty("enbd.rocksdb.state.checkpoint.dir"),
      true
    )

    rocksDBStateBackend.setDbStoragePath(configHelper.getProperty("enbd.rocksdb.state.backend.storage.dir"))
    env.setStateBackend(rocksDBStateBackend)
    env
  }

}
