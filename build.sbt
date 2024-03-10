import sbtrelease.ReleasePlugin.autoImport.releaseStepCommandAndRemaining
import xerial.sbt.Sonatype.GitHubHosting

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
  val junitJupiter = "5.9.1"
  val junitPlatform = "1.9.1"
  val junitVintage = "5.9.1"
  val testInterface = "1.0"
}

crossSbtVersions := Vector("0.13.16", "1.0.0")

lazy val library = (project in file("src/library"))
  .settings(
    name := "jupiter-interface",
    autoScalaLibrary := false,
    crossPaths := false,
    (javacOptions in compile) ++= Seq("-source", "1.8", "-target", "1.8"),
    (javacOptions in doc) := Seq("-source", "1.8"),
    libraryDependencies ++= Seq(
      "org.junit.platform" % "junit-platform-launcher" % Versions.junitPlatform,
      "org.junit.jupiter" % "junit-jupiter-engine" % Versions.junitJupiter,
      "org.scala-sbt" % "test-interface" % Versions.testInterface
    ),
    libraryDependencies ++= Seq(
      "org.junit.jupiter" % "junit-jupiter-params" % Versions.junitJupiter % Test,
      "org.junit.vintage" % "junit-vintage-engine" % Versions.junitVintage % Test,
      "org.hamcrest" % "hamcrest-library" % "1.3" % Test,
      "org.mockito" % "mockito-core" % "2.28.2" % Test,
      "com.novocode" % "junit-interface" % "0.11" % Test,
      "junit" % "junit" % "4.12" % Test
    ),
    testOptions += Tests.Argument(TestFrameworks.JUnit, "-a", "-v"),
    publishTo := sonatypePublishTo.value
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
    publishArtifact in Test := false,
    (javacOptions in compile) ++= Seq("-source", "1.6", "-target", "1.6"),
    (javacOptions in doc) := Seq("-source", "1.6"),
    publishTo := sonatypePublishTo.value
  )

lazy val root = (project in file("."))
  .aggregate(library)
  .aggregate(plugin)
  .settings(
    name := "jupiter-root",
    publishTo := sonatypePublishTo.value
  )
  .settings(
    inThisBuild(Seq(
      homepage := Some(url("https://github.com/maichler/sbt-jupiter-interface")),
      licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0")),
      organization := "net.aichler",
      scalaVersion := {
        val sbtCrossVersion = (sbtVersion in pluginCrossBuild).value
        sbtCrossVersion match {
          case v if v.startsWith("0.13.") => "2.10.6"
          case v if v.startsWith("1.") => "2.12.3"
          case _ => sys.error(s"Unhandled sbt version $sbtCrossVersion")
        }
      },
      usePgpKeyHex("4944210D30F5FBFC1D57957908C33DB0CBC4CC5A"),
      publishMavenStyle := true,
      sonatypeProfileName := "net.aichler",
      sonatypeProjectHosting := Some(GitHubHosting("maichler", "sbt-jupiter-interface", "maichler@gmail.com")),
      developers := List(
        Developer(id = "maichler", name = "Michael Aichler", email = "maichler@gmail.com", url = url("https://github.com/maichler"))
      ),
      scmInfo := Some(
        ScmInfo(
          url("https://github.com/maichler/sbt-jupiter-interface"),
          "scm:git@github.com:maichler/sbt-jupiter-interface.git"
        )
      )
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
        releaseStepCommandAndRemaining("^ test"),
        releaseStepCommandAndRemaining("^ scripted"),
        setReleaseVersion,
        commitReleaseVersion,
        tagRelease,
        releaseStepCommandAndRemaining("^ publishSigned"),
        setNextVersion,
        commitNextVersion,
        releaseStepCommandAndRemaining("^ sonatypeReleaseAll")
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
