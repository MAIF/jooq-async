import sbt.Keys.organization
import Dependencies._

organization := "fr.maif"

name := "jooq-async-api-tck"

scalaVersion := "2.12.12"

libraryDependencies ++= Seq(
  "io.vavr"                         % "vavr"                      % vavrVersion,
  "org.jooq"                        % "jooq"                      % _jooqVersion,
  "com.typesafe.akka"               %% "akka-stream"              % akkaVersion,
  "com.fasterxml.jackson.core"      % "jackson-databind"          % jacksonVersion,
  "io.vavr"                         % "vavr-jackson"              % vavrVersion,
  "com.novocode"                    % "junit-interface"           % "0.11",
  "org.assertj"                     % "assertj-core"               % "3.10.0",
  "org.postgresql"                  % "postgresql"                 % "42.2.5",
  "org.mockito"                     % "mockito-core"               % "2.22.0"
)


