# Web support for fakeSMTP running in docker


Work in progress

## Goals 

- Provide app front end connected to websockets for real time emails.
- Provide rest interface to access emails


## Implementation 


## Dev stuff

webpack builds the elm bundle and assets into `resources/static` which is where spring boot serves static content by default.
webpack also generates the ui entry point `index.html` in `resources/templates` where spring boot maps this to
the root path `localhost:8080` by default.  

`cd /src/main/ui`

`yarn run build`

Development can be done using webpack dev server `yarn run start`.

# rest api

Only HTTP GET is permitted to the following endpoints

The json structure of each email is outlined below where the /emails end point returns a paged HATEOAS response.
a
### /emails

Gets all the emails contained in `EMAIL_INPUT_DIR`

A sort query parameter can order emails on any field / order. For example the below url will return all emails ordered 
from newest to oldest.
`http://localhost:8080/emails?sort=sendDate,desc`

        {
            "_embedded": {
                "emails": [
                    {d
                        "id": String UUID,
                        "subject": String | null
                        "from": List String,
                        "replyTo": List String
                        "body": {
                            "content": String,
                            "contentType": {
                                "mediaType": MimeType - "text/html" | "text/plain" | ... | null
                                "charset": "utf-8" | "us-ascii" | ... | null
                            } | null
                        },
                        "receivedDate": "2017-12-28T20:15:22" | null,
                        "sentDate": "2017-12-28T20:15:22" | null
                        "description": String | null,
                        "toRecipients": List String,
                        "ccRecipients": List String,
                        "bccRecipients": List String,
                        "attachments": [
                            {
                                "id": String UUID,
                                "fileName": String | null,
                                "disposition": "inline" | "attachment" | null,
                                "contentType": {
                                        "mediaType": MimeType - "text/html" | "text/plain" | ... | null
                                        "charset": "utf-8" | "us-ascii" | ... | null
                                } | null
                            },
                         ],
                        "_links": {
                            "self": {
                                "href": "http://localhost:8080/emails/{id}"
                            },
                            "email": {
                                "href": "http://localhost:8080/emails/{id}"
                            }
                        }
                    }
                ]
            },
            "_links": {
                "self": {
                    "href": "http://localhost:8080/emails{?page,size,sort}",
                    "templated": true
                },
                "profile": {
                    "href": "http://localhost:8080/profile/emails"
                }
            }
        }
        
### /emails/id
Return single email by id


# Server Sent Events

### /stream/emails

When a new email is sent 