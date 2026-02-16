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
ThisBuild / scalaVersion := "2.12.21"

lazy val commonSettings: Seq[Setting[_]] = Def.settings(
  Compile / javacOptions ++= Seq(
    "-encoding", "UTF-8", "-Xlint:all", "-Xlint:-processing", "-source", "17", "-target", "17"
  ),
  Compile / doc / javacOptions := Seq("-encoding", "UTF-8", "-source", "17"),
)

lazy val library = (project in file("src/library"))
  .settings(
    commonSettings,
    name := "jupiter-interface",
    autoScalaLibrary := false,
    crossPaths := false,
    libraryDependencies ++= Seq(
      junitPlatformLauncher,
      junitJupiterEngine,
      testInterface,
      junitJupiterParams % Test,
      junitVintageEngine % Test,
      junitPlatformSuite % Test,
      hamcrestLibrary % Test,
      mockitoCore % Test,
      junit4Interface % Test,
      junit4 % Test,
    ),
    testOptions += Tests.Argument(TestFrameworks.JUnit, "-a", "-v"),
    bspEnabled := false,
  )

lazy val plugin = (project in file("src/plugin"))
  .enablePlugins(SbtPlugin)
  .dependsOn(library)
  .settings(
    commonSettings,
    name := "sbt-jupiter-interface",
    Compile / scalacOptions ++= Seq("-Xlint"),
    Compile / scalacOptions ++= {
      scalaBinaryVersion.value match {
        case "2.12" =>
          Seq("-Xfatal-warnings")
        case _ =>
          Nil
      }
    },
    TaskKey[Unit]("scriptedTestSbt2") := Def.taskDyn {
      val values = sbtTestDirectory.value
        .listFiles(_.isDirectory)
        .flatMap { dir1 =>
          dir1.listFiles(_.isDirectory).map { dir2 =>
            dir1.getName -> dir2.getName
          }
        }
        .toList
      val log = streams.value.log
      // TODO enable all tests
      val exclude: Set[(String, String)] = Set(
        "basic" -> "dispatcher",
        "basic" -> "finds-test-from-extended-config",
        "basic" -> "finds-test-from-it-config",
        "basic" -> "tags",
      )
      val args = values.filterNot(exclude).map { case (x1, x2) => s"${x1}/${x2}" }
      val arg = args.mkString(" ", " ", "")
      log.info("scripted" + arg)
      scripted.toTask(arg)
    }.value,
    scriptedBufferLog := false,
    scriptedLaunchOpts ++= Seq(
      s"-Dproject.version=${version.value}",
      s"-Djunit.jupiter.version=${junitVersion}",
      s"-Djunit.platform.version=${junitVersion}"
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
        case "3" => "2.0.0-RC9"
      }
    },
    crossScalaVersions += "3.8.1"
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
    s"junit.platform.version=${junitVersion}\n" +
    s"junit.jupiter.version=${junitVersion}\n" +
    s"junit.vintage.version=${junitVersion}\n"
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
ThisBuild / githubWorkflowJavaVersions := List(JavaSpec.temurin("17"))
ThisBuild / githubWorkflowBuildMatrixExclusions ++= {
  val sv = (ThisBuild / scalaVersion).value
  List(
    MatrixExclude(Map("scala" -> sv, "java" -> "temurin@17", "os" -> "windows-latest")),
  )
}
ThisBuild / githubWorkflowBuild := Seq(WorkflowStep.Sbt(List("javafmtCheckAll", "+ test", "scripted", "++ 3.x", "scriptedTestSbt2")))
ThisBuild / githubWorkflowBuildPostamble += WorkflowStep.Run(
  commands = List("""rm -rf "$HOME/.ivy2/local""""),
  name = Some("Clean up Ivy Local repo")
)
ThisBuild / githubWorkflowTargetTags ++= Seq("v*")
ThisBuild / githubWorkflowPublishTargetBranches :=
  Seq(
    RefPredicate.StartsWith(Ref.Tag("v")),
    RefPredicate.Equals(Ref.Branch("main"))
  )
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
Global / onChangedBuildSource := ReloadOnSourceChanges
