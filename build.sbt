import sbt.Keys._

name := """imirp"""

Common.settings

libraryDependencies ++= Seq(
  "org.mindrot" % "jbcrypt" % "0.3m",
  "com.typesafe" %% "play-plugins-mailer" % "2.2.0"
)

resolvers ++= Seq(
    "Apache" at "http://repo1.maven.org/maven2/",
    "jBCrypt Repository" at "http://repo1.maven.org/maven2/org/",
    "Sonatype OSS Snasphots" at "http://oss.sonatype.org/content/repositories/snapshots"
)

EclipseKeys.skipParents in ThisBuild := false

EclipseKeys.projectFlavor := EclipseProjectFlavor.Java

EclipseKeys.executionEnvironment := Some(EclipseExecutionEnvironment.JavaSE17)

lazy val root = (project in file("."))
	.enablePlugins(play.PlayJava)
	.aggregate(imirp_core)
	.dependsOn(imirp_core)

lazy val imirp_core = (project in file("imirp_core"))
