/**
  * Created by vjivane on 5/9/16.
  */
object ElasticGetDocTest extends App{

  System.setProperty("archaius.fixedDelayPollingScheduler.delayMills","55555555")
  PropertiesManager.initializePropertiesManager("iot-properties", "iot-streaming.config")

  Elastic.initializeElastic(PropertiesManager.elasticsearch_host,PropertiesManager.elasticsearch_cluster,PropertiesManager.elasticsearch_raw_data_index, PropertiesManager.elasticsearch_raw_data_type)

  //Elastic.getDocuments(1)

}
