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
  "org.junit.jupiter" % "junit-jupiter-api" % JupiterKeys.junitJupiterVersion.value % Test
)

/*
 * Tests that the plugin throws an exception if there are JUnit 5 tests but the runtime
 * library is missing.
 */
TaskKey[Unit]("checkDefinedTestsThrowsException") := {
  (Test / definedTests).result.value match {
    case Inc(cause:Incomplete) =>
      val actualMessage = cause.directCause.map(c => c.getMessage).getOrElse("")
      val expectedMessage = "Found at least one JUnit 5 test"
      assert(actualMessage.startsWith(expectedMessage),
        s"Expected an exception containing a message starting with `$expectedMessage` (actual: `$actualMessage`)")
      Seq.empty
    case Value(_:Seq[TestDefinition]) =>
      sys.error(s"Expected task `definedTests` to throw an exception due to " +
        "`jupiter-interface` not being on the test-classpath.")
  }
}
