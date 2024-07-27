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

import Dependencies.*

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
      junitPlatformLauncher,
      junitJupiterEngine,
      testInterface,
      junitJupiterParams % Test,
      junitVintageEngine % Test,
      hamcrestLibrary % Test,
      mockitoCore % Test,
      junit4Interface % Test,
      junit4 % Test,
    ),
    testOptions += Tests.Argument(TestFrameworks.JUnit, "-a", "-v"),
    bspEnabled := false,
    sonatypeProfileName := (ThisBuild / sonatypeProfileName).value,
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
      s"-Djunit.jupiter.version=${junitJupiterVer}",
      s"-Djunit.platform.version=${junitPlatformVer}"
    ),
    scriptedDependencies := {
      val () = publishLocal.value
      val () = (library / publishLocal).value
    },
    Compile / resourceGenerators += generateVersionFile.taskValue,
    Test / publishArtifact := false,
    (pluginCrossBuild / sbtVersion) := {
      scalaBinaryVersion.value match {
        case "2.12" => "1.5.8"
      }
    },
    sonatypeProfileName := (ThisBuild / sonatypeProfileName).value,
  )

lazy val root = (project in file("."))
  .aggregate(library, plugin)
  .settings(
    name := "jupiter-root",
    publish / skip := true,
    bspEnabled := false,
    sonatypeProfileName := (ThisBuild / sonatypeProfileName).value,
  )

def generateVersionFile = Def.task {
  val version = (library / Keys.version).value
  val file = (Compile / resourceManaged).value / "jupiter-interface.properties"
  val content = s"version=$version\n" +
    s"junit.platform.version=${junitPlatformVer}\n" +
    s"junit.jupiter.version=${junitJupiterVer}\n" +
    s"junit.vintage.version=${junitVintageVer}\n"
  IO.write(file, content)
  Seq(file)
}

ThisBuild / homepage := Some(url("https://github.com/sbt/sbt-jupiter-interface"))
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
    env = Map(
      "PGP_PASSPHRASE" -> "${{ secrets.PGP_PASSPHRASE }}",
      "PGP_SECRET" -> "${{ secrets.PGP_SECRET }}",
      "SONATYPE_PASSWORD" -> "${{ secrets.SONATYPE_PASSWORD }}",
      "SONATYPE_USERNAME" -> "${{ secrets.SONATYPE_USERNAME }}",
    )
  )
)
ThisBuild / sonatypeProfileName := "com.github.sbt"
Global / onChangedBuildSource := ReloadOnSourceChanges
