import java.util

import com.twilio.sdk._
import org.apache.http.NameValuePair
import org.apache.http.message.BasicNameValuePair

import scala.collection.mutable.ListBuffer


object Twilio {

  val account = "AC034a435547f36a73cafb5f48488b90b6"
  val auth = "33dfa8abe6dcc29b668c2c82e2c9770f"

  //  val account = "AC6cb87d8cec26189a854e544a61650b5a"
  //  val auth = "d7f97795334af9a35d8cbee3858cb1e7"

  val client = new TwilioRestClient(account, auth);


  def sendSMS(text: String): Unit = {

    //    val shortCode = client.getAccount().getShortCode(account);
    //    System.out.println(shortCode.getShortCode);

    val params = new util.ArrayList[NameValuePair]()
    params.add(new BasicNameValuePair("To", "+16692268165"))
    params.add(new BasicNameValuePair("From", "+14086281781"))
    params.add(new BasicNameValuePair("Body", text))
    val messageFactory = client.getAccount().getMessageFactory
    val message = messageFactory.create(params)
    System.out.println(message.getSid)

    //
    //    val params = new util.HashMap[String, String]()
    //    params.put("AreaCode", "408")
    //
    //    val numbers: AvailablePhoneNumberList = client.getAccount().getAvailablePhoneNumbers(params, "US", "Local")
    //    val list = numbers.getPageData
    //
    //    val itr = list.iterator()
    //
    //    while (itr.hasNext) {
    //      println(itr.next().getPhoneNumber)
    //    }
    //
    //    val purchaseParams = new util.ArrayList[NameValuePair]();
    //    purchaseParams.add(new BasicNameValuePair("PhoneNumber", list.get(0).getPhoneNumber()));
    //    client.getAccount().getIncomingPhoneNumberFactory().create(purchaseParams);
    //  }

  }


  def receiveSMS() = {
    val messages = client.getAccount().getMessages
    val list = new ListBuffer[String]
    val msg = messages.iterator()
    while (msg.hasNext) {
      val m = msg.next()
      val text = m.getBody
      if (text.contains("Bin#") && (text.contains("full") || text.contains("Full") || text.contains("FULL"))) {
        list.append(text.split("#")(1).split(" ")(0).toInt + "," + m.getFrom + "," + m.getDateSent  )
        m.delete()
      }else
      m.delete()
    }
  list
  }

}