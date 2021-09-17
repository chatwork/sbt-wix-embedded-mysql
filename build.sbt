import Dependencies._
import sbt.ScriptedPlugin.autoImport._

ThisBuild / scalafixScalaBinaryVersion := CrossVersion.binaryScalaVersion(scalaVersion.value)

lazy val baseSettings = Seq(
  organization := "com.chatwork",
  homepage := Some(url("https://github.com/chatwork/sbt-wix-embedded-mysql")),
  licenses := List("The MIT License" -> url("http://opensource.org/licenses/MIT")),
  developers := List(
    Developer(
      id = "j5ik2o",
      name = "Junichi Kato",
      email = "j5ik2o@gmail.com",
      url = url("https://blog.j5ik2o.me")
    ),
    Developer(
      id = "exoego",
      name = "TATSUNO Yasuhiro",
      email = "ytatsuno.jp@gmail.com",
      url = url("https://www.exoego.net")
    )
  ),
  scalaVersion := Versions.scala212Version,
  scalacOptions ++= (
    Seq(
      "-feature",
      "-deprecation",
      "-unchecked",
      "-encoding",
      "UTF-8",
      "-language:_",
      "-Ydelambdafy:method",
      "-target:jvm-1.8",
      "-Yrangepos",
      "-Ywarn-unused"
    )
  ),
  resolvers ++= Seq(
    Resolver.sonatypeRepo("snapshots"),
    Resolver.sonatypeRepo("releases")
  ),
  semanticdbEnabled := true,
  semanticdbVersion := scalafixSemanticdb.revision,
  Test / publishArtifact := false,
  Test / parallelExecution := false,
  sbtPlugin := true
)

val root = (project in file("."))
  .enablePlugins(SbtPlugin)
  .settings(baseSettings)
  .settings(
    name := "sbt-wix-embedded-mysql",
    scriptedBufferLog := false,
    scriptedLaunchOpts := {
      scriptedLaunchOpts.value ++
      Seq("-Xmx1024M", "-Dproject.version=" + version.value)
    },
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest"          % "3.2.10" % Test,
      "com.wix"        % "wix-embedded-mysql" % "4.6.1"
    )
  )

addCommandAlias("lint", ";scalafmtCheck;test:scalafmtCheck;scalafmtSbtCheck;scalafixAll --check")
addCommandAlias("fmt", ";scalafmtAll;scalafmtSbt;scalafix RemoveUnused")
