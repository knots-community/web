name := "knots-play"

version := "1.0"

scalacOptions += "-feature"

lazy val root = (project in file(".")).enablePlugins(PlayScala, SbtWeb)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  "org.webjars" % "requirejs" % "2.1.14-1",
  "org.webjars" % "underscorejs" % "1.6.0-3",
  "org.webjars" % "bootstrap" % "3.1.1-2" exclude("org.webjars", "jquery"),
  "org.webjars" % "angularjs" % "1.2.18" exclude("org.webjars", "jquery"),
  "org.webjars" % "angular-ui-bootstrap" % "0.11.0-2",
  "org.webjars" % "jquery" % "2.1.0-2",
  "com.typesafe.play" %% "play-slick" % "0.8.0",
  "postgresql" % "postgresql" % "9.1-901-1.jdbc4",
  "org.slf4j" % "slf4j-nop" % "1.6.4",
  "com.mohiva" %% "play-silhouette" % "1.0",
  "net.codingwell" %% "scala-guice" % "4.0.0-beta4",
  "com.typesafe.play.plugins" %% "play-plugins-mailer" % "2.3.0",
  "com.kenshoo" %% "metrics-play" % "2.3.0_0.1.6",
  "com.atlassian.jwt" % "jwt-core" % "1.2.1",
  "com.atlassian.jwt" % "jwt-api" % "1.2.1",
  "joda-time" % "joda-time" % "2.5",
  "org.joda" % "joda-convert" % "1.7",
  "com.github.tototoshi" %% "slick-joda-mapper" % "1.2.0",
  cache,
  // Test dependencies
  "org.webjars" % "rjs" % "2.1.11-1-trireme" % "test",
  "org.webjars" % "squirejs" % "0.1.0" % "test"
)

// Scala Compiler Options
scalacOptions in ThisBuild ++= Seq(
  "-target:jvm-1.7",
  "-encoding", "UTF-8",
  "-deprecation", // warning and location for usages of deprecated APIs
  "-feature", // warning and location for usages of features that should be imported explicitly
  "-unchecked", // additional warnings where generated code depends on assumptions
  "-Xlint", // recommended additional warnings
  "-Ywarn-adapted-args", // Warn if an argument list is modified to match the receiver
  "-Ywarn-value-discard", // Warn when non-Unit expression results are unused
  "-Ywarn-inaccessible",
  "-Ywarn-dead-code",
  "-language:implicitConversions",
  "-language:postfixOps"
)

resolvers += Resolver.sonatypeRepo("snapshots")

// Apply RequireJS optimization, digest calculation and gzip compression to assets
//pipelineStages := Seq(digest, gzip)

//RjsKeys.paths += ("jsRoutes" -> ("/jsroutes" -> "empty:"))

includeFilter in (Assets, LessKeys.less) := "*.less"

excludeFilter in (Assets, LessKeys.less) := "_*.less"
