'use strict';

// for webpack bundling
require('semantic-ui-css/semantic.min.css');
require('../src/styles/core.css');
require('semantic-ui-css/semantic.min.js');

var apiUrl = process.env.FAKE_SMTP_WEB_API_URL || 'localhost:8080';

var Elm = require('./Main.elm');
var mountNode = document.getElementById('main');

var app = Elm.Main.embed(mountNode, {
    apiUrl: apiUrl
});

app.ports.showErrorModal.subscribe(function(apiErrorModal) {
    var modal = $('.ui.modal');
    modal.find('.header').html(apiErrorModal.title);
    modal.find('.content .description').html(apiErrorModal.description);
    
    modal.modal({
        onHidden: function() {
            app.ports.errorModalClosed.send(null);
        }
    })
    .modal('show')
});


app.ports.subscribeToEmailStream.subscribe(function(appIdentifier) {
    var CLOSED = 2;

    var source = new EventSource(apiUrl + "/api/stream/emails/" + appIdentifier);

    if (source.readyState === CLOSED) {
        app.ports.emailStreamClosed.send(null);
        return;
    }

    source.onopen = function(e) {
        app.ports.emailStreamOpened.send(null);
    };

    source.onmessage = function(e) {
        app.ports.emailStreamOnMessage.send(e.data);
    };

    source.onerror = function() {
        app.ports.emailStreamError.send(null);
    };
    
});