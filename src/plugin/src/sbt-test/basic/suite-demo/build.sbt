val junit             = Def.setting { "org.junit.jupiter"  % "junit-jupiter-api"           % JupiterKeys.junitJupiterVersion.value }
val junitEngine       = Def.setting { "org.junit.jupiter"  % "junit-jupiter-engine"        % JupiterKeys.junitJupiterVersion.value }
val junitSuiteEngine  = Def.setting { "org.junit.platform" % "junit-platform-suite-engine" % JupiterKeys.junitPlatformVersion.value }

name := "test-project"
libraryDependencies ++= Seq(
  junit.value % Test,
  junitEngine.value % Test,
  junitSuiteEngine.value % Test,
  "com.github.sbt.junit" % "jupiter-interface" % JupiterKeys.jupiterVersion.value % Test,
)
