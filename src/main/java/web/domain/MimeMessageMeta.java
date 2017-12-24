package web.domain;

import io.vavr.CheckedFunction0;
import io.vavr.control.Try;
import org.hibernate.annotations.NaturalId;
import web.mailextractors.MimeAttachment;

import javax.mail.Address;
import javax.mail.Message;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.*;
import java.util.stream.Collectors;

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
    private Date receivedDate;
    private Date sentDate;
    private String description;
    private List<String> toRecipients;
    private List<String> ccRecipients;
    private List<String> bccRecipients;
    private Set<MimeAttachment> attachments;

    public MimeMessageMeta() {
    }

    public static MimeMessageMeta from(Message message) {
        MimeMessageMeta mimeMessage = new MimeMessageMeta();
        mimeMessage.emailId = UUID.randomUUID();

        mimeMessage.subject = stringExtractor(message::getSubject);
        mimeMessage.from = addressExtractor(message::getFrom);
        mimeMessage.replyTo = addressExtractor(message::getReplyTo);

        mimeMessage.receivedDate = dateExtractor(message::getReceivedDate);
        mimeMessage.sentDate = dateExtractor(message::getSentDate);

        mimeMessage.description = stringExtractor(message::getDescription);
        mimeMessage.toRecipients = addressExtractor((CheckedFunction0<Address[]>) () -> message.getRecipients(Message.RecipientType.TO));
        mimeMessage.ccRecipients = addressExtractor((CheckedFunction0<Address[]>) () -> message.getRecipients(Message.RecipientType.CC));
        mimeMessage.bccRecipients = addressExtractor((CheckedFunction0<Address[]>) () -> message.getRecipients(Message.RecipientType.BCC));

        // attachments...
        return mimeMessage;
    }

    private static List<String> toList(Address[] addresses) {
        return addresses == null ? List.of() : Arrays.stream(addresses).map(Address::toString).collect(Collectors.toList());
    }

    private static List<String> addressExtractor(CheckedFunction0<Address[]> f) {
        return Try.of(f).map(MimeMessageMeta::toList).getOrElseGet(t -> List.of());
    }

    private static String stringExtractor(CheckedFunction0<String> f) {
        return Try.of(f).getOrElseGet(t -> null);
    }

    private static Date dateExtractor(CheckedFunction0<Date> f) {
        return Try.of(f).getOrElseGet(t -> null);
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

    public UUID getEmailId() {
        return emailId;
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

    @Override
    public String toString() {
        return "MimeMessageMeta{" +
                "id=" + id +
                ", emailId=" + emailId +
                ", subject='" + subject + '\'' +
                ", from=" + from +
                ", replyTo=" + replyTo +
                ", receivedDate=" + receivedDate +
                ", sentDate=" + sentDate +
                ", description='" + description + '\'' +
                ", toRecipients=" + toRecipients +
                ", ccRecipients=" + ccRecipients +
                ", bccRecipients=" + bccRecipients +
                ", attachments=" + attachments +
                '}';
    }
}
