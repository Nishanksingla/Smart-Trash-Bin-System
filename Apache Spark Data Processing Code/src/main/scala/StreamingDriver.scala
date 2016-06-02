import org.apache.spark.SparkConf
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}

object StreamingDriver extends App{

  System.setProperty("archaius.fixedDelayPollingScheduler.delayMills","55555555")
  PropertiesManager.initializePropertiesManager("iot-properties", "iot-streaming.config")

  println("Elastic => " + PropertiesManager.elasticsearch_host )
  println("Elastic Index => " + PropertiesManager.elasticsearch_raw_data_index )
  println("Elastic Raw => " + PropertiesManager.elasticsearch_raw_data_type )
  println("Kafka Broker => " + PropertiesManager.kafka_broker )
  println("Kafka Topic => " + PropertiesManager.kafka_topics)

  val sparkConf = new SparkConf().setAppName(PropertiesManager.spark_app_name).setMaster(PropertiesManager.spark_app_master)

  val ssc = new StreamingContext(sparkConf, Seconds(PropertiesManager.kafka_polling))

  val deviceTracker: Broadcast[DeviceTracker] = ssc.sparkContext.broadcast(new DeviceTracker)

  val lines = KafkaUtils.createStream(ssc, PropertiesManager.kafka_broker, PropertiesManager.kafka_receivers_group, PropertiesManager.kafka_topics).map(_._2)
  lines.foreachRDD({
    entry => entry.foreach({message =>
      println(message)
      StreamProcessor.process(message, deviceTracker)
    })
  })

  ssc.start()
  ssc.awaitTermination()
}