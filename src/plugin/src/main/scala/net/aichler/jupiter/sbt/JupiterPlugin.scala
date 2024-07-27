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
package net.aichler.jupiter.sbt

import java.net.URLClassLoader

import net.aichler.jupiter.api.JupiterFramework
import net.aichler.jupiter.api.JupiterTestCollector
import sbt.Keys.testFrameworks
import sbt.Keys.*
import sbt.plugins.JvmPlugin
import sbt.AutoPlugin
import sbt.Def
import sbt.*
import scala.collection.JavaConverters.*

object Import {

  val jupiterTestFramework:TestFramework = new TestFramework("net.aichler.jupiter.api.JupiterFramework")

  object JupiterKeys {

    val jupiterVersion: SettingKey[String] = SettingKey[String]("jupiter-version",
      "The jupiter-interface version that should be added to the library dependencies.")
    val junitPlatformVersion: SettingKey[String] = SettingKey[String]("junit-platform-version",
      "The JUnit Platform version which is used by this plugin.")
    val junitJupiterVersion: SettingKey[String] = SettingKey[String]("junit-jupiter-version",
      "The JUnit Jupiter version which is used by this plugin.")
    val junitVintageVersion: SettingKey[String] = SettingKey[String]("junit-vintage-version",
      "The JUnit Vintage version which is compatible with this plugin.")
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

  override def globalSettings: Seq[Def.Setting[_]] = Seq(

    jupiterVersion := readResourceProperty("jupiter-interface.properties", "version"),
    junitPlatformVersion := readResourceProperty("jupiter-interface.properties", "junit.platform.version"),
    junitJupiterVersion := readResourceProperty("jupiter-interface.properties", "junit.jupiter.version"),
    junitVintageVersion := readResourceProperty("jupiter-interface.properties", "junit.vintage.version")
  )

  override def projectSettings: Seq[Def.Setting[_]] = inConfig(Test)(scopedSettings) ++ unscopedSettings

  /**
   * Configuration scope specific plugin settings.
   *
   * Intercepts sbt.Keys.definedTests.
   *
   * By default this is applied to the Test configuration only.
   */
  def scopedSettings: Seq[Def.Setting[_]] = Seq(

    definedTests := collectTests.value
  )

  /*
   * Adds this plugins test framework to the list of testFrameworks.
   */
  private def unscopedSettings = Seq(

    testFrameworks += jupiterTestFramework
  )

  /*
   * Collects available tests through JUnit Jupiter's discovery mechanism and
   * combines them with the result of sbt.Keys.definedTests.
   */
  private def collectTests = Def.task {

    val classes = classDirectory.value
    val classpath = dependencyClasspath.value.map(_.data.toURI.toURL).toArray :+ classes.toURI.toURL
    val result = Defaults.detectTests.value

    val collector = new JupiterTestCollector.Builder()
      .withClassDirectory(classes)
      .withClassLoader(getClass.getClassLoader)
      .withRuntimeClassPath(classpath)
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

    result ++ discoveredTests
  }

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
