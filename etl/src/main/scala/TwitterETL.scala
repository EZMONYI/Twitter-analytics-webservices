package com.clouddeadline

import org.apache.spark.sql.functions._
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.SparkSession
import java.net.URL
import org.apache.spark.SparkFiles

object TwitterETL {

  def twitterDataETL(
                         inputPath: String,
                         outputPath: String,
                         spark: SparkSession): Unit = {
    import spark.implicits._
    spark.conf.set("spark.sql.legacy.timeParserPolicy","LEGACY")

    val df = spark.read.json(inputPath)
    df.createOrReplaceTempView("source")

    val df_process_date = spark.sql("""
    SELECT entities.hashtags AS hashtags, lang, created_at, id, id_str, user, text, in_reply_to_user_id, retweeted_status.user AS retweeted_user
    FROM source
    """).withColumn("created_at", to_timestamp(col("created_at"),"EEE MMM d HH:mm:ss Z yyyy"))

    val df_filter_hashtag = df_process_date.filter($"hashtags".isNotNull && size($"hashtags") > 0)
    val df_filter_lang = df_filter_hashtag.filter($"lang" isin ("ar","en","fr","in","pt","es","tr","ja"))
    val df_filter_created_at = df_filter_lang.filter($"created_at".isNotNull)
    val df_filter_duplicate = df_filter_created_at.dropDuplicates(("id"))
    val df_filter_user_id = df_filter_duplicate.filter($"user.id".isNotNull && $"user.id_str".isNotNull)
    val df_filter_id = df_filter_user_id.filter($"id".isNotNull && $"id_str".isNotNull)
    val df_filter_text = df_filter_id.filter($"text".isNotNull && length($"text") > 0).cache()

    df_filter_text.createOrReplaceTempView("processed")

    val all_posts = spark.sql("""
    SELECT hashtags, created_at, id, id_str, user.id as user_id, text, in_reply_to_user_id, retweeted_user.id AS retweeted_user_id
    FROM processed
    """).withColumn("hashtags", expr("hashtags.text"))
      .withColumn("type", when($"in_reply_to_user_id".isNotNull, "reply").when($"retweeted_user_id".isNotNull, "retweet").otherwise("original"))
      .withColumn("interactor_id", when($"in_reply_to_user_id".isNotNull, $"in_reply_to_user_id").when($"retweeted_user_id".isNotNull, $"retweeted_user_id").otherwise(null))
      .drop(col("in_reply_to_user_id"))
      .drop(col("retweeted_user_id"))
      .withColumn("tmp", $"user_id") // sort
      .withColumn("user_id", when($"interactor_id".isNotNull && $"interactor_id" < $"user_id", $"interactor_id").otherwise($"user_id"))
      .withColumn("interactor_id", when($"interactor_id".isNull || $"interactor_id" > $"tmp", $"interactor_id").otherwise($"tmp")).cache()
//      .drop(col("tmp"))
      .cache()
    all_posts.createOrReplaceTempView("all_posts")

    // create post table
    val post_table = all_posts
      .withColumn("hashtags", concat(concat_ws(",", col("hashtags").cast("array<string>"))))
      .drop(col("tmp")).cache()
    post_table.createOrReplaceTempView("post_table")


    val urlfile = "https://s3.amazonaws.com/cmucc-datasets/15619/f22/popular_hashtags.txt"
    spark.sparkContext.addFile(urlfile)

    val hashtag_not_allow_list = spark.read
      .option("inferSchema", true)
      .option("header", false)
      .text("file://" + SparkFiles.get("popular_hashtags.txt"))
      .select("value").map(f => f.getString(0))
      .collect
      .toList


    // create intermediate hash table
    val flatten_hashtag = all_posts.flatMap(f => {
          val hashtags = f.getSeq[String](0)
          hashtags.map((f.getLong(8), _))
        }).toDF("id", "hashtag")
    val lowercase_hashtag = flatten_hashtag
      .filter(!flatten_hashtag.col("hashtag").isin(hashtag_not_allow_list:_*))
      .withColumn("hashtag", lower(col("hashtag")))
    val hashtag_table = lowercase_hashtag.groupBy($"id", $"hashtag").count

    hashtag_table.createOrReplaceTempView("hashtag_table")

    val unique_user_pairs = post_table.select("user_id", "interactor_id").distinct
    unique_user_pairs.createOrReplaceTempView("unique_user_pairs")

    val user_hashtag_count = spark.sql(
      """
      SELECT user_id, interactor_id, hashtag, count AS count1
               FROM unique_user_pairs u JOIN hashtag_table h
               ON u.user_id = h.id
      """)
    user_hashtag_count.createOrReplaceTempView("user_hashtag_count")

    val hashtag_count = spark.sql(
      """
      SELECT user_id AS id1, interactor_id AS id2, u.hashtag, count1, h.count AS count2
               FROM user_hashtag_count u JOIN hashtag_table h
               ON u.interactor_id = h.id AND u.hashtag = h.hashtag
      """).withColumn("count", $"count1" + $"count2")
      .drop("count1")
      .drop("count2")
    hashtag_count.createOrReplaceTempView("hashtag_count")

    val score2 = spark.sql(
        """
        SELECT id1, id2, SUM(count) as score2
        FROM hashtag_count
        GROUP BY id1, id2
        """).withColumn("score2", when($"score2" > 10, log($"score2" - 10 +1)+1).otherwise(1.0))

    score2.createOrReplaceTempView("score2")

    // create hashtag table
//    val flatten_hashtag = all_posts.flatMap(f => {
//      val hashtags = f.getSeq[String](0)
//      hashtags.map((f.getLong(2), _))
//    }).toDF("id", "hashtag")
//    val lowercase_hashtag = flatten_hashtag.withColumn("hashtag", lower(col("hashtag")))
//    val hashtag_table = lowercase_hashtag.groupBy($"id", $"hashtag").count

    // create user table
    val main_user = spark.sql("""
    SELECT created_at, id, user AS all_user
    FROM processed
    WHERE user IS NOT NULL
    """)

    val retweeted_user = spark.sql("""
    SELECT created_at, id, retweeted_user as all_user
    FROM processed
    WHERE retweeted_user IS NOT NULL
    """)

    val all_users = main_user.union(retweeted_user)

    val partition = Window.partitionBy("all_user.id").orderBy(col("created_at").desc, col("id").desc)
    val all_users1 = all_users.withColumn("row", row_number.over(partition)).where($"row" === 1).drop("row")
    all_users1.createOrReplaceTempView("all_users")
    val user_table = spark.sql("""
    SELECT all_user.id as id, all_user.screen_name as screen_name, all_user.description as description
    FROM all_users
    """).cache()
    user_table.createOrReplaceTempView("user_table")

    // create score 1 table
    val reply_score = spark.sql("""
    SELECT user_id, interactor_id, 2 * COUNT(*) AS count
    FROM all_posts
    WHERE type = 'reply'
    GROUP BY user_id, interactor_id
    """)

    val retweet_score = spark.sql("""
    SELECT user_id, interactor_id, COUNT(*) AS count
    FROM all_posts
    WHERE type = 'retweet'
    GROUP BY user_id, interactor_id
    """)

    val score_1 = reply_score.union(retweet_score)
      .groupBy("user_id", "interactor_id")
      .agg(sum("count").as("score1"))
      .withColumn("score1", log($"score1" + 1))

    score_1.createOrReplaceTempView("score1")
    val posts_score1 = spark.sql("""
    SELECT hashtags, created_at, id, id_str, type, text, p.user_id, p.interactor_id, score1
    FROM post_table p JOIN score1 s
    ON p.user_id = s.user_id AND p.interactor_id = s.interactor_id
    """)
    posts_score1.createOrReplaceTempView("post_score1")
    val posts_score2 = spark.sql(
        """
         SELECT hashtags, created_at, id, id_str, type, text, p.user_id, p.interactor_id, score1, score2
           FROM post_score1 p LEFT JOIN score2 s
           ON p.user_id = s.id1 AND p.interactor_id = s.id2
        """).withColumn("score2",when(isnull($"score2"),1.0).otherwise($"score2"))

    posts_score2.createOrReplaceTempView("post_score2")
    val posts_score_description = spark.sql(
        """
          SELECT hashtags, created_at, p.id, id_str, type, text, p.user_id, p.interactor_id, score1, score2, screen_name as user_screen_name, description as user_description
                   FROM post_score2 p LEFT JOIN user_table u
                   ON p.user_id = u.id
          """)
    posts_score_description.createOrReplaceTempView("posts_score_description")
    val posts_score_description_final = spark.sql(
      """
      SELECT hashtags, created_at, p.id, id_str, type, text, p.user_id, p.interactor_id, score1, score2, user_screen_name, user_description, u.screen_name as interactor_screen_name, u.description as interactor_description
               FROM posts_score_description p LEFT JOIN user_table u
               ON p.interactor_id = u.id
      """)

    posts_score_description_final.write.mode("overwrite").option("header", "true").csv(outputPath + "/post_table.csv")
//    user_table.write.mode("overwrite").option("header", "true").csv(outputPath + "/user_table.csv")
//    hashtag_table.write.mode("overwrite").option("header","true").csv(outputPath + "/hashtag_table.csv")
//    score2.write.mode("overwrite").option("header","true").csv(outputPath + "/hashtag_score2.csv")
  }
}
