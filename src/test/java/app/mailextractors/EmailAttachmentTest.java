package app.mailextractors;

import org.junit.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

public class EmailAttachmentTest {
    @Test
    public void excludeIdComparator_handlesNullFields() {
        // Expect ordering to be by fileName, contentType.mediaType
        // nulls are stored last.
        Comparator<EmailAttachment> comp = EmailAttachment.excludeIdComparator();

        EmailAttachment a = new EmailAttachment("a", new ContentType("a"));
        EmailAttachment b = new EmailAttachment("a", new ContentType("a1"));
        EmailAttachment c = new EmailAttachment("a", new ContentType("b"));
        EmailAttachment f = new EmailAttachment("b", new ContentType("b2"));
        EmailAttachment g = new EmailAttachment("c", new ContentType("c2"));
        EmailAttachment h = new EmailAttachment("c", null);
        EmailAttachment i = new EmailAttachment(null, null);

        List<EmailAttachment> expect = new ArrayList<>(Arrays.asList(a, b, c, f, g, h, i));
        List<EmailAttachment> attachments = new ArrayList<>(Arrays.asList(a, b, c, f, g, h, i));

        Collections.shuffle(attachments);
        attachments.sort(comp);

        assertThat(attachments.get(0).getId()).isEqualTo(expect.get(0).getId());
        assertThat(attachments.get(1).getId()).isEqualTo(expect.get(1).getId());
        assertThat(attachments.get(2).getId()).isEqualTo(expect.get(2).getId());
        assertThat(attachments.get(3).getId()).isEqualTo(expect.get(3).getId());
        assertThat(attachments.get(4).getId()).isEqualTo(expect.get(4).getId());
        assertThat(attachments.get(5).getId()).isEqualTo(expect.get(5).getId());
        assertThat(attachments.get(6).getId()).isEqualTo(expect.get(6).getId());
    }
}