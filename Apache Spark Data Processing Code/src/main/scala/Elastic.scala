import java.net.InetAddress
import java.util

import org.apache.spark.broadcast.Broadcast
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.action.update.UpdateRequest
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.aggregations.AggregationBuilders
import org.elasticsearch.search.aggregations.bucket.terms.Terms
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object Elastic {

  var client: Option[TransportClient] = None
  var _index : Option[String] = None
  var _type : Option[String] = None

  def initializeElastic(nodeEndpoint: String,clusterName: String, index: String, recType: String) = {
    client = Some(TransportClient.builder()
      .settings(Settings.settingsBuilder().put("cluster.name", clusterName).build()).build()
      .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(nodeEndpoint), 9300)))
    _index = Some(index)
    _type = Some(recType)
  }


  def insertDocument(json: util.HashMap[String, Object]) = client.get.index(new IndexRequest(_index.get, _type.get).source(json)).get


  def insertDeviceDocument(json: util.HashMap[String, Object], _id: String) = client.get.index( new IndexRequest("devices", "rec", _id).source(json)).get


  def upsertDeviceDocument(_id: String)(json: util.HashMap[String, Object]) = client.get.update(new UpdateRequest("devices", "rec", _id).doc(json).upsert(json)).get

  def indexExists(_index: String) = client.get.admin().indices().prepareExists(_index).execute().actionGet().isExists

//  def listUniqueDevices(): ListBuffer[String] = {
//    val teams = new ListBuffer[String]
//    var response : SearchResponse= null
//    var aggTerms : Terms = null
//    val agg = AggregationBuilders.terms("agg").field("id")
//    while( response == null || response.getHits.hits().length != 0){
//      response = client.get.prepareSearch("devices")
//        .setTypes("rec")
//        .setQuery(QueryBuilders.matchAllQuery())
//        .addAggregation(agg)
//        .setSize(0)
//        .execute()
//        .actionGet()
//      aggTerms = response.getAggregations.get("agg")
//    }
//    for(i <- 0 until aggTerms.getBuckets.size ){
//      teams.append(aggTerms.getBuckets.get(i).getKeyAsString)
//    }
//    teams
//  }

  def listDevices() = {
    val esData = new mutable.HashMap[String, String]//new util.ArrayList[util.HashMap[String,Object]]()
    var response : SearchResponse= null
    var i = 0
    while( response == null || response.getHits.hits().length != 0){
      response = client.get.prepareSearch("devices")
        .setTypes("rec")
        .setQuery(QueryBuilders.matchAllQuery())
        .setSize(1000)
        .setFrom(i * 1000)
        .execute()
        .actionGet()

      response.getHits.getHits.foreach(entry => esData.put(entry.id(),entry.sourceAsString()))
      i+=1
    }
    esData
  }

  def refreshDeviceTracker(updatedListOfDevices: mutable.HashMap[String, String], deviceTracker: Broadcast[DeviceTracker]) = {
    updatedListOfDevices.foreach({device =>
      val parser = new JSONParser()
      val rec = parser.parse(device._2).asInstanceOf[JSONObject]
        deviceTracker.value.insert(device._1, rec.get("capacity").toString.toInt, rec.get("ip").toString)
    })
  }


  def getDocuments(_id: String) = {

    val esData = new ListBuffer[String]//new util.ArrayList[util.HashMap[String,Object]]()
    var response : SearchResponse= null
    var i = 0
    while( response == null || response.getHits.hits().length != 0){
    response = client.get.prepareSearch("devices")
        .setQuery(QueryBuilders.matchQuery("_id",_id))
      .setSize(1)
      .setFrom(i * 10)
      .execute()
      .actionGet()

      response.getHits().getHits.foreach(entry => esData.append(entry.sourceAsString()))
      i+=1
    }
    esData(0)
  }








}





