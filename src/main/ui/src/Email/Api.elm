module Email.Api exposing (..)

import Messages as Msgs exposing (Msg)
import Http
import Email.Encoders as EmailEncoders
import Email.Decoders as EmailDecoders
import Email.Types exposing (..)
import HttpExtras
import AppTypes
import Utils


getEmails : Cmd Msg
getEmails =
    let
        request : Http.Request (List Email)
        request =
            Http.get "http://localhost:8080/api/emails?sort=sentDate,desc" EmailDecoders.decodeEmails
    in
        Http.send Msgs.GetEmailsResult request


requestToggleEmailRead : Email -> Cmd Msg
requestToggleEmailRead email =
    let
        url : AppTypes.Url
        url =
            appendEmailIdToUrl (AppTypes.Url "http://localhost:8080/api/emails") email.id

        body : Http.Body
        body =
            Http.jsonBody <| EmailEncoders.encodeEmailRead (not email.read)

        request : Http.Request Email
        request =
            HttpExtras.httpPatch url body EmailDecoders.decodeEmail
    in
        Http.send Msgs.ToggleEmailReadResult request


requestReadAllEmails : Cmd Msg
requestReadAllEmails =
    let
        url =
            "http://localhost:8080/api/emails/actions"

        body : Http.Body
        body =
            Http.jsonBody <| EmailEncoders.encodeActionRequest Msgs.ReadAll

        request : Http.Request (List EmailReadStatus)
        request =
            Http.post url body EmailDecoders.decodeManyEmailsReadStatus
    in
        Http.send Msgs.ReadAllResult request


requestUnreadAllEmails : Cmd Msg
requestUnreadAllEmails =
    let
        url =
            "http://localhost:8080/api/emails/actions"

        body : Http.Body
        body =
            Http.jsonBody <| EmailEncoders.encodeActionRequest Msgs.UnreadAll

        request : Http.Request (List EmailReadStatus)
        request =
            Http.post url body EmailDecoders.decodeManyEmailsReadStatus
    in
        Http.send Msgs.UnreadAllResult request


requestDeleteEmail : EmailId -> Cmd Msg
requestDeleteEmail id =
    let
        url : AppTypes.Url
        url =
            appendEmailIdToUrl (AppTypes.Url "http://localhost:8080/api/emails") id

        request : Http.Request EmailId
        request =
            HttpExtras.httpDelete url id
    in
        Http.send Msgs.DeleteEmailResult request


requestDeleteAllEmails : Cmd Msg
requestDeleteAllEmails =
    let
        url : AppTypes.Url
        url =
            AppTypes.Url "http://localhost:8080/api/emails/actions"

        request : Http.Request ()
        request =
            HttpExtras.httpDelete url ()
    in
        Http.send Msgs.DeleteAllEmailsResult request


appendEmailIdToUrl : AppTypes.Url -> EmailId -> AppTypes.Url
appendEmailIdToUrl url id =
    Utils.appendToUrl url id
