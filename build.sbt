name := """sector-select"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

resolvers += Resolver.sonatypeRepo("releases")

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  javaWs,
  "ws.securesocial" %% "securesocial" % "3.0-M3"
)
