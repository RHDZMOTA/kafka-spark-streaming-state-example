import Dependencies._


lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.rhdzmota",
      scalaVersion := "2.11.11"
    )),
    name := "kafka-spark-streaming-state-example",
    libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-streaming" % "2.2.0",
      "org.apache.spark" %% "spark-streaming-kafka-0-10" % "2.2.0"
    ),
    assemblyMergeStrategy in assembly := {
     case PathList("META-INF", xs @ _*) => MergeStrategy.discard
     case x => MergeStrategy.first
    }

  )
