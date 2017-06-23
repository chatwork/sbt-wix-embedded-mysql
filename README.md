# sbt-wix-embedded-mysql

sbt-wix-embedded-mysql is sbt-plugin for [wix-embedded-mysql](https://github.com/wix/wix-embedded-mysql)

## Installation

Add this to your project/plugins.sbt file:

**`project/plugins.sbt`**

```scala
resolvers += "Sonatype OSS Release Repository" at "https://oss.sonatype.org/content/repositories/releases/"

addSbtPlugin("com.chatwork" % "sbt-wix-embedded-mysql" % "1.0.7")
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
