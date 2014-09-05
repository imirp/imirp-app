name := "imirp_core"

mainClass := Some("org.imirp.imirp.App")

Common.settings

EclipseKeys.projectFlavor := EclipseProjectFlavor.Java

EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource

libraryDependencies ++= Seq(	
	"log4j" % "log4j" % "1.2.17",
	"junit" % "junit" % "4.10",
	"com.novocode" % "junit-interface" % "0.10-M2" % Test,
	"com.typesafe.akka" %% "akka-actor" % "2.3.4",
	"com.typesafe.akka" %% "akka-cluster" % "2.3.4",
	"com.typesafe.akka" %% "akka-testkit" % "2.3.4" % Test,
	"org.mongodb" % "mongo-java-driver" % "2.12.2",
	"org.jongo" % "jongo" % "1.0",
	"com.google.inject" % "guice" % "3.0",
	"javax.inject" % "javax.inject" % "1",
	"aopalliance" % "aopalliance" % "1.0",
	"org.mockito" % "mockito-all" % "1.9.5" % Test,
	"commons-lang" % "commons-lang" % "2.6",
	"net.sourceforge.findbugs" % "jsr305" % "1.3.7",
	"com.google.guava" % "guava" % "17.0"
)
