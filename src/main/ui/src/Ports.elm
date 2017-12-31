port module Ports exposing (..)

import AppTypes


port showErrorModal : AppTypes.ApiErrorModal -> Cmd msg


port errorModalClosed : (() -> msg) -> Sub msg


{-| String is the app identifier so the server knows who we are. This prevents
page refreshes creating unowned SSE streams. Its not an issue for this app as only 1
client will ever be connected.
-}
port subscribeToEmailStream : String -> Cmd msg


{-| Json string is sent from js back to elm for decoding into Email
-}
port emailStreamOnMessage : (String -> msg) -> Sub msg


port emailStreamOpened : (() -> msg) -> Sub msg


port emailStreamClosed : (() -> msg) -> Sub msg


port emailStreamError : (() -> msg) -> Sub msg
