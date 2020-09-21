import ReleaseTransformations._

name := "jooq-async"
organization := "fr.maif"

scalaVersion := "2.12.12"

val res = Seq(
  Resolver.jcenterRepo,
  Resolver.bintrayRepo("maif-jooq-async", "maven")
)


resolvers ++= res

lazy val root = (project in file("."))
  .aggregate(
    `jooq-async-api`,
    `jooq-async-api-tck`,
    `jooq-async-jdbc`,
    `jooq-async-reactive`
  )
  .enablePlugins(NoPublish, GitVersioning, GitBranchPrompt)
  .disablePlugins(BintrayPlugin)

lazy val `jooq-async-api` = project
  .settings(publishCommonsSettings: _*)

lazy val `jooq-async-api-tck` = project
  .dependsOn(`jooq-async-api`)
  .settings(publishCommonsSettings: _*)

lazy val `jooq-async-jdbc` = project
  .dependsOn(`jooq-async-api`, `jooq-async-api-tck` %  "compile->test")
  .settings(publishCommonsSettings: _*)

lazy val `jooq-async-reactive` = project
  .dependsOn(`jooq-async-api`, `jooq-async-api-tck` %  "compile->test")
  .settings(publishCommonsSettings: _*)


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

lazy val publishCommonsSettings = Seq(
  homepage := Some(url(s"https://github.com/$githubRepo")),
  startYear := Some(2020),
  bintrayOmitLicense := true,
  crossPaths := false,
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
  publishArtifact in Test := false,
  bintrayVcsUrl := Some(s"scm:git:git@github.com:$githubRepo.git"),
  resolvers ++= res,
  bintrayOrganization := Some("maif-jooq-async"),
  bintrayRepository := "maven",
  pomIncludeRepository := { _ =>
    false
  }
)
