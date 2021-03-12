import ReleaseTransformations._
import xerial.sbt.Sonatype.autoImport.sonatypeCredentialHost

name := "jooq-async"
organization := "fr.maif"

scalaVersion := "2.12.12"
crossScalaVersions := List("2.13.5", "2.12.13")

usePgpKeyHex("01BA0C89CEC406826F7680A162D9B4F3D67419B7")
sonatypeRepository := "https://s01.oss.sonatype.org/service/local"
sonatypeCredentialHost := "s01.oss.sonatype.org"

lazy val root = (project in file("."))
  .aggregate(
    `jooq-async-api`,
    `jooq-async-api-tck`,
    `jooq-async-jdbc`,
    `jooq-async-reactive`
  )
  .enablePlugins(NoPublish, GitVersioning, GitBranchPrompt)
  .settings(
    skip in publish := true
  )

lazy val `jooq-async-api` = project
  .settings(
      sonatypeRepository := "https://s01.oss.sonatype.org/service/local",
      sonatypeCredentialHost := "s01.oss.sonatype.org"
  )

lazy val `jooq-async-api-tck` = project
  .dependsOn(`jooq-async-api`)
  .settings(
    sonatypeRepository := "https://s01.oss.sonatype.org/service/local",
    sonatypeCredentialHost := "s01.oss.sonatype.org"
  )

lazy val `jooq-async-jdbc` = project
  .dependsOn(`jooq-async-api`, `jooq-async-api-tck` %  "compile->test")
  .settings(
    sonatypeRepository := "https://s01.oss.sonatype.org/service/local",
    sonatypeCredentialHost := "s01.oss.sonatype.org"
  )

lazy val `jooq-async-reactive` = project
  .dependsOn(`jooq-async-api`, `jooq-async-api-tck` %  "compile->test")
  .settings(
    sonatypeRepository := "https://s01.oss.sonatype.org/service/local",
    sonatypeCredentialHost := "s01.oss.sonatype.org"
  )


javacOptions in Compile ++= Seq("-source", "8", "-target", "8", "-Xlint:unchecked", "-Xlint:deprecation")

testFrameworks := Seq(TestFrameworks.JUnit)
testOptions += Tests.Argument(TestFrameworks.JUnit, "-v")

(parallelExecution in Test) := false

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  setNextVersion,
  commitNextVersion,
  pushChanges
)

lazy val githubRepo = "maif/jooq-async"

inThisBuild(List(
  homepage := Some(url(s"https://github.com/$githubRepo")),
  startYear := Some(2020),
  scmInfo := Some(
    ScmInfo(
      url(s"https://github.com/$githubRepo"),
      s"scm:git:https://github.com/$githubRepo.git",
      Some(s"scm:git:git@github.com:$githubRepo.git")
    )
  ),
  licenses := Seq(("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))),
  developers := List(
    Developer("alexandre.delegue", "Alexandre Delègue", "", url(s"https://github.com/larousso")),
    Developer("benjamin.cavy", "Benjamin Cavy", "", url(s"https://github.com/ptitFicus")),
    Developer("gregory.bevan", "Grégory Bévan", "", url(s"https://github.com/GregoryBevan"))
  ),
  releaseCrossBuild := true,
  publishMavenStyle := true,
  publishArtifact in Test := false
))
