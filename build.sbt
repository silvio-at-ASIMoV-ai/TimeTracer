
ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.1"

lazy val root = (project in file("."))
  .settings(
    name := "TimeTracer"
  )

libraryDependencies ++= Seq(
  "io.github.nremond" %% "pbkdf2-scala" % "0.7.0",
  "mysql" % "mysql-connector-java" % "8.0.33",
  // https://mvnrepository.com/artifact/org.xerial/sqlite-jdbc
  "org.xerial" % "sqlite-jdbc" % "3.34.0",
// https://mvnrepository.com/artifact/org.scala-lang.modules/scala-swing
  "org.scala-lang.modules" %% "scala-swing" % "3.0.0",
  // https://mvnrepository.com/artifact/com.miglayout/miglayout
  "com.miglayout" % "miglayout" % "3.7.4",
  // https://mvnrepository.com/artifact/com.formdev/flatlaf
  "com.formdev" % "flatlaf" % "3.3",
)

assembly / assemblyMergeStrategy := {
  case PathList("META-INF", xs*) => MergeStrategy.discard
  case "reference.conf" => MergeStrategy.concat
  case x => MergeStrategy.first
}

assembly / assemblyJarName := "TimeTracer.jar"

ThisBuild/scalacOptions ++= Seq("-deprecation")

