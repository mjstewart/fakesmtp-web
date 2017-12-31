module Utils exposing (..)

import Dict exposing (Dict)
import AppTypes


truncateText : Int -> String -> String
truncateText n text =
    if n <= 0 || String.length text <= n then
        text
    else
        (String.slice 0 n text) ++ "..."


toCsv : List String -> String
toCsv xs =
    String.join ", " xs


maybeOrElse : a -> Maybe a -> a
maybeOrElse orElseValue m =
    case m of
        Just x ->
            x

        Nothing ->
            orElseValue


maybePredicate : (a -> Bool) -> Maybe a -> Bool
maybePredicate p m =
    case m of
        Nothing ->
            False

        Just a ->
            p a


{-| Inserts each list value into the Dict with the key retrieved from the value using
the key extracting function.

listToDict Tuple.first [(1, "one")]
{ 1: (1, "one") }

Disadvantages of using Dict.fromList is it requires O(n) to map the list to get the correct structure
to insert into the Dict. This function avoids the initial mapping step and inserts into the Dict
as each list value is encountered.

-}
listToDict : (v -> comparable) -> List v -> Dict comparable v
listToDict keyExtractor xs =
    listToDictHelper keyExtractor xs (Dict.empty)


listToDictHelper : (v -> comparable) -> List v -> Dict comparable v -> Dict comparable v
listToDictHelper keyExtractor xs dict =
    case xs of
        [] ->
            dict

        [ x ] ->
            Dict.insert (keyExtractor x) x dict

        x :: ys ->
            listToDictHelper keyExtractor ys <| Dict.insert (keyExtractor x) x dict


maybeGetDict : Maybe comparable -> Dict comparable v -> Maybe v
maybeGetDict maybeKey dict =
    case maybeKey of
        Nothing ->
            Nothing

        Just key ->
            Dict.get key dict


appendToUrl : AppTypes.Url -> String -> AppTypes.Url
appendToUrl url x =
    if String.endsWith "/" url.value then
        AppTypes.Url <| url.value ++ x
    else
        AppTypes.Url <| url.value ++ "/" ++ x
