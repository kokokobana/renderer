name := "renderer"

organization := "org.bitbucket.wakfuthesaurus"

version := "0.1-SNAPSHOT"

scalaVersion in ThisBuild := "2.12.6"

scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-Xlint",
  "-Xfuture",
  "-Ywarn-dead-code",
  "-Ywarn-unused-import",
  "-opt-warnings",
  "-unchecked"
)

libraryDependencies ++= Seq(
  "org.typelevel" %%% "cats-free" % "1.1.0",
  "org.scala-js" %%% "scalajs-dom" % "0.9.5",

  // these need to be built locally
  "org.bitbucket.wakfuthesaurus" %%% "shared" % "0.2-SNAPSHOT"
)

enablePlugins(ScalaJSPlugin)
