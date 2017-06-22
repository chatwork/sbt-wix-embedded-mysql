# sbt-wix-embedded-mysql (WIP)

sbt-wix-embedded-mysql is sbt-plugin for [wix-embedded-mysql](https://github.com/wix/wix-embedded-mysql)

## Installation

Add this to your project/plugins.sbt file:

```scala
resolvers += "Sonatype OSS Release Repository" at "https://oss.sonatype.org/content/repositories/releases/"

addSbtPlugin("com.chatwork" % "sbt-wix-embedded-mysql" % "1.0.4")
```

## Usage

### Basic Configuration

If you want default settings, no configuration is necessary.Please refer to [here](src/main/scala/com/chatwork/sbt/wix/embedded/mysql/WixMySQLPlugin.scala) for sbt keys of the plugin.

### An example for configuration

**`build.sbt`**

```scala
wixMySQLVersion := com.wix.mysql.distribution.Version.v5_7_latest

wixMySQLSchemaName := "your schema name"

wixMySQLUserName := Some("my-db-user")

wixMySQLPassword := Some("my-db-passwd")
```

### Task of sbt

You can use sbt tasks that the followings.

```scala
// start mysqld
> wixMySQLStart

// stop mysqld
> wixMySQLStop
```


### How to use for testing

```
testOptions in Test ++= Seq(
  Tests.Setup { () =>
    wixMySQLStart.value
  },
  Tests.Cleanup { () =>
    wixMySQLStop.value
  }
)
```
