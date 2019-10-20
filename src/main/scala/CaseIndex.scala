// Code Written By:- Anchit Verma
// This code takes directory of XML files as input

// spark-shell --packages "org.elasticsearch:elasticsearch-spark-20_2.11:6.4.2,org.scalaj:scalaj-http_2.11:2.4.2,com.databricks:spark-xml_2.11:0.5.0"

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types.{StructType, StructField, StringType}
import org.apache.spark.sql.functions._
import org.elasticsearch.spark.sql._
import com.databricks.spark.xml._
import scalaj.http._


object CaseIndex {

    def nlp (s:String) : Array[String] = {
            val ner_output = Http("http://localhost:9000").postData(s).param("annotators","ner").param("outputFormat","xml").timeout(connTimeoutMs = 10000 , readTimeoutMs = 100000).asString

            val ner_xml = scala.xml.XML.loadString(ner_output.body)

            val person = (ner_xml\\"token").map(a => if ((a\\"NER").text == "PERSON") (a\\"word").text else "").filter(_.trim != "").mkString(" ")

            val location = (ner_xml\\"token").map(a => if ((a\\"NER").text == "LOCATION") (a\\"word").text else "").filter(_.trim != "").mkString(" ")

            val organisation = (ner_xml\\"token").map(a => if ((a\\"NER").text == "ORGANIZATION") (a\\"word").text else "").filter(_.trim != "").mkString(" ")
            
            return Array(person,location,organisation)
    }

    def main(args: Array[String]) {
        // Defining schema to be read from xml file.
        val customSchema = StructType(Array(
        StructField("name", StringType, nullable = true),
        StructField("AustLII", StringType, nullable = true),
        StructField("catchphrases", StringType, nullable = true),
        StructField("sentences", StringType, nullable = true)))


        // Read XML file into a dataframe.
        val spark1 = SparkSession.builder.getOrCreate()
        import spark1.implicits._

        var df = spark1.read.option("rowTag", "case").schema(customSchema).xml(args(0))

        // Points to nlp function which returns an array with
        // person, location and organizations found in input string.
        def enrich : (String => Array[String]) = { s => nlp(s) }

        // User defined functfion which returns a column to be
        // added to dataframe. Calls another function enrich.
        val myUDF = udf(enrich)

        // Takes old dataframe, combines all columns into a string, then calls udf
        // to do data enrichment by getting person, location and organization entities
        // which are returned as an array. .select is used to split this array into 
        // different columns. The output is a dataframe with columns which will be 
        // used as mapping when adding to elasticsearch index. 
        val newDF = df.withColumn("newCol", myUDF(df("catchphrases"))).select($"catchphrases", $"name", $"sentences", $"newCol"(0).as("person"), $"newCol"(1).as("location"), $"newCol"(2).as("organisation"))


        // Initialise configurations to interact with elasticsearch.
        val spark = SparkSession.builder().appName("WriteToES").master("local[*]").config("spark.es.nodes","localhost").config("spark.es.port","9200").getOrCreate()

        // Write content from dataframe into elasticsearch index.
        df.saveToEs("legal_idx/cases")

    }
}

