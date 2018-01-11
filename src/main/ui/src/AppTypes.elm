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


{-| Flags passed in for app configuration
-}
type alias Config =
    { apiUrl : String
    }
