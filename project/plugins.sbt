// Comment to get more information during initialization
logLevel := Level.Warn

// The Typesafe repository
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

// Cloudbees
resolvers += "Sonatype OSS Snasphots" at "http://oss.sonatype.org/content/repositories/snapshots"

resolvers += Resolver.file("Local Ivy Repository", file("/home/torben/.ivy2/local/"))(Resolver.ivyStylePatterns)

// Use the Play sbt plugin for Play projects
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.3.0-RC2")

addSbtPlugin("com.typesafe.sbt" % "sbt-less" % "1.0.0-RC2")

addSbtPlugin("se.marcuslonnberg" % "sbt-docker" % "0.5.1")

addSbtPlugin("com.banno" % "sbt-license-plugin" % "0.1.0")
