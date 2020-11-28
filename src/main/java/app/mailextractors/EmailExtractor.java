package app.mailextractors;

import app.domain.EmailMessage;
import io.vavr.CheckedFunction0;
import io.vavr.control.Try;
import org.apache.commons.mail.util.MimeMessageParser;
import org.springframework.http.MediaType;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.internet.MimeMessage;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class EmailExtractor {

    public static EmailMessage parse(MimeMessage message) throws Exception {
        MimeMessageParser parser = new MimeMessageParser(message).parse();

        Set<EmailAttachment> attachments = parser.getAttachmentList().stream().map(ds ->
                new EmailAttachment(ds.getName(), new ContentType(ds.getContentType()))
        ).collect(Collectors.toSet());

        return EmailMessage.builder()
                .subject(parser.getSubject())
                .fromWho(addressExtractor(message::getFrom))
                .replyTo(addressExtractor(message::getReplyTo))
                .body(getBody(parser))
                .receivedDate(dateExtractor(message::getReceivedDate))
                .sentDate(dateExtractor(message::getSentDate))
                .description(stringExtractor(message::getDescription))
                .toRecipients(addressExtractor((CheckedFunction0<Address[]>)
                        () -> message.getRecipients(Message.RecipientType.TO)))
                .ccRecipients(addressExtractor((CheckedFunction0<Address[]>)
                        () -> message.getRecipients(Message.RecipientType.CC)))
                .bccRecipients(addressExtractor((CheckedFunction0<Address[]>)
                        () -> message.getRecipients(Message.RecipientType.BCC)))
                .attachments(attachments)
                .create();
    }

    public static Body getBody(MimeMessageParser p) {
        if (p.hasHtmlContent()) {
            return new Body(p.getHtmlContent(), new ContentType(MediaType.TEXT_HTML.toString()));
        }
        return new Body(p.getPlainContent(), new ContentType(MediaType.TEXT_PLAIN.toString()));
    }

    public static Set<String> toSet(Address[] addresses) {
        return addresses == null ? new HashSet<>() :
                Arrays.stream(addresses).map(Address::toString).collect(Collectors.toSet());
    }

    public static Set<String> addressExtractor(CheckedFunction0<Address[]> f) {
        return Try.of(f).map(EmailExtractor::toSet).getOrElseGet(t -> new HashSet<>());
    }

    public static String stringExtractor(CheckedFunction0<String> f) {
        return Try.of(f).getOrElseGet(t -> null);
    }

    public static Date dateExtractor(CheckedFunction0<Date> f) {
        return Try.of(f).getOrElseGet(t -> null);
    }
}
