name := "akka-experiments"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.4.9",
  "com.typesafe" % "config" % "1.3.0",
  "com.typesafe.akka" %% "akka-testkit" % "2.4.9",
  "org.scalatest" %% "scalatest" % "3.0.0" % "test",
  "org.mockito" % "mockito-core" % "2.1.0-beta.120" % "test"
)

