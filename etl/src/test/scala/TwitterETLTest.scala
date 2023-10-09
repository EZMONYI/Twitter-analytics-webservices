package com.clouddeadline

import com.clouddeadline.TwitterETL.twitterDataETL
import org.apache.spark.sql.SparkSession
import org.scalatest.BeforeAndAfterEach
import org.scalatest.funsuite.AnyFunSuite

class TwitterETLTest extends AnyFunSuite with BeforeAndAfterEach {

  var sparkSession : SparkSession = _
  override def beforeEach() {
    sparkSession = SparkSession.builder.
      master("local")
      .appName("etl session")
      .getOrCreate()
  }

  test("Test the ETL process could run without error"){
    twitterDataETL("data/tiny", "data/tiny-etl", sparkSession)
    assert("true" == "true")
  }

  override def afterEach() {
    sparkSession.stop()
  }
}