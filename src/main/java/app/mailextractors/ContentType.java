package app.mailextractors;

import javax.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public class ContentType {
    private String mediaType;
    private String charset;

    public ContentType() {}

    public ContentType(String mediaType) {
        this(mediaType, null);
    }

    public ContentType(String mediaType, String charset) {
        this.mediaType = mediaType;
        this.charset = charset;
    }

    public String getMediaType() {
        return mediaType;
    }

    public String getCharset() {
        return charset;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContentType that = (ContentType) o;
        return Objects.equals(mediaType, that.mediaType) &&
                Objects.equals(charset, that.charset);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mediaType, charset);
    }

    @Override
    public String toString() {
        return "ContentType{" +
                "mediaType='" + mediaType + '\'' +
                ", charset='" + charset + '\'' +
                '}';
    }
}
