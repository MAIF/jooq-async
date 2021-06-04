import sbt.Keys.organization
import Dependencies._

organization := "fr.maif"

name := "jooq-async-reactive"

scalaVersion := "2.12.12"

libraryDependencies ++= Seq(
  "io.vavr"                         % "vavr"                      % vavrVersion,
  "org.jooq"                        % "jooq"                      % _jooqVersion,
  "com.fasterxml.jackson.core"      % "jackson-databind"          % jacksonVersion,
  "org.slf4j"                       % "slf4j-api"                 % "1.7.26",
  "io.vertx"                        % "vertx-pg-client"           % "4.1.0",
  "com.novocode"                    % "junit-interface" % "0.11"  % Test
)
