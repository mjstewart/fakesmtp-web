package web.mailextractors;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class BodyAttachment {
    private Body body;
    private List<MimeAttachment> attachments;

    public BodyAttachment(Body body, List<MimeAttachment> attachments) {
        this.body = body;
        this.attachments = attachments;
    }

    public Optional<Body> getBody() {
        return Optional.ofNullable(body);
    }

    public List<MimeAttachment> getAttachments() {
        return attachments;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BodyAttachment that = (BodyAttachment) o;
        return Objects.equals(body, that.body) &&
                Objects.equals(attachments, that.attachments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(body, attachments);
    }

    @Override
    public String toString() {
        return "BodyAttachment{" +
                "body=" + body +
                ", attachments=" + attachments +
                '}';
    }
}
