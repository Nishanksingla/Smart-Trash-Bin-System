import java.io.File
import java.net.InetAddress

import com.maxmind.geoip2.DatabaseReader

object IP2Geo {

  val database = new File("GeoLite2-City.mmdb")
  val reader = new DatabaseReader.Builder(database).build()

  def getLocation(ip: String) ={
    val ipAddress = InetAddress.getByName(ip)
    val response = reader.city(ipAddress)
    response.getCountry.getName + "," + response.getCity.getName + "," + response.getPostal.getCode + "," + response.getLocation.getLatitude + "," +response.getLocation.getLongitude
  }
}
