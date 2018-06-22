package utility;

import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

public class Email {

   public  static boolean send(String email, String subject, String text) {
      String to = email;
      String host = "smtp.yandex.ru";

      Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.yandex.ru");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");
      Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("info@zavrus.com", "DU21071969");
            }
        });
      try {
          Message message = new MimeMessage(session);
          message.setFrom(new InternetAddress("info@zavrus.com"));
          message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
          message.setSubject(subject);
          message.setText(text);
          Transport.send(message);
          return true;
      } catch (MessagingException mex) {
         mex.printStackTrace();
         return false;
      }
  }
}
