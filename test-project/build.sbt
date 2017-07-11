import sbt.Keys.libraryDependencies


//logBuffered in Test := false

lazy val junit = (project in file("src/junit"))
  .enablePlugins(JvmPlugin)
  .settings(
    libraryDependencies ++= Seq(
      "com.novocode" % "junit-interface" % "0.11" % "test",
      "junit" % "junit" % "4.12" % "test"
    )
  )

lazy val jupiter = (project in file("src/jupiter"))
  .settings(
    libraryDependencies ++= Seq(
      "org.junit.jupiter" % "junit-jupiter-params" % readJupiterVersion % "test"
    ),
    resolvers += Resolver.mavenLocal,
    parallelExecution in Test := true
  )

lazy val root = (project in file("."))
  .aggregate(junit)
  .aggregate(jupiter)

/*
 * Reads Versions.junitJupiter value from ../build.sbt
 */
def readJupiterVersion = {

  IO.readLines(file("../build.sbt"))
    .filter(_.contains("val junitJupiter ="))
    .map(line => line.substring(line.indexOf('"')+1, line.lastIndexOf('"')))
    .head
}
