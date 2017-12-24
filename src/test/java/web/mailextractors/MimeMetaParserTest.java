package web.mailextractors;

import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.Address;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.*;

public class MimeMetaParserTest {

    private final Session session = Session.getDefaultInstance(new Properties());

    private final String TEST_DOCUMENTS_PATH = "./test-data/";
    private final String TEST_EMAILS_PATH = "./test-data/eml/";

    private final String INLINE = "inline";
    private final String ATTACHMENT = "attachment";
    private final String CHARSET = "us-ascii";

    private void createTestFileWithAttachments() throws Exception {
        MimeMessage message = new MimeMessage(session);
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        String body = "<html><body><h1>Hi there</h1></body></html>";
        helper.setFrom("test@email.com");
        helper.setTo("person@email.com");
        helper.setCc(new String[]{"me1@email.com", "me2@email.com"});
        helper.setBcc(new String[]{"me3@email.com", "me4@email.com"});
        helper.setSubject("A simple test email");
        helper.setText(body, true);
        helper.addInline("house", new File(TEST_DOCUMENTS_PATH + "house.png"));
        helper.addInline("styles", new File(TEST_DOCUMENTS_PATH + "styles.css"));
        helper.addAttachment("list.txt", new File(TEST_DOCUMENTS_PATH + "list.txt"));
        helper.addAttachment("notes.txt", new File(TEST_DOCUMENTS_PATH + "notes.txt"));
        message.setDescription("a message description");

        File mail = new File(TEST_EMAILS_PATH + "with-attachments");
        FileOutputStream out = new FileOutputStream(mail);
        message.writeTo(out);
        out.close();
    }

    private void createTestFileWithNoAttachments() throws Exception {
        MimeMessage message = new MimeMessage(session);
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        String body = "<html><body><h1>Hi there</h1></body></html>";
        helper.setFrom("test@email.com");
        helper.setTo("person@email.com");
        helper.setCc(new String[]{"me1@email.com", "me2@email.com"});
        helper.setBcc(new String[]{"me3@email.com", "me4@email.com"});
        helper.setSubject("A simple test email");
        helper.setText(body, true);

        File mail = new File(TEST_EMAILS_PATH + "with-no-attachments");
        FileOutputStream out = new FileOutputStream(mail);
        message.writeTo(out);
        out.close();
    }


    @Test
    public void emailWithAttachments_FindsAllAttachments() throws Exception {
        File withAttachments = new File(TEST_EMAILS_PATH + "with-attachments");
        MimeMessage message = new MimeMessage(session, new FileInputStream(withAttachments));

        List<MimeAttachment> attachments = MimeMetaParser.getAllAttachments(message);
        assertThat(attachments).containsExactlyInAnyOrder(
                new MimeAttachment("house", INLINE,
                        new MimeAttachment.ContentType(MediaType.IMAGE_PNG_VALUE, null)),

                new MimeAttachment("styles", INLINE,
                        new MimeAttachment.ContentType("text/css", CHARSET)),

                new MimeAttachment("list.txt", ATTACHMENT,
                        new MimeAttachment.ContentType(MediaType.TEXT_PLAIN_VALUE, CHARSET)),

                new MimeAttachment("notes.txt", ATTACHMENT,
                        new MimeAttachment.ContentType(MediaType.TEXT_PLAIN_VALUE, CHARSET))
                );
    }

    @Test
    public void emailWithNoAttachments_NoAttachmentsFound() throws Exception {
        createTestFileWithNoAttachments();
        File withAttachments = new File(TEST_EMAILS_PATH + "with-no-attachments");
        MimeMessage message = new MimeMessage(session, new FileInputStream(withAttachments));

        List<MimeAttachment> attachments = MimeMetaParser.getAllAttachments(message);
        assertThat(attachments).isEmpty();
    }


    @Test
    public void isNotEmpty_IsTrueWhenNotEmpty() {
        Object[] numbers = {1, 2, 3};
        assertThat(MimeMetaParser.isNotEmpty(numbers)).isTrue();
    }

    @Test
    public void isNotEmpty_IsFalseWhenEmpty() {
        Object[] numbers = {};
        assertThat(MimeMetaParser.isNotEmpty(numbers)).isFalse();
    }

    @Test
    public void removeAngleBrackets() {
        assertThat(MimeMetaParser.removeAngleBrackets("")).isEqualTo("");
        assertThat(MimeMetaParser.removeAngleBrackets("hello")).isEqualTo("hello");
        assertThat(MimeMetaParser.removeAngleBrackets("<hello>")).isEqualTo("hello");
    }

    @Test
    public void parseContentType() {
        assertThat(MimeMetaParser.parseContentType("text/plain"))
                .isEqualTo(new MimeAttachment.ContentType("text/plain"));

        assertThat(MimeMetaParser.parseContentType("text/plain; charset=us-ascii"))
                .isEqualTo(new MimeAttachment.ContentType("text/plain", "us-ascii"));

        assertThat(MimeMetaParser.parseContentType("text/plain; charset=us-ascii; name=hello; type=important"))
                .isEqualTo(new MimeAttachment.ContentType("text/plain", "us-ascii"));

        assertThat(MimeMetaParser.parseContentType("text/plain; blah=us-ascii; name=hello; type=important"))
                .isEqualTo(new MimeAttachment.ContentType("text/plain"));

        // The method shouldn't worry about the media type being valid or not.
        assertThat(MimeMetaParser.parseContentType("blah"))
                .isEqualTo(new MimeAttachment.ContentType("blah"));

        assertThat(MimeMetaParser.parseContentType("")).isNull();
    }

    @Test
    public void toList() throws AddressException {
        Address[] addresses = {new InternetAddress("me@email.com"), new InternetAddress("you@email.com")};
        assertThat(MimeMetaParser.toList(addresses)).containsExactly("me@email.com", "you@email.com");
    }
}