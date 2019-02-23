# sbt-wix-embedded-mysql

[![Build Status](https://travis-ci.org/chatwork/sbt-wix-embedded-mysql.svg?branch=master)](https://travis-ci.org/chatwork/sbt-wix-embedded-mysql)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.chatwork/sbt-wix-embedded-mysql/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.chatwork/sbt-wix-embedded-mysql)
[![Scaladoc](http://javadoc-badge.appspot.com/com.chatwork/sbt-wix-embedded-mysql.svg?label=scaladoc)](http://javadoc-badge.appspot.com/com.chatwork/sbt-wix-embedded-mysql)
[![Reference Status](https://www.versioneye.com/java/com.chatwork:sbt-wix-embedded-mysql/reference_badge.svg?style=flat)](https://www.versioneye.com/java/com.chatwork:sbt-wix-embedded-mysql/references)

sbt-wix-embedded-mysql is sbt-plugin for [wix-embedded-mysql](https://github.com/wix/wix-embedded-mysql)

You can start and stop embedded MySQL on sbt task.

## Installation

Add this to your project/plugins.sbt file:

**`project/plugins.sbt`**

Supported sbt version is 1.x.

```scala
resolvers += "Sonatype OSS Release Repository" at "https://oss.sonatype.org/content/repositories/releases/"

addSbtPlugin("com.chatwork" % "sbt-wix-embedded-mysql" % "1.0.9")
```

## Usage

### Basic Configuration

If you want default settings, no configuration is necessary. Please refer to [here](src/main/scala/com/chatwork/sbt/wix/embedded/mysql/WixMySQLPlugin.scala) for sbt keys of the plugin.These sbt keys base on the API of [wix-embedded-mysql](https://github.com/wix/wix-embedded-mysql). If you understand how to use wix-embedded-mysql, it is easy to use this plugin.

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

**`build.sbt`**

```scala
testOptions in Test ++= Seq(
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
### How to use on Travis

**`.travis.yml`**

```yaml
# -- snip

before_install:
 - sudo apt-get update -qq && sudo apt-get install -y libaio1
 - sudo hostname "$(hostname | cut -c1-63)"
 
os: linux
dist: trusty
sudo: required

# -- snip
```

#### Seting up download cache on Travis

**`build.sbt`**

```scala
wixMySQLDownloadPath := Some(sys.env("HOME") + "/.wixMySQL/downloads"),
```

**`.travis.yml`**

```yaml
# -- snip

cache:
  directories:
    - $HOME/.wixMySQL
    
# -- snip
```
