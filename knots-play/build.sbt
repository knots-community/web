name := "knots-play"

version := "1.0"

scalacOptions += "-feature"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
    "org.webjars" % "bootstrap" % "3.1.1",
    "org.webjars" % "jquery" % "2.1.0-2",
    "org.webjars" % "requirejs" % "2.1.11-1",
  "com.typesafe.play" %% "play-slick" % "0.8.0",
    "postgresql" % "postgresql" % "9.1-901-1.jdbc4",
    "joda-time" % "joda-time" % "2.4",
    "org.joda" % "joda-convert" % "1.6",
    "org.slf4j" % "slf4j-nop" % "1.6.4",
    "com.mohiva" %% "play-silhouette" % "1.0",
    "net.codingwell" %% "scala-guice" % "4.0.0-beta4",
    "com.typesafe.play.plugins" %% "play-plugins-mailer" % "2.3.0",
    "com.kenshoo" %% "metrics-play" % "2.3.0_0.1.6",
    cache,
    // Test dependencies
    "org.webjars" % "rjs" % "2.1.11-1-trireme" % "test",
    "org.webjars" % "squirejs" % "0.1.0" % "test"
)

resolvers += Resolver.sonatypeRepo("snapshots")

// Apply RequireJS optimization, digest calculation and gzip compression to assets
pipelineStages := Seq(rjs, digest, gzip)