module Main exposing (..)

import Http
import Dict exposing (Dict)
import Messages as Msgs exposing (Msg)
import Html as H exposing (Html, program)
import Html.Attributes exposing (class, classList)
import Html.Events exposing (onClick)
import Email.Api as EmailApi
import Email.Types
    exposing
        ( Email
        , EmailId
        , EmailBody
        , EmailAttachment
        , EmailReadStatus
        , EmailModel
        )
import Views.EmailView exposing (emailView)
import Views.EmailDetailView exposing (emailDetailView)
import Utils
import Ports
import AppTypes
import Email.Decoders as EmailDecoders
import Json.Decode as Decode
import Maybe.FlatMap as MaybeExtra
import Debug


-- MODEL


type alias LoadIndicator =
    { -- Full page dims with load indicator while app is initialized
      app : Bool

    -- Less intrusive load indicator to avoid screen flashing due to fast api response and page dimmer.
    , mini : Bool
    }


type alias Model =
    { emailModel : EmailModel
    , loadIndicator : LoadIndicator
    , emailStreamStatus : Msgs.StreamState
    , selectedEmailId : Maybe EmailId
    }


{-| Used to map an Email to its key in the model dict
-}
emailKeyExtractor : Email -> EmailId
emailKeyExtractor email =
    email.id


{-| Given a list of emails, generate the model representation of using a Dict for fast
lookups/updates while the ids are kept to keep orderings.
-}
toEmailModel : List Email -> EmailModel
toEmailModel emails =
    { mappings = Utils.listToDict emailKeyExtractor emails
    , ids = List.map emailKeyExtractor emails
    }


{-| Joins the ids and email mappings back into a list form for other UI components to use.
-}
getEmailsInModel : EmailModel -> List Email
getEmailsInModel { mappings, ids } =
    List.filterMap ((flip Dict.get) mappings) ids


{-| Lookup the value in the dict by comparable key and apply the updating function to return a new updated Dict.
The motivation is to lookup a value in the Dict and update it through a mapping function.
-}
dictUpdateMapper : comparable -> (x -> x) -> Dict comparable x -> Dict comparable x
dictUpdateMapper id updater =
    Dict.update id <| Maybe.map updater


{-| Update the email in the existing model mappings with whatever value is in the EmailReadStatus read field.
The EmailReadStatus id used to find the email to get updated in the mapping Dict.
-}
updateEmailRead : EmailReadStatus -> Dict EmailId Email -> Dict EmailId Email
updateEmailRead { id, read } mappings =
    dictUpdateMapper id (\email -> { email | read = read }) mappings


{-| Simply calls updateEmailRead for each EmailReadStatus to update and returns the new updated Dict.
-}
updateEmailsRead : List EmailReadStatus -> Dict EmailId Email -> Dict EmailId Email
updateEmailsRead statusList existingMappings =
    List.foldl updateEmailRead existingMappings statusList


{-| Replaces an existing email with the supplied email. The supplied email id is used to lookup
the existing value.
-}
replaceExistingEmail : Email -> Dict EmailId Email -> Dict EmailId Email
replaceExistingEmail email mappings =
    dictUpdateMapper email.id (always email) mappings


deleteEmail : EmailModel -> EmailId -> EmailModel
deleteEmail { ids, mappings } emailId =
    { ids = List.filter (\id -> id /= emailId) ids
    , mappings = Dict.remove emailId mappings
    }


addToEmailModel : Email -> EmailModel -> EmailModel
addToEmailModel email existingModel =
    if Dict.member email.id existingModel.mappings then
        existingModel
    else
        { ids = email.id :: existingModel.ids
        , mappings = Dict.insert email.id email existingModel.mappings
        }


getEmptyEmailModel : EmailModel
getEmptyEmailModel =
    { ids = []
    , mappings = Dict.empty
    }


updateEmailModel : Model -> EmailModel -> Model
updateEmailModel existingModel newEmailModel =
    let
        emailModel =
            existingModel.emailModel
    in
        { existingModel | emailModel = newEmailModel }


showMiniLoadIndicator : Bool -> LoadIndicator
showMiniLoadIndicator show =
    { app = False
    , mini = show
    }


showAppLoadIndicator : Bool -> LoadIndicator
showAppLoadIndicator show =
    { app = show
    , mini = False
    }


showNoLoadIndicator : LoadIndicator
showNoLoadIndicator =
    { app = False
    , mini = False
    }


{-| Returns False if there is no selected email, otherwise checks if the 2 email ids match.
-}
isSameEmailId : Maybe EmailId -> Maybe EmailId -> Bool
isSameEmailId e1 e2 =
    case Maybe.map2 (==) e1 e2 of
        Nothing ->
            False

        Just x ->
            x


{-| Returns the email id to be selected after an email is deleted. There are 3 cases to consider below.

    1. id = 1, Nothing = Nothing
    2. id = 1, Just 2 = Just 2
    3. id = 1, Just 1 = Nothing

-}
deleteSelectedEmail : EmailId -> Maybe EmailId -> Maybe EmailId
deleteSelectedEmail emailId maybeSelectedEmail =
    MaybeExtra.flatMap2
        (\id selectedId ->
            if id == selectedId then
                Nothing
            else
                Just selectedId
        )
        (Just emailId)
        maybeSelectedEmail


init : ( Model, Cmd Msg )
init =
    ( { emailModel = EmailModel [] Dict.empty
      , loadIndicator = showAppLoadIndicator True
      , emailStreamStatus = Msgs.Closed
      , selectedEmailId = Nothing
      }
    , EmailApi.getEmails
    )



-- VIEW


view : Model -> Html Msg
view model =
    H.div []
        [ headerView model
        , H.div [ class "content-wrapper" ]
            [ if model.loadIndicator.app then
                H.div [ class "ui active dimmer" ]
                    [ H.div [ class "ui text loader" ] [ H.text "Loading" ]
                    ]
              else
                H.text ""
            , H.div
                [ class "ui grid" ]
                [ H.div [ class "five wide column" ] [ inboxView (getEmailsInModel model.emailModel) <| model.selectedEmailId ]
                , H.div [ class "eleven wide column" ] [ emailDetailView <| Utils.maybeGetDict model.selectedEmailId model.emailModel.mappings ]
                ]
            ]
        ]


headerView : Model -> Html Msg
headerView model =
    H.header [ class "ui inverted segment app-header" ]
        [ H.div [ class "app-header-title-loader" ]
            [ H.h1 [] [ H.text "Emails" ]
            , H.div
                [ classList
                    [ ( "ui inline loader inverted", True )
                    , ( "active", model.loadIndicator.mini )
                    ]
                ]
                []
            ]
        , H.div [ class "app-header-stream-status" ]
            (case model.emailStreamStatus of
                Msgs.Connecting ->
                    [ H.div [ class "ui blue circular empty mini label" ] []
                    , H.text "Connecting to server... "
                    ]

                Msgs.Opened ->
                    [ H.div [ class "ui green circular empty mini label" ] []
                    , H.text "Connected to server"
                    ]

                Msgs.Closed ->
                    [ H.div [ class "ui red circular empty mini label" ] []
                    , H.text "Disconnected from server"
                    ]

                Msgs.Error ->
                    [ H.div [ class "ui red circular empty mini label" ] []
                    , H.text "Error communicating with server"
                    ]
            )
        ]


inboxView : List Email -> Maybe EmailId -> Html Msg
inboxView emails selectedEmailId =
    let
        { total, unread } =
            getEmailStats emails
    in
        H.div []
            [ H.div [ class "inbox-header-container dividing" ]
                [ H.div [ class "inbox-header" ]
                    [ H.h3 [] [ H.text ("Inbox (" ++ (toString total) ++ ")") ]
                    , H.div [ class "ui label" ]
                        [ H.i [ class "mail icon" ] []
                        , H.text ("unread (" ++ (toString unread) ++ ")")
                        ]
                    ]
                , if (total == 0) then
                    H.text ""
                  else
                    H.div [ class "inbox-header-actions" ]
                        [ if (unread > 0) then
                            H.button [ class "ui button mini", onClick (Msgs.EmailAction Msgs.ReadAll) ]
                                [ H.i [ class "icon mail outline" ] []
                                , H.text "Read all"
                                ]
                          else
                            H.button [ class "ui button mini", onClick (Msgs.EmailAction Msgs.UnreadAll) ]
                                [ H.i [ class "icon mail" ] []
                                , H.text "Unread all"
                                ]
                        , if total > 0 then
                            H.button [ class "ui button mini", onClick (Msgs.DeleteAllEmails) ]
                                [ H.i [ class "icon trash outline" ] []
                                , H.text "Delete all"
                                ]
                          else
                            H.text ""
                        ]
                ]
            , H.div [ class "inbox-email-list" ]
                (let
                    mapper : Email -> Html Msg
                    mapper email =
                        emailView email <| isSameEmailId (Just email.id) selectedEmailId
                 in
                    List.map mapper emails
                )
            ]


type alias EmailStats =
    { total : Int
    , unread : Int
    }


getEmailStats : List Email -> EmailStats
getEmailStats emails =
    { total = List.length emails
    , unread = List.filter (.read >> not) emails |> List.length
    }



-- UPDATE


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        Msgs.GetEmailsResult (Ok allEmails) ->
            ( { model
                | emailModel = toEmailModel allEmails
                , loadIndicator = showAppLoadIndicator False
                , emailStreamStatus = Msgs.Connecting
              }
            , Ports.subscribeToEmailStream "fake-smtp-web-client"
            )

        Msgs.GetEmailsResult (Err e) ->
            handleApiError model e

        Msgs.ToggleEmailRead email ->
            ( { model
                | loadIndicator = showMiniLoadIndicator True
                , selectedEmailId = Just email.id
              }
            , EmailApi.requestToggleEmailRead email
            )

        Msgs.EmailAction Msgs.ReadAll ->
            ( { model | loadIndicator = showMiniLoadIndicator True }, EmailApi.requestReadAllEmails )

        Msgs.EmailAction Msgs.UnreadAll ->
            ( { model | loadIndicator = showMiniLoadIndicator True }, EmailApi.requestUnreadAllEmails )

        Msgs.ToggleEmailReadResult (Ok updatedEmail) ->
            let
                _ =
                    Debug.log "ToggleEmailReadResult" updatedEmail

                newMappings : Dict EmailId Email
                newMappings =
                    replaceExistingEmail updatedEmail model.emailModel.mappings

                emailModel =
                    model.emailModel
            in
                ( { model
                    | loadIndicator = showMiniLoadIndicator False
                    , emailModel = { emailModel | mappings = newMappings }
                  }
                , Cmd.none
                )

        Msgs.ToggleEmailReadResult (Err e) ->
            handleApiError model e

        Msgs.ReadAllResult (Ok statusList) ->
            let
                newMappings : Dict EmailId Email
                newMappings =
                    updateEmailsRead statusList model.emailModel.mappings

                emailModel =
                    model.emailModel

                command : Cmd Msg
                command =
                    if List.length statusList /= List.length model.emailModel.ids then
                        showErrorModal <| getErrorModalPayload "The server failed to mark all emails as read, try again."
                    else
                        Cmd.none
            in
                ( { model
                    | loadIndicator = showMiniLoadIndicator False
                    , emailModel = { emailModel | mappings = newMappings }
                  }
                , command
                )

        Msgs.ReadAllResult (Err e) ->
            handleApiError model e

        Msgs.UnreadAllResult (Ok statusList) ->
            let
                newMappings : Dict EmailId Email
                newMappings =
                    updateEmailsRead statusList model.emailModel.mappings

                emailModel =
                    model.emailModel

                command : Cmd Msg
                command =
                    if List.length statusList /= List.length model.emailModel.ids then
                        showErrorModal <| getErrorModalPayload "The server failed mark all emails as unread, try again."
                    else
                        Cmd.none
            in
                ( { model
                    | loadIndicator = showMiniLoadIndicator False
                    , emailModel = { emailModel | mappings = newMappings }
                  }
                , command
                )

        Msgs.UnreadAllResult (Err e) ->
            handleApiError model e

        Msgs.ErrorModalClosed ->
            ( model, Cmd.none )

        Msgs.DeleteEmail id ->
            ( { model | loadIndicator = showMiniLoadIndicator True }, EmailApi.requestDeleteEmail id )

        Msgs.DeleteEmailResult (Ok emailId) ->
            let
                updatedModel : Model
                updatedModel =
                    updateEmailModel model <| deleteEmail model.emailModel emailId
            in
                ( { updatedModel
                    | loadIndicator = showMiniLoadIndicator False
                    , selectedEmailId = deleteSelectedEmail emailId model.selectedEmailId
                  }
                , Cmd.none
                )

        Msgs.DeleteEmailResult (Err reason) ->
            handleApiError model reason

        Msgs.DeleteAllEmails ->
            ( { model | loadIndicator = showMiniLoadIndicator True }, EmailApi.requestDeleteAllEmails )

        Msgs.DeleteAllEmailsResult (Ok ()) ->
            let
                updatedModel : Model
                updatedModel =
                    updateEmailModel model getEmptyEmailModel
            in
                ( { updatedModel | loadIndicator = showMiniLoadIndicator False, selectedEmailId = Nothing }, Cmd.none )

        Msgs.DeleteAllEmailsResult (Err e) ->
            handleApiError model e

        Msgs.EmailStream Msgs.Connecting ->
            ( model, Cmd.none )

        Msgs.EmailStream Msgs.Opened ->
            ( { model | emailStreamStatus = Msgs.Opened }, Cmd.none )

        Msgs.EmailStream Msgs.Closed ->
            ( { model | emailStreamStatus = Msgs.Closed }, Cmd.none )

        Msgs.EmailStream Msgs.Error ->
            ( { model | emailStreamStatus = Msgs.Error }, Cmd.none )

        Msgs.ReceiveEmailStreamMessage (Ok email) ->
            let
                emailModel =
                    model.emailModel

                newEmailModel =
                    addToEmailModel email emailModel
            in
                ( { model | emailModel = newEmailModel }, Cmd.none )

        Msgs.ReceiveEmailStreamMessage (Err _) ->
            ( model, showErrorModal <| getErrorModalPayload "Error decoding email received from server sent event stream." )


handleApiError : Model -> Http.Error -> ( Model, Cmd Msg )
handleApiError model error =
    ( { model
        | loadIndicator = showNoLoadIndicator
      }
    , Ports.showErrorModal <| getApiErrorModalPayload error
    )


showErrorModal : AppTypes.ApiErrorModal -> Cmd Msg
showErrorModal error =
    Ports.showErrorModal <| error


getApiErrorModalPayload : Http.Error -> AppTypes.ApiErrorModal
getApiErrorModalPayload error =
    { title = "Error"
    , description = httpErrorString error
    }


getErrorModalPayload : String -> AppTypes.ApiErrorModal
getErrorModalPayload description =
    { title = "Error"
    , description = description
    }


httpErrorString : Http.Error -> String
httpErrorString error =
    case error of
        Http.BadUrl url ->
            "Bad URL provided: "
                ++ if String.isEmpty url then
                    "URL is empty"
                   else
                    url

        Http.Timeout ->
            "The server took too long to respond"

        Http.NetworkError ->
            "Network error, the server could be down or you are not permitted to access this resource."

        Http.BadStatus httpResponse ->
            "Bad http status: " ++ toString httpResponse.status.code

        Http.BadPayload reason httpResponse ->
            if String.isEmpty reason then
                "Error decoding server response: http code = " ++ toString httpResponse.status.code
            else
                "Error decoding server response: "
                    ++ if String.isEmpty reason then
                        reason ++ " http code = " ++ toString httpResponse.status.code
                       else
                        "http code = " ++ toString httpResponse.status.code



-- SUBSCRIPTIONS


subscriptions : Model -> Sub Msg
subscriptions model =
    Sub.batch
        [ Ports.errorModalClosed <| always Msgs.ErrorModalClosed
        , Ports.emailStreamOpened <| always (Msgs.EmailStream Msgs.Opened)
        , Ports.emailStreamClosed <| always (Msgs.EmailStream Msgs.Closed)
        , Ports.emailStreamError <| always (Msgs.EmailStream Msgs.Error)
        , Ports.emailStreamOnMessage decodeEmailStreamMessage
        ]


decodeEmailStreamMessage : String -> Msg
decodeEmailStreamMessage jsonString =
    Msgs.ReceiveEmailStreamMessage <|
        Decode.decodeString EmailDecoders.decodeEmail jsonString



-- MAIN


main : Program Never Model Msg
main =
    program
        { init = init
        , view = view
        , update = update
        , subscriptions = subscriptions
        }
