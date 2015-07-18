name := "play_angular_amd_scala"

version := "1.0"

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

scalaVersion := "2.11.6"

libraryDependencies += specs2 % Test

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "anorm" % "2.4.0",
  "org.webjars" %% "webjars-play" % "2.4.0-1",
  "org.webjars" % "bootstrap" % "3.1.1-2"
)

libraryDependencies ++= Seq( jdbc , cache, ws )

includeFilter in (Assets, LessKeys.less) := "*.less"


lazy val `play_angular_amd_scala` = (project in file(".")).enablePlugins(PlayScala, PlayEbean, SbtWeb)

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )  