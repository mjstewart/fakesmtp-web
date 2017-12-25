package app.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.NaturalId;
import app.mailextractors.Body;
import app.mailextractors.MimeAttachment;
import org.springframework.data.rest.core.annotation.RestResource;

import javax.persistence.*;
import java.util.*;

@Entity
@AttributeOverrides({
        @AttributeOverride(
                name = "body.contentType.mediaType",
                column = @Column(name = "body_content_media_type")
        ),
        @AttributeOverride(
                name = "body.contentType.charset",
                column = @Column(name = "body_content_charset")
        )
})
public class MimeMessageMeta {
    @Id
    private UUID id;

    private String subject;

    // fromWho as from is a reserved sql keyword
    @JsonProperty("from")
    @ElementCollection
    private Set<String> fromWho;

    @ElementCollection
    private Set<String> replyTo;

    private Body body;
    private Date receivedDate;
    private Date sentDate;
    private String description;

    @ElementCollection
    private Set<String> toRecipients;

    @ElementCollection
    private Set<String> ccRecipients;

    @ElementCollection
    private Set<String> bccRecipients;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<MimeAttachment> attachments;

    private MimeMessageMeta() {
    }

    private MimeMessageMeta(UUID id, String subject, Set<String> fromWho,
                            Set<String> replyTo, Body body, Date receivedDate, Date sentDate,
                            String description, Set<String> toRecipients,
                            Set<String> ccRecipients, Set<String> bccRecipients,
                            Set<MimeAttachment> attachments) {
        this.id = id;
        this.subject = subject;
        this.fromWho = fromWho;
        this.replyTo = replyTo;
        this.body = body;
        this.receivedDate = receivedDate;
        this.sentDate = sentDate;
        this.description = description;
        this.toRecipients = toRecipients;
        this.ccRecipients = ccRecipients;
        this.bccRecipients = bccRecipients;
        this.attachments = attachments;
    }

    public UUID getId() {
        return id;
    }
    
    public String getSubject() {
        return subject;
    }

    public Set<String> getFromWho() {
        return fromWho;
    }

    public Set<String> getReplyTo() {
        return replyTo;
    }

    public Body getBody() {
        return body;
    }

    public Date getReceivedDate() {
        return receivedDate;
    }

    public Date getSentDate() {
        return sentDate;
    }

    public String getDescription() {
        return description;
    }

    public Set<String> getToRecipients() {
        return toRecipients;
    }

    public Set<String> getCcRecipients() {
        return ccRecipients;
    }

    public Set<String> getBccRecipients() {
        return bccRecipients;
    }

    public Set<MimeAttachment> getAttachments() {
        return attachments;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MimeMessageMeta that = (MimeMessageMeta) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "MimeMessageMeta{" +
                "id=" + id +
                ", subject='" + subject + '\'' +
                ", fromWho=" + fromWho +
                ", replyTo=" + replyTo +
                ", body=" + body +
                ", receivedDate=" + receivedDate +
                ", sentDate=" + sentDate +
                ", description='" + description + '\'' +
                ", toRecipients=" + toRecipients +
                ", ccRecipients=" + ccRecipients +
                ", bccRecipients=" + bccRecipients +
                ", attachments=" + attachments +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id = UUID.randomUUID();
        private String subject;
        private Set<String> fromWho = Set.of();
        private Set<String> replyTo = Set.of();
        private Body body;
        private Date receivedDate;
        private Date sentDate;
        private String description;
        private Set<String> toRecipients = Set.of();
        private Set<String> ccRecipients = Set.of();
        private Set<String> bccRecipients = Set.of();
        private Set<MimeAttachment> attachments = Set.of();

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder subject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder fromWho(Set<String> fromWho) {
            this.fromWho = fromWho;
            return this;
        }

        public Builder replyTo(Set<String> replyTo) {
            this.replyTo = replyTo;
            return this;
        }

        public Builder body(Body body) {
            this.body = body;
            return this;
        }

        public Builder receivedDate(Date receivedDate) {
            this.receivedDate = receivedDate;
            return this;
        }

        public Builder sentDate(Date sentDate) {
            this.sentDate = sentDate;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder toRecipients(Set<String> toRecipients) {
            this.toRecipients = toRecipients;
            return this;
        }

        public Builder ccRecipients(Set<String> ccRecipients) {
            this.ccRecipients = ccRecipients;
            return this;
        }

        public Builder bccRecipients(Set<String> bccRecipients) {
            this.bccRecipients = bccRecipients;
            return this;
        }

        public Builder attachments(Set<MimeAttachment> attachments) {
            this.attachments = attachments;
            return this;
        }

        public MimeMessageMeta create() {
            return new MimeMessageMeta(id, subject, fromWho, replyTo, body,
                    receivedDate, sentDate, description,
                    toRecipients, ccRecipients, bccRecipients, attachments);
        }
    }
}
