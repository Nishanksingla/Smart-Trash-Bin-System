import java.util
import java.util.Date

import org.apache.spark.broadcast.Broadcast
import org.joda.time.DateTime
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

object StreamProcessor {

  Elastic.initializeElastic(PropertiesManager.elasticsearch_host, PropertiesManager.elasticsearch_cluster, PropertiesManager.elasticsearch_raw_data_index, PropertiesManager.elasticsearch_raw_data_type)
  Email.initializeEmailSession()

  var initialRefresh = false
  var msgsProcessedForCurrentSlot = false
  var deviceAlertTracker = new mutable.HashMap[String, Boolean]
  val parser = new JSONParser()

  def process(message: String, deviceTracker: Broadcast[DeviceTracker]): Unit = {


    val json = parser.parse(message).asInstanceOf[JSONObject]

    //    val deviceDocument = new util.HashMap[String, Object]()
    //    deviceDocument.put("capacity", new Integer(140))
    //    Elastic.upsertDeviceDocument("1")(deviceDocument)

    pushToElasticSearch(json) // OK
    processDevice(json, deviceTracker) // Work

    DateTime.now().getMinuteOfDay % 5 == 0 match {

      case true => if(!msgsProcessedForCurrentSlot) {
        processSMS()
        msgsProcessedForCurrentSlot = true
      }
      case false => msgsProcessedForCurrentSlot = false
    }
  }

  def pushToElasticSearch(json: JSONObject): Unit = {
    import ExecutionContext.Implicits.global
    Future {
      val id = new Integer(json.get("id").toString)
      val distance = new Integer(json.get("distance").toString)
      val timestamp = json.get("@timestamp").toString
      val ip = json.get("ip").toString

      val document = new util.HashMap[String, Object]()

      document.put("id", id)
      document.put("distance", distance)
      document.put("@timestamp", timestamp)
      document.put("ip", ip)
      val location = IP2Geo.getLocation(ip).split(",")
      document.put("city", location(1))
      document.put("area_code", location(2))
      document.put("latitude", location(3))
      document.put("longitude", location(4))

      Elastic.insertDocument(document)
    }
  }

  def processDevice(json: JSONObject, deviceTracker: Broadcast[DeviceTracker]) = {

    val _id = json.get("id").toString
    val currentLevel = new Integer(json.get("distance").toString)
    val ip = json.get("ip").toString
    val timestamp = json.get("@timestamp").toString
    val location = IP2Geo.getLocation(ip).split(",")


    if (!initialRefresh) {
      Elastic.indexExists("devices") match {

        case true =>
          val updatedListOfDevices = Elastic.listDevices()
          Elastic.refreshDeviceTracker(updatedListOfDevices, deviceTracker)

        case false =>
          val deviceDocument = new util.HashMap[String, Object]()
          deviceDocument.put("capacity", currentLevel)
          deviceDocument.put("@timestamp", timestamp)
          deviceDocument.put("ip", ip)
          deviceDocument.put("city", location(1))
          deviceDocument.put("area_code", location(2))
          deviceDocument.put("latitude", location(3))
          deviceDocument.put("longitude", location(4))
          deviceTracker.value.insert(_id, currentLevel, ip)
          Elastic.insertDeviceDocument(deviceDocument, _id)
          pushNewDeviceAlerts(_id, currentLevel)
      }
      initialRefresh = true
    }

    var deviceCapacity = 0

    deviceTracker.value.isDeviceRegistered(_id) match {
      case true =>
        deviceCapacity = deviceTracker.value.deviceCap(_id).capacity
      case false =>
        val deviceDocument = new util.HashMap[String, Object]()
        deviceDocument.put("capacity", currentLevel)
        deviceDocument.put("@timestamp", timestamp)
        deviceDocument.put("ip", ip)
        deviceDocument.put("city", location(1))
        deviceDocument.put("area_code", location(2))
        deviceDocument.put("latitude", location(3))
        deviceDocument.put("longitude", location(4))
        deviceDocument.put("numBags", new Integer(10))
        deviceTracker.value.insert(_id, currentLevel, ip)
        Elastic.insertDeviceDocument(deviceDocument, _id)

        pushNewDeviceAlerts(_id, currentLevel)
    }


//    if (currentLevel >= deviceCapacity - (deviceCapacity * 0.9) && currentLevel <= deviceCapacity) {
    if (currentLevel >= deviceCapacity - (deviceCapacity * 0.9)){
      deviceAlertTracker.put(_id, false)
    }


      println("Current list of registered devices:")
    deviceTracker.value.deviceCapacityMap.foreach(entry => println(s"Device ID: ${entry._1} \t Device Capcity: ${entry._2.capacity} \t Device Gateway: ${entry._2.ip}"))


    deviceTracker.value.isDeviceRegistered(_id) match {

      case true => {


        if (currentLevel <= deviceCapacity - (deviceCapacity * 0.9) && currentLevel >= 0) {
          println(s"Alert 1 => $deviceCapacity")
          pushAlerts(_id, currentLevel,deviceCapacity)
        }
        else if (currentLevel > deviceTracker.value.deviceCap(_id).capacity || (!ip.equalsIgnoreCase(deviceTracker.value.deviceCap(_id).ip))) {
          deviceTracker.value.insert(_id, currentLevel, ip)

          val deviceDocument = new util.HashMap[String, Object]()
          deviceDocument.put("capacity", currentLevel)
          deviceDocument.put("@timestamp", timestamp)
          deviceDocument.put("ip", ip)
          deviceDocument.put("city", location(1))
          deviceDocument.put("area_code", location(2))
          deviceDocument.put("latitude", location(3))
          deviceDocument.put("longitude", location(4))
          Elastic.upsertDeviceDocument(_id)(deviceDocument)
        }
      }

      case false => {

//        val deviceDocument = new util.HashMap[String, Object]()
//        deviceDocument.put("capacity", currentLevel)
//        deviceDocument.put("@timestamp", timestamp)
//        deviceDocument.put("ip", ip)
//        deviceDocument.put("city", location(1))
//        deviceDocument.put("area_code", location(2))
//        deviceDocument.put("latitude", location(3))
//        deviceDocument.put("longitude", location(4))
//
//        if (Elastic.indexExists("devices")) {
//          val updatedListOfDevices = Elastic.listDevices()
//          Elastic.refreshDeviceTracker(updatedListOfDevices, deviceTracker)
//          if (updatedListOfDevices.contains(_id)) {
//            val deviceCapacity = deviceTracker.value.deviceCap(_id).capacity
//            if (currentLevel <= deviceCapacity - (deviceCapacity * 0.9) && currentLevel >= 0) {
//              println(s"Alert 2 => ${deviceTracker.value.deviceCap(_id)}")
//              pushAlerts(_id, currentLevel,deviceCapacity)
//            } else {
//              deviceTracker.value.insert(_id, currentLevel, ip)
//              Elastic.upsertDeviceDocument(_id)(deviceDocument)
//            }
//          } else {
//            deviceTracker.value.insert(_id, currentLevel, ip)
//            Elastic.insertDeviceDocument(deviceDocument, _id)
//            pushNewDeviceAlerts(_id, currentLevel)
//          }
//        }
          // else {
//          deviceTracker.value.insert(_id, currentLevel, ip)
//          Elastic.insertDeviceDocument(deviceDocument, _id)
//          pushNewDeviceAlerts(_id, currentLevel)
//        }
      }
    }
  }


  def pushAlerts(id: String, currentLevel: Int, deviceCapacity: Int) = {


    if(deviceAlertTracker.contains(id)) {
      if(!deviceAlertTracker.get(id).get) {
        println(s"\n\n #########$id############ \n\n Pushing Alert ! \n\n ##########$currentLevel########### \n\n")
        Email.sendEmail("vireshjivane5@gmail.com", "Admin", s"SmartBin Alert - Bin #$id at 101 E San Fernando is about to get full. Current: $currentLevel. Max: $deviceCapacity", "This is an auto-generated message from SmartBin system. Thanks")
        Email.sendEmail("nishanksingla@gmail.com", "Admin", s"SmartBin Alert - Bin #$id at 101 E San Fernando is about to get full. Current: $currentLevel. Max: $deviceCapacity", "This is an auto-generated message from SmartBin system. Thanks")
        Email.sendEmail("vaibhav.k.tupe@gmail.com", "Admin", s"SmartBin Alert - Bin #$id at 101 E San Fernando is about to get full. Current: $currentLevel. Max: $deviceCapacity", "This is an auto-generated message from SmartBin system. Thanks")

        //  Twilio.sendSMS(s"SmartBin Alert - Bin #$id at 101 E San Fernando is about to get full. Current: $currentLevel. Max: $deviceCapacity")


        updateNumofBags(id)

        deviceAlertTracker.put(id,true)
      }
      println("Complete...")
    }


  }

  def pushNewDeviceAlerts(id: String, deviceCapacity: Int) = {

    println(s"\n\n #########$id############ \n\n Pushing New Device Alert ! \n\n ##########$deviceCapacity########### \n\n")

    Email.sendEmail("vireshjivane5@gmail.com", "Admin", s"New Device Registered - Bin #$id has been registered at 200 Java Dr with capacity: $deviceCapacity", "This is an auto-generated message from SmartBin system. Thanks")
    Email.sendEmail("nishanksingla@gmail.com", "Admin", s"New Device Registered - Bin #$id has been registered at 200 Java Dr with capacity: $deviceCapacity", "This is an auto-generated message from SmartBin system. Thanks")
    Email.sendEmail("vaibhav.k.tupe@gmail.com", "Admin", s"New Device Registered - Bin #$id has been registered at 200 Java Dr with capacity: $deviceCapacity", "This is an auto-generated message from SmartBin system. Thanks")
    println("Complete...")

  }


  def processSMS() = {
    println("Processing messages...start")
    Twilio.receiveSMS().foreach({ message =>
      val array = message.split(",")
      val id = array(0)
      val from = array(1)
      val timestamp = array(2)
      Email.sendEmail("vireshjivane5@gmail.com", "Admin", s"User SMS Alert - Bin #$id has been reported FULL by $from at $timestamp", "This is an auto-generated message from SmartBin system. Thanks")
    })
    println("Processing messages...stop")
  }

  def updateNumofBags(_id: String) ={
    val json = parser.parse(Elastic.getDocuments(_id)).asInstanceOf[JSONObject]

    val deviceDocument = new util.HashMap[String, Object]()
    deviceDocument.put("capacity", json.get("capacity").toString)
    deviceDocument.put("@timestamp", json.get("@timestamp").toString)
    deviceDocument.put("ip", json.get("ip").toString)
    val location = IP2Geo.getLocation(json.get("ip").toString).split(",")
    deviceDocument.put("city", location(1))
    deviceDocument.put("area_code", location(2))
    deviceDocument.put("latitude", location(3))
    deviceDocument.put("longitude", location(4))
    deviceDocument.put("numBags", new Integer(json.get("numBags").toString.toInt - 1))
    Elastic.upsertDeviceDocument(_id)(deviceDocument)

    println(s"NumBags Changed from ${json.get("numBags").toString.toInt} to ${json.get("numBags").toString.toInt -1}")
  }
}
