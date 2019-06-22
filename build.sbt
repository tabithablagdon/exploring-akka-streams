name := "exploring-akka-streams"

version := "1.0"

scalaVersion := "2.11.12"

lazy val akkaVersion = "2.5.16"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream"         % akkaVersion,
  "com.typesafe.akka" %% "akka-actor"          % akkaVersion,
  "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion  % Test, 
  "com.typesafe.akka" %% "akka-testkit"        % akkaVersion  % Test,
  "org.scalatest"     %% "scalatest"           % "3.0.5"      % Test
)

parallelExecution in Test := false