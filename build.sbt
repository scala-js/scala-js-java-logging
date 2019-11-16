import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

val scala210 = "2.10.7"
val scala211 = "2.11.12"
val scala212 = "2.12.10"
val scala213 = "2.13.1"

crossScalaVersions in ThisBuild := {
  val allVersions = Seq(scala210, scala211, scala212, scala213)
  if (scalaJSVersion.startsWith("0.6."))
    allVersions
  else
    allVersions.filter(_ != scala210)
}
scalaVersion in ThisBuild := scala212

val commonSettings: Seq[Setting[_]] = Seq(
  version := "0.1.6-SNAPSHOT",
  organization := "org.scala-js",
  scalacOptions ++= Seq("-deprecation", "-feature", "-Xfatal-warnings"),

  homepage := Some(url("http://scala-js.org/")),
  licenses += ("BSD New",
      url("https://github.com/scala-js/scala-js-java-logging/blob/master/LICENSE")),
  scmInfo := Some(ScmInfo(
      url("https://github.com/scala-js/scala-js-java-logging"),
      "scm:git:git@github.com:scala-js/scala-js-java-logging.git",
      Some("scm:git:git@github.com:scala-js/scala-js-java-logging.git")))
)

val nativeSettings = Seq(
  scalaVersion := scala211,
  crossScalaVersions := Seq(scala211),
  sources in (Compile,doc) := Seq.empty
)

lazy val root = crossProject(JSPlatform, NativePlatform).
  crossType(CrossType.Pure).
  in(file(".")).
  settings(commonSettings).
  settings(
    name := "scalajs-java-logging",

    mappings in (Compile, packageBin) ~= {
      _.filter(!_._2.endsWith(".class"))
    },
    exportJars := true,

    publishMavenStyle := true,
    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      if (isSnapshot.value)
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases" at nexus + "service/local/staging/deploy/maven2")
    },
    pomExtra := (
        <developers>
          <developer>
            <id>sjrd</id>
            <name>Sébastien Doeraene</name>
            <url>https://github.com/sjrd/</url>
          </developer>
          <developer>
            <id>gzm0</id>
            <name>Tobias Schlatter</name>
            <url>https://github.com/gzm0/</url>
          </developer>
          <developer>
            <id>nicolasstucki</id>
            <name>Nicolas Stucki</name>
            <url>https://github.com/nicolasstucki/</url>
          </developer>
        </developers>
    ),
    pomIncludeRepository := { _ => false }
  ).
  nativeSettings(nativeSettings)

lazy val rootJS = root.js
lazy val rootNative = root.native

lazy val testSuite = crossProject(JSPlatform, JVMPlatform).
  jsConfigure(_.enablePlugins(ScalaJSJUnitPlugin)).
  settings(commonSettings: _*).
  settings(
    testOptions +=
      Tests.Argument(TestFramework("com.novocode.junit.JUnitFramework"), "-v", "-a")
  ).
  jsSettings(
    name := "java.logging testSuite on JS"
  ).
  jsConfigure(_.dependsOn(rootJS)).
  jvmSettings(
    name := "java.logging testSuite on JVM",
    libraryDependencies +=
      "com.novocode" % "junit-interface" % "0.9" % "test"
  )

lazy val nativeTestSuite = project.
  settings(
    name := "java.logging testSuite on Native",
    nativeSettings
  ).
  dependsOn(rootNative).
  enablePlugins(ScalaNativePlugin)
