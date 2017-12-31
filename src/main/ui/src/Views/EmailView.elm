module Views.EmailView exposing (emailView)

import Messages as Msgs exposing (Msg)
import Html exposing (Html, div, header, h1, h2, h3, h4, h5, h6, i, p, text)
import Html.Events exposing (onClick)
import Html.Attributes exposing (class, classList, title)
import Email.Types exposing (Email, EmailBody, EmailAttachment)
import Maybe.FlatMap as MaybeExtra
import Utils
import Views.FormatUtils as Format


{-| Max character length before truncation in the inbox preview
-}
inboxMaxTextLength : Int
inboxMaxTextLength =
    60


inboxTextTruncator : String -> String
inboxTextTruncator =
    Utils.truncateText inboxMaxTextLength


emailView : Email -> Bool -> Html Msg
emailView email isSelected =
    let
        from =
            Utils.toCsv email.from |> inboxTextTruncator

        subject =
            Format.getSubject email.subject |> inboxTextTruncator
    in
        div
            [ classList
                [ ( "inbox-email-container", True )
                , ( "inbox-email-read", email.read )
                , ( "inbox-email-unread", not email.read )
                ]
            ]
            [ div
                [ classList
                    [ ( "inbox-email", True )
                    , ( "inbox-email-selected", isSelected )
                    ]
                ]
                [ div
                    [ class "clickable"
                    , onClick <| Msgs.ToggleEmailRead email
                    , title
                        (if email.read then
                            "unread"
                         else
                            "read"
                        )
                    ]
                    [ div [ class "inbox-email-header" ]
                        [ h3 [] [ text from ]
                        , p [ class "inbox-email-subject" ] [ text subject ]
                        , p [ class "inbox-email-time" ] [ text (Format.toShortDateFormat email.sentDate) ]
                        ]
                    , div [ class "index-email-preview-body" ]
                        [ inboxPreviewBody email.body
                        ]
                    ]
                , footer email
                ]
            ]


inboxPreviewBody : EmailBody -> Html Msg
inboxPreviewBody { content, contentType } =
    let
        contentTypeText : String
        contentTypeText =
            MaybeExtra.flatMap (\ct -> ct.mediaType) contentType
                |> Utils.maybeOrElse "unknown content type"
    in
        div [ class "inbox-email-preview-body" ]
            [ p [] [ text contentTypeText ]
            , p [] [ text (inboxTextTruncator content) ]
            ]


inboxQuickActions : Email -> Html Msg
inboxQuickActions email =
    div [ class "inbox-quick-actions" ]
        [ i
            [ class "icon red trash outline clickable"
            , onClick <| Msgs.DeleteEmail email.id
            , title "Delete"
            ]
            []
        ]


footer : Email -> Html Msg
footer email =
    let
        attachmentCount =
            List.length email.attachments

        ccCount =
            List.length email.ccRecipients

        bccCount =
            List.length email.bccRecipients
    in
        div [ class "inbox-email-footer" ]
            [ div []
                [ (if attachmentCount > 0 then
                    div [ class "ui label" ]
                        [ i [ class "attach icon" ] []
                        , text (toString (List.length email.attachments))
                        ]
                   else
                    text ""
                  )
                , (if ccCount > 0 then
                    div [ class "ui label" ]
                        [ text ("cc: " ++ (toString ccCount)) ]
                   else
                    text ""
                  )
                , (if bccCount > 0 then
                    div [ class "ui label" ]
                        [ text ("bcc: " ++ (toString bccCount)) ]
                   else
                    text ""
                  )
                ]
            , inboxQuickActions email
            ]
