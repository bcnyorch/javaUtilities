import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * 
 * @author jcastilla <bcnyorch@gmail.com> 
 * @version 0.1
 * @since 2016/02/08
 * 
 * Sends mails via SMTP auth TLS. 
 *
 */
public class SendMailTLS {
	
	final static String HOST = "ip_or_hostName";
	final static String PORT = "25";
	static Properties props;
	
	static {
		props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", HOST);
		props.put("mail.smtp.port", PORT);

	}
	
	public static void sendWithAttachement(String[] recipients, String subject, String body, String filename, File attachement) throws IOException {
		// TODO encrypt pass &| put in database
		final String from = "user";
		final String password = "password";

		Session session = Session.getInstance(props, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(from, password);
			}
		});
		session.setDebug(true);

		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(from));
			message.setSubject(subject);
			// WARNING: this does not notify sender in all cases
			message.addHeader("Disposition-Notification-To", from); 
			
			for (int i=0;i<recipients.length;i++){
				message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipients[i]));	
			}
			
			MimeBodyPart mimeBodyPart = new MimeBodyPart();
	        	mimeBodyPart.setText(body);
	        
	        	MimeBodyPart attachPart = new MimeBodyPart();
	        	attachPart.attachFile(attachement);
	        
	        	Multipart multipart = new MimeMultipart();
	        	multipart.addBodyPart(mimeBodyPart);
	        	multipart.addBodyPart(attachPart);
	        
	        	message.setContent(multipart);

			Transport.send(message);

			// TODO
			System.out.println("Done");
		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void send(String[] recipients, String subject, String body) throws IOException {
		// TODO encrypt pass &| put in database
		final String from = "user";
		final String password = "password";
		final String to = "destiny";

		Session session = Session.getInstance(props, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(from, password);
			}
		});
		session.setDebug(true);
		
		try {
			
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(from));
			message.setSubject(subject);
			message.setText(body);
			// WARNING: this does not notify sender in all cases
			message.addHeader("Disposition-Notification-To", from);

			for (int i=0;i<recipients.length;i++){
				message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipients[i]));	
			}
			
			Transport.send(message);
			
			// TODO
			System.out.println("Done");
		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}
	}
	
}
