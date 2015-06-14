import sbt._
import Keys._

object Common {
  val settings: Seq[Setting[_]] = Seq(
    organization := "org.imirp.imirp",
    version := "0.8",
		scalaVersion := "2.10.5",
		javacOptions ++= Seq("-source", "1.8")
  )
}
