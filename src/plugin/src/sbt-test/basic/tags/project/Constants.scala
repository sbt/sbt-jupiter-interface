// because it seems impossible in a set command to put a string literal in a scripted test
object Constants {
  val IncludeFast = "--include-tags=fast"
  val IncludeSlow = "--include-tags=slow"
  val IncludeFastAndSlow = "--include-tags=fast,slow"
  val ExcludeFast = "--exclude-tags=fast"
  val ExcludeSlow = "--exclude-tags=slow"
  val ExcludeFastAndSlow = "--exclude-tags=fast,slow"
}
