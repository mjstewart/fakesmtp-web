package app.mailextractors;

import org.springframework.lang.Nullable;

import javax.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public class ContentType {
    private String mediaType;

    public ContentType() {
    }

    public ContentType(String mediaType) {
        this.mediaType = mediaType;
    }

    @Nullable
    public String getMediaType() {
        return mediaType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ContentType that = (ContentType) o;

        return Objects.equals(mediaType, that.mediaType);
    }

    @Override
    public int hashCode() {
        return mediaType != null ? mediaType.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "ContentType{" +
                "mediaType='" + mediaType + '\'' +
                '}';
    }
}
