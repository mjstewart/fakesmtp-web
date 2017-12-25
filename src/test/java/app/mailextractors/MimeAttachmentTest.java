package app.mailextractors;

import org.junit.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

public class MimeAttachmentTest {
    @Test
    public void excludeIdComparator_handlesNullFields() {
        // Expect ordering to be by fileName, disposition, contentType.mediaType then contentType.charset.
        // nulls are stored last.
        Comparator<MimeAttachment> comp = MimeAttachment.excludeIdComparator();

        MimeAttachment a = new MimeAttachment("a", "a", new ContentType("a", "a"));
        MimeAttachment b = new MimeAttachment("a", "a", new ContentType("a", "b"));
        MimeAttachment c = new MimeAttachment("a", "a", new ContentType("b", "b"));
        MimeAttachment d = new MimeAttachment("a", "b", new ContentType("b", "b"));
        MimeAttachment e = new MimeAttachment("b", "b", new ContentType("b", "b"));
        MimeAttachment f = new MimeAttachment("c", "b", new ContentType("b", "b"));
        MimeAttachment g = new MimeAttachment("c", "b", null);
        MimeAttachment h = new MimeAttachment("c", null, null);
        MimeAttachment i = new MimeAttachment(null, null, null);

        List<MimeAttachment> expect = new ArrayList<>(Arrays.asList(a, b, c, d, e, f, g, h, i));
        List<MimeAttachment> attachments = new ArrayList<>(Arrays.asList(a, b, c, d, e, f, g, h, i));

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