/**
  * Created by vjivane on 4/13/16.
  */
object EmailTest extends App {

  println("Start...")

  Email.initializeEmailSession()
  Email.sendEmail("vireshjivane5@gmail.com","Admin","SmartBin Alert - Bin #9 is about to get full [Currently at 85% of its capacity]!","This is an auto-generated message from SmartBin system. Thanks")
  println("Complete...")

}

//nishanksingla@gmail.com
