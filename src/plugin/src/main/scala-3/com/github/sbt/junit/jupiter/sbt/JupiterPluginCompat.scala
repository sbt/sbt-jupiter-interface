package com.github.sbt.junit.jupiter.sbt

import _root_.sbt.Def
import _root_.sbt.Keys.*
import _root_.sbt.Task
import java.net.URL

private[sbt] object JupiterPluginCompat {
  val dependencyClasspathUrlArray: Def.Initialize[Task[Array[URL]]] = Def.task {
    val converter = fileConverter.value
    dependencyClasspath.value.map(x => converter.toPath(x.data).toFile.toURI.toURL).toArray
  }
}
