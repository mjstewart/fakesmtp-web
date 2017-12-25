package web.mailextractors;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public class Body {

    @Column(columnDefinition = "clob")
    private String body;

    private ContentType contentType;

    public Body() {}

    public Body(String body, ContentType contentType) {
        this.body = body;
        this.contentType = contentType;
    }

    public Body(String body) {
        this(body, null);
    }

    public String getBody() {
        return body;
    }

    public ContentType getContentType() {
        return contentType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Body body1 = (Body) o;
        return Objects.equals(body, body1.body) &&
                Objects.equals(contentType, body1.contentType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(body, contentType);
    }

    @Override
    public String toString() {
        return "Body{" +
                "body='" + body + '\'' +
                ", contentType=" + contentType +
                '}';
    }
}

