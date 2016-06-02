import scala.collection.mutable

/**
  * Created by vjivane on 5/7/16.
  */
class DeviceTracker extends Serializable{

  case class DeviceRecord (capacity: Int, ip: String)

  val deviceCapacityMap = new mutable.HashMap[String, DeviceRecord]()

  def insert(id: String, cap: Int, ip: String) = deviceCapacityMap.put(id,new DeviceRecord(cap, ip))
  def remove(id: String) = deviceCapacityMap.remove(id).get
  def isDeviceRegistered(id: String) = deviceCapacityMap.contains(id)
  def deviceCap (id: String)= deviceCapacityMap.get(id).get
  def countRegisteredDevices() = deviceCapacityMap.size
  def listRegisteredDevices() = deviceCapacityMap.keySet.toList

}
