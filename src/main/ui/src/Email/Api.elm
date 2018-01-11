module Email.Api exposing (..)

import Messages as Msgs exposing (Msg)
import Http
import Email.Encoders as EmailEncoders
import Email.Decoders as EmailDecoders
import Email.Types exposing (..)
import HttpExtras
import AppTypes
import Utils


getEmails : AppTypes.Config -> Cmd Msg
getEmails { apiUrl } =
    let
        request : Http.Request (List Email)
        request =
            Http.get (apiUrl ++ "/api/emails?sort=sentDate,desc") EmailDecoders.decodeEmails
    in
        Http.send Msgs.GetEmailsResult request


requestToggleEmailRead : AppTypes.Config -> Email -> Cmd Msg
requestToggleEmailRead { apiUrl } email =
    let
        url : AppTypes.Url
        url =
            appendEmailIdToUrl (AppTypes.Url (apiUrl ++ "/api/emails")) email.id

        body : Http.Body
        body =
            Http.jsonBody <| EmailEncoders.encodeEmailRead (not email.read)

        request : Http.Request Email
        request =
            HttpExtras.httpPatch url body EmailDecoders.decodeEmail
    in
        Http.send Msgs.ToggleEmailReadResult request


requestReadAllEmails : AppTypes.Config -> Cmd Msg
requestReadAllEmails { apiUrl } =
    let
        url =
            apiUrl ++ "/api/emails/actions"

        body : Http.Body
        body =
            Http.jsonBody <| EmailEncoders.encodeActionRequest Msgs.ReadAll

        request : Http.Request (List EmailReadStatus)
        request =
            Http.post url body EmailDecoders.decodeManyEmailsReadStatus
    in
        Http.send Msgs.ReadAllResult request


requestUnreadAllEmails : AppTypes.Config -> Cmd Msg
requestUnreadAllEmails { apiUrl } =
    let
        url =
            apiUrl ++ "/api/emails/actions"

        body : Http.Body
        body =
            Http.jsonBody <| EmailEncoders.encodeActionRequest Msgs.UnreadAll

        request : Http.Request (List EmailReadStatus)
        request =
            Http.post url body EmailDecoders.decodeManyEmailsReadStatus
    in
        Http.send Msgs.UnreadAllResult request


requestDeleteEmail : AppTypes.Config -> EmailId -> Cmd Msg
requestDeleteEmail { apiUrl } id =
    let
        url : AppTypes.Url
        url =
            appendEmailIdToUrl (AppTypes.Url (apiUrl ++ "/api/emails")) id

        request : Http.Request EmailId
        request =
            HttpExtras.httpDelete url id
    in
        Http.send Msgs.DeleteEmailResult request


requestDeleteAllEmails : AppTypes.Config -> Cmd Msg
requestDeleteAllEmails { apiUrl } =
    let
        url : AppTypes.Url
        url =
            AppTypes.Url (apiUrl ++ "/api/emails/actions")

        request : Http.Request ()
        request =
            HttpExtras.httpDelete url ()
    in
        Http.send Msgs.DeleteAllEmailsResult request


appendEmailIdToUrl : AppTypes.Url -> EmailId -> AppTypes.Url
appendEmailIdToUrl url id =
    Utils.appendToUrl url id
