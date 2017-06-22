# sbt-wix-embedded-mysql (WIP)

sbt-wix-embedded-mysql is sbt-plugin for [wix-embedded-mysql](https://github.com/wix/wix-embedded-mysql)

## Installation

Add this to your project/plugins.sbt file:

```scala
resolvers += "Sonatype OSS Release Repository" at "https://oss.sonatype.org/content/repositories/releases/"

addSbtPlugin("com.chatwork" % "sbt-wix-embedded-mysql" % "1.0.0")
```

## Usage

### Basic Configuration

```scala
wixMySQLVersion := com.wix.mysql.distribution.Version.v5_7_latest

wixMySQLSchemaName := "your schema name"

wixMySQLUserName := Some("my-db-user")

wixMySQLPassword := Some("my-db-passwd")
```

### Task

```scala
// start mysqld
wixMySQLStart

// stop mysqld
wixMySQLStop
```
