libraryDependencies ++= Seq(
  "org.scala-sbt" % "scripted-plugin" % sbtVersion.value
)

resolvers += Resolver.typesafeIvyRepo("releases")

addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.7")

// For sbt 0.13.x (upto sbt-sonatype 2.3)
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "2.3")
addSbtPlugin("com.github.sbt" % "sbt-pgp" % "2.1.2")
