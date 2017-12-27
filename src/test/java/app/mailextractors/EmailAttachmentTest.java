package app.mailextractors;

import org.junit.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

public class EmailAttachmentTest {
    @Test
    public void excludeIdComparator_handlesNullFields() {
        // Expect ordering to be by fileName, disposition, contentType.mediaType then contentType.charset.
        // nulls are stored last.
        Comparator<EmailAttachment> comp = EmailAttachment.excludeIdComparator();

        EmailAttachment a = new EmailAttachment("a", "a", new ContentType("a", "a"));
        EmailAttachment b = new EmailAttachment("a", "a", new ContentType("a", "b"));
        EmailAttachment c = new EmailAttachment("a", "a", new ContentType("b", "b"));
        EmailAttachment d = new EmailAttachment("a", "b", new ContentType("b", "b"));
        EmailAttachment e = new EmailAttachment("b", "b", new ContentType("b", "b"));
        EmailAttachment f = new EmailAttachment("c", "b", new ContentType("b", "b"));
        EmailAttachment g = new EmailAttachment("c", "b", null);
        EmailAttachment h = new EmailAttachment("c", null, null);
        EmailAttachment i = new EmailAttachment(null, null, null);

        List<EmailAttachment> expect = new ArrayList<>(Arrays.asList(a, b, c, d, e, f, g, h, i));
        List<EmailAttachment> attachments = new ArrayList<>(Arrays.asList(a, b, c, d, e, f, g, h, i));

        Collections.shuffle(attachments);
        attachments.sort(comp);

        assertThat(attachments.get(0).getId()).isEqualTo(expect.get(0).getId());
        assertThat(attachments.get(1).getId()).isEqualTo(expect.get(1).getId());
        assertThat(attachments.get(2).getId()).isEqualTo(expect.get(2).getId());
        assertThat(attachments.get(3).getId()).isEqualTo(expect.get(3).getId());
        assertThat(attachments.get(4).getId()).isEqualTo(expect.get(4).getId());
        assertThat(attachments.get(5).getId()).isEqualTo(expect.get(5).getId());
        assertThat(attachments.get(6).getId()).isEqualTo(expect.get(6).getId());
        assertThat(attachments.get(7).getId()).isEqualTo(expect.get(7).getId());
        assertThat(attachments.get(8).getId()).isEqualTo(expect.get(8).getId());
    }
}