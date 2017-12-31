module AppTypes exposing (..)


type alias ApiErrorModal =
    { title : String
    , description : String
    }


{-| More type safety to avoid annoying bugs of having a type alias refer to a String.
-}
type alias Url =
    { value : String
    }
