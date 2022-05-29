

lazy val commonSettings = Seq(
  libraryDependencies ++= Seq(
    "net.aichler" % "jupiter-interface" % JupiterKeys.jupiterVersion.value % Test
  )
)

lazy val moduleA = project.in(file("moduleA"))
  .enablePlugins(JvmPlugin)
  .settings(commonSettings)
  .settings(
    name := "moduleA"
  )

lazy val moduleB = project.in(file("moduleB"))
  .enablePlugins(JvmPlugin)
  .settings(commonSettings)
  .settings(
    name := "moduleB"
  )

lazy val root = project.in(file("."))
  .settings(commonSettings)
  .settings(
    name := "multi-project"
  )
  .aggregate(moduleA, moduleB)