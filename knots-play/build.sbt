name := "knots-play"

version := "1.0"

scalacOptions += "-feature"

lazy val root = (project in file(".")).enablePlugins(PlayScala, SbtWeb)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  filters,
  cache,
  "org.webjars" % "requirejs" % "2.1.15",
  "org.webjars" % "bootstrap" % "3.3.0" exclude("org.webjars", "jquery"),
  "org.webjars" % "bootstrap-datepicker" % "1.3.0-3",
  "org.webjars" % "bootstrap-timepicker" % "0.2.3-1",
  "org.webjars" % "angularjs" % "1.3.2" exclude("org.webjars", "jquery"),
  "org.webjars" % "fullcalendar" % "2.0.3",
  "org.webjars" % "html5shiv" % "3.7.2",
  "org.webjars" % "jquery" % "2.1.1",
  "org.webjars" % "jquery-maskedinput" % "1.3.1",
  "org.webjars" % "nanoScrollerJS" % "0.7.6",
  "org.webjars" % "jquery-ui" % "1.11.1",
  "org.webjars" % "momentjs" % "2.8.3",
  "org.webjars" % "select2" % "3.5.1",
  "org.webjars" % "angular-ui-bootstrap" % "0.11.2",
  "org.webjars" % "jquery" % "2.1.0-2",
  "org.webjars" % "font-awesome" % "4.2.0",
  "com.typesafe.play" %% "play-slick" % "0.8.0",
  "postgresql" % "postgresql" % "9.1-901-1.jdbc4",
  "org.slf4j" % "slf4j-nop" % "1.6.4",
  "com.mohiva" %% "play-silhouette" % "1.0",
  "net.codingwell" %% "scala-guice" % "4.0.0-beta4",
  "com.typesafe.play.plugins" %% "play-plugins-mailer" % "2.3.1",
  "com.kenshoo" %% "metrics-play" % "2.3.0_0.1.6",
  "com.atlassian.jwt" % "jwt-core" % "1.2.1",
  "com.atlassian.jwt" % "jwt-api" % "1.2.1",
  "joda-time" % "joda-time" % "2.5",
  "org.joda" % "joda-convert" % "1.7",
  "com.github.tototoshi" %% "slick-joda-mapper" % "1.2.0",
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
//pipelineStages := Seq(rjs, digest, gzip)

//RjsKeys.paths += ("jsRoutes" -> ("/jsroutes" -> "empty:"))

includeFilter in (Assets, LessKeys.less) := "*.less"

excludeFilter in (Assets, LessKeys.less) := "_*.less"

sources in (Compile,doc) := Seq.empty

publishArtifact in (Compile, packageDoc) := false

RjsKeys.paths += ("jsRoutes" -> ("/jsroutes" -> "empty:"))