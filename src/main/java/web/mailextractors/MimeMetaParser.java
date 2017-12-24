package web.mailextractors;

import io.vavr.CheckedFunction0;
import io.vavr.control.Try;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Part;
import java.util.*;
import java.util.stream.Collectors;

public class MimeMetaParser {
    public static List<String> toList(Address[] addresses) {
        return addresses == null ? List.of() : Arrays.stream(addresses).map(Address::toString).collect(Collectors.toList());
    }

    public static List<String> addressExtractor(CheckedFunction0<Address[]> f) {
        return Try.of(f).map(MimeMetaParser::toList).getOrElseGet(t -> List.of());
    }

    public static String stringExtractor(CheckedFunction0<String> f) {
        return Try.of(f).getOrElseGet(t -> null);
    }

    public static Date dateExtractor(CheckedFunction0<Date> f) {
        return Try.of(f).getOrElseGet(t -> null);
    }

    /**
     * As per the java ee spec, https://javaee.github.io/javamail/FAQ#examples.
     *
     * <p>You cant rely upon the disposition type being present, therefore at the expense of some duplicated code, this
     * method is designed to extract all the inline attachments from the first body part containing the email body text.
     */
    public static List<MimeAttachment> getInlineAttachments(Part part, int bodyPart) throws Exception {
        if (part.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) part.getContent();
            List<MimeAttachment> attachments = new ArrayList<>();
            for (int i = 0; i < mp.getCount(); i++) {
                attachments.addAll(getInlineAttachments(mp.getBodyPart(i), i));
            }
            return attachments;
        } else if (bodyPart == 0) {
            // email body can be ignored
            return List.of();
        } else {
            // All body parts > 0 are inline disposition types.
            MimeAttachment messageAttachment = new MimeAttachment(
                    getFileName(part),
                    stringExtractor(part::getDisposition),
                    createContentType(part)
            );
            return List.of(messageAttachment);
        }
    }

    /**
     * As per java ee spec https://javaee.github.io/javamail/FAQ#examples.
     * <p>
     * <p>The first body part of the multipart object wil be the main text of the message (which will contain all
     * inline dispositions). The other body parts will be attachments
     */
    private static List<MimeAttachment> getAllAttachmentsHelper(Part part) throws Exception {
        if (part.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) part.getContent();
            List<MimeAttachment> attachments = new ArrayList<>();
            for (int i = 0; i < mp.getCount(); i++) {
                if (i == 0) {
                    attachments.addAll(getInlineAttachments(mp.getBodyPart(i), i));
                } else {
                    // Each body parts >= 1 will be an attachment disposition type.
                    attachments.addAll(getAllAttachmentsHelper(mp.getBodyPart(i)));
                }
            }
            return attachments;
        } else {
            MimeAttachment messageAttachment = new MimeAttachment(
                    getFileName(part),
                    stringExtractor(part::getDisposition),
                    createContentType(part)
            );
            return List.of(messageAttachment);
        }
    }

    public static List<MimeAttachment> getAllAttachments(Message message) {
        try {
            if (message.isMimeType("multipart/*")) {
                return getAllAttachmentsHelper(message);
            }
        } catch (Exception ignored) {}
        return List.of();
    }

    /**
     * Extracts and maps header into a domain type.
     * <p>{@code Content-Type : text/plain; charset=us-ascii}
     * <p>
     * <code>[text/plain; charset=us-ascii]</code> is in index 0.
     */
    public static MimeAttachment.ContentType createContentType(Part part) {
        return Try.of((CheckedFunction0<String[]>) () -> part.getHeader("Content-Type"))
                .filter(MimeMetaParser::isNotEmpty)
                .map(header -> parseContentType(header[0]))
                .getOrElseGet(t -> null);

    }

    /**
     * This method only cares about the first 2 values of media type and charset.
     *
     * <pre>
     *     "text/plain; charset=us-ascii; ..." -> MimeAttachment.ContentType(mediaType, charset)
     *     "text/plain" -> MimeAttachment.ContentType(mediaType, charset=null)
     * </pre>
     */
    public static MimeAttachment.ContentType parseContentType(String header) {
        String[] tokens = header.split(";");
        if (tokens.length == 0 || tokens[0].isEmpty()) return null;

        String mediaType = tokens[0].trim();
        if (tokens.length == 1) return new MimeAttachment.ContentType(mediaType);

        String[] charsetTokens = tokens[1].split("charset=");
        if (charsetTokens.length == 2) {
            return new MimeAttachment.ContentType(mediaType, charsetTokens[1].trim());
        }
        return new MimeAttachment.ContentType(mediaType);
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

    private static String getContentType(Part part) {
          return Try.of((CheckedFunction0<String[]>) () -> part.getHeader("Content-ID"))
                .filter(MimeMetaParser::isNotEmpty)
                .map(header -> header[0])
                .map(MimeMetaParser::removeAngleBrackets)
                .getOrElseGet(t -> null);
    }

    public static String removeAngleBrackets(String contentId) {
        return contentId.replaceAll("[<>]", "");
    }

    public static boolean isNotEmpty(Object[] array) {
        return array.length > 0;
    }
}
