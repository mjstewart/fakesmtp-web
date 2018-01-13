module MainTest exposing (..)

import Expect exposing (Expectation)
import Test exposing (..)
import Dict exposing (Dict)
import Main
import Email.Types exposing (..)


getTestEmailOne : Email
getTestEmailOne =
    { id = "111"
    , subject = Just "Subject test email one"
    , from = [ "me@email.com" ]
    , replyTo = []
    , body = Just <| EmailBody (Just "body content") (Just <| ContentType (Just "text/plain") (Just "utf-8"))
    , receivedDate = Nothing
    , sentDate = "2018-01-06T05:15:36"
    , description = Just "test email one description"
    , toRecipients = [ "person1@email.com", "person2@email.com" ]
    , ccRecipients = []
    , bccRecipients = []
    , attachments = []
    , read = False
    }


getTestEmailTwo : Email
getTestEmailTwo =
    { id = "222"
    , subject = Just "Subject test email two"
    , from = [ "me@email.com" ]
    , replyTo = []
    , body = Just <| EmailBody (Just "body content") (Just <| ContentType (Just "text/plain") (Just "utf-8"))
    , receivedDate = Nothing
    , sentDate = "2018-01-06T05:15:36"
    , description = Just "test email two description"
    , toRecipients = [ "person1@email.com", "person2@email.com" ]
    , ccRecipients = []
    , bccRecipients = []
    , attachments = []
    , read = False
    }


getTestEmailThree : Email
getTestEmailThree =
    { id = "333"
    , subject = Just "Subject test email three"
    , from = [ "me@email.com" ]
    , replyTo = []
    , body = Just <| EmailBody (Just "body content") (Just <| ContentType (Just "text/plain") (Just "utf-8"))
    , receivedDate = Nothing
    , sentDate = "2018-01-06T05:15:36"
    , description = Just "test email three description"
    , toRecipients = [ "person1@email.com", "person2@email.com" ]
    , ccRecipients = []
    , bccRecipients = []
    , attachments = []
    , read = False
    }


suite : Test
suite =
    describe "Main module model helpers"
        [ describe "dictUpdateMapper"
            [ test "returns original Dict without changes when key is not found" <|
                \_ ->
                    let
                        expect =
                            Dict.fromList [ ( 1, "one" ), ( 2, "two" ) ]

                        result =
                            Main.dictUpdateMapper 3 (\v -> String.toUpper v) expect
                    in
                        Expect.equalDicts expect result
            , test "returns Dict with updated change when key is found" <|
                \_ ->
                    let
                        original =
                            Dict.fromList [ ( 1, "one" ), ( 2, "two" ) ]

                        expect =
                            Dict.fromList [ ( 1, "one" ), ( 2, "TWO" ) ]

                        result =
                            Main.dictUpdateMapper 2 (\v -> String.toUpper v) original
                    in
                        Expect.equalDicts expect result
            ]
        , describe "update emails"
            [ test "updateEmailRead - Single email read status is changed to its EmailReadStatus value" <|
                \_ ->
                    let
                        unreadEmail =
                            { getTestEmailOne | read = False }

                        expectEmail =
                            { getTestEmailOne | read = True }

                        status : EmailReadStatus
                        status =
                            { id = unreadEmail.id
                            , read = True
                            }

                        existingMappings =
                            Dict.fromList [ ( unreadEmail.id, unreadEmail ) ]

                        expectedMappings =
                            Dict.fromList [ ( expectEmail.id, expectEmail ) ]
                    in
                        Main.updateEmailRead status existingMappings
                            |> Expect.equal expectedMappings
            , test "updateEmailsRead - All emails read status is changed to its corresponding value in the EmailReadStatus list" <|
                \_ ->
                    let
                        emails =
                            List.map (\email -> { email | read = True }) [ getTestEmailOne, getTestEmailTwo ]

                        expectEmail =
                            List.map (\email -> { email | read = False }) [ getTestEmailOne, getTestEmailTwo ]

                        statusList : List EmailReadStatus
                        statusList =
                            [ { id = getTestEmailOne.id
                              , read = False
                              }
                            , { id = getTestEmailTwo.id
                              , read = False
                              }
                            ]

                        existingMappings : Dict EmailId Email
                        existingMappings =
                            emails |> List.map (\email -> ( email.id, email )) |> Dict.fromList

                        expectedMappings =
                            Dict.map (\key email -> { email | read = False }) existingMappings
                    in
                        Main.updateEmailsRead statusList existingMappings
                            |> Expect.equal expectedMappings
            ]
        , describe "remove emails"
            [ test "delete 1 email is removed for id list and dict mappings" <|
                \_ ->
                    let
                        testEmail =
                            getTestEmailOne

                        model : EmailModel
                        model =
                            { ids = [ testEmail.id ]
                            , mappings = Dict.fromList [ ( testEmail.id, testEmail ) ]
                            }

                        expectedModel : EmailModel
                        expectedModel =
                            { ids = []
                            , mappings = Dict.empty
                            }
                    in
                        Main.deleteEmail model testEmail.id
                            |> Expect.equal expectedModel
            , test "deleted email keeps email model is sorted order" <|
                \_ ->
                    let
                        a =
                            getTestEmailOne

                        b =
                            getTestEmailTwo

                        c =
                            getTestEmailThree

                        model : EmailModel
                        model =
                            { ids = [ a.id, b.id, c.id ]
                            , mappings = Dict.fromList [ ( a.id, a ), ( b.id, b ), ( c.id, c ) ]
                            }

                        expectedModel : EmailModel
                        expectedModel =
                            { ids = [ a.id, c.id ]
                            , mappings = Dict.fromList [ ( a.id, a ), ( c.id, c ) ]
                            }
                    in
                        Main.deleteEmail model b.id
                            |> Expect.equal expectedModel
            , test "delete 1 email that does not exist in model has no effect" <|
                \_ ->
                    let
                        testEmail =
                            getTestEmailOne

                        model : EmailModel
                        model =
                            { ids = [ testEmail.id ]
                            , mappings = Dict.fromList [ ( testEmail.id, testEmail ) ]
                            }
                    in
                        Main.deleteEmail model getTestEmailTwo.id
                            |> Expect.equal model
            , test "delete all emails returns empty email model" <|
                \_ ->
                    let
                        expectedModel : EmailModel
                        expectedModel =
                            { ids = []
                            , mappings = Dict.empty
                            }
                    in
                        Main.getEmptyEmailModel
                            |> Expect.equal expectedModel
            ]
        , describe "replaceExistingEmail"
            [ test "replaces an existing email with a new email" <|
                \_ ->
                    let
                        email =
                            { getTestEmailOne | subject = Just "subject A", read = False }

                        updatedEmail =
                            { email | subject = Just "subject B", read = True }

                        anotherEmail =
                            getTestEmailTwo

                        mappings : Dict EmailId Email
                        mappings =
                            Dict.fromList [ ( email.id, email ), ( anotherEmail.id, anotherEmail ) ]

                        expectMappings : Dict EmailId Email
                        expectMappings =
                            Dict.fromList [ ( email.id, updatedEmail ), ( anotherEmail.id, anotherEmail ) ]
                    in
                        Main.replaceExistingEmail updatedEmail mappings
                            |> Expect.equalDicts expectMappings
            , test "has no effect when email to update does not exist" <|
                \_ ->
                    let
                        email =
                            getTestEmailOne

                        updatedEmail =
                            getTestEmailTwo

                        mappings : Dict EmailId Email
                        mappings =
                            Dict.fromList [ ( email.id, email ) ]
                    in
                        Main.replaceExistingEmail updatedEmail mappings
                            |> Expect.equal mappings
            ]
        , describe "addToEmailModel"
            [ test "adds new email to model with the id added to the start of the ids list to keep new to old ordering" <|
                \_ ->
                    let
                        a =
                            getTestEmailOne

                        b =
                            getTestEmailTwo

                        existingModel : EmailModel
                        existingModel =
                            { ids = [ a.id ]
                            , mappings = Dict.fromList [ ( a.id, a ) ]
                            }

                        expectModel : EmailModel
                        expectModel =
                            { ids = [ b.id, a.id ]
                            , mappings = Dict.fromList [ ( a.id, a ), ( b.id, b ) ]
                            }
                    in
                        Main.addToEmailModel b existingModel
                            |> Expect.equal expectModel
            , test "attempt to add an existing email does nothing" <|
                \_ ->
                    let
                        a =
                            getTestEmailOne

                        existingModel : EmailModel
                        existingModel =
                            { ids = [ a.id ]
                            , mappings = Dict.fromList [ ( a.id, a ) ]
                            }
                    in
                        Main.addToEmailModel a existingModel
                            |> Expect.equal existingModel
            , describe "isSameEmailId"
                [ test "True when same" <|
                    \_ ->
                        let
                            a =
                                Just getTestEmailOne.id
                        in
                            Expect.true "expected email ids to be the same" <|
                                Main.isSameEmailId a a
                , test "False when different" <|
                    \_ ->
                        let
                            a =
                                Just getTestEmailOne.id

                            b =
                                Just getTestEmailTwo.id
                        in
                            Expect.false "expected email ids to be different" <|
                                Main.isSameEmailId a b
                , test "False when Nothing 1" <|
                    \_ ->
                        Expect.false "expected false when at least 1 Nothing is provided" <|
                            Main.isSameEmailId Nothing <|
                                Just getTestEmailOne.id
                , test "False when Nothing 2" <|
                    \_ ->
                        Expect.false "expected false when at least 1 Nothing is provided" <|
                            Main.isSameEmailId (Just getTestEmailOne.id) Nothing
                , test "False when Nothing 3" <|
                    \_ ->
                        Expect.false "expected false when at least 1 Nothing is provided" <|
                            Main.isSameEmailId Nothing Nothing
                ]
            , describe "deleteSelectedEmail"
                [ test "when there is no selected email, return Nothing" <|
                    \_ ->
                        let
                            selectedEmailId : Maybe EmailId
                            selectedEmailId =
                                Nothing

                            deletedEmailId : EmailId
                            deletedEmailId =
                                "199"
                        in
                            Main.deleteSelectedEmail deletedEmailId selectedEmailId
                                |> Expect.equal Nothing
                , test "when the selected email matches the deleted email, return Nothing" <|
                    \_ ->
                        let
                            selectedEmailId : Maybe EmailId
                            selectedEmailId =
                                Just "199"

                            deletedEmailId : EmailId
                            deletedEmailId =
                                "199"
                        in
                            Main.deleteSelectedEmail deletedEmailId selectedEmailId
                                |> Expect.equal Nothing
                , test "when the selected email and deleted email are different, return the selected email" <|
                    \_ ->
                        let
                            selectedEmailId : Maybe EmailId
                            selectedEmailId =
                                Just "5"

                            deletedEmailId : EmailId
                            deletedEmailId =
                                "199"
                        in
                            Main.deleteSelectedEmail deletedEmailId selectedEmailId
                                |> Expect.equal selectedEmailId
                ]
            ]
        ]
