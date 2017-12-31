module HttpExtras exposing (..)

import Http
import Json.Decode as Decode exposing (Decoder)
import AppTypes


{-| a = The payload of the expected Result.Ok scenario -> Http.Request a
-}
httpDelete : AppTypes.Url -> a -> Http.Request a
httpDelete url resultPayload =
    Http.request
        { method = "DELETE"
        , headers = []
        , url = url.value
        , body = Http.emptyBody
        , expect = expectEmptyBody resultPayload
        , timeout = Nothing
        , withCredentials = False
        }


{-| Always returns Result.Ok with a supplied payload value which could be an id to allow the caller
to further process.
-}
expectEmptyBody : a -> Http.Expect a
expectEmptyBody resultPayload =
    Http.expectStringResponse << always <| Result.Ok resultPayload


{-| Url -> The Body contains which fields to update on the server -> how to decode the response
-}
httpPatch : AppTypes.Url -> Http.Body -> Decoder a -> Http.Request a
httpPatch url body responseDecoder =
    Http.request
        { method = "PATCH"
        , headers = []
        , url = url.value
        , body = body
        , expect = Http.expectJson responseDecoder
        , timeout = Nothing
        , withCredentials = False
        }
