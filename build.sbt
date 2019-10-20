name := "CaseIndex"
version := "1.0"
scalaVersion := "2.11.12"
libraryDependencies ++= Seq(
			"org.scalaj" %% "scalaj-http" % "2.4.2",
			"org.apache.spark" %% "spark-core" % "2.4.3",
			"org.apache.spark" %% "spark-sql" % "2.4.3",
			"org.elasticsearch" %% "elasticsearch-spark-20" % "6.4.2",
  			"com.databricks" %% "spark-xml" % "0.5.0"
			)

