/**
  * Created by vjivane on 5/8/16.
  */
object TwilioTest extends App{

//  Twilio.receiveSMS().foreach(println)


  val array = IP2Geo.getLocation("50.112.165.205").split(",")

  println("\n\n\n\nBin#1 is located at:")
  println("City: " + array(1))
  println("Area Code: " + array(2))
  println("Country: " + array(0))
  println("Latitude: " + array(3))
  println("Longitude: " + array(4) +"\n\n\n\n\n\n\n\n")

}
