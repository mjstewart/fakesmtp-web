module JsonTests exposing (..)

import Expect exposing (Expectation)
import Test exposing (..)
import Json.Encode as Encode
import Messages as Msgs
import Email.Encoders exposing (encodeActionRequest, encodeEmailRead)


suite : Test
suite =
    describe "encoders"
        [ test "encodeActionRequest creates correct json" <|
            \_ ->
                Expect.equal """{"action":"READ_ALL"}""" <|
                    Encode.encode 0 (encodeActionRequest <| Msgs.ReadAll)
        , test "encodeEmailRead creates correct json" <|
            \_ ->
                Expect.equal """{"read":true}""" <|
                    Encode.encode 0 (encodeEmailRead True)
        ]
