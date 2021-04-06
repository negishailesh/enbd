ThisBuild / resolvers ++= Seq(
  "Apache Development Snapshot Repository" at "https://repository.apache.org/content/repositories/snapshots/",
  Resolver.mavenLocal
)
scalacOptions in ThisBuild += "-target:jvm-1.8"
resolvers += "confluent" at "http://packages.confluent.io/maven"
resolvers += "Artima Maven Repository" at "http://repo.artima.com/releases"
name := "enbd"
version := "0.1-master"
organization := "enbd"
ThisBuild / scalaVersion := "2.11.12"
val flinkVersion = "1.12.1"
val flinkDependencies = Seq(
  "org.apache.flink" %% "flink-scala" % flinkVersion,
  "org.apache.flink" %% "flink-streaming-scala" % flinkVersion,
  "org.apache.flink" %% "flink-connector-kafka-0.11" % "1.11.3",
  "org.apache.flink" %% "flink-statebackend-rocksdb" % flinkVersion,
  "org.apache.flink" %% "flink-connector-filesystem" % "1.11.3",
  "org.apache.flink" % "flink-streaming-connectors" % "1.1.5" % "provided",
  "org.apache.flink" %% "flink-runtime-web" % flinkVersion,
  "org.apache.flink" % "flink-avro-confluent-registry" % flinkVersion exclude ("com.fasterxml.jackson.core", "jackson-databind"),
  "com.github.wnameless" % "json-flattener" % "0.7.1",
  "com.softwaremill.sttp" %% "core" % "1.7.2",
  "org.apache.avro" % "avro" % "1.9.2",
  "org.mapdb" % "mapdb" % "3.0.8",
  "org.apache.logging.log4j" % "log4j-api" % "2.11.1",
  "org.apache.logging.log4j" % "log4j-core" % "2.11.1",
  "org.slf4j" % "slf4j-simple" % "1.7.21",
  "org.slf4j" % "slf4j-api" % "1.7.21",
  "org.json4s" %% "json4s-core" % "3.6.2",
  "org.json4s" %% "json4s-native" % "3.6.2",
  "org.json4s" %% "json4s-jackson" % "3.6.2"
)
libraryDependencies += "com.spatial4j" % "spatial4j" % "0.5"
libraryDependencies += "io.github.openfeign" % "feign-core" % "11.0"
libraryDependencies += "io.github.openfeign" % "feign-jackson" % "11.0"
libraryDependencies += "com.maxmind.db" % "maxmind-db" % "1.4.0"
libraryDependencies += "redis.clients" % "jedis" % "3.3.0"
libraryDependencies += "org.apache.flink" % "flink-s3-fs-hadoop" % flinkVersion
libraryDependencies += "com.redislabs" % "jrebloom" % "2.0.0-m2"
// https://mvnrepository.com/artifact/org.apache.flink/flink-test-utils
libraryDependencies += "org.apache.flink" %% "flink-test-utils" % flinkVersion % Test
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % "test"
libraryDependencies += "nl.basjes.parse.useragent" % "yauaa" % "5.22"
lazy val root = (project in file(".")).
  settings(
    libraryDependencies ++= flinkDependencies
  )
assembly / mainClass := Some("com.embd.flink.job.Job")
assemblyJarName in assembly := s"enbd.jar"
// make run command include the provided dependencies
Compile / run  := Defaults.runTask(Compile / fullClasspath,
  Compile / run / mainClass,
  Compile / run / runner
).evaluated
// stays inside the sbt console when we press "ctrl-c" while a Flink programme executes with "run" or "runMain"
Compile / run / fork := true
Global / cancelable := true
// exclude Scala library from assembly
assembly / assemblyOption  := (assembly / assemblyOption).value.copy(includeScala = false)
assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x                             => MergeStrategy.first
}