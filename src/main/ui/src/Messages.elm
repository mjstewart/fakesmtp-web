module Messages exposing (..)

import Http
import Email.Types exposing (Email, EmailId, EmailReadStatus)


type Msg
    = GetEmailsResult (Result Http.Error (List Email))
    | ToggleEmailRead Email
    | ToggleEmailReadResult (Result Http.Error Email)
    | ReadAllResult (Result Http.Error (List EmailReadStatus))
    | UnreadAllResult (Result Http.Error (List EmailReadStatus))
    | EmailAction EmailAction
    | DeleteEmailResult (Result Http.Error EmailId)
    | DeleteEmail EmailId
    | DeleteAllEmails
    | DeleteAllEmailsResult (Result Http.Error ())
    | EmailStream StreamState
    | ReceiveEmailStreamMessage (Result String Email)
    | ErrorModalClosed 


type EmailAction
    = ReadAll
    | UnreadAll


type StreamState
    = Connecting
    | Opened
    | Closed
    | Error
