module Views.FormatUtils exposing (..)

import Date
import Date.Format as DF
import Email.Types exposing (..)


{-| Tue 3/Oct/2017 5:34:03 am
-}
toShortDateFormat : String -> String
toShortDateFormat serverTimestamp =
    case Date.fromString serverTimestamp of
        Ok date ->
            DF.format "%a %e/%b/%Y %l:%M:%S %P" date

        Err _ ->
            "Invalid date"


getSubject : Maybe String -> String
getSubject maybe =
    case maybe of
        Just subject ->
            subject

        Nothing ->
            "No subject"


getEmailBodyContent : Maybe EmailBody -> String
getEmailBodyContent maybeEmailBody =
    case maybeEmailBody of
        Nothing ->
            "No body"

        Just emailBody ->
            case emailBody.content of
                Nothing ->
                    "No body"

                Just content ->
                    content
