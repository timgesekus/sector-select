import com.typesafe.sbteclipse.core.EclipsePlugin.EclipseKeys

import sbtprotobuf.{ProtobufPlugin=>PB}

name := """sector-select"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.6"

Seq(PB.protobufSettings: _*)

//protoc in PB.protobufConfig := "D:/Users/tim/Software/protoc/protoc"

version in PB.protobufConfig := "2.6.1"

resolvers ++= Seq (
  "Tei repro" at "http://lgnjiboo.srv.dfs.local/artifactory/repo1",
  "Local libs" at "http://lgnjiboo.srv.dfs.local/artifactory/libs-releases"
)

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  javaWs,
  "be.objectify" %% "deadbolt-java" % "2.3.1",
  "com.google.inject" % "guice" % "3.0",
  "com.google.protobuf" % "protobuf-java" % "2.6.1",
  "com.googlecode.protobuf-java-format" % "protobuf-java-format" % "1.2",
  "de.dfs.utils" % "config" % "1.0.0"
)

EclipseKeys.withSource := true

EclipseKeys.preTasks := Seq()

resolvers += Resolver.url("Objectify Play Repository", url("http://deadbolt.ws/releases/"))(Resolver.ivyStylePatterns)

includeFilter in (Assets, LessKeys.less) := "*.less"

excludeFilter in (Assets, LessKeys.less) := "_*.less"
