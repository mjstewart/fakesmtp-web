module Email.Decoders exposing (..)

import Json.Decode as Decode exposing (Decoder)
import Json.Decode.Pipeline exposing (..)
import Email.Types exposing (..)


decodeEmails : Decoder (List Email)
decodeEmails =
    Decode.at [ "_embedded", "emails" ] (Decode.list decodeEmail)


decodeEmail : Decoder Email
decodeEmail =
    decode Email
        |> required "id" Decode.string
        |> required "subject" (Decode.maybe Decode.string)
        |> required "from" (Decode.list Decode.string)
        |> required "replyTo" (Decode.list Decode.string)
        |> required "body" decodeEmailBody
        |> required "receivedDate" (Decode.maybe Decode.string)
        |> required "sentDate" Decode.string
        |> required "description" (Decode.maybe Decode.string)
        |> required "toRecipients" (Decode.list Decode.string)
        |> required "ccRecipients" (Decode.list Decode.string)
        |> required "bccRecipients" (Decode.list Decode.string)
        |> required "attachments" (Decode.list decodeAttachment)
        |> required "read" Decode.bool


decodeEmailBody : Decoder EmailBody
decodeEmailBody =
    Decode.map2 EmailBody
        (Decode.field "content" Decode.string)
        (Decode.field "contentType" (Decode.maybe decodeContentType))


decodeContentType : Decoder ContentType
decodeContentType =
    Decode.map2 ContentType
        (Decode.field "mediaType" (Decode.maybe Decode.string))
        (Decode.field "charset" (Decode.maybe Decode.string))


decodeAttachment : Decoder EmailAttachment
decodeAttachment =
    Decode.map4 EmailAttachment
        (Decode.field "id" Decode.string)
        (Decode.field "fileName" (Decode.maybe Decode.string))
        (Decode.field "disposition" (Decode.maybe Decode.string))
        (Decode.field "contentType" (Decode.maybe decodeContentType))


decodeManyEmailsReadStatus : Decoder (List EmailReadStatus)
decodeManyEmailsReadStatus =
    Decode.list decodeEmailReadStatus


decodeEmailReadStatus : Decoder EmailReadStatus
decodeEmailReadStatus =
    Decode.map2 EmailReadStatus
        (Decode.field "id" Decode.string)
        (Decode.field "read" Decode.bool)
