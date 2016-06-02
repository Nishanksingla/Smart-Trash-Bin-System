import java.io.FileInputStream
import java.util.Properties
import javax.mail._
import javax.mail.internet.{AddressException, InternetAddress, MimeMessage}


object Email {

  var host: Option[String] = None
  var username: Option[String] = None
  var password: Option[String] = None
  var session: Option[Session] = None

  def initializeEmailSession(): Unit = {
    loadClientProperties()
    val props = new Properties()
    props.put("mail.smtp.starttls.enable", "true")
    props.put("mail.smtp.host", host.get)
    props.put("mail.smtp.port", "587")
    props.put("mail.smtp.auth", "true")
    props.put("mail.debug", "true")
    session = Some(Session.getInstance(props, getPasswordAuthentication(username.get, password.get)))
  }

  def getPasswordAuthentication(uname: String, psw: String): Authenticator = {
    new Authenticator() {
      override def getPasswordAuthentication: PasswordAuthentication = {
        new PasswordAuthentication(uname, psw)
      }
    }
  }

  def loadClientProperties(): Unit = {
    val properties = new Properties()
    properties.load(new FileInputStream("client.properties"))
    host = Some(properties.getProperty("email.host"))
    username = Some(properties.getProperty("email.username"))
    password = Some(properties.getProperty("email.password"))
  }

  def sendEmail(userEmailAddress: String, userName: String, subject: String, msgBody: String) = {
    try {
      val msg = new MimeMessage(session.get)
      msg.setFrom(new InternetAddress("System", "SmartBin"))
      msg.addRecipient(Message.RecipientType.TO, new InternetAddress(userEmailAddress, userName))
      msg.setSubject(subject)
      msg.setText(msgBody)
      Transport.send(msg)
    } catch {
      case e: AddressException => println(e.getMessage)
    }
  }
}
