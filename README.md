# sbt-wix-embedded-mysql

[![CI](https://github.com/chatwork/sbt-wix-embedded-mysql/workflows/CI/badge.svg)](https://github.com/chatwork/sbt-wix-embedded-mysql/actions?query=workflow%3ACI)
[![Scala Steward badge](https://img.shields.io/badge/Scala_Steward-helping-blue.svg?style=flat&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAQCAMAAAARSr4IAAAAVFBMVEUAAACHjojlOy5NWlrKzcYRKjGFjIbp293YycuLa3pYY2LSqql4f3pCUFTgSjNodYRmcXUsPD/NTTbjRS+2jomhgnzNc223cGvZS0HaSD0XLjbaSjElhIr+AAAAAXRSTlMAQObYZgAAAHlJREFUCNdNyosOwyAIhWHAQS1Vt7a77/3fcxxdmv0xwmckutAR1nkm4ggbyEcg/wWmlGLDAA3oL50xi6fk5ffZ3E2E3QfZDCcCN2YtbEWZt+Drc6u6rlqv7Uk0LdKqqr5rk2UCRXOk0vmQKGfc94nOJyQjouF9H/wCc9gECEYfONoAAAAASUVORK5CYII=)](https://scala-steward.org)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.chatwork/sbt-wix-embedded-mysql/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.chatwork/sbt-wix-embedded-mysql)
[![Scaladoc](http://javadoc-badge.appspot.com/com.chatwork.sbt.wix.embedded.mysql/sbt-wix-embedded-mysql_2.12.svg?label=scaladoc)](http://javadoc-badge.appspot.com/com.chatwork.sbt.wix.embedded.mysql/sbt-wix-embedded-mysql_2.12)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)

sbt-wix-embedded-mysql is sbt-plugin for [wix-embedded-mysql](https://github.com/wix/wix-embedded-mysql)

You can start and stop embedded MySQL on sbt task.

## Installation

Add this to your project/plugins.sbt file:

**`project/plugins.sbt`**

Supported sbt version is 1.x.

```scala
resolvers += "Sonatype OSS Release Repository" at "https://oss.sonatype.org/content/repositories/releases/"

addSbtPlugin("com.chatwork" % "sbt-wix-embedded-mysql" % "1.0.11")
```

## Usage

### Basic Configuration

If you want default settings, no configuration is necessary. Please refer to [here](src/main/scala/com/chatwork/sbt/wix/embedded/mysql/WixMySQLPlugin.scala) for sbt keys of the plugin.These sbt keys base on the API of [wix-embedded-mysql](https://github.com/wix/wix-embedded-mysql). If you understand how to use wix-embedded-mysql, it is easy to use this plugin.

### An example for configuration

**`build.sbt`**

```scala
wixMySQLVersion := com.wix.mysql.distribution.Version.v8_latest

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

**`build.sbt`**

```scala
Test / testOptions ++= Seq(
  Tests.Setup { () =>
    wixMySQLStart.value
    // If you want to use the flywayMigrate together, please join the two tasks using `Def.sequential` as follows.
    // Def.sequential(wixMySQLStart, flywayMigrate).value
  },
  Tests.Cleanup { () =>
    wixMySQLStop.value
  }
)
```
### How to use on Github Actions

**`.github/workflows/ci.yml`**

```yaml
# -- snip
steps:
  - uses: actions/checkout@v2.3.4
    with:
      fetch-depth: 0
  - uses: olafurpg/setup-scala@v12
    with:
      java-version: "adopt@1.8"
  - uses: coursier/cache-action@v6
  - run: |
    sudo echo 'deb http://security.ubuntu.com/ubuntu xenial-security main' | sudo tee -a /etc/apt/sources.list
    sudo apt-get update -qq
    sudo apt-get install -y libaio1 libevent-dev libssl-dev libssl1.0.0
  - run: sbt -v test
# -- snip
```

#### Seting up download cache on Github Actions 

**`build.sbt`**

```scala
wixMySQLDownloadPath := Some(sys.env("HOME") + "/.wixMySQL/downloads"),
```

**`.github/workflows/ci.yml`**

```yaml
# -- snip
steps:
  - uses: actions/cache@v2
    with:
      path: |
        ~/.wixMySQL
# -- snip
```
