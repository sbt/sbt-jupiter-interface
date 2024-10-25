import sbt.*

object Dependencies {
  val junitJupiterVer = "5.11.0"
  // based on https://github.com/junit-team/junit5/releases/tag/r5.10.3 etc
  // The platform version seems to be "1." of the corresponding JUnit 5.x version.
  def junitPlatformVer = junitJupiterVer.replaceFirst("""^5\.""", "1.")
  def junitVintageVer = junitJupiterVer
  val testInterfaceVer = "1.0"

  val junitPlatformLauncher = "org.junit.platform" % "junit-platform-launcher" % junitPlatformVer
  val junitJupiterEngine = "org.junit.jupiter" % "junit-jupiter-engine" % junitJupiterVer
  val testInterface = "org.scala-sbt" % "test-interface" % testInterfaceVer
  val junitJupiterParams = "org.junit.jupiter" % "junit-jupiter-params" % junitJupiterVer
  val junitVintageEngine = "org.junit.vintage" % "junit-vintage-engine" % junitVintageVer
  val junitPlatformSuite = "org.junit.platform" % "junit-platform-suite" % junitPlatformVer
  val hamcrestLibrary = "org.hamcrest" % "hamcrest-library" % "3.0"
  val mockitoCore = "org.mockito" % "mockito-core" % "4.11.0"
  val junit4Interface = "com.github.sbt" % "junit-interface" % "0.13.3"
  val junit4 = "junit" % "junit" % "4.13.2"
}
