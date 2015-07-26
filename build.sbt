name := "play_angular_amd_scala"

version := "1.0"

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

scalaVersion := "2.11.6"

libraryDependencies += specs2 % Test

libraryDependencies ++= Seq(
  "org.webjars" %% "webjars-play" % "2.4.0-1",
  "org.webjars" % "bootstrap" % "3.1.1-2",
  "com.typesafe.slick" %% "slick" % "3.0.0",
  "com.typesafe.play" %% "play-slick" % "1.0.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "1.0.0",
  "com.h2database" % "h2" % "1.4.187"
)

libraryDependencies ++= Seq( evolutions, cache, ws )
includeFilter in (Assets, LessKeys.less) := "*.less"


lazy val `play_angular_amd_scala` = (project in file(".")).enablePlugins(PlayScala,  SbtWeb)

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )  