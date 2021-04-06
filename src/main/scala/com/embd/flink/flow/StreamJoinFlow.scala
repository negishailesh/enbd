package com.embd.flink.flow

import com.embd.flink.utils.ConfigHelper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.flink.streaming.api.datastream.DataStream
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time


object StreamJoinFlow {

  def joinStream(stream1: DataStream[ObjectNode],
                 stream2: DataStream[ObjectNode],
                 configHelper: ConfigHelper) = {
    val joinedStream = stream1.join(stream2)
      .where(stream1._0)
      .equalTo(stream2._1)
      .window(TumblingProcessingTimeWindows.of(Time.seconds(60)))
      .apply(new AggregationFunction())

  }







}
