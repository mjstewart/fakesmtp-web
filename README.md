# Web support for FakeSMTP running in docker

The original project [FakeSMTP](https://github.com/Nilhcem/FakeSMTP) offers a GUI in java swing which is fine if you
want something basic and don't wish to use docker.

This project provides web support for FakeSMTP running in docker with the following features
- rest api supporting common crud operations
- new emails are published to a server sent event stream
- web ui written in elm

The rest api produces email data modelled from a [MimeMessage](https://docs.oracle.com/javaee/7/api/javax/mail/internet/MimeMessage.html)
including attachments and other nice to know things you would typically like to see during development. 
See [API](#API).

[![Youtube demo](https://github.com/mjstewart/fakesmtp-web/blob/master/video_thumbnail.png)](https://www.youtube.com/watch?v=iINMUVll6TU "Youtube demo")

# Setup

### Recommended  - Use docker compose

1. copy and paste the projects `docker-compose.yml` file into an empty directory
2. cd into the directory containing the `docker-compose.yml file`
2. `docker-compose up -d`. 

Open a browser and navigate to `http://localhost:60500` which will display the ui (it may take a minute to start up).

- See [API](#API) for rest endpoints. 
 
- See [API URL and port settings ](#API-URL-and-port-settings ) to change the IP and port.


### Without docker compose

If you don't wish to use docker compose, you'll need to start each container individually. 

1 - [FakeSMTP docker](https://github.com/munkyboy/docker-fakesmtp) needs to be run first

`docker run --name fake-smtp -d -p 25:25 -v ~/fake-smtp-emails:/var/mail munkyboy/fakesmtp`
 
2 - Start the fakesmtp-web container (Note: the host port must be 60500, see [Configuration](#Configuration))

`docker run --name fake-smtp-web -d -p 60500:8080 -v ~/fake-smtp-emails:/var/mail mjstewart/fakesmtp-web:1.0`

If you need to change any configuration settings outlined in [Configuration](#Configuration), the docker syntax
for passing in environment variables is

```
docker run --name fake-smtp-web -d -p 60500:8080 \
-v ~/fake-smtp-emails:/var/mail \ 
-e EMAIL_INPUT_DIR_POLL_RATE_SECONDS=10 \
mjstewart/fakesmtp-web:1.0
```

# Configuration

`docker-compose.yml` is used as the walk through example.

### Volumes
The `volumes` mapping for both containers is

`~/fake-smtp-emails:/var/mail`

You can read this as - Within the docker container, `/var/mail` is used to store emails which is mounted to the host
directory `~/fake-smtp-emails`. **IMPORTANT** - Please ensure the host directory `~/fake-smtp-emails` has the correct
permissions such as non root owner and its writable. Both can be changed using `chown` and `chmod` respectively.
 
This results in 
- [docker-fakesmtp](https://github.com/munkyboy/docker-fakesmtp) writing emails into `~/fake-smtp-emails` 
- [fakesmtp-web](https://github.com/mjstewart/fakesmtp-web) reading emails from `~/fake-smtp-emails`
 
If you want a different host directory, be sure to change both volumes for each service eg:
`/some-other-dir:/var/mail`


### Poll rate

`~/fake-smtp-emails` is polled every 10 seconds to check for new emails. 
This can be changed by setting `EMAIL_INPUT_DIR_POLL_RATE_SECONDS`.

Anything over 1 second is recommended to avoid potential issues in emails not getting parsed correctly.

### API URL and port settings

`http://localhost:60500` is used by default to prevent port clashes on the host machine. The docker port mappings must NOT
be changed as the ui is a SPA (Single page application). This means webpack injects the API endpoints when the bundle is built.

If you wish to deploy on a different IP and port, you'll have to manually build the project. 
See [Build custom docker image](#Build-custom-docker-image)

# Build custom docker image

By default, `http://localhost:60500` is the server IP and port the application is accessible on.
This behaviour can be changed by manually building a new docker image through the following steps.
  
You will need yarn and maven installed on your system. Once installed, go to the project directory and
execute the `build.sh` script.

1. Optional - set server IP and port in `build.sh` using environment variable `FAKE_SMTP_WEB_API`

2. If `FAKE_SMTP_WEB_API` is updated, the `fake-smtp-web` service in `docker-compose.yml` must have its port mappings
updated to be the same. 

3. To avoid the docker image name clashing with the existing image on docker hub, change the image name
in `build.sh` to something unique.

    `docker build -t custom/fakesmtp-web .`
    
4. Run `./build.sh`

See [FAQ](#FAQ) for non docker build instructions.

# API

## Server sent event stream  /api/stream/emails/{id} 

Subscribe to receive new emails. The id should be a unique identifier the server uses to track your session.
Eg - something like this is fine.

`http://localhost:60500/api/stream/emails/client123Blah`

The frequency of receiving new emails depends on the environment variable
`EMAIL_INPUT_DIR_POLL_RATE_SECONDS`. This is configurable in `docker-compose.yml`. 

##  GET /api/emails
Returns collection of all the emails.

Its possible to sort the emails by any field and order. For example, to get all emails 
ordered from newest to oldest.

`/api/emails?sort=sentDate,desc`

#### Example json email structure

- All string fields should be considered optional and may return null depending on the email parsing.
- Arrays are always empty if there's no data rather than null.
- Attachment disposition 'inline' refers to content belonging to the email body. Disposition 'attachment' is an explicit
attachment added to the email like a pdf file or something.

```$json
{
    "_embedded": {
        "emails": [
            {
                "id": "95f665cc-bbf3-4da5-a2cb-621c69d59b50",
                "subject": "Testing registration service",
                "replyTo": [
                    "no-reply@user-registration.com"
                ],
                "body": {
                    "content": "some html string here",
                    "contentType": {
                        "mediaType": "text/html",
                        "charset": "utf-8"
                    }
                },
                "receivedDate": null,
                "sentDate": "2017-12-25T06:55:34",
                "description": "a test email",
                "toRecipients": [
                    "user100@email.com"
                ],
                "ccRecipients": ["person1@email.com", "person2@email.com"],
                "bccRecipients": ["person3@email.com", "person4@email.com"],
                "attachments": [
                    {
                        "id": "d81dcf50-cb89-4cd2-b348-d41215020513",
                        "fileName": "styles.css",
                        "disposition": "attachment",
                        "contentType": {
                            "mediaType": "text/css",
                            "charset": "us-ascii"
                        }
                    },
                    {
                        "id": "f4bc2c82-7dad-40f4-9ae1-f102525cb525",
                        "fileName": "notes.txt",
                        "disposition": "attachment",
                        "contentType": {
                            "mediaType": "text/plain",
                            "charset": "us-ascii"
                        }
                    },
                    {
                        "id": "62edda6b-7d67-4c43-8093-4af029c19e0f",
                        "fileName": "menu",
                        "disposition": "inline",
                        "contentType": {
                            "mediaType": "text/plain",
                            "charset": "us-ascii"
                        }
                    },
                    {
                        "id": "86495017-7988-496c-8e3b-b8d597e91853",
                        "fileName": "styles",
                        "disposition": "inline",
                        "contentType": {
                            "mediaType": "text/css",
                            "charset": "us-ascii"
                        }
                    }
                ],
                "read": false,
                "from": [
                    "no-reply@user-registration.com"
                ],
                "_links": {
                    "self": {
                        "href": "http://localhost:60500/api/emails/95f665cc-bbf3-4da5-a2cb-621c69d59b50"
                    },
                    "email": {
                        "href": "http://localhost:60500/api/emails/95f665cc-bbf3-4da5-a2cb-621c69d59b50"
                    }
                }
            }
        ]
    },
    "_links": {
        "self": {
            "href": "http://localhost:60500/api/emails"
        },
        "profile": {
            "href": "http://localhost:60500/api/profile/emails"
        }
    }
}
```

##  GET /api/emails/{id}
Get a single email by id

## DELETE /api/emails/{id}
Delete a single email by id. Returns 204 No Content on successful deletion.

## DELETE /api/emails/actions
Delete all emails. Returns 204 No Content on successful deletion.

## POST /api/emails/actions

Include a json body with either action type to mark all emails read / unread.

```
{
 "action": "READ_ALL" | "UNREAD_ALL" 
}
```

The response is a list containing the read status of each updated email.

```$xslt
[
    {
        "id": "95f665cc-bbf3-4da5-a2cb-621c69d59b50",
        "read": true
    },
    {
        "id": "35f665cc-ccc3-4da5-a2cb-621c69d59b50",
        "read": true
    },
]
```

## PATCH /api/emails/{id}
Update any field in a single email. Returns 200 OK on successful update.

For example, to change an emails `subject, read, replyTo` fields.

```
{
   "subject": "a new subject...",
   "read": true,
   "replyTo": ["someone-different@email.com"]
} 
```

# FAQ

### Network error, the server could be down or you are not permitted to access this resource.

1. Confirm the volume directory on the host has the correct permissions. Such as read/write/execute and non root user.

2. If you're running this on a remote server or docker in a VM such as on a mac or windows, its likely the default image cannot be
used since the client javascript bundle has already been injected with the `localhost:60500` API endpoint. You will need to rebuild your own image using the docker ip
on your machine - See [Build custom docker image](#Build-custom-docker-image)

### Will this only work in docker?

Technically no, but using `docker-compose` really simplifies the setup.

To use without docker assuming you have some form of [FakeSMTP](https://github.com/Nilhcem/FakeSMTP) 
(standalone jar or in docker) writing emails to an output directory then...

1. clone this repo

2. set environment variables on your host machine. (Be careful - its common to need to reboot your 
computer for these variables to be updated or to `source ~/.bashrc` depending on your method.)

   - `EMAIL_INPUT_DIR=/output-directory` (The directory FakeSMTP is writing emails into)

   - `EMAIL_INPUT_DIR_POLL_RATE_SECONDS=10`

   - `FAKE_SMTP_WEB_API` (IP address and port the API will be deployed on, eg `http://localhost:60500`).

3. build

You will need yarn and maven installed on your system. Once installed, go to the project directory and
execute the following commands in order.

```
cd src/main/ui
yarn
yarn run build
cd ../../../
mvn clean package -DskipTests
```

Step 4. 

Run the jar maven created in the target folder of the project root directory.

`java -jar target/fakesmtp-web-1.0.jar`

# Set context path
Step 1.
When you want to set context path, you can do it for spring boot using `server.servlet.context-path` property.
In addition you need to update the `FAKE_SMTP_WEB_API` variable.
for example if you add context path as 
`server.servlet.context-path=/mail`
append the context to `FAKE_SMTP_WEB_API` as below
`http://localhost:8080 -> http://localhost:8080/mail` 

Same applies to port number as well.
Note : for standalone(no docker) implementation we can keep same port number. 

Step 2.
Change `publicPath` in file below to update the context path. 
src\main\ui\webpack.config.js
for eg. if context path is `/mail` make change as 
`const publicPath = (env === 'prod') ? '/ui/' : '/';`
`const publicPath = (env === 'prod') ? '/mail/ui/' : '/';`

Step 3.
Recompile the application using step 3 mentioned above.

# Implementation details
- Spring Boot
- Spring Integration
- Elm

Spring integration is used to poll the `EMAIL_INPUT_DIR` representing the directory FakeSMTP outputs emails into. This
directory corresponds to the mounted host directory used in docker `~/fake-smtp-emails`.
The poll rate is configurable using `EMAIL_INPUT_DIR_POLL_RATE_SECONDS`.

Spring integration reads every new email in this directory and parses it into valid domain objects before sending
it into a pub/sub channel where 2 subscribers are waiting.

- subscriber 1 saves the email into a h2 in memory database which enables the rest api.

- subscriber 2 emits the email through a server sent event stream for real time email updates.

### UI
Since the UI is a SPA, it doesn't work so nicely with docker host:container port mappings since the javascript
bundle has the API endpoints injected during the webpack build.

webpack builds the elm bundle and assets into `resources/static` which is where spring boot serves static content by default.
webpack also generates the ui entry point `index.html` in `resources/templates` which spring boot serves up by default.


`cd /src/main/ui`

`yarn run build`

Development can be done using webpack dev server `yarn run start`.