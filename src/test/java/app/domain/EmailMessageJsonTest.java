package app.domain;

import app.configuration.jackson.JacksonConfiguration;
import app.mailextractors.Body;
import app.mailextractors.ContentType;
import app.mailextractors.EmailAttachment;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@Import(JacksonConfiguration.class)
@JsonTest
public class EmailMessageJsonTest {

    @Autowired
    private JacksonTester<EmailMessage> jsonTester;

    @Test
    public void testSerialize() throws Exception {
        // dates are expected to be serialized to ISO-8601 format
        Date sentDate = new GregorianCalendar(2017, Calendar.JANUARY, 1, 9, 30, 5).getTime();

        String body = "<html><body><h1>Hi there with attachments</h1></body></html>";
        EmailMessage message = EmailMessage.builder()
                .id(UUID.fromString("ff92e909-aafd-4ee2-affe-ecf631efe582"))
                .fromWho(new HashSet<>(Arrays.asList("test@email.com")))
                .replyTo(new HashSet<>(Arrays.asList("test@email.com")))
                .subject("a test email subject")
                .toRecipients(new HashSet<>(Arrays.asList("person1@email.com", "person2@email.com")))
                .ccRecipients(new HashSet<>(Arrays.asList("person3@email.com", "person4@email.com")))
                .bccRecipients(new HashSet<>())
                .description("a test email description")
                .sentDate(sentDate)
                .body(new Body(body, new ContentType(MediaType.TEXT_HTML_VALUE)))
                .attachments(new HashSet<>(Arrays.asList(
                        new EmailAttachment(UUID.fromString("ff92e909-aafd-4ee2-affe-ecf631efe100"), "style",
                                new ContentType("text/css")),
                        new EmailAttachment(UUID.fromString("ff92e909-aafd-4ee2-affe-ecf631efe101"), "house",
                                new ContentType(MediaType.IMAGE_JPEG_VALUE)),
                        new EmailAttachment(UUID.fromString("ff92e909-aafd-4ee2-affe-ecf631efe102"), "notes.txt",
                                new ContentType(MediaType.TEXT_PLAIN_VALUE)),
                        new EmailAttachment(UUID.fromString("ff92e909-aafd-4ee2-affe-ecf631efe103"), "sales.pdf",
                                new ContentType(MediaType.APPLICATION_PDF_VALUE))
                )))
                .create();

        JsonContent<EmailMessage> json = jsonTester.write(message);
        assertThat(json).isEqualToJson(new ClassPathResource("EmailMessageExpectedJson.json"), JSONCompareMode.NON_EXTENSIBLE);
    }
}