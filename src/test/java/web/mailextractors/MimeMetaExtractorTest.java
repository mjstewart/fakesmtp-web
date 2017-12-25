package web.mailextractors;

import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.MimeMessageHelper;
import web.domain.MimeMessageMeta;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

public class MimeMetaExtractorTest {

    private final Session session = Session.getDefaultInstance(new Properties());

    private final String TEST_DOCUMENTS_PATH = "./test-data/";
    private final String TEST_EMAILS_PATH = "./test-data/eml/";

    private final String INLINE = "inline";
    private final String ATTACHMENT = "attachment";

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

//    @Test
//    public void setupTestDataFiles() throws Exception {
//        createTestFileWithAttachments();
//        createTestFileWithNoAttachments();
//        createTestFileWithPlainTextBodyAndNoAttachments();
//    }

    @Test
    public void parse_SetsAllFieldsCorrectly_WithAttachments() throws Exception {
        File withAttachments = new File(TEST_EMAILS_PATH + "with-attachments");
        FileInputStream input = new FileInputStream(withAttachments);
        MimeMessage message = new MimeMessage(session, input);
        input.close();

        // Expect all values that were used in createTestMessageWithAttachments.
        MimeMessageMeta meta = MimeMetaExtractor.parse(message);
        assertThat(meta.getEmailId()).isNotNull();
        assertThat(meta.getFrom()).containsExactlyInAnyOrder("test@email.com");
        assertThat(meta.getBody()).isEqualTo(new Body(
                "<html><body><h1>Hi there with attachments</h1></body></html>",
                new ContentType(MediaType.TEXT_HTML_VALUE, "UTF-8")
        ));
        assertThat(meta.getToRecipients()).containsExactlyInAnyOrder("person1@email.com", "person2@Email.com");
        assertThat(meta.getCcRecipients()).containsExactlyInAnyOrder("me1@email.com", "me2@email.com");
        assertThat(meta.getBccRecipients()).containsExactlyInAnyOrder("me3@email.com", "me4@email.com");
        assertThat(meta.getSubject()).isEqualTo("A simple test email");
        assertThat(meta.getSentDate()).isNotNull();
        assertThat(meta.getDescription()).isEqualTo("a message description");
        assertThat(meta.getAttachments()).isNotNull();
        assertThat(meta.getAttachments()).containsExactlyInAnyOrder(
                new MimeAttachment("house", INLINE,
                        new ContentType(MediaType.IMAGE_PNG_VALUE, null)),

                new MimeAttachment("styles", INLINE,
                        new ContentType("text/css", "us-ascii")),

                new MimeAttachment("list.txt", ATTACHMENT,
                        new ContentType(MediaType.TEXT_PLAIN_VALUE, "us-ascii")),

                new MimeAttachment("notes.txt", ATTACHMENT,
                        new ContentType(MediaType.TEXT_PLAIN_VALUE, "us-ascii"))
        );
    }

    @Test
    public void parse_SetsAllFieldsCorrectly_WithPlainTextBodyAndNoAttachments() throws Exception {
        File plainText = new File(TEST_EMAILS_PATH + "plain-text-with-no-attachments");
        FileInputStream input = new FileInputStream(plainText);
        MimeMessage message = new MimeMessage(session, input);
        input.close();

        // Expect all values that were used in createTestMessageWithPlainTextBodyAndNoAttachments.
        MimeMessageMeta meta = MimeMetaExtractor.parse(message);
        assertThat(meta.getEmailId()).isNotNull();
        assertThat(meta.getFrom()).containsExactlyInAnyOrder("test@email.com");
        assertThat(meta.getBody()).isEqualTo(new Body(
                "Hi there this is a plain text body",
                new ContentType(MediaType.TEXT_PLAIN_VALUE, "UTF-8")
        ));
        assertThat(meta.getToRecipients()).containsExactlyInAnyOrder("person1@email.com", "person2@Email.com");
        assertThat(meta.getCcRecipients()).isEmpty();
        assertThat(meta.getBccRecipients()).isEmpty();
        assertThat(meta.getSubject()).isEqualTo("A simple test email for a plain text body");
        assertThat(meta.getSentDate()).isNotNull();
        assertThat(meta.getDescription()).isNull();
        assertThat(meta.getAttachments()).isEmpty();
    }

    @Test
    public void parse_MimeMetaMessage_HasAllExpectedDefaultValues() {
        MimeMessageMeta meta = MimeMessageMeta.builder().create();

        assertThat(meta.getEmailId()).isNotNull();
        assertThat(meta.getSubject()).isNull();
        assertThat(meta.getFrom()).isEmpty();
        assertThat(meta.getReplyTo()).isEmpty();
        assertThat(meta.getBody()).isNull();
        assertThat(meta.getReceivedDate()).isNull();
        assertThat(meta.getSentDate()).isNull();
        assertThat(meta.getDescription()).isNull();
        assertThat(meta.getToRecipients()).isEmpty();
        assertThat(meta.getCcRecipients()).isEmpty();
        assertThat(meta.getBccRecipients()).isEmpty();
        assertThat(meta.getAttachments()).isEmpty();
    }

    @Test
    public void emailWithAttachments_FindsBodyAndAllAttachments() throws Exception {
        File withAttachments = new File(TEST_EMAILS_PATH + "with-attachments");
        FileInputStream input = new FileInputStream(withAttachments);
        MimeMessage message = new MimeMessage(session, input);
        input.close();

        Optional<BodyAttachment> result = MimeMetaExtractor.getAllAttachments(message);

        assertThat(result).isNotNull();
        assertThat(result.isPresent()).isTrue();
        assertThat(result.get().getBody().isPresent()).isTrue();
        assertThat(result.get().getBody().get()).isEqualTo(new Body(
                "<html><body><h1>Hi there with attachments</h1></body></html>",
                new ContentType(MediaType.TEXT_HTML_VALUE, "UTF-8")
        ));

        assertThat(result.get().getAttachments()).isNotNull();
        assertThat(result.get().getAttachments()).containsExactlyInAnyOrder(
                new MimeAttachment("house", INLINE,
                        new ContentType(MediaType.IMAGE_PNG_VALUE, null)),

                new MimeAttachment("styles", INLINE,
                        new ContentType("text/css", "us-ascii")),

                new MimeAttachment("list.txt", ATTACHMENT,
                        new ContentType(MediaType.TEXT_PLAIN_VALUE, "us-ascii")),

                new MimeAttachment("notes.txt", ATTACHMENT,
                        new ContentType(MediaType.TEXT_PLAIN_VALUE, "us-ascii"))
        );
    }

    @Test
    public void emailWithNoAttachments_NoAttachmentsFound() throws Exception {
        File withAttachments = new File(TEST_EMAILS_PATH + "with-no-attachments");
        FileInputStream input = new FileInputStream(withAttachments);
        MimeMessage message = new MimeMessage(session, input);
        input.close();

        Optional<BodyAttachment> result = MimeMetaExtractor.getAllAttachments(message);

        assertThat(result).isNotNull();
        assertThat(result.isPresent()).isTrue();
        assertThat(result.get().getBody().isPresent()).isTrue();
        assertThat(result.get().getBody().get()).isEqualTo(new Body(
                "<html><body><h1>Hi there with no attachments</h1></body></html>",
                new ContentType(MediaType.TEXT_HTML_VALUE, "UTF-8")
        ));

        assertThat(result.get().getAttachments()).isEmpty();
    }

    @Test
    public void isNotEmpty_IsTrueWhenNotEmpty() {
        Object[] numbers = {1, 2, 3};
        assertThat(MimeMetaExtractor.isNotEmpty(numbers)).isTrue();
    }

    @Test
    public void isNotEmpty_IsFalseWhenEmpty() {
        Object[] numbers = {};
        assertThat(MimeMetaExtractor.isNotEmpty(numbers)).isFalse();
    }

    @Test
    public void removeAngleBrackets() {
        assertThat(MimeMetaExtractor.removeAngleBrackets("")).isEqualTo("");
        assertThat(MimeMetaExtractor.removeAngleBrackets("hello")).isEqualTo("hello");
        assertThat(MimeMetaExtractor.removeAngleBrackets("<hello>")).isEqualTo("hello");
    }

    @Test
    public void parseContentType() {
        assertThat(MimeMetaExtractor.parseContentType("text/plain"))
                .isEqualTo(new ContentType("text/plain"));

        assertThat(MimeMetaExtractor.parseContentType("text/plain; charset=us-ascii"))
                .isEqualTo(new ContentType("text/plain", "us-ascii"));

        assertThat(MimeMetaExtractor.parseContentType("text/plain; charset=UTF-8; name=hello; type=important"))
                .isEqualTo(new ContentType("text/plain", "UTF-8"));

        assertThat(MimeMetaExtractor.parseContentType("text/plain; blah=us-ascii; name=hello; type=important"))
                .isEqualTo(new ContentType("text/plain"));

        // The method shouldn't worry about the media type being valid or not.
        assertThat(MimeMetaExtractor.parseContentType("blah"))
                .isEqualTo(new ContentType("blah"));

        assertThat(MimeMetaExtractor.parseContentType("")).isNull();
    }

    @Test
    public void toList() throws AddressException {
        Address[] addresses = {new InternetAddress("me@email.com"), new InternetAddress("you@email.com")};
        assertThat(MimeMetaExtractor.toList(addresses)).containsExactly("me@email.com", "you@email.com");
    }
}