# SBT Jupiter Plugin

This module provides a SBT plugin which adds support for the JUnit Jupiter test framework.

## Configuration

In order to make SBT generally aware of another test framework, `sbt-jupiter-interface` needs to be added to the projects list of SBT plugins.

project/plugins.sbt:
```
addSbtPlugin("com.github.sbt.junit" % "sbt-jupiter-interface" % "0.8.2")
```

Additionally a test dependency to this plugins runtime library is required for every module which wants to run `JUnit 5` tests.

build.sbt:
```
libraryDependencies ++= Seq(
    "com.github.sbt.junit" % "jupiter-interface" % JupiterKeys.jupiterVersion.value % Test
)
```
