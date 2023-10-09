ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

lazy val root = (project in file("."))
  .settings(
    name := "ETL",
    idePackagePrefix := Some("com.clouddeadline"),
  )

libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.14" % "test"
libraryDependencies += "org.apache.spark" %% "spark-sql" % "3.3.1" % "provided"
libraryDependencies += "org.apache.spark" %% "spark-core" % "3.3.1"
testOptions ++= Seq(Tests.Argument(TestFrameworks.ScalaTest, "-o"), Tests.Argument(TestFrameworks.ScalaTest, "-h", "target/test-reports"))