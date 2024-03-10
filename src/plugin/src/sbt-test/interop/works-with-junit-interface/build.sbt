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

name := "test-project"
libraryDependencies ++= Seq(
  "net.aichler" % "jupiter-interface" % JupiterKeys.jupiterVersion.value % Test,
  "org.junit.platform" % "junit-platform-runner" % JupiterKeys.junitPlatformVersion.value % Test,
  "com.github.sbt" % "junit-interface" % "0.13.3" % Test,
  "junit" % "junit" % "4.12" % Test
)

testOptions += Tests.Argument("-v", "-n")

val checkTestDefinitions = taskKey[Unit]("Checks that tests were discovered properly")

checkTestDefinitions := {

  val definitions = (definedTests in Test).value
  streams.value.log.info("" + definitions)


  val expected = Map(
    "interop.JUnit4Test" -> "com.novocode.junit",
    "interop.JUnitVintageTest" -> "net.aichler.jupiter",
    "interop.JUnitJupiterTest" -> "net.aichler.jupiter"
  )

  expected.foreach { case (testName, annotationName) =>
    val count = definitions.find(_.name.endsWith(testName))
      .map(_.fingerprint.getClass.getName)
      .count(_.startsWith(annotationName))
    assert(count == 1, s"Expected test <$testName> to have annotation <$annotationName>")
  }

  assert(definitions.length == 3, "Found more than the 3 tests (" + definitions.length + ")!")
}
