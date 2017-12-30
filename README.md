# Web support for fakeSMTP running in docker


Work in progress

## Goals 

- Provide app front end connected to websockets for real time emails.
- Provide rest interface to access emails



# rest api

Only HTTP GET is permitted to the following endpoints

The json structure of each email is outlined below where the /emails end point returns a paged HATEOAS response.
a
### /emails

        {
            "_embedded": {
                "emails": [
                    {
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
            },
            "page": {
                "size": 20,
                "totalElements": 1,
                "totalPages": 1,
                "number": 0
            }
        }
        
### /emails/id
Return single email by id


# Server Sent Events

### /stream/emails

When a new email is sent 