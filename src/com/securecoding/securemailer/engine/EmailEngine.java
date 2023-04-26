package com.securecoding.securemailer.engine;

import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.mail.HtmlEmail;

public class EmailEngine {
	//취약점 3 : 전역으로 선언된 상수이 초기화가 안되어있음 
    private Properties props;
    private Session session;
    private Message message;
    private MimeMultipart multipart;
    private BodyPart messageBodyPart;
    
    public EmailEngine() {
        props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        // This mail has 2 part, the BODY and the embedded image
        multipart = new MimeMultipart("related");
        
        //취약점 3 솔루션 : 생성자에 전역으로 선언된 상수가 초기화
        message = null;
        
    }

    public EmailEngine setAuth(String email, String password) {
        session = Session.getInstance(props,
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(email, password);
                    }
                });

        // Create a default MimeMessage object.
        message = new MimeMessage(session);

        // Set From: header field of the header.
        try {
            message.setFrom(new InternetAddress(email));
        } catch (MessagingException e) {
            e.printStackTrace();
        }

        return this;
    }

    public EmailEngine setSubject(String subject) {
    	// 취약점 검증 2 : null 체크 누락
    	/*
    	if(subject != null) {
	        // Set Subject: header field
	        try {
				
	        	//취약점 4 : 이메일 html 방식의 값 설정
	        	//message.setSubject(subject);
				//신규 라이브러리 사용
	        } catch (MessagingException e) {
	            e.printStackTrace();
	        }
    	}
    	*/
    	HtmlEmail email = new HtmlEmail();
    	email.setSubject(subject);
        return this;
    }

    public EmailEngine setContent(String htmlContent) {
        // first part (the html)
        messageBodyPart = new MimeBodyPart();
        try {
            messageBodyPart.setContent(htmlContent, "text/html");
            // add it
            multipart.addBodyPart(messageBodyPart);
        } catch (MessagingException e) {
            e.printStackTrace();
        }

        return this;
    }

    public EmailEngine setHeaderImage(String path) {
        // second part (the image)
        messageBodyPart = new MimeBodyPart();
        DataSource fds = new FileDataSource(path);

        try {
            messageBodyPart.setDataHandler(new DataHandler(fds));
            messageBodyPart.setHeader("Content-ID", "<headerImage>");

            // add image to the multipart
            multipart.addBodyPart(messageBodyPart);
        } catch (MessagingException e) {
            e.printStackTrace();
        }

        return this;
    }

    public boolean send(String recipient) {
        try {
        	// Set To: header field of the header.
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));

            // put everything together
            message.setContent(multipart);

            // Send message
            Transport.send(message);
           return true;
        } 
        catch (MessagingException me) {
        	//취약점 1 솔루션- 메세지 관련 에러는 메세지 에러를 통해 범위 좁힘
        	return false;
        }
        catch (Exception e) {
        	//취약점 1 - 모든 예외에 대해 모두 처리하는 것
            return false;
        }
    }
}
