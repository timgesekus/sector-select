import com.typesafe.sbteclipse.core.EclipsePlugin.EclipseKeys

import sbtprotobuf.{ProtobufPlugin=>PB}

name := """sector-select"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.6"

Seq(PB.protobufSettings: _*)

protoc in PB.protobufConfig := "D:/Users/tim/Software/protoc/protoc"

version in PB.protobufConfig := "2.6.1"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  javaWs,
  "be.objectify" %% "deadbolt-java" % "2.3.1",
  "com.google.inject" % "guice" % "3.0",
  "com.google.protobuf" % "protobuf-java" % "2.6.1"
)

EclipseKeys.withSource := true

EclipseKeys.preTasks := Seq()

resolvers += Resolver.url("Objectify Play Repository", url("http://deadbolt.ws/releases/"))(Resolver.ivyStylePatterns)

