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

val Versions = new {
  val junitJupiter = "5.0.0"
  val junitPlatform = "1.0.0"
  val junitVintage = "4.12.0"
  val testInterface = "1.0"
  val scalaVersion = "2.10.6"
}

lazy val library = (project in file("src/library"))
  .settings(
    name := "jupiter-interface",
    autoScalaLibrary := false,
    crossPaths := false,
    (javacOptions in compile) ++= Seq("-source", "1.8", "-target", "1.8"),
    (javacOptions in doc) := Seq("-source", "1.8"),
    libraryDependencies ++= Seq(
      "org.junit.platform" % "junit-platform-runner" % Versions.junitPlatform,
      "org.junit.jupiter" % "junit-jupiter-engine" % Versions.junitJupiter,
      "org.scala-sbt" % "test-interface" % Versions.testInterface
    ),
    libraryDependencies ++= Seq(
      "org.junit.jupiter" % "junit-jupiter-params" % Versions.junitJupiter % Test,
      "org.hamcrest" % "hamcrest-library" % "1.3" % Test,
      "org.mockito" % "mockito-core" % "2.7.22" % Test,
      "com.novocode" % "junit-interface" % "0.11" % Test,
      "junit" % "junit" % "4.12" % Test
    ),
    testOptions += Tests.Argument(TestFrameworks.JUnit, "-a", "-v"),
    publishMavenStyle := true,
    bintrayRepository := "maven"
  )

lazy val plugin = (project in file("src/plugin"))
  .dependsOn(library)
  .settings(
    name := "sbt-jupiter-interface",
    sbtPlugin := true,
    scriptedSettings,
    scriptedBufferLog := false,
    scriptedLaunchOpts ++= Seq(
      s"-Dproject.version=${version.value}",
      s"-Djunit.jupiter.version=${Versions.junitJupiter}",
      s"-Djunit.platform.version=${Versions.junitPlatform}"
    ),
    scriptedDependencies := {
      val () = publishLocal.value
      val () = (publishLocal in library).value
    },
    resourceGenerators in Compile += generateVersionFile.taskValue,
    publishMavenStyle := false,
    publishArtifact in Test := false,
    (javacOptions in compile) ++= Seq("-source", "1.6", "-target", "1.6"),
    (javacOptions in doc) := Seq("-source", "1.6"),
    bintrayRepository := "sbt-plugins"
  )

lazy val root = (project in file("."))
  .aggregate(library)
  .aggregate(plugin)
  .settings(
    name := "jupiter-root",
    publish := {},
    publishLocal := {},
    publishTo := Some(Resolver.file("no-publish", crossTarget.value / "no-publish")),
    bintrayRelease := {}
  )
  .settings(
    inThisBuild(Seq(
      licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0")),
      organization := "net.aichler",
      scalaVersion := Versions.scalaVersion,
      bintrayOrganization := None,
      bintrayVcsUrl := Some("git@github.com:maichler/sbt-jupiter-interface.git"),
      bintrayPackageLabels := Seq("sbt", "junit", "jupiter"),
      bintrayReleaseOnPublish := false
    ))
  )
  .settings(
    releaseTagName := (version in ThisBuild).value,
    releaseCrossBuild := false,
    releaseProcess := {
      import ReleaseTransformations._
      Seq[ReleaseStep](
        checkSnapshotDependencies,
        inquireVersions,
        runTest,
        releaseStepTask(releaseExtraTests in thisProjectRef.value),
        setReleaseVersion,
        commitReleaseVersion,
        tagRelease,
        publishArtifacts,
        releaseStepTask(bintrayRelease in thisProjectRef.value),
        setNextVersion,
        commitNextVersion//,
//        pushChanges
      )
    }
  )

def generateVersionFile = Def.task {
  val version = (Keys.version in library).value
  val file = (resourceManaged in Compile).value / "jupiter-interface.properties"
  val content = s"version=$version\n" +
    s"junit.platform.version=${Versions.junitPlatform}\n" +
    s"junit.jupiter.version=${Versions.junitJupiter}\n" +
    s"junit.vintage.version=${Versions.junitVintage}\n"
  IO.write(file, content)
  Seq(file)
}

lazy val releaseExtraTests = taskKey[Unit]("Run extra tests during the release")

releaseExtraTests := {
  (scripted in plugin).toTask("").value
}