package app.mailextractors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.util.comparator.Comparators;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;

@Entity
public class MimeAttachment {
    @Id
    private UUID id;
    
    private String fileName;
    private String disposition;
    private ContentType contentType;

    public MimeAttachment() {}

    public MimeAttachment(String fileName, String disposition, ContentType contentType) {
        this(UUID.randomUUID(), fileName, disposition, contentType);
    }
    
    public MimeAttachment(UUID id, String fileName, String disposition, ContentType contentType) {
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
        MimeAttachment that = (MimeAttachment) o;
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
    public static Comparator<MimeAttachment> excludeIdComparator() {
        Comparator<String> compareSafeString = Comparator.nullsLast(String::compareTo);

        Comparator<MimeAttachment> contentTypeComparator =
                Comparator.comparing(MimeAttachment::getContentType, Comparator.nullsLast(
                        Comparator.comparing(ContentType::getMediaType, compareSafeString)
                                .thenComparing(ContentType::getCharset, compareSafeString)
                ));

        return Comparator
                .comparing(MimeAttachment::getFileName, compareSafeString)
                .thenComparing(MimeAttachment::getDisposition, compareSafeString)
                .thenComparing(contentTypeComparator);
    }

    @Override
    public String toString() {
        return "MimeAttachment{" +
                "id=" + id +
                ", fileName='" + fileName + '\'' +
                ", disposition='" + disposition + '\'' +
                ", contentType=" + contentType +
                '}';
    }
}
