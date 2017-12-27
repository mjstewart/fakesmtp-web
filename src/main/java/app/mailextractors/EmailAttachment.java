package app.mailextractors;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;

@Entity
public class EmailAttachment {
    @Id
    private UUID id;
    
    private String fileName;
    private String disposition;
    private ContentType contentType;

    public EmailAttachment() {}

    public EmailAttachment(String fileName, String disposition, ContentType contentType) {
        this(UUID.randomUUID(), fileName, disposition, contentType);
    }
    
    public EmailAttachment(UUID id, String fileName, String disposition, ContentType contentType) {
        this.id = id;
        this.fileName = fileName;
        this.disposition = disposition;
        this.contentType = contentType;
    }

    public UUID getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    public String getDisposition() {
        return disposition;
    }

    public ContentType getContentType() {
        return contentType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmailAttachment that = (EmailAttachment) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Compare all fields excluding {@code id, attachmentId}. Useful for testing when the
     * attachmentId is not known.
     *
     * <p>By fileName, disposition, contentType.mediaType then contentType.charset.
     * nulls are stored last.</p>
     */
    public static Comparator<EmailAttachment> excludeIdComparator() {
        Comparator<String> compareSafeString = Comparator.nullsLast(String::compareTo);

        Comparator<EmailAttachment> contentTypeComparator =
                Comparator.comparing(EmailAttachment::getContentType, Comparator.nullsLast(
                        Comparator.comparing(ContentType::getMediaType, compareSafeString)
                                .thenComparing(ContentType::getCharset, compareSafeString)
                ));

        return Comparator
                .comparing(EmailAttachment::getFileName, compareSafeString)
                .thenComparing(EmailAttachment::getDisposition, compareSafeString)
                .thenComparing(contentTypeComparator);
    }

    @Override
    public String toString() {
        return "EmailAttachment{" +
                "id=" + id +
                ", fileName='" + fileName + '\'' +
                ", disposition='" + disposition + '\'' +
                ", contentType=" + contentType +
                '}';
    }
}
