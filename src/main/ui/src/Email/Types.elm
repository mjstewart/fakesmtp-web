module Email.Types exposing (..)

import Dict exposing (Dict)


type alias Email =
    { id : EmailId
    , subject : Maybe String
    , from : List String
    , replyTo : List String
    , body : Maybe EmailBody
    , receivedDate : Maybe String
    , sentDate : String
    , description : Maybe String
    , toRecipients : List String
    , ccRecipients : List String
    , bccRecipients : List String
    , attachments : List EmailAttachment
    , read : Bool
    }


type alias EmailId =
    String


type alias EmailBody =
    { content : Maybe String
    , contentType : Maybe ContentType
    }


type alias EmailAttachment =
    { id : String
    , fileName : Maybe String
    , disposition : Maybe String
    , contentType : Maybe ContentType
    }


type alias ContentType =
    { mediaType : Maybe String
    , charset : Maybe String
    }


type alias EmailReadStatus =
    { id : EmailId
    , read : Bool
    }


type alias EmailActionRequest =
    { action : String
    }


{-| Dict is to allow faster lookups/updates, ids are for ordering.
-}
type alias EmailModel =
    { ids : List EmailId
    , mappings : Dict EmailId Email
    }
