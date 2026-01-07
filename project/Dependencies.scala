import sbt.*

object Dependencies {
  val junitVersion = "6.0.2"
  val testInterfaceVer = "1.0"

  val junitPlatformLauncher = "org.junit.platform" % "junit-platform-launcher" % junitVersion
  val junitJupiterEngine = "org.junit.jupiter" % "junit-jupiter-engine" % junitVersion
  val testInterface = "org.scala-sbt" % "test-interface" % testInterfaceVer
  val junitJupiterParams = "org.junit.jupiter" % "junit-jupiter-params" % junitVersion
  val junitVintageEngine = "org.junit.vintage" % "junit-vintage-engine" % junitVersion
  val junitPlatformSuite = "org.junit.platform" % "junit-platform-suite" % junitVersion
  val hamcrestLibrary = "org.hamcrest" % "hamcrest-library" % "3.0"
  val mockitoCore = "org.mockito" % "mockito-core" % "4.11.0"
  val junit4Interface = "com.github.sbt" % "junit-interface" % "0.13.3"
  val junit4 = "junit" % "junit" % "4.13.2"
}
