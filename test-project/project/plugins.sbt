lazy val pluginVersion = IO.readLines(file("../version.sbt"))
  .filter(_.startsWith("ThisBuild / version"))
  .map(line => line.substring(line.indexOf('"')+1, line.lastIndexOf('"')))
  .head

addSbtPlugin("net.aichler" % "sbt-jupiter-interface" % pluginVersion)
