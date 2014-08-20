import sbt._
import Keys._

object Common {
  val settings: Seq[Setting[_]] = Seq(
    organization := "org.imirp.imirp",
    version := "0.1",
		scalaVersion := "2.10.2",
		javacOptions ++= Seq("-source", "1.7")
  )
}
