module EmailDecoderTests exposing (..)

import Expect exposing (Expectation)
import Test exposing (..)
import Email.Types exposing (..)
import Email.Decoders as EmailDecoders
import Json.Decode as Decode


suite : Test
suite =
    describe "Email decoders"
        [ test "decodeEmailReadStatus decodes all fields correctly" <|
            \_ ->
                let
                    json =
                        """
                        {
                          "read": true,
                          "id": "8a61c245-7925-4b23-a91e-5e901882da71"
                        }
                        """

                    expect : EmailReadStatus
                    expect =
                        EmailReadStatus "8a61c245-7925-4b23-a91e-5e901882da71" True

                    actual : Result String EmailReadStatus
                    actual =
                        Decode.decodeString EmailDecoders.decodeEmailReadStatus json
                in
                    Expect.equal actual (Ok expect)
        , test "decodeAttachment decodes all fields correctly" <|
            \_ ->
                let
                    json =
                        """
                        {
                          "id": "ff84c957-911f-49d2-8675-79baf7c85fcc",
                          "fileName": "notes.txt",
                           "contentType": {
                          "mediaType": "text/plain"
                           }
                        }
                        """

                    expect : EmailAttachment
                    expect =
                        { id = "ff84c957-911f-49d2-8675-79baf7c85fcc"
                        , fileName = Just "notes.txt"
                        , contentType = Just { mediaType = Just "text/plain" }
                        }

                    actual : Result String EmailAttachment
                    actual =
                        Decode.decodeString EmailDecoders.decodeAttachment json
                in
                    Expect.equal actual (Ok expect)
        , test "decodeAttachment decodes all null fields correctly" <|
            \_ ->
                let
                    json =
                        """
                        {
                          "id": "ff84c957-911f-49d2-8675-79baf7c85fcc",
                          "fileName": null,
                           "contentType": null
                        }
                        """

                    expect : EmailAttachment
                    expect =
                        { id = "ff84c957-911f-49d2-8675-79baf7c85fcc"
                        , fileName = Nothing
                        , contentType = Nothing
                        }

                    actual : Result String EmailAttachment
                    actual =
                        Decode.decodeString EmailDecoders.decodeAttachment json
                in
                    Expect.equal actual (Ok expect)
        , test "decodeContentType decodes all fields correctly" <|
            \_ ->
                let
                    json =
                        """
                        {
                          "mediaType": "text/plain"
                        }
                        """

                    expect : ContentType
                    expect =
                        { mediaType = Just "text/plain"
                        }

                    actual : Result String ContentType
                    actual =
                        Decode.decodeString EmailDecoders.decodeContentType json
                in
                    Expect.equal actual (Ok expect)
        , test "decodeContentType decodes all null fields correctly" <|
            \_ ->
                let
                    json =
                        """
                        {
                          "mediaType": null
                        }
                        """

                    expect : ContentType
                    expect =
                        { mediaType = Nothing
                        }

                    actual : Result String ContentType
                    actual =
                        Decode.decodeString EmailDecoders.decodeContentType json
                in
                    Expect.equal actual (Ok expect)
        , test "decodeEmailBody decodes all fields correctly" <|
            \_ ->
                let
                    json =
                        """
                        {
                          "content": "hello world",
                          "contentType": {
                              "mediaType": "text/plain"
                          }
                        }
                        """

                    expect : EmailBody
                    expect =
                        { content = Just "hello world"
                        , contentType =
                            Just
                                { mediaType = Just "text/plain"
                                }
                        }

                    actual : Result String EmailBody
                    actual =
                        Decode.decodeString EmailDecoders.decodeEmailBody json
                in
                    Expect.equal actual (Ok expect)
        , test "decodeEmailBody decodes all null fields correctly" <|
            \_ ->
                let
                    json =
                        """
                        {
                          "content": "hello world",
                          "contentType": null
                        }
                        """

                    expect : EmailBody
                    expect =
                        { content = Just "hello world"
                        , contentType = Nothing
                        }

                    actual : Result String EmailBody
                    actual =
                        Decode.decodeString EmailDecoders.decodeEmailBody json
                in
                    Expect.equal actual (Ok expect)
        , test "decodeEmail decodes all fields correctly" <|
            \_ ->
                let
                    json =
                        """
                        {
                            "id": "0551ae7b-abe1-4900-ba30-3727151746ba",
                            "subject": "Activate new account",
                            "replyTo": [
                                "no-reply@user-registration.com"
                            ],
                            "body": {
                                "content": "<h1>hello there</h1>",
                                "contentType": {
                                    "mediaType": "text/html"
                                }
                            },
                            "receivedDate": "2017-12-25T17:25:34",
                            "sentDate": "2017-12-25T17:25:34",
                            "description": "test email description",
                            "toRecipients": [
                                "user1011@email.com"
                            ],
                            "ccRecipients": [
                                "person1@email.com",
                                "person2@email.com"
                            ],
                            "bccRecipients": [
                                "person3@email.com",
                                "person4@email.com"
                            ],
                            "attachments": [
                                {
                                    "id": "ff84c957-911f-49d2-8675-79baf7c85fcc",
                                    "fileName": "notes.txt",
                                    "contentType": {
                                        "mediaType": "text/plain",
                                    }
                                },
                                {
                                    "id": "95104391-0acf-49dc-b997-3dcaad5e6273",
                                    "fileName": "styles.css",
                                    "contentType": {
                                        "mediaType": "text/css"
                                    }
                                }
                            ],
                            "read": true,
                            "from": [
                                "no-reply@user-registration.com"
                            ],
                            "_links": {
                                "self": {
                                    "href": "http://localhost:8080/api/emails/0551ae7b-abe1-4900-ba30-3727151746ba"
                                },
                                "email": {
                                    "href": "http://localhost:8080/api/emails/0551ae7b-abe1-4900-ba30-3727151746ba"
                                }
                            }
                        }
                        """

                    expect : Email
                    expect =
                        { id = "0551ae7b-abe1-4900-ba30-3727151746ba"
                        , subject = Just "Activate new account"
                        , from = [ "no-reply@user-registration.com" ]
                        , replyTo = [ "no-reply@user-registration.com" ]
                        , body =
                            Just
                                { content = Just "<h1>hello there</h1>"
                                , contentType =
                                    Just
                                        { mediaType = Just "text/html"
                                        }
                                }
                        , receivedDate = Just "2017-12-25T17:25:34"
                        , sentDate = "2017-12-25T17:25:34"
                        , description = Just "test email description"
                        , toRecipients = [ "user1011@email.com" ]
                        , ccRecipients = [ "person1@email.com", "person2@email.com" ]
                        , bccRecipients = [ "person3@email.com", "person4@email.com" ]
                        , attachments =
                            [ { id = "ff84c957-911f-49d2-8675-79baf7c85fcc"
                              , fileName = Just "notes.txt"
                              , contentType =
                                    Just
                                        { mediaType = Just "text/plain"
                                        }
                              }
                            , { id = "95104391-0acf-49dc-b997-3dcaad5e6273"
                              , fileName = Just "styles.css"
                              , contentType =
                                    Just
                                        { mediaType = Just "text/css"
                                        }
                              }
                            ]
                        , read = True
                        }

                    actual : Result String Email
                    actual =
                        Decode.decodeString EmailDecoders.decodeEmail json
                in
                    Expect.equal actual (Ok expect)
        , test "decodeEmail decodes all null fields correctly" <|
            \_ ->
                let
                    json =
                        """
                        {
                            "id": "0551ae7b-abe1-4900-ba30-3727151746ba",
                            "subject": null,
                            "replyTo": [],
                            "body": null,
                            "receivedDate": null,
                            "sentDate": "2017-12-25T17:25:34",
                            "description": null,
                            "toRecipients": [
                                "user1011@email.com"
                            ],
                            "ccRecipients": [],
                            "bccRecipients": [],
                            "attachments": [],
                            "read": true,
                            "from": [
                                "no-reply@user-registration.com"
                            ],
                            "_links": {
                                "self": {
                                    "href": "http://localhost:8080/api/emails/0551ae7b-abe1-4900-ba30-3727151746ba"
                                },
                                "email": {
                                    "href": "http://localhost:8080/api/emails/0551ae7b-abe1-4900-ba30-3727151746ba"
                                }
                            }
                        }
                        """

                    expect : Email
                    expect =
                        { id = "0551ae7b-abe1-4900-ba30-3727151746ba"
                        , subject = Nothing
                        , from = [ "no-reply@user-registration.com" ]
                        , replyTo = []
                        , body = Nothing
                        , receivedDate = Nothing
                        , sentDate = "2017-12-25T17:25:34"
                        , description = Nothing
                        , toRecipients = [ "user1011@email.com" ]
                        , ccRecipients = []
                        , bccRecipients = []
                        , attachments = []
                        , read = True
                        }

                    actual : Result String Email
                    actual =
                        Decode.decodeString EmailDecoders.decodeEmail json
                in
                    Expect.equal actual (Ok expect)
        ]
