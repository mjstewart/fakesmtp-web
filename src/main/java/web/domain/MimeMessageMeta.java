package web.domain;

import org.hibernate.annotations.NaturalId;
import web.mailextractors.Body;
import web.mailextractors.MimeAttachment;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.*;

@Entity
public class MimeMessageMeta {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NaturalId
    private UUID emailId;

    private String subject;
    private List<String> from;
    private List<String> replyTo;
    private Body body;
    private Date receivedDate;
    private Date sentDate;
    private String description;
    private List<String> toRecipients;
    private List<String> ccRecipients;
    private List<String> bccRecipients;
    private Set<MimeAttachment> attachments;

    private MimeMessageMeta() {}

    private MimeMessageMeta(UUID emailId, String subject, List<String> from,
                           List<String> replyTo, Body body, Date receivedDate, Date sentDate,
                           String description, List<String> toRecipients,
                           List<String> ccRecipients, List<String> bccRecipients,
                           Set<MimeAttachment> attachments) {
        this.emailId = emailId;
        this.subject = subject;
        this.from = from;
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

    public Long getId() {
        return id;
    }

    public UUID getEmailId() {
        return emailId;
    }

    public String getSubject() {
        return subject;
    }

    public List<String> getFrom() {
        return from;
    }

    public List<String> getReplyTo() {
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

    public List<String> getToRecipients() {
        return toRecipients;
    }

    public List<String> getCcRecipients() {
        return ccRecipients;
    }

    public List<String> getBccRecipients() {
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
        return Objects.equals(emailId, that.emailId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(emailId);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String subject;
        private List<String> from = List.of();
        private List<String> replyTo = List.of();
        private Body body;
        private Date receivedDate;
        private Date sentDate;
        private String description;
        private List<String> toRecipients = List.of();
        private List<String> ccRecipients = List.of();
        private List<String> bccRecipients = List.of();
        private Set<MimeAttachment> attachments = Set.of();

        public Builder subject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder from(List<String> from) {
            this.from = from;
            return this;
        }

        public Builder replyTo(List<String> replyTo) {
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

        public Builder toRecipients(List<String> toRecipients) {
            this.toRecipients = toRecipients;
            return this;
        }

        public Builder ccRecipients(List<String> ccRecipients) {
            this.ccRecipients = ccRecipients;
            return this;
        }

        public Builder bccRecipients(List<String> bccRecipients) {
            this.bccRecipients = bccRecipients;
            return this;
        }

        public Builder attachments(Set<MimeAttachment> attachments) {
            this.attachments = attachments;
            return this;
        }

        public MimeMessageMeta create() {
            return new MimeMessageMeta(UUID.randomUUID(), subject, from, replyTo, body,
                    receivedDate, sentDate, description,
                    toRecipients, ccRecipients, bccRecipients, attachments);
        }
    }
}
