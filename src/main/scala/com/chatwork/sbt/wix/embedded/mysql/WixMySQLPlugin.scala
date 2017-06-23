package com.chatwork.sbt.wix.embedded.mysql

import java.util.TimeZone
import java.util.concurrent.TimeUnit

import com.wix.mysql.SqlScriptSource
import com.wix.mysql.config.{ Charset, DownloadConfig, MysqldConfig, SchemaConfig }
import sbt.Keys._
import sbt.{ AutoPlugin, _ }
import com.wix.mysql.config.DownloadConfig.aDownloadConfig
import com.wix.mysql.distribution.{ Version => WixMySQLVersion }
import com.wix.mysql.EmbeddedMysql
import sbt.plugins.JvmPlugin

import scala.collection.JavaConverters._
import scala.concurrent.duration.Duration
import scala.concurrent.duration._
import sbt._
import sbt.Keys._

import scala.collection.mutable.ArrayBuffer

object WixMySQLPlugin extends AutoPlugin {
  private var mysqld: Option[EmbeddedMysql] = None

  override def trigger = allRequirements

  override def requires: Plugins = JvmPlugin

  import autoImport._

  override def projectSettings = Seq(
    wixMySQLVersion := WixMySQLVersion.v5_7_latest,
    wixMySQLDownloadPath := Some(baseDirectory.value + "/target"),
    wixMySQLDownloadConfig := Some(
      wixMySQLDownloadPath.value.fold(aDownloadConfig())(aDownloadConfig().withCacheDir).build()
    ),
    wixMySQLTempPath := {
      wixMySQLDownloadPath.value.map { path =>
        path + s"/MySQL-${wixMySQLVersion.value.getMajorVersion}/tmp"
      }
    },
    wixMySQLPort := None,
    wixMySQLCharset := None,
    wixMySQLTimeout := Some(30 seconds),
    wixMySQLTimeZone := None,
    wixMySQLUserName := None,
    wixMySQLPassword := None,
    wixMySQLMysqldConfig := {
      val builder = MysqldConfig.aMysqldConfig(wixMySQLVersion.value)
      val builderWithUserConfig = (wixMySQLUserName.value, wixMySQLPassword.value) match {
        case (Some(u), Some(p)) =>
          builder.withUser(u, p)
        case _ => builder
      }
      val builderWithCharset = wixMySQLCharset.value.fold(builderWithUserConfig)(builderWithUserConfig.withCharset)
      val builderWithPort    = wixMySQLPort.value.fold(builderWithCharset)(builderWithCharset.withPort)
      val builderWithTempDir = wixMySQLTempPath.value.fold(builderWithPort)(builderWithPort.withTempDir)
      val builderWithTimeout =
        wixMySQLTimeout.value.fold(builderWithTempDir)(v => builderWithTempDir.withTimeout(v.length, v.unit))
      val builderWithTimeZone = wixMySQLTimeZone.value.fold(builderWithTimeout)(builderWithTimeout.withTimeZone)
      Some(builderWithTimeZone.build())
    },
    wixMySQLSchemaName := "public",
    wixMySQLSchemaCharset := None,
    wixMySQLSchemaCommands := Seq.empty,
    wixMySQLSchamaScripts := Seq.empty,
    wixMySQLSchemaConfig := {
      val builder            = SchemaConfig.aSchemaConfig(wixMySQLSchemaName.value)
      val builderWithCharset = wixMySQLSchemaCharset.value.fold(builder)(builder.withCharset)
      val commands           = wixMySQLSchemaCommands.value
      val builderWithCommands =
        if (commands.isEmpty) builderWithCharset else builderWithCharset.withCommands(commands.asJava)
      val scripts = wixMySQLSchamaScripts.value
      val builderWithScripts =
        if (scripts.isEmpty) builderWithCommands else builderWithCommands.withScripts(scripts.asJava)
      Some(builderWithScripts.build())
    },
    wixMySQLStart := Def.task {
      val logger = streams.value.log
      val ab     = ArrayBuffer.empty[String]
      ab.append("wixMySQL")
      def log(config: MysqldConfig) = {
        ab.append(s" Version := ${config.getVersion}")
        ab.append(s" TempDir := ${config.getTempDir}")
        ab.append(s" Port := ${config.getPort}")
        ab.append(s" Charset := ${config.getCharset}")
        ab.append(s" Timeout(sec) := ${config.getTimeout(TimeUnit.SECONDS)}")
        ab.append(s" Timezone := ${config.getTimeZone}")
        ab.append(s" Username := ${config.getUsername}")
        // ab.append(s"wixMySQL: Password := ${config.getPassword}")
        ab.foreach(s => logger.info(s))
      }
      require(mysqld.isEmpty)
      val instance =
        (wixMySQLMysqldConfig.value, wixMySQLDownloadConfig.value, wixMySQLSchemaConfig.value) match {
          case (Some(m), dOpt, sOpt) =>
            val builder1 = dOpt.fold(EmbeddedMysql.anEmbeddedMysql(m)) { d =>
              ab.append(s" Download BaseUrl := ${d.getBaseUrl}")
              ab.append(s" Download CacheDir := ${d.getCacheDir}")
              EmbeddedMysql.anEmbeddedMysql(m, d)
            }
            val builder2 = sOpt.fold(builder1) { s =>
              ab.append(s" Schema Name := ${s.getName}")
              ab.append(s" Schema Charset := ${s.getCharset}")
              ab.append(s" Schema Scripts := ${s.getScripts}")
              builder1.addSchema(s)
            }
            val instance = builder2.start
            log(instance.getConfig)
            instance
          case _ =>
            val instance = EmbeddedMysql.anEmbeddedMysql(wixMySQLVersion.value).start
            log(instance.getConfig)
            instance
        }
      mysqld = Some(instance)
    }.value,
    wixMySQLStop := Def.task {
      val logger = streams.value.log
      mysqld.foreach { e =>
        try {
          e.stop()
        } catch {
          case ex: Throwable =>
            logger.error(ex.getMessage)
        }
        mysqld = None
      }
    }.value
  )

  object autoImport {
    val wixMySQLVersion        = settingKey[WixMySQLVersion]("wix-mysql-version")
    val wixMySQLDownloadConfig = settingKey[Option[DownloadConfig]]("wix-mysql-download-config")
    val wixMySQLDownloadPath   = settingKey[Option[String]]("wix-mysql-download-path")
    val wixMySQLMysqldConfig   = settingKey[Option[MysqldConfig]]("wix-mysql-mysqld-config")
    val wixMySQLTempPath       = settingKey[Option[String]]("wix-mysql-temp-path")
    val wixMySQLPort           = settingKey[Option[Int]]("wix-mysql-port")
    val wixMySQLCharset        = settingKey[Option[Charset]]("wix-mysql-charset")
    val wixMySQLTimeout        = settingKey[Option[Duration]]("wix-mysql-timeout")
    val wixMySQLTimeZone       = settingKey[Option[TimeZone]]("wix-mysql-timezone")
    val wixMySQLUserName       = settingKey[Option[String]]("wix-mysql-user-name")
    val wixMySQLPassword       = settingKey[Option[String]]("wix-mysql-password")
    val wixMySQLSchemaConfig   = settingKey[Option[SchemaConfig]]("wix-mysql-schema-config")
    val wixMySQLSchemaName     = settingKey[String]("wix-mysql-schema-name")
    val wixMySQLSchemaCharset  = settingKey[Option[Charset]]("wix-mysql-schema-charset")
    val wixMySQLSchemaCommands = settingKey[Seq[String]]("wix-msyql-schema-commands")
    val wixMySQLSchamaScripts  = settingKey[Seq[SqlScriptSource]]("wix-mysql-schema-scripts")
    val wixMySQLStart          = taskKey[Unit]("wix-mysql-start")
    val wixMySQLStop           = taskKey[Unit]("wix-mysql-stop")
  }

}
