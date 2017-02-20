import java.util.*;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class SendingEmail {
	private String to;
	private String from;
	private String username;
	private String password;
	private String host;

	public SendingEmail(String to, String from, String username, String password, String host) {
		this.to = to;
		this.from = from;
		this.username = username;
		this.password = password;
		this.host = host;
	}

	public void send() {
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.port", "587");
		Session session = Session.getInstance(props, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});
		
		try {
			   System.out.println("Inside Try");
			   // Create a default MimeMessage object.
			   Message message = new MimeMessage(session);
			
			   // Set From: header field of the header.
			   message.setFrom(new InternetAddress(from));
			
			   // Set To: header field of the header.
			   message.setRecipients(Message.RecipientType.TO,
		               InternetAddress.parse(to));
			
			   // Set Subject: header field
			   message.setSubject("Testing Subject");
			
			   // Now set the actual message
			   message.setText("Hello, this is sample for to check send " +
				"email using JavaMailAPI ");

			   System.out.println("Before Sending");
			   // Send message
			   Transport.send(message);
			   
			   System.out.println("Sent message successfully....");

		      } catch (MessagingException e) {
		         throw new RuntimeException(e);
		      }
	}

}
