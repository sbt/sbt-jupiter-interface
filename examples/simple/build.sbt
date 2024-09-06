scalaVersion := "2.12.20"
name := "simple"
libraryDependencies ++= Seq(
  "com.github.sbt.junit" % "jupiter-interface" % JupiterKeys.jupiterVersion.value % Test
)
