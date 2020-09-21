import sbt.Keys.organization
import Dependencies._

organization := "fr.maif"

name := "jooq-async-api"

scalaVersion := "2.12.12"

libraryDependencies ++= Seq(
  "io.vavr"                         % "vavr"                      % vavrVersion,
  "org.jooq"                        % "jooq"                      % _jooqVersion,
  "com.typesafe.akka"               %% "akka-stream"              % akkaVersion,
  "org.slf4j"                       % "slf4j-api"                 % "1.7.30",
  "org.slf4j"                       % "slf4j-log4j12"             % "1.7.30"
)


