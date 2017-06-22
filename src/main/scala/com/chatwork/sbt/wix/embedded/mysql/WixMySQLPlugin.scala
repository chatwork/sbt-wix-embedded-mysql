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
      require(mysqld.isEmpty)
      val _mysqld = (wixMySQLMysqldConfig.value, wixMySQLDownloadConfig.value, wixMySQLSchemaConfig.value) match {
        case (Some(m), dOpt, sOpt) =>
          val builder1 = dOpt.fold(EmbeddedMysql.anEmbeddedMysql(m)) { d =>
            logger.info(s"baseUrl = ${d.getBaseUrl}")
            logger.info(s"cacheDir = ${d.getCacheDir}")
            EmbeddedMysql.anEmbeddedMysql(m, d)
          }
          val builder2 = sOpt.fold(builder1) { s =>
            logger.info(s"Schema Name = ${s.getName}")
            logger.info(s"Charset = ${s.getCharset}")
            logger.info(s"Scripts = ${s.getScripts}")
            builder1.addSchema(s)
          }
          builder2.start()
        case _ =>
          EmbeddedMysql.anEmbeddedMysql(wixMySQLVersion.value).start()
      }
      val config = _mysqld.getConfig
      logger.info(s"MySQL Version := ${config.getVersion}")
      logger.info(s"MySQL TempDir := ${config.getTempDir}")
      logger.info(s"MySQL Port := ${config.getPort}")
      logger.info(s"MySQL Charset := ${config.getCharset}")
      logger.info(s"MySQL Timeout(sec) := ${config.getTimeout(TimeUnit.SECONDS)}")
      logger.info(s"MySQL Timezone := ${config.getTimeZone}")
      logger.info(s"MySQL Username := ${config.getUsername}")
      logger.info(s"MySQL Password := ${config.getPassword}")
      mysqld = Some(_mysqld)
    }.value,
    wixMySQLStop := Def.task {
      mysqld.foreach { e =>
        e.stop()
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
