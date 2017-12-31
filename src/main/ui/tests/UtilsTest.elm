module UtilsTest exposing (..)

import Expect exposing (Expectation)
import Test exposing (..)
import Dict exposing (Dict)
import Utils
import AppTypes


suite : Test
suite =
    describe "Utils module"
        [ describe "maybeOrElse"
            [ test "returns Just value" <|
                \_ -> Expect.equal "hi" (Utils.maybeOrElse "orElseValue" (Just "hi"))
            , test "returns else value when Nothing" <|
                \_ -> Expect.equal "orElseValue" (Utils.maybeOrElse "orElseValue" Nothing)
            ]
        , describe "toCsv"
            [ test "empty list returns empty string" <|
                \_ -> Expect.equal "" (Utils.toCsv [])
            , test "1 element list returns string with no comma" <|
                \_ -> Expect.equal "a" (Utils.toCsv [ "a" ])
            , test "many elements have commas between" <|
                \_ -> Expect.equal "a, b, c, d" (Utils.toCsv [ "a", "b", "c", "d" ])
            ]
        , describe "truncateText"
            [ test "n == 0 returns full text" <|
                \_ -> Expect.equal "hello" (Utils.truncateText 0 "hello")
            , test "n < 0 returns full text" <|
                \_ -> Expect.equal "hello" (Utils.truncateText -1 "hello")
            , test "n == text length returns full text" <|
                \_ -> Expect.equal "hello" (Utils.truncateText (String.length "hello") "hello")
            , test "truncate text when text length exceeds n" <|
                \_ -> Expect.equal "he..." (Utils.truncateText 2 "hello")
            ]
        , describe "listToDict"
            [ test "empty list returns empty Dict" <|
                \_ ->
                    Utils.listToDict identity []
                        |> Dict.isEmpty
                        |> Expect.true "expected Dict to be empty"
            , test "single element list returns Dict with same entry" <|
                \_ ->
                    let
                        expectedDict : Dict Int ( Int, String )
                        expectedDict =
                            Dict.fromList [ ( 1, ( 1, "one" ) ) ]
                    in
                        Utils.listToDict Tuple.first [ ( 1, "one" ) ]
                            |> Expect.equalDicts expectedDict
            ]
        , test "many elements all get inserted into Dict" <|
            \_ ->
                let
                    expectedDict : Dict Int ( Int, String )
                    expectedDict =
                        Dict.fromList
                            [ ( 1, ( 1, "one" ) )
                            , ( 2, ( 2, "two" ) )
                            , ( 3, ( 3, "three" ) )
                            ]
                in
                    Utils.listToDict Tuple.first [ ( 1, "one" ), ( 2, "two" ), ( 3, "three" ) ]
                        |> Expect.equalDicts expectedDict
        , test "duplicate list elements are ignored" <|
            \_ ->
                let
                    expectedDict : Dict Int ( Int, String )
                    expectedDict =
                        Dict.fromList
                            [ ( 1, ( 1, "one" ) )
                            , ( 2, ( 2, "two" ) )
                            , ( 3, ( 3, "three" ) )
                            ]
                in
                    Utils.listToDict Tuple.first
                        [ ( 1, "one" )
                        , ( 2, "two" )
                        , ( 1, "one" )
                        , ( 3, "three" )
                        , ( 1, "one" )
                        , ( 1, "one" )
                        , ( 2, "two" )
                        ]
                        |> Expect.equalDicts expectedDict
        , describe "appendToUrl"
            [ test "handles url with trailing /" <|
                \_ ->
                    let
                        expect =
                            AppTypes.Url "localhost:8080/emails/123"

                        actual =
                            Utils.appendToUrl (AppTypes.Url "localhost:8080/emails/") "123"
                    in
                        Expect.equal expect actual
            , test "handles url with no trailing /" <|
                \_ ->
                    let
                        expect =
                            AppTypes.Url "localhost:8080/emails/123"

                        actual =
                            Utils.appendToUrl (AppTypes.Url "localhost:8080/emails") "123"
                    in
                        Expect.equal expect actual
            ]
        , describe "maybePredicate"
            [ test "Nothing returns False" <|
                \_ ->
                    let
                        result : Bool
                        result =
                            Utils.maybePredicate (\x -> x == 2) Nothing
                    in
                        Expect.false "expected false but got true" result
            , test "Just evaluates True predicate /" <|
                \_ ->
                    let
                        result : Bool
                        result =
                            Utils.maybePredicate (\x -> x == 2) <| Just 2
                    in
                        Expect.true "expected true but got false" result
            ]
        ]
