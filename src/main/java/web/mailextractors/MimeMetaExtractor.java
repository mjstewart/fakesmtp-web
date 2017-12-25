package web.mailextractors;

import io.vavr.CheckedFunction0;
import io.vavr.Tuple2;
import io.vavr.control.Try;
import web.domain.MimeMessageMeta;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Part;
import java.util.*;
import java.util.stream.Collectors;

public class MimeMetaExtractor {

    public static MimeMessageMeta parse(Message message) {
        Optional<BodyAttachment> bodyAttachment = getAllAttachments(message);
        Body body = bodyAttachment.flatMap(BodyAttachment::getBody).orElse(null);
        Set<MimeAttachment> attachments = new HashSet<>(
                bodyAttachment.map(BodyAttachment::getAttachments).orElse(List.of())
        );

        return MimeMessageMeta.builder()
                .subject(stringExtractor(message::getSubject))
                .from(addressExtractor(message::getFrom))
                .replyTo(addressExtractor(message::getReplyTo))
                .body(body)
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


    public static List<String> toList(Address[] addresses) {
        return addresses == null ? List.of() : Arrays.stream(addresses).map(Address::toString).collect(Collectors.toList());
    }

    public static List<String> addressExtractor(CheckedFunction0<Address[]> f) {
        return Try.of(f).map(MimeMetaExtractor::toList).getOrElseGet(t -> List.of());
    }

    public static String stringExtractor(CheckedFunction0<String> f) {
        return Try.of(f).getOrElseGet(t -> null);
    }

    public static Date dateExtractor(CheckedFunction0<Date> f) {
        return Try.of(f).getOrElseGet(t -> null);
    }

    /**
     * As per the java ee spec, https://javaee.github.io/javamail/FAQ#examples.
     * <p>
     * <p>You cant rely upon the disposition type being present, therefore at the expense of some duplicated code, this
     * method extracts inline attachments and the email text body.
     * <p>
     * <p>Body part 0 = email body, Body part 1,2... are inline attachments</p>
     */
    public static Tuple2<Body, List<MimeAttachment>> getBodyAndInlineAttachments(Part part, int bodyPart) throws Exception {
        if (part.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) part.getContent();

            Body body = null;
            List<MimeAttachment> attachments = new ArrayList<>();

            for (int i = 0; i < mp.getCount(); i++) {
                Tuple2<Body, List<MimeAttachment>> result = getBodyAndInlineAttachments(mp.getBodyPart(i), i);
                attachments.addAll(result._2);
                if (result._1 != null) {
                    body = result._1;
                }
            }
            return new Tuple2<>(body, attachments);
        } else if (bodyPart == 0) {
            if (part.isMimeType("text/plain") || part.isMimeType("text/html")) {
                String body = (String) part.getContent();
                return new Tuple2<>(new Body(body, parseContentType(part.getContentType())), List.of());
            }
            return new Tuple2<>(new Body("mime type not supported"), List.of());
        } else {
            // All body parts >= 1 are inline disposition types.
            MimeAttachment messageAttachment = new MimeAttachment(
                    getFileName(part),
                    stringExtractor(part::getDisposition),
                    createContentType(part)
            );
            return new Tuple2<>(null, List.of(messageAttachment));
        }
    }

    /**
     * As per java ee spec https://javaee.github.io/javamail/FAQ#examples.
     * <p>
     * <p>The first body part of the multipart object wil be the main text of the message (which will contain all
     * inline dispositions). The other body parts will be attachments
     */
    private static Tuple2<Body, List<MimeAttachment>> getAllAttachmentsHelper(Part part) throws Exception {
        if (part.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) part.getContent();

            Body body = null;
            List<MimeAttachment> attachments = new ArrayList<>();

            for (int i = 0; i < mp.getCount(); i++) {
                if (i == 0) {
                    Tuple2<Body, List<MimeAttachment>> result = getBodyAndInlineAttachments(mp.getBodyPart(i), i);
                    attachments.addAll(result._2);
                    body = result._1;
                } else {
                    // Each body parts >= 1 will be an attachment disposition type.
                    Tuple2<Body, List<MimeAttachment>> result = getAllAttachmentsHelper(mp.getBodyPart(i));
                    attachments.addAll(result._2);
                }
            }
            return new Tuple2<>(body, attachments);
        } else {
            MimeAttachment messageAttachment = new MimeAttachment(
                    getFileName(part),
                    stringExtractor(part::getDisposition),
                    createContentType(part)
            );
            return new Tuple2<>(null, List.of(messageAttachment));
        }
    }

    /**
     * Extract the email body and all attachments (inline included).
     */
    public static Optional<BodyAttachment> getAllAttachments(Message message) {
        try {
            if (message.isMimeType("multipart/*")) {
                Tuple2<Body, List<MimeAttachment>> result = getAllAttachmentsHelper(message);
                return Optional.of(new BodyAttachment(result._1, result._2));
            }
        } catch (Exception ignored) {
        }
        return Optional.empty();
    }

    /**
     * Extracts and maps header into a domain type.
     * <p>{@code Content-Type : text/plain; charset=us-ascii}
     * <p>
     * <code>[text/plain; charset=us-ascii]</code> is in index 0.
     */
    public static ContentType createContentType(Part part) {
        return Try.of((CheckedFunction0<String[]>) () -> part.getHeader("Content-Type"))
                .filter(MimeMetaExtractor::isNotEmpty)
                .map(header -> parseContentType(header[0]))
                .getOrElseGet(t -> null);

    }

    /**
     * This method only cares about the first 2 values of media type and charset.
     * <p>
     * <pre>
     *     "text/plain; charset=us-ascii; ..." -> MimeAttachment.ContentType(mediaType, charset)
     *     "text/plain" -> MimeAttachment.ContentType(mediaType, charset=null)
     * </pre>
     */
    public static ContentType parseContentType(String header) {
        String[] tokens = header.split(";");
        if (tokens.length == 0 || tokens[0].isEmpty()) return null;

        String mediaType = tokens[0].trim();
        if (tokens.length == 1) return new ContentType(mediaType);

        String[] charsetTokens = tokens[1].split("charset=");
        if (charsetTokens.length == 2) {
            return new ContentType(mediaType, charsetTokens[1].trim());
        }
        return new ContentType(mediaType);
    }

    /**
     * If the {@code Part} is {@code inline}, the filename is the {@code Content-ID} header, otherwise its the
     * attachments filename.
     */
    public static String getFileName(Part part) {
        return Try.of(part::getFileName)
                .filter(Objects::nonNull)
                .getOrElseGet(t -> getContentType(part));

    }

    /**
     * Extracts the <code>Content-ID</code> header and removes the angle bracket formatting, otherwise
     * returns null.
     */
    private static String getContentType(Part part) {
        return Try.of((CheckedFunction0<String[]>) () -> part.getHeader("Content-ID"))
                .filter(MimeMetaExtractor::isNotEmpty)
                .map(header -> header[0])
                .map(MimeMetaExtractor::removeAngleBrackets)
                .getOrElseGet(t -> null);
    }

    public static String removeAngleBrackets(String contentId) {
        return contentId.replaceAll("[<>]", "");
    }

    public static boolean isNotEmpty(Object[] array) {
        return array.length > 0;
    }
}
