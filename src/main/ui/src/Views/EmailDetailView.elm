module Views.EmailDetailView exposing (emailDetailView)

import Messages as Msgs exposing (Msg)
import Html as H exposing (Html)
import Html.Events exposing (onClick)
import Html.Attributes exposing (class, classList, title, property, srcdoc)
import Email.Types exposing (..)
import Utils
import Views.FormatUtils as Format


emailDetailView : Maybe Email -> Html Msg
emailDetailView maybeEmail =
    case maybeEmail of
        Nothing ->
            H.text ""

        Just email ->
            emailDetail email


emailDetail : Email -> Html Msg
emailDetail email =
    H.div [ class "email-detail-container" ]
        [ H.div [ class "email-detail-header email-detail-section" ]
            [ H.h2 [ class "dividing" ] [ H.text <| Format.getSubject email.subject ]
            ]
        , quickActionView email
        , emailRoutingInfoView email
        , getAttachmentsView email.attachments
        , getBodyView email.body
        ]


quickActionView : Email -> Html Msg
quickActionView email =
    H.div [ class "email-detail-actions email-detail-section" ]
        [ H.button [ class "ui button mini", onClick (Msgs.ToggleEmailRead email) ]
            [ H.i [ class "icon unhide" ] []
            , H.text
                (if email.read then
                    "Mark Unread"
                 else
                    "Mark Read"
                )
            ]
        , H.button [ class "ui button mini", onClick (Msgs.DeleteEmail email.id) ]
            [ H.i [ class "icon trash alternate outline" ] []
            , H.text "Delete"
            ]
        ]


emailRoutingInfoView : Email -> Html Msg
emailRoutingInfoView email =
    H.div [ class "email-detail-routing email-detail-section" ]
        [ H.div [ class "ui grid" ]
            [ getSentDate email
            , getReceivedDate email
            , getFromElement email
            , getToElement email
            , getCcElement email
            , getBccElement email
            , getReplyToElement email
            , getDescriptionElement email
            ]
        ]


getSentDate : Email -> Html Msg
getSentDate email =
    H.div [ class "row" ]
        [ H.div [ class "two wide column" ]
            [ H.p [ class "subtle-text " ] [ H.text "Sent:" ] ]
        , H.div
            [ class "fourteen wide column" ]
            [ H.p [] [ H.text <| Format.toShortDateFormat email.sentDate ]
            ]
        ]


getReceivedDate : Email -> Html Msg
getReceivedDate email =
    case email.receivedDate of
        Nothing ->
            H.text ""

        Just date ->
            H.div [ class "row" ]
                [ H.div [ class "two wide column" ]
                    [ H.p [ class "subtle-text " ] [ H.text "Received:" ] ]
                , H.div
                    [ class "fourteen wide column" ]
                    [ H.p [] [ H.text <| Format.toShortDateFormat date ]
                    ]
                ]


getFromElement : Email -> Html Msg
getFromElement email =
    case email.from of
        [] ->
            H.div [ class "row" ]
                [ H.div [ class "two wide column" ]
                    [ H.p [ class "subtle-text" ] [ H.text "From:" ] ]
                , H.div
                    [ class "fourteen wide column" ]
                    [ H.p [ class "error" ] [ H.text "Missing" ]
                    ]
                ]

        _ ->
            H.div [ class "row" ]
                [ H.div [ class "two wide column" ]
                    [ H.p [ class "subtle-text" ] [ H.text "From:" ] ]
                , H.div
                    [ class "fourteen wide column" ]
                    [ H.p [] [ H.text <| Utils.toCsv email.from ]
                    ]
                ]


getToElement : Email -> Html Msg
getToElement email =
    case email.toRecipients of
        [] ->
            H.text ""

        _ ->
            H.div [ class "row" ]
                [ H.div [ class "two wide column" ]
                    [ H.p [ class "subtle-text" ] [ H.text "To:" ] ]
                , H.div
                    [ class "fourteen wide column" ]
                    [ H.p [] [ H.text <| Utils.toCsv email.toRecipients ]
                    ]
                ]


getCcElement : Email -> Html Msg
getCcElement email =
    case email.ccRecipients of
        [] ->
            H.text ""

        _ ->
            H.div [ class "row" ]
                [ H.div [ class "two wide column" ]
                    [ H.p [ class "subtle-text" ] [ H.text "Cc:" ] ]
                , H.div
                    [ class "fourteen wide column" ]
                    [ H.p [] [ H.text <| Utils.toCsv email.ccRecipients ]
                    ]
                ]


getBccElement : Email -> Html Msg
getBccElement email =
    case email.bccRecipients of
        [] ->
            H.text ""

        _ ->
            H.div [ class "row" ]
                [ H.div [ class "two wide column" ]
                    [ H.p [ class "subtle-text" ] [ H.text "Bcc:" ] ]
                , H.div
                    [ class "fourteen wide column" ]
                    [ H.p [] [ H.text <| Utils.toCsv email.bccRecipients ]
                    ]
                ]


getReplyToElement : Email -> Html Msg
getReplyToElement email =
    case email.replyTo of
        [] ->
            H.text ""

        _ ->
            H.div [ class "row" ]
                [ H.div [ class "two wide column" ]
                    [ H.p [ class "subtle-text" ] [ H.text "Reply to:" ] ]
                , H.div
                    [ class "fourteen wide column" ]
                    [ H.p [] [ H.text <| Utils.toCsv email.replyTo ]
                    ]
                ]


getDescriptionElement : Email -> Html Msg
getDescriptionElement email =
    case email.description of
        Nothing ->
            H.text ""

        Just description ->
            H.div [ class "row" ]
                [ H.div [ class "two wide column" ]
                    [ H.p [ class "subtle-text" ] [ H.text "Description:" ] ]
                , H.div
                    [ class "fourteen wide column" ]
                    [ H.p [] [ H.text description ]
                    ]
                ]


getAttachmentsView : List EmailAttachment -> Html Msg
getAttachmentsView attachments =
    case attachments of
        [] ->
            H.text ""

        _ ->
            H.div [ class "email-detail-attachments-container email-detail-section" ]
                [ H.div [ class "header section-header" ]
                    [ H.h4 []
                        [ H.text <| "Attachments (" ++ toString (List.length attachments) ++ ")"
                        ]
                    ]
                , H.div [ class "attachments" ] <|
                    List.map getAttachmentView attachments
                ]


getAttachmentView : EmailAttachment -> Html Msg
getAttachmentView attachment =
    H.div [ class "email-detail-attachment" ]
        [ H.div [ class "header" ]
            [ H.i [ class "attach icon" ] []
            ]
        , H.div [ class "body" ]
            [ (case attachment.fileName of
                Nothing ->
                    H.h3 [] [ H.text "No file name" ]

                Just fileName ->
                    H.h3 [] [ H.text fileName ]
              )
            ]
        , getContentTypeView attachment.contentType
        ]


getAttachmentContentTypeView : EmailAttachment -> Html Msg
getAttachmentContentTypeView attachment =
    case attachment.contentType of
        Nothing ->
            H.div [ class "footer" ]
                [ H.p [] [ H.text "No content type" ]
                ]

        Just contentType ->
            H.div [ class "footer" ]
                [ case contentType.mediaType of
                    Nothing ->
                        H.p [ class "subtle-text" ] [ H.text "No media type" ]

                    Just mediaType ->
                        H.p [ class "subtle-text" ] [ H.text mediaType ]
                ]


{-| Render an iframe for any existing EmailBody content other than text/plain.
An iframe is used to avoid any css taking effect. The fake email could have css classes that this
application uses and could interfere.
-}
getBodyView : Maybe EmailBody -> Html Msg
getBodyView maybeEmailBody =
    case maybeEmailBody of
        Nothing ->
            H.div [ class "email-detail-body-wrapper email-detail-section" ]
                [ H.div [ class "header section-header" ]
                    [ H.h4 [] [ H.text "Body" ]
                    , H.p [] [ H.text "No body" ]
                    ]
                ]

        Just emailBody ->
            H.div [ class "email-detail-body-wrapper email-detail-section" ]
                [ H.div [ class "header section-header" ]
                    [ H.h4 [] [ H.text "Body" ]
                    , getContentTypeView emailBody.contentType
                    ]
                , H.div [ class "body" ]
                    [ case emailBody.content of
                        Nothing ->
                            H.p [] [ H.text "No body" ]

                        Just content ->
                            case emailBody.contentType of
                                Nothing ->
                                    H.iframe [ srcdoc content ] []

                                Just contentType ->
                                    case contentType.mediaType of
                                        Just "text/plain" ->
                                            H.p [] [ H.text content ]

                                        _ ->
                                            H.iframe [ srcdoc content ] []
                    ]
                ]


getContentTypeView : Maybe ContentType -> Html Msg
getContentTypeView maybeContentType =
    case maybeContentType of
        Nothing ->
            H.div [ class "content-type" ]
                [ H.p [] [ H.text "No content type" ]
                ]

        Just contentType ->
            H.div [ class "content-type" ]
                [ case contentType.mediaType of
                    Nothing ->
                        H.p [ class "subtle-text" ] [ H.text "No media type" ]

                    Just mediaType ->
                        H.p [ class "subtle-text" ] [ H.text mediaType ]
                ]
