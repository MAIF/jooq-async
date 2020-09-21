import sbt.Keys.organization
import Dependencies._

organization := "fr.maif"

name := "jooq-async-jdbc"

scalaVersion := "2.12.12"

libraryDependencies ++= Seq(
  "io.vavr"                         % "vavr"                      % vavrVersion,
  "org.jooq"                        % "jooq"                      % _jooqVersion,
  "com.novocode"                    % "junit-interface" % "0.11"  % Test,
  "org.assertj"                     % "assertj-core"               % "3.10.0" % Test,
  "org.postgresql"                  % "postgresql"                 % "42.2.5" % Test,
  "org.mockito"                     % "mockito-core"               % "2.22.0" % Test
)


