sbtPlugin := true

sonatypeProfileName := "com.chatwork.sbt.wix.embedded.mysql"

organization := "com.chatwork"

name := "sbt-wix-embedded-mysql"

publishMavenStyle := true

scalaVersion := "2.10.5"

publishArtifact in Test := false

pomIncludeRepository := {
  _ => false
}

pomExtra := {
  <url>https://github.com/chatwork/sbt-wix-embedded-mysql</url>
    <licenses>
      <license>
        <name>The MIT License</name>
        <url>http://opensource.org/licenses/MIT</url>
      </license>
    </licenses>
    <scm>
      <url>git@github.com:chatwork/sbt-wix-embedded-mysql.git</url>
      <connection>scm:git:github.com/chatwork/sbt-wix-embedded-mysql</connection>
      <developerConnection>scm:git:git@github.com:chatwork/sbt-wix-embedded-mysql.git</developerConnection>
    </scm>
    <developers>
      <developer>
        <id>j5ik2o</id>
        <name>Junichi Kato</name>
      </developer>
    </developers>
}

credentials := Def.task {
  val ivyCredentials = (baseDirectory in LocalRootProject).value / ".credentials"
  val result = Credentials(ivyCredentials) :: Nil
  result
}.value


scalacOptions ++= Seq(
  "-feature"
  , "-deprecation"
  , "-unchecked"
  , "-encoding"
  , "UTF-8"
  , "-Xfatal-warnings"
  , "-language:_"
  , "-Ywarn-adapted-args" // Warn if an argument list is modified to match the receiver
  , "-Ywarn-dead-code" // Warn when dead code is identified.
  , "-Ywarn-inaccessible" // Warn about inaccessible types in method signatures.
  , "-Ywarn-nullary-override" // Warn when non-nullary `def f()' overrides nullary `def f'
  , "-Ywarn-nullary-unit" // Warn when nullary methods return Unit.
  , "-Ywarn-numeric-widen" // Warn when numerics are widened.
)

resolvers ++= Seq(
  "Sonatype OSS Snapshot Repository" at "https://oss.sonatype.org/content/repositories/snapshots/",
  "Sonatype OSS Release Repository" at "https://oss.sonatype.org/content/repositories/releases/",
  "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"
)

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.1" % Test,
  "com.wix"         % "wix-embedded-mysql" % "2.2.4"
)

