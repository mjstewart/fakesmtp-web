package app.mailextractors;

import org.springframework.lang.Nullable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public class Body {

    @Column(columnDefinition = "clob")
    private String content;

    private ContentType contentType;

    public Body() {}

    public Body(String content, ContentType contentType) {
        this.content = content;
        this.contentType = contentType;
    }

    public Body(String content) {
        this(content, null);
    }

    public String getContent() {
        return content;
    }

    @Nullable
    public ContentType getContentType() {
        return contentType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Body body1 = (Body) o;
        return Objects.equals(content, body1.content) &&
                Objects.equals(contentType, body1.contentType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, contentType);
    }

    @Override
    public String toString() {
        return "Body{" +
                "content='" + content + '\'' +
                ", contentType=" + contentType +
                '}';
    }
}

