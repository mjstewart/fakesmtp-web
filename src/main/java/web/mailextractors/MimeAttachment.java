package web.mailextractors;

import java.util.Objects;

public class MimeAttachment {
    private String fileName;
    private String disposition;
    private ContentType contentType;

    public MimeAttachment() {
    }

    public MimeAttachment(String fileName, String disposition, ContentType contentType) {
        this.fileName = fileName;
        this.disposition = disposition;
        this.contentType = contentType;
    }


    public String getFileName() {
        return fileName;
    }

    public String getDisposition() {
        return disposition;
    }

    public ContentType getContentType() {
        return contentType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MimeAttachment that = (MimeAttachment) o;
        return Objects.equals(fileName, that.fileName) &&
                Objects.equals(disposition, that.disposition) &&
                Objects.equals(contentType, that.contentType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileName, disposition, contentType);
    }

    @Override
    public String toString() {
        return "MimeAttachment{" +
                "fileName='" + fileName + '\'' +
                ", disposition='" + disposition + '\'' +
                ", contentType=" + contentType +
                '}';
    }
}
