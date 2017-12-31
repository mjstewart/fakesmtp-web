module Email.Encoders exposing (..)

import Json.Encode as Encode
import Messages as Msgs exposing (Msg)


encodeActionRequest : Msgs.EmailAction -> Encode.Value
encodeActionRequest action =
    case action of
        Msgs.ReadAll ->
            Encode.object [ ( "action", Encode.string "READ_ALL" ) ]

        Msgs.UnreadAll ->
            Encode.object [ ( "action", Encode.string "UNREAD_ALL" ) ]


encodeEmailRead : Bool -> Encode.Value
encodeEmailRead read =
    Encode.object [ ( "read", Encode.bool read ) ]
