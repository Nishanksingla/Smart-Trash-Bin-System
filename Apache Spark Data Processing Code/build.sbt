import sbt.Keys._

name := "SmartProjectStreaming"

version := "1.0"

scalaVersion := "2.10.5"

libraryDependencies ++= Seq(
  "org.apache.spark" % "spark-core_2.10" % "1.6.1",
  "org.apache.spark" % "spark-bagel_2.10" % "1.6.1",
  "org.elasticsearch" % "elasticsearch" % "2.3.1",
  "com.googlecode.json-simple" % "json-simple" % "1.1.1",
  "com.netflix.archaius" % "archaius-aws" % "0.6.3",
  "org.apache.spark" % "spark-streaming-kafka_2.10" % "1.6.1",
  "org.apache.spark" % "spark-streaming_2.10" % "1.6.1",
  "javax.mail" % "mail" % "1.4",
  "com.twilio.sdk" % "twilio-java-sdk" % "6.3.0",
  "com.maxmind.geoip2" % "geoip2" % "2.1.0"
 )

libraryDependencies ++= Seq(
  ("org.apache.spark" % "spark-core_2.10" % "1.6.1").
    //exclude("org.eclipse.jetty.orbit", "javax.servlet").
    exclude("org.eclipse.jetty.orbit", "javax.transaction").
    exclude("org.eclipse.jetty.orbit", "javax.mail").
    exclude("org.eclipse.jetty.orbit", "javax.activation").
    exclude("commons-beanutils", "commons-beanutils-core").
    exclude("commons-collections", "commons-collections").
    exclude("commons-collections", "commons-collections").
    exclude("com.esotericsoftware.minlog", "minlog")
//    exclude("com.fasterxml.jackson.core","jackson-databind")
)

//libraryDependencies ++= Seq(
//  ("com.maxmind.geoip2" % "geoip2" % "2.7.0").
//    exclude("com.fasterxml.jackson.core", "jackson-databind")
//)


mainClass in(Compile, packageBin) := Some("StreamingDriver")

mainClass in(Compile, run) := Some("StreamingDriver")

assemblyMergeStrategy in assembly := {
  case PathList("javax", "servlet", xs@_*) => MergeStrategy.last
  case PathList("javax", "xml", xs@_*) => MergeStrategy.last
  case PathList("javax", "activation", xs@_*) => MergeStrategy.last
  case PathList("org", "apache", xs@_*) => MergeStrategy.last
  case PathList("com", "google", xs@_*) => MergeStrategy.last
  case PathList("com", "esotericsoftware", xs@_*) => MergeStrategy.last
  case PathList("com", "codahale", xs@_*) => MergeStrategy.last
  case PathList("com", "yammer", xs@_*) => MergeStrategy.last
  case PathList("org", "joda", xs@_*) => MergeStrategy.last
  case "about.html" => MergeStrategy.rename
  case "META-INF/ECLIPSEF.RSA" => MergeStrategy.last
  case "META-INF/mailcap" => MergeStrategy.last
  case "META-INF/mimetypes.default" => MergeStrategy.last
  case "plugin.properties" => MergeStrategy.last
  case "log4j.properties" => MergeStrategy.last
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

//assemblyShadeRules in assembly := Seq(
//  ShadeRule.rename("com.fasterxml.jackson.core.**"       -> "shadedjackson.core.@1").inAll,
//  ShadeRule.rename("com.fasterxml.jackson.annotation.**" -> "shadedjackson.annotation.@1").inAll,
//  ShadeRule.rename("com.fasterxml.jackson.databind.**"   -> "shadedjackson.databind.@1").inAll,
//  ShadeRule.rename("com.fasterxml.jackson.module.**"   -> "shadedjackson.module.@1").inAll
//)

dependencyOverrides ++= Set(
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.4.4"
)