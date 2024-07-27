name := "test-project"
libraryDependencies ++= Seq(
  "com.github.sbt.junit" % "jupiter-interface" % JupiterKeys.jupiterVersion.value % Test
)

InputKey[Unit]("tests-executed") := {
  val expected = Def.spaceDelimited("<test-classes>").parsed
  val testsrun = IO.readLines(target.value / "testsrun").toSet
  expected.foreach { test =>
    if (!testsrun(test)) {
      throw new RuntimeException("Expected test " + test + " to be run, but it wasn't.  Tests that were run:\n" + testsrun.mkString("\n"))
    }
  }

  val unexpected = testsrun filterNot expected.contains
  if (unexpected.nonEmpty) {
    throw new RuntimeException("Unexpected tests were run:\n" + unexpected.mkString("\n"))
  }
}

InputKey[Unit]("tests-not-executed") := {
  val notExpected = Def.spaceDelimited("<test-classes>").parsed
  val testsrun = IO.readLines(target.value / "testsrun").toSet
  notExpected.foreach { test =>
    if (testsrun(test)) {
      throw new RuntimeException("Expected test " + test + " not to be run, but it was.  Tests that were run:\n" + testsrun.mkString("\n"))
    }
  }
}

TaskKey[Unit]("reset-tests") := {
  (target.value / "testsrun").delete()
}
