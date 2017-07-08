name := "test-project"

val traceFile = file("target/jupiterDispatchEvents.log")

InputKey[Unit]("check-status") := {

  val input = Def.spaceDelimited("<status> <count>").parsed
  assert(input.length == 2, "Expecting two arguments (<status> <count>")

  val status = input.head
  val expected = Integer.valueOf(input.last)

  val events = IO.readLines(traceFile)
  val actual = events.count(_.contains(s"($status,"))

  assert(expected == actual,
    s"Expected $expected <$status> events (got $actual instead). Events:\n" +
      events.mkString("\n")
  )
}

InputKey[Unit]("check-total") := {

  val input = Def.spaceDelimited("<count>").parsed
  assert(input.length == 1, "Expecting one argument (<count>")

  val expected = Integer.valueOf(input.head)

  val events = IO.readLines(traceFile)
  val actual = events.count(_.startsWith("DispatchEvent("))

  assert(expected == actual,
    s"Expected a total of $expected events (got $actual instead). Events:\n" +
      events.mkString("\n")
  )
}

TaskKey[Unit]("test-silent") := {
  (test in Test).result.value
}

TaskKey[Unit]("reset-tests") := {
  traceFile.delete()
}