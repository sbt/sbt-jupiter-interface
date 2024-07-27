/*
 * jupiter-interface
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

Global / onChangedBuildSource := ReloadOnSourceChanges

val Versions = new {
  val junitJupiter = "5.9.1"
  val junitPlatform = "1.9.1"
  val junitVintage = "5.9.1"
  val testInterface = "1.0"
}

ThisBuild / organization := "com.github.sbt.junit"
ThisBuild / scalaVersion := "2.12.19"

lazy val library = (project in file("src/library"))
  .settings(
    name := "jupiter-interface",
    autoScalaLibrary := false,
    crossPaths := false,
    (compile / javacOptions) ++= Seq("-source", "1.8", "-target", "1.8"),
    (doc / javacOptions) := Seq("-source", "1.8"),
    libraryDependencies ++= Seq(
      "org.junit.platform" % "junit-platform-launcher" % Versions.junitPlatform,
      "org.junit.jupiter" % "junit-jupiter-engine" % Versions.junitJupiter,
      "org.scala-sbt" % "test-interface" % Versions.testInterface
    ),
    libraryDependencies ++= Seq(
      "org.junit.jupiter" % "junit-jupiter-params" % Versions.junitJupiter % Test,
      "org.junit.vintage" % "junit-vintage-engine" % Versions.junitVintage % Test,
      "org.hamcrest" % "hamcrest-library" % "1.3" % Test,
      "org.mockito" % "mockito-core" % "2.23.4" % Test,
      "com.github.sbt" % "junit-interface" % "0.13.3" % Test,
      "junit" % "junit" % "4.13.2" % Test,
    ),
    testOptions += Tests.Argument(TestFrameworks.JUnit, "-a", "-v"),
    bspEnabled := false,
  )

lazy val plugin = (project in file("src/plugin"))
  .enablePlugins(SbtPlugin)
  .dependsOn(library)
  .settings(
    name := "sbt-jupiter-interface",
    Compile / scalacOptions ++= Seq("-Xlint", "-Xfatal-warnings"),
    scriptedBufferLog := false,
    scriptedLaunchOpts ++= Seq(
      s"-Dproject.version=${version.value}",
      s"-Djunit.jupiter.version=${Versions.junitJupiter}",
      s"-Djunit.platform.version=${Versions.junitPlatform}"
    ),
    scriptedDependencies := {
      val () = publishLocal.value
      val () = (library / publishLocal).value
    },
    Compile / resourceGenerators += generateVersionFile.taskValue,
    Test / publishArtifact := false,
    publishTo := sonatypePublishTo.value,
    (pluginCrossBuild / sbtVersion) := {
      scalaBinaryVersion.value match {
        case "2.12" => "1.5.8"
      }
    },
  )

lazy val root = (project in file("."))
  .aggregate(library, plugin)
  .settings(
    name := "jupiter-root",
    publish / skip := true,
    bspEnabled := false,
  )

def generateVersionFile = Def.task {
  val version = (library / Keys.version).value
  val file = (Compile / resourceManaged).value / "jupiter-interface.properties"
  val content = s"version=$version\n" +
    s"junit.platform.version=${Versions.junitPlatform}\n" +
    s"junit.jupiter.version=${Versions.junitJupiter}\n" +
    s"junit.vintage.version=${Versions.junitVintage}\n"
  IO.write(file, content)
  Seq(file)
}

ThisBuild / homepage := Some(url("https://github.com/maichler/sbt-jupiter-interface"))
ThisBuild / licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))
ThisBuild / developers := List(
  Developer(id = "maichler", name = "Michael Aichler", email = "maichler@gmail.com", url = url("https://github.com/maichler"))
)
ThisBuild / scmInfo := Some(
  ScmInfo(
      url("https://github.com/sbt/sbt-jupiter-interface"),
      "scm:git@github.com:sbt/sbt-jupiter-interface.git"
  )
)
ThisBuild / githubWorkflowOSes := Seq("ubuntu-latest", "macos-latest", "windows-latest")
ThisBuild / githubWorkflowBuild := Seq(WorkflowStep.Sbt(List("test", "scripted")))
ThisBuild / githubWorkflowTargetTags ++= Seq("v*")
ThisBuild / githubWorkflowPublishTargetBranches :=
  Seq(RefPredicate.StartsWith(Ref.Tag("v")))
ThisBuild / githubWorkflowPublish := Seq(
  WorkflowStep.Sbt(
    commands = List("ci-release"),
    name = Some("Publish project"),
  )
)
