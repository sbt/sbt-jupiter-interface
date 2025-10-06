Jupiter Interface
=================

[![Build Status](https://github.com/sbt/sbt-jupiter-interface/actions/workflows/ci.yml/badge.svg)](https://github.com/sbt/sbt-jupiter-interface/actions/workflows/ci.yml)
[![License](https://img.shields.io/hexpm/l/plug.svg)](https://raw.githubusercontent.com/sbt/sbt-jupiter-interface/main/LICENSE)
![Sonatype Nexus (Releases)](https://img.shields.io/nexus/r/com.github.sbt.junit/jupiter-interface?server=https%3A%2F%2Foss.sonatype.org)

An implementation of [sbt's test interface](https://github.com/sbt/test-interface) for [JUnit Jupiter](http://junit.org/junit5). This allows you to run JUnit 5 tests from [sbt](http://www.scala-sbt.org/).

The code is split into:

* `jupiter-interface`: a pure Java runtime library. The runtime library is written in pure Java and does all the heavy lifting like collecting and running tests.
* `sbt-jupiter-interface`: sbt plugin. The sbt plugin makes the runtime library available to the sbt build by adding it to `sbt.Keys.testFrameworks`. It also overwrites `sbt.Keys.detectTests` with a custom task that uses JUnits discovery mechanism to collect available tests. This step is necessary since sbt is currently not capable of detecting package private test classes.

## Usage

![Sonatype Nexus (Releases)](https://img.shields.io/nexus/r/com.github.sbt.junit/jupiter-interface?server=https%3A%2F%2Foss.sonatype.org)

Add the following line to `./project/plugins.sbt`:

```scala
addSbtPlugin("com.github.sbt.junit" % "sbt-jupiter-interface" % "x.y.z")
```

**Note**: We changed the organization from `"net.aichler"` to `"com.github.sbt.junit"` starting 0.11.3.

Additionally a `Test` dependency to the runtime library `jupiter-interface` is required for every module which wants to run JUnit 5 tests:

```scala
libraryDependencies ++= Seq(
  "com.github.sbt.junit" % "jupiter-interface" % JupiterKeys.jupiterVersion.value % Test
)
```

Note: If you want to restore default behaviour like in versions before `0.8.0` you can globally activate this plugin by adding the runtime dependency to `ThisBuild / libraryDependencies`:

```scala
ThisBuild / libraryDependencies += "com.github.sbt.junit" % "jupiter-interface" % JupiterKeys.jupiterVersion.value % Test
```

## Releases

 sbt Plugin | JUnit Platform | JUnit Engine | Minimum Java version |
:-----------|:---------------|:-------------|:---------------------|
 0.16.x     | 6.0.x          | 6.0.x        | 17+                  |
 0.15.x     | 1.13.x         | 5.13.x       | 8+                   |
 0.14.x     | 1.12.x         | 5.12.x       | 8+                   |
 0.13.x     | 1.11.x         | 5.11.x       | 8+                   |
 0.12.x     | 1.10.x         | 5.10.x       | 8+                   |
 0.11.x     | 1.9.x          | 5.9.x        | 8+                   |
 0.10.x     | 1.8.x          | 5.8.x        | 8+                   |
 0.9.x      | 1.7.x          | 5.7.x        | 8+                   |
 0.8.x      | 1.1.x          | 5.1.x        | 8+                   |
 0.7.x      | 1.1.0          | 5.1.0        | 8+                   |
 0.6.x      | 1.0.0          | 5.0.0        | 8+                   |
 0.5.x      | 1.0.0          | 5.0.0        | 8+                   |
 0.4.x      | 1.0.0-RC2      | 5.0.0-RC2    | 8+                   |
 0.3.x      | 1.0.0-M6       | 5.0.0-M6     | 8+                   |
 0.2.x      | 1.0.0-M5       | 5.0.0-M5     | 8+                   |
 0.1.x      | 1.0.0-M4       | 5.0.0-M4     | 8+                   |

**Note** `0.9.0`+ artifacts are published on Maven Central via Sonatype OSS. Previous versions were available via <https://repo.scala-sbt.org/scalasbt/sbt-plugin-releases/net.aichler/sbt-jupiter-interface/scala_2.10/sbt_0.13/>, whose underlying host has migrated from Bintray to Scala Center Artifactory instance.

## Framework Options

The following options are supported when running JUnit Jupiter tests:

 Option                           | Description
:---------------------------------|:---------------------------------
 `-v`                             | Log "test run started" / "test started" / "test run finished" events on log level "info" instead of "debug".
 `-q`                             | Suppress `STDOUT` for successful tests. Does not affect `STDERR` which is printed to the console normally. If a test fails the previously buffered output is dumped to the console.
 `-n`                             | Do not use ANSI colors in the output even if sbt reports that they are supported.
 `-s`                             | Try to decode Scala names in stack traces and test names. Fall back silently to non-decoded names if no matching Scala library is on the class path.
 `-a`                             | Show stack traces and exception class name for AssertionErrors (thrown by all assert* methods in JUnit).`
 `-c`                             | Do not print the exception class name prefix for any messages. With this option, only the result of getMessage() plus a stack trace is shown.
 `+v`                             | Turn off `-v`. Takes precedence over `-v`.
 `+q`                             | Turn off `-q`. Takes precedence over `-q`.
 `+n`                             | Turn off `-n`. Takes precedence over `-n`.
 `+s`                             | Turn off `-s`. Takes precedence over `-s`.
 `+a`                             | Turn off `-a`. Takes precedence over `-a`.
 `+c`                             | Turn off `-c`. Takes precedence over `-c`.
 `--display-mode=<MODE>`          | Select an output display mode for when tests are executed. Use either `flat` or `tree`.
 `--tests=<REGEXPS>`              | Run only the tests whose names match one of the specified regular expressions (in a comma-separated list). Non-matched tests are ignored. Only individual test case names are matched, not test classes. Example: For test `MyClassTest.testBasic()` only "testBasic" is matched. Use sbt's `test-only` command instead to match test classes.
 `-Dkey=value`                    | Temporarily set a system property for the duration of the test run. The property is restored to its previous value after the test has ended. Note that system properties are global to the entire JVM and they can be modified in a non-transactional way, so you should run tests serially and not perform any other tasks in parallel which depend on the modified property.
 `--run-listener=<CLASS_NAME>`    | A (user defined) class which extends either `org.junit.platform.launcher.TestExecutionListener` or `com.github.sbt.junit.jupiter.api.JupiterTestListener`. An instance of this class is created and added to the JUnit Launcher, so that it will receive test execution events. For more information, see [TestExecutionListener](http://junit.org/junit5/docs/current/api/org/junit/platform/launcher/TestExecutionListener.html). *Note: this uses the test-classloader, so the class needs to be defined in `src/test` or `src/main` or included as a test or compile dependency*
 `--include-tags=<EXPRESSIONS>`   | A comma separated list of tag expressions which should be included. Only tests matching one or more of those expressions will be run.
 `--exclude-tags=<EXPRESSIONS>`   | A comma separated list of tag expressions which should be excluded. Any test matching one or more of those expressions  will not be run.
 `--trace-dispatch-events`        | Write dispatch events to file `target/jupiterDispatchEvents.log` (used internally to test the event dispatcher).
 `--with-types`                   | When using the standard `flat` display mode, this flag causes the internal JUnit types of test identifiers to be added to the test name.

Any parameter not starting with `-` or `+` is treated as a glob pattern for matching tests. Unlike the patterns given directly to sbt's `test-only` command, the patterns given to jupiter-interface will match against the full test names (as displayed by jupiter-interface) of all atomic test cases, so you can match on test methods and parts of suites.

You can set default options in your build.sbt file:

```scala
testOptions += Tests.Argument(jupiterTestFramework, "-q", "-v")
```

Or use them with the `testQuick` and `testOnly` commands:

```scala
testOnly -- +q +v *Sequence*h2mem*
```

### Tag Expressions

Tag expressions can be used with `--include-tags` and `exclude-tags` respectively.

```scala
testOnly -- --include-tags=(micro&product),(micro&shipping)
```

Special care has to be taken if any expression contains white-space. In that case the entire parameter needs to be enclosed by double quotes.

```scala
testOnly -- "include-tags=(micro & product),(micro & shipping)"
```

Please see the corresponding chapter in [JUnit Documentation](https://junit.org/junit5/docs/current/user-guide/#running-tests-tag-expressions) for a detailed description on how to build tag expressions.

## Credits

* Jupiter Interface was originally developed by Michael Aichler in 2017.
* This plugin is heavily inspired by [JUnit Interface](https://github.com/sbt/junit-interface) and the console launcher from [JUnit Team](https://github.com/junit-team/junit5). Parts of the output capturing is based on code from [Apache Geronimo GShell](http://geronimo.apache.org/gshell/index.html).
