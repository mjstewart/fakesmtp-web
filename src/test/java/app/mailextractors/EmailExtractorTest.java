package app.mailextractors;

import app.domain.EmailMessage;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

public class EmailExtractorTest {

    private final Session session = Session.getDefaultInstance(new Properties());

    private final String TEST_DOCUMENTS_PATH = "./test-data/";
    private final String TEST_EMAILS_PATH = "./test-data/eml/";

    private Message createTestMessageWithAttachments() throws Exception {
        MimeMessage message = new MimeMessage(session);
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        String body = "<html><body><h1>Hi there with attachments</h1></body></html>";
        helper.setFrom("test@email.com");
        helper.setTo(new String[]{"person1@email.com", "person2@Email.com"});
        helper.setCc(new String[]{"me1@email.com", "me2@email.com"});
        helper.setBcc(new String[]{"me3@email.com", "me4@email.com"});
        helper.setSubject("A simple test email");
        helper.setText(body, true);
        helper.addInline("house", new File(TEST_DOCUMENTS_PATH + "house.png"));
        helper.addInline("styles", new File(TEST_DOCUMENTS_PATH + "styles.css"));
        helper.addAttachment("list.txt", new File(TEST_DOCUMENTS_PATH + "list.txt"));
        helper.addAttachment("notes.txt", new File(TEST_DOCUMENTS_PATH + "notes.txt"));
        message.setDescription("a message description");
        return message;
    }

    private Message createTestMessageWithNoAttachments() throws Exception {
        MimeMessage message = new MimeMessage(session);
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        String body = "<html><body><h1>Hi there with no attachments</h1></body></html>";
        helper.setFrom("test@email.com");
        helper.setTo(new String[]{"person1@email.com", "person2@Email.com"});
        helper.setCc(new String[]{"me1@email.com", "me2@email.com"});
        helper.setBcc(new String[]{"me3@email.com", "me4@email.com"});
        helper.setSubject("A simple test email");
        helper.setText(body, true);

        return message;
    }

    private Message createTestMessageWithPlainTextBodyAndNoAttachments() throws Exception {
        MimeMessage message = new MimeMessage(session);
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        String body = "Hi there this is a plain text body";
        helper.setFrom("test@email.com");
        helper.setTo(new String[]{"person1@email.com", "person2@Email.com"});
        helper.setSubject("A simple test email for a plain text body");
        helper.setText(body);

        return message;
    }

    private void createTestFileWithAttachments() throws Exception {
        File mail = new File(TEST_EMAILS_PATH + "with-attachments");
        FileOutputStream out = new FileOutputStream(mail);
        createTestMessageWithAttachments().writeTo(out);
        out.close();
    }

    private void createTestFileWithNoAttachments() throws Exception {
        File mail = new File(TEST_EMAILS_PATH + "with-no-attachments");
        FileOutputStream out = new FileOutputStream(mail);
        createTestMessageWithNoAttachments().writeTo(out);
        out.close();
    }

    private void createTestFileWithPlainTextBodyAndNoAttachments() throws Exception {
        File mail = new File(TEST_EMAILS_PATH + "plain-text-with-no-attachments");
        FileOutputStream out = new FileOutputStream(mail);
        createTestMessageWithPlainTextBodyAndNoAttachments().writeTo(out);
        out.close();
    }

    @Test
    public void getAllAttachments_EmailWithAttachments_FindsBodyAndAllAttachments() throws Exception {
        File withAttachments = new File(TEST_EMAILS_PATH + "with-attachments");
        FileInputStream input = new FileInputStream(withAttachments);
        MimeMessage message = new MimeMessage(session, input);
        input.close();

        EmailMessage result = EmailExtractor.parse(message);

        assertThat(result).isNotNull();
        assertThat(result.getBody()).isEqualTo(new Body(
                "<html><body><h1>Hi there with attachments</h1></body></html>",
                new ContentType(MediaType.TEXT_HTML_VALUE)
        ));

        assertThat(result.getAttachments())
                .usingElementComparator(EmailAttachment.excludeIdComparator())
                .containsExactlyInAnyOrder(
                        new EmailAttachment(null, new ContentType(MediaType.IMAGE_PNG_VALUE)),

                        new EmailAttachment(null,
                                new ContentType("text/css")),

                        new EmailAttachment("list.txt",
                                new ContentType(MediaType.TEXT_PLAIN_VALUE)),

                        new EmailAttachment("notes.txt",
                                new ContentType(MediaType.TEXT_PLAIN_VALUE))
                );
    }

    @Test
    public void getAllAttachments_EmailWithNoAttachments_NoAttachmentsFound() throws Exception {
        File withAttachments = new File(TEST_EMAILS_PATH + "with-no-attachments");
        FileInputStream input = new FileInputStream(withAttachments);
        MimeMessage message = new MimeMessage(session, input);
        input.close();

        EmailMessage result = EmailExtractor.parse(message);

        assertThat(result).isNotNull();
        assertThat(result.getBody()).isEqualTo(new Body(
                "<html><body><h1>Hi there with no attachments</h1></body></html>",
                new ContentType(MediaType.TEXT_HTML_VALUE)
        ));

        assertThat(result.getAttachments()).isEmpty();
    }

    @Test
    public void parse_SetsAllFieldsCorrectly_WithAttachments() throws Exception {
        File withAttachments = new File(TEST_EMAILS_PATH + "with-attachments");
        FileInputStream input = new FileInputStream(withAttachments);
        MimeMessage message = new MimeMessage(session, input);
        input.close();

        // Expect all values that were used in createTestMessageWithAttachments.
        EmailMessage meta = EmailExtractor.parse(message);
        assertThat(meta.getId()).isNotNull();
        assertThat(meta.getFromWho()).containsExactlyInAnyOrder("test@email.com");
        assertThat(meta.getBody()).isEqualTo(new Body(
                "<html><body><h1>Hi there with attachments</h1></body></html>",
                new ContentType(MediaType.TEXT_HTML_VALUE)
        ));
        assertThat(meta.getToRecipients()).containsExactlyInAnyOrder("person1@email.com", "person2@Email.com");
        assertThat(meta.getCcRecipients()).containsExactlyInAnyOrder("me1@email.com", "me2@email.com");
        assertThat(meta.getBccRecipients()).containsExactlyInAnyOrder("me3@email.com", "me4@email.com");
        assertThat(meta.getSubject()).isEqualTo("A simple test email");
        assertThat(meta.getSentDate()).isNotNull();
        assertThat(meta.getDescription()).isEqualTo("a message description");
        assertThat(meta.getAttachments()).isNotNull();
        assertThat(meta.isRead()).isFalse();
        assertThat(meta.getAttachments())
                .usingElementComparator(EmailAttachment.excludeIdComparator())
                .containsExactlyInAnyOrder(
                        new EmailAttachment(null,
                                new ContentType(MediaType.IMAGE_PNG_VALUE)),

                        new EmailAttachment(null,
                                new ContentType("text/css")),

                        new EmailAttachment("list.txt",
                                new ContentType(MediaType.TEXT_PLAIN_VALUE)),

                        new EmailAttachment("notes.txt",
                                new ContentType(MediaType.TEXT_PLAIN_VALUE))
                );
    }

    @Test
    public void parse_SetsAllFieldsCorrectly_WithPlainTextBodyAndNoAttachments() throws Exception {
        File plainText = new File(TEST_EMAILS_PATH + "plain-text-with-no-attachments");
        FileInputStream input = new FileInputStream(plainText);
        MimeMessage message = new MimeMessage(session, input);
        input.close();

        // Expect all values that were used in createTestMessageWithPlainTextBodyAndNoAttachments.
        EmailMessage meta = EmailExtractor.parse(message);
        assertThat(meta.getId()).isNotNull();
        assertThat(meta.getFromWho()).containsExactlyInAnyOrder("test@email.com");
        assertThat(meta.getBody()).isEqualTo(new Body(
                "Hi there this is a plain text body",
                new ContentType(MediaType.TEXT_PLAIN_VALUE)
        ));
        assertThat(meta.getToRecipients()).containsExactlyInAnyOrder("person1@email.com", "person2@Email.com");
        assertThat(meta.getCcRecipients()).isEmpty();
        assertThat(meta.getBccRecipients()).isEmpty();
        assertThat(meta.getSubject()).isEqualTo("A simple test email for a plain text body");
        assertThat(meta.getSentDate()).isNotNull();
        assertThat(meta.getDescription()).isNull();
        assertThat(meta.getAttachments()).isEmpty();
        assertThat(meta.isRead()).isFalse();
    }

    @Test
    public void parse_MimeMetaMessage_HasAllExpectedDefaultValues() {
        EmailMessage meta = EmailMessage.builder().create();

        assertThat(meta.getId()).isNotNull();
        assertThat(meta.getSubject()).isNull();
        assertThat(meta.getFromWho()).isEmpty();
        assertThat(meta.getReplyTo()).isEmpty();
        assertThat(meta.getBody()).isNull();
        assertThat(meta.getReceivedDate()).isNull();
        assertThat(meta.getSentDate()).isNotNull();
        assertThat(meta.getDescription()).isNull();
        assertThat(meta.getToRecipients()).isEmpty();
        assertThat(meta.getCcRecipients()).isEmpty();
        assertThat(meta.getBccRecipients()).isEmpty();
        assertThat(meta.getAttachments()).isEmpty();
        assertThat(meta.isRead()).isFalse();
    }

    @Test
    public void parse_fakeSMTPGeneratedNoAttachments_IsValid() throws Exception {
        File withNoAttachments = new File(TEST_EMAILS_PATH + "fakeSMTP-generated-no-attachments.eml");
        FileInputStream input = new FileInputStream(withNoAttachments);
        MimeMessage message = new MimeMessage(session, input);
        input.close();

        // Expect all values that are in fakeSMTP-generated-no-attachments.eml
        EmailMessage meta = EmailExtractor.parse(message);

        assertThat(meta.getBody()).isEqualTo(new Body(
                "<!DOCTYPE html>\n" +
                        "<html lang=\"en\" xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
                        "<head>\n" +
                        "\n" +
                        "    <meta charset=\"UTF-8\"/>\n" +
                        "    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\"/>\n" +
                        "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"/>\n" +
                        "    <link rel=\"stylesheet\" href=\"cid:styles\">\n" +
                        "</head>\n" +
                        "<body>\n" +
                        "<div class=\"ui container\">\n" +
                        "    <div>\n" +
                        "    <h1 class=\"ui dividing header\">Account verification</h1>\n" +
                        "    <p>Hey there paul smith!,</p>\n" +
                        "    <p>Thanks for signing up, please click the below link to activate your account</p>\n" +
                        "    <a href=\"localhost:8080/register/verify?token=b7ba6cd0-cdcf-4189-8dbd-85e7fb5d785c\">Activate</a>\n" +
                        "    <p>Hope to catch your soon</p>\n" +
                        "    <p>Warehouse management system</p>\n" +
                        "</div>\n" +
                        "</div>\n" +
                        "</body>\n" +
                        "</html>",
                new ContentType(MediaType.TEXT_HTML_VALUE)
        ));

        assertThat(meta.getId()).isNotNull();
        assertThat(meta.getFromWho()).containsExactlyInAnyOrder("no-reply@user-registration.com");
        assertThat(meta.getToRecipients()).containsExactlyInAnyOrder("user535@email.com");
        assertThat(meta.getCcRecipients()).isEmpty();
        assertThat(meta.getBccRecipients()).isEmpty();
        assertThat(meta.getSubject()).isEqualTo("Warehouse manager - Activate new account");
        assertThat(meta.getSentDate()).isNotNull();
        assertThat(meta.getDescription()).isNull();
        assertThat(meta.getAttachments()).isEmpty();
        assertThat(meta.isRead()).isFalse();
    }

    @Test
    public void parse_fakeSMTPGeneratedWithAttachments_IsValid() throws Exception {
        File withAttachments = new File(TEST_EMAILS_PATH + "fakeSMTP-generated-with-attachments.eml");
        FileInputStream input = new FileInputStream(withAttachments);
        MimeMessage message = new MimeMessage(session, input);
        input.close();

        // Expect all values that are in fakeSMTP-generated-with-attachments.eml
        EmailMessage meta = EmailExtractor.parse(message);

        assertThat(meta.getBody()).isEqualTo(new Body(
                "<!DOCTYPE html>\n" +
                        "<html lang=\"en\" xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
                        "<head>\n" +
                        "\n" +
                        "    <meta charset=\"UTF-8\"/>\n" +
                        "    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\"/>\n" +
                        "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"/>\n" +
                        "    <link rel=\"stylesheet\" href=\"cid:styles\">\n" +
                        "</head>\n" +
                        "<body>\n" +
                        "<div class=\"ui container\">\n" +
                        "    <div>\n" +
                        "    <h1 class=\"ui dividing header\">Account verification</h1>\n" +
                        "    <p>Hey there paul rogers!,</p>\n" +
                        "    <p>Thanks for signing up, please click the below link to activate your account</p>\n" +
                        "    <a href=\"localhost:8080/register/verify?token=1a0c920c-1eb6-4642-a982-aa8f60b78401\">Activate</a>\n" +
                        "    <p>Hope to catch your soon</p>\n" +
                        "    <p>Warehouse management system</p>\n" +
                        "</div>\n" +
                        "</div>\n" +
                        "</body>\n" +
                        "</html>",
                new ContentType(MediaType.TEXT_HTML_VALUE)
        ));

        assertThat(meta.getId()).isNotNull();
        assertThat(meta.getFromWho()).containsExactlyInAnyOrder("no-reply@user-registration.com");
        assertThat(meta.getToRecipients()).containsExactlyInAnyOrder("user1011@email.com");
        assertThat(meta.getCcRecipients()).isEmpty();
        assertThat(meta.getBccRecipients()).isEmpty();
        assertThat(meta.getSubject()).isEqualTo("Warehouse manager - Activate new account");
        assertThat(meta.getSentDate()).isNotNull();
        assertThat(meta.getDescription()).isNull();
        assertThat(meta.isRead()).isFalse();
        assertThat(meta.getAttachments())
                .usingElementComparator(EmailAttachment.excludeIdComparator())
                .containsExactlyInAnyOrder(
                        new EmailAttachment(null,
                                new ContentType("text/css")),

                        new EmailAttachment("styles.css",
                                new ContentType("text/css")),

                        new EmailAttachment("notes.txt",
                                new ContentType(MediaType.TEXT_PLAIN_VALUE))
                );
    }

    @Test
    public void toList() throws AddressException {
        Address[] addresses = {new InternetAddress("me@email.com"), new InternetAddress("you@email.com")};
        assertThat(EmailExtractor.toSet(addresses)).containsExactlyInAnyOrder("me@email.com", "you@email.com");
    }
}