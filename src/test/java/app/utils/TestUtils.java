package app.utils;

import app.domain.EmailMessage;
import app.mailextractors.EmailExtractor;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class TestUtils {
    public static EmailMessage createTestEmailOne() throws MessagingException {
        MimeMessage message = new MimeMessage(Session.getDefaultInstance(new Properties()));
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom("test100@email.com");
        helper.setTo("you@email.com");
        helper.setSubject("test 1 email subject");
        helper.setCc("person3@email.com");
        helper.setText("some body text for email 1", false);
        return EmailExtractor.parse(message);
    }

    public static EmailMessage createTestEmailTwo() throws MessagingException {
        MimeMessage message = new MimeMessage(Session.getDefaultInstance(new Properties()));
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom("test200@email.com");
        helper.setTo("person@email.com");
        helper.setSubject("test 2 email subject");
        helper.setCc("person12@email.com");
        helper.setBcc("person33@email.com");
        helper.setText("some body text for another email 2", false);
        return EmailExtractor.parse(message);
    }

    public static EmailMessage createTestEmailThree() throws MessagingException {
        MimeMessage message = new MimeMessage(Session.getDefaultInstance(new Properties()));
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom("test300@email.com");
        helper.setTo("person33@email.com");
        helper.setSubject("test 3 email subject");
        helper.setCc("person894@email.com");
        helper.setBcc("person911@email.com");
        helper.setText("some body text for another email 3", false);
        return EmailExtractor.parse(message);
    }
}
