/*
 * sbt-jupiter-interface
 *
 * Copyright (c) 2017, Michael Aichler.
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.sbt.junit.jupiter.sbt

import java.net.URLClassLoader

import com.github.sbt.junit.jupiter.api.JupiterFramework
import com.github.sbt.junit.jupiter.api.JupiterTestCollector
import sbt.Keys.testFrameworks
import sbt.Keys.*
import sbt.plugins.JvmPlugin
import sbt.AutoPlugin
import sbt.Def
import sbt.*
import scala.collection.JavaConverters.*

object Import {

  val jupiterTestFramework:TestFramework = new TestFramework("com.github.sbt.junit.jupiter.api.JupiterFramework")

  object JupiterKeys {

    val jupiterVersion: SettingKey[String] = SettingKey[String]("jupiter-version",
      "The jupiter-interface version that should be added to the library dependencies.")
    val junitPlatformVersion: SettingKey[String] = SettingKey[String]("junit-platform-version",
      "The JUnit Platform version which is used by this plugin.")
    val junitJupiterVersion: SettingKey[String] = SettingKey[String]("junit-jupiter-version",
      "The JUnit Jupiter version which is used by this plugin.")
    val junitVintageVersion: SettingKey[String] = SettingKey[String]("junit-vintage-version",
      "The JUnit Vintage version which is compatible with this plugin.")

    val jupiterTestEngineAutoRegistrationEnabled: SettingKey[Boolean] =
      settingKey("Enable automatic registration of test engines (default true)")
    val jupiterLauncherSessionListenerAutoRegistrationEnabled: SettingKey[Boolean] =
      settingKey("Enable automatic registration of launcher session listeners (default true)")
    val jupiterLauncherDiscoveryListenerAutoRegistrationEnabled: SettingKey[Boolean] =
      settingKey("Enable automatic registration of launcher discovery listeners (default true)")
    val jupiterTestExecutionListenerAutoRegistrationEnabled: SettingKey[Boolean] =
      settingKey("Enable automatic registration of test execution listeners (default true)")
    val jupiterPostDiscoveryFilterAutoRegistrationEnabled: SettingKey[Boolean] =
      settingKey("Enable automatic registration of post discovery filters (default true)")

    val jupiterTestEngines: SettingKey[Seq[String]] =
      settingKey("Fully-qualified class names of TestEngine implementations to manually register (default empty)")
    val jupiterLauncherSessionListeners: SettingKey[Seq[String]] =
      settingKey("Fully-qualified class names of LauncherSessionListener implementations to manually register (default empty)")
    val jupiterLauncherDiscoveryListeners: SettingKey[Seq[String]] =
      settingKey("Fully-qualified class names of LauncherDiscoveryListener implementations to manually register (default empty)")
    val jupiterTestExecutionListeners: SettingKey[Seq[String]] =
      settingKey("Fully-qualified class names of TestExecutionListener implementations to manually register (default empty)")
    val jupiterPostDiscoveryFilters: SettingKey[Seq[String]] =
      settingKey("Fully-qualified class names of PostDiscoveryFilter implementations to manually register (default empty)")
  }
}

/**
  * SBT plugin for the JUnit Jupiter test framework.
  *
  * @author Michael Aichler
  */
object JupiterPlugin extends AutoPlugin {

  val autoImport: Import.type = Import

  import autoImport._
  import JupiterKeys._

  override def requires = JvmPlugin
  override def trigger: PluginTrigger = allRequirements

  override def globalSettings: Seq[Def.Setting[?]] = Seq(

    jupiterVersion := readResourceProperty("jupiter-interface.properties", "version"),
    junitPlatformVersion := readResourceProperty("jupiter-interface.properties", "junit.platform.version"),
    junitJupiterVersion := readResourceProperty("jupiter-interface.properties", "junit.jupiter.version"),
    junitVintageVersion := readResourceProperty("jupiter-interface.properties", "junit.vintage.version"),
    jupiterTestEngineAutoRegistrationEnabled := true,
    jupiterLauncherSessionListenerAutoRegistrationEnabled := true,
    jupiterLauncherDiscoveryListenerAutoRegistrationEnabled := true,
    jupiterTestExecutionListenerAutoRegistrationEnabled := true,
    jupiterPostDiscoveryFilterAutoRegistrationEnabled := true,
    jupiterTestEngines := Seq.empty[String],
    jupiterLauncherSessionListeners := Seq.empty[String],
    jupiterLauncherDiscoveryListeners := Seq.empty[String],
    jupiterTestExecutionListeners := Seq.empty[String],
    jupiterPostDiscoveryFilters := Seq.empty[String]
  )

  override def projectSettings: Seq[Def.Setting[?]] = inConfig(Test)(scopedSettings) ++ unscopedSettings

  /**
   * Configuration scope specific plugin settings.
   *
   * Intercepts sbt.Keys.definedTests.
   *
   * By default this is applied to the Test configuration only.
   */
  def scopedSettings: Seq[Def.Setting[?]] = Seq(
    definedTests ++= collectTests.value,
    testOptions += Tests.Argument(jupiterTestFramework,
      s"--test-engine-auto-registration=${jupiterTestEngineAutoRegistrationEnabled.value}",
      s"--launcher-session-listener-auto-registration=${jupiterLauncherSessionListenerAutoRegistrationEnabled.value}",
      s"--launcher-discovery-listener-auto-registration=${jupiterLauncherDiscoveryListenerAutoRegistrationEnabled.value}",
      s"--test-execution-listener-auto-registration=${jupiterTestExecutionListenerAutoRegistrationEnabled.value}",
      s"--post-discovery-filter-auto-registration=${jupiterPostDiscoveryFilterAutoRegistrationEnabled.value}",
      s"--test-engines=${jupiterTestEngines.value.mkString(",")}",
      s"--launcher-session-listeners=${jupiterLauncherSessionListeners.value.mkString(",")}",
      s"--launcher-discovery-listeners=${jupiterLauncherDiscoveryListeners.value.mkString(",")}",
      s"--test-execution-listeners=${jupiterTestExecutionListeners.value.mkString(",")}",
      s"--post-discovery-filters=${jupiterPostDiscoveryFilters.value.mkString(",")}")
  )

  /*
   * Adds this plugins test framework to the list of testFrameworks.
   */
  private def unscopedSettings: Seq[Def.Setting[?]] = Seq(
    testFrameworks += jupiterTestFramework
  )

  /*
   * Collects available tests through JUnit Jupiter's discovery mechanism.
   */
  private def collectTests = Def.task[Seq[TestDefinition]] {
    val classes = classDirectory.value
    val classpath = JupiterPluginCompat.dependencyClasspathUrlArray.value :+ classes.toURI.toURL

    val collector = new JupiterTestCollector.Builder()
      .withClassDirectory(classes)
      .withClassLoader(getClass.getClassLoader)
      .withRuntimeClassPath(classpath)
      .withTestEngineAutoRegistrationEnabled(jupiterTestEngineAutoRegistrationEnabled.value)
      .withLauncherSessionListenerAutoRegistrationEnabled(jupiterLauncherSessionListenerAutoRegistrationEnabled.value)
      .withLauncherDiscoveryListenerAutoRegistrationEnabled(jupiterLauncherDiscoveryListenerAutoRegistrationEnabled.value)
      .withTestExecutionListenerAutoRegistrationEnabled(jupiterTestExecutionListenerAutoRegistrationEnabled.value)
      .withPostDiscoveryFilterAutoRegistrationEnabled(jupiterPostDiscoveryFilterAutoRegistrationEnabled.value)
      .withTestEngines(jupiterTestEngines.value.asJava)
      .withLauncherSessionListeners(jupiterLauncherSessionListeners.value.asJava)
      .withLauncherDiscoveryListeners(jupiterLauncherDiscoveryListeners.value.asJava)
      .withTestExecutionListeners(jupiterTestExecutionListeners.value.asJava)
      .withPostDiscoveryFilters(jupiterPostDiscoveryFilters.value.asJava)
      .build()

    val discoveredTests = collector.collectTests().getDiscoveredTests.asScala.toList.map(toTestDefinition)
    if (discoveredTests.nonEmpty) {
      if (!hasRuntimeLibrary(classpath)) {
        throw new RuntimeException(
          "Found at least one JUnit 5 test silently ignored by SBT due to `jupiter-interface` " +
            "not being on this projects test-classpath."
        )
      }
    }

    discoveredTests
  }.dependsOn(compile)

  /*
   * Checks whether this plugins runtime library is on the given classpath.
   */
  private def hasRuntimeLibrary(classpath:Array[URL]):Boolean = {

    val runtimeClassLoader = new URLClassLoader(classpath)

    try {
      runtimeClassLoader.loadClass(classOf[JupiterFramework].getName)
      true
    }
    catch {
      case _:ClassNotFoundException =>
        false
    }
    finally {
        runtimeClassLoader.close()
    }
  }

  private def toTestDefinition(item:JupiterTestCollector.Item) = {
    new TestDefinition(
      item.getFullyQualifiedClassName,
      item.getFingerprint,
      item.isExplicit,
      item.getSelectors)
  }

  private def readResourceProperty(resource: String, property: String): String = {
    val props = new java.util.Properties
    val stream = getClass.getClassLoader.getResourceAsStream(resource)
    try { props.load(stream) }
    catch { case e: Exception => println(e.getMessage)}
    finally { if (stream ne null) stream.close() }
    val result = props.getProperty(property)
    result
  }
}
