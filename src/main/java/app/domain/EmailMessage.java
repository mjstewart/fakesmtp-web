package app.domain;

import app.mailextractors.EmailAttachment;
import com.fasterxml.jackson.annotation.JsonProperty;
import app.mailextractors.Body;
import org.springframework.lang.Nullable;

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
public class EmailMessage {
    @Id
    private UUID id;

    @Nullable
    private String subject;

    // cant use 'from' as its a reserved sql keyword
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
    private Set<EmailAttachment> attachments;

    private EmailMessage() {
    }

    private EmailMessage(UUID id, String subject, Set<String> fromWho,
                         Set<String> replyTo, Body body, Date receivedDate, Date sentDate,
                         String description, Set<String> toRecipients,
                         Set<String> ccRecipients, Set<String> bccRecipients,
                         Set<EmailAttachment> attachments) {
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

    @Nullable
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

    @Nullable
    public Date getReceivedDate() {
        return receivedDate;
    }

    /**
     * The Date the email was sent by the mail client, otherwise the current Date that this EmailMessage
     * was created at which is near enough the mail client send time.
     */
    public Date getSentDate() {
        return sentDate;
    }

    @Nullable
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

    public Set<EmailAttachment> getAttachments() {
        return attachments;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmailMessage that = (EmailMessage) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "EmailMessage{" +
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
        private Set<String> fromWho = new HashSet<>();
        private Set<String> replyTo = new HashSet<>();
        private Body body;
        private Date receivedDate;
        private Date sentDate;
        private String description;
        private Set<String> toRecipients = new HashSet<>();
        private Set<String> ccRecipients = new HashSet<>();
        private Set<String> bccRecipients = new HashSet<>();
        private Set<EmailAttachment> attachments = new HashSet<>();

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

        public Builder attachments(Set<EmailAttachment> attachments) {
            this.attachments = attachments;
            return this;
        }

        public EmailMessage create() {
            return new EmailMessage(id, subject, fromWho, replyTo, body,
                    receivedDate, sentDate == null ? new Date() : sentDate, description,
                    toRecipients, ccRecipients, bccRecipients, attachments);
        }
    }
}
