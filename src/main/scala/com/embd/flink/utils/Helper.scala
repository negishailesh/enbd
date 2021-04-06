package com.embd.flink.utils

import org.apache.flink.api.common.state.{MapStateDescriptor, StateTtlConfig}
import com.embd.flink.models.AccountType
import org.apache.flink.api.common.time.Time

object Helper {

  val accountStateDesciptor: MapStateDescriptor[String, AccountType] =
    new MapStateDescriptor[String, AccountType](
      "sessions",
      classOf[String],
      classOf[AccountType]
    )

  val ttlConfig: StateTtlConfig = StateTtlConfig
    .newBuilder(Time.minutes(30L))
    .updateTtlOnReadAndWrite()
    .returnExpiredIfNotCleanedUp()
    .useProcessingTime()
    .build()
  accountStateDesciptor.enableTimeToLive(ttlConfig)



}
