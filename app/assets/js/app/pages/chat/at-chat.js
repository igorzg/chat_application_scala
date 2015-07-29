/**
 * Created by igi on 19/07/15.
 */
(function () {

    var module = ngAngular.define("at-chat");

    module.controller('atChat', [
        "$scope", "atTransport", "nick",
        function (scope, atTransport, nick) {
            scope.navigation = {
                name: 'at-nav-bar',
                locals: {}
            };
            scope.context = "CHAT user: " + nick;

            atTransport.allMessages().then(null, null, function (data) {
                scope.messages = data;
            });

            scope.send = function send($event) {
                if (
                    ($event.type === "keydown" && $event.keyCode === 13 ) ||
                    ($event.type === "click")
                ) {
                    atTransport.addMessage({
                        message: scope.message,
                        user: nick,
                        session_id: SESSION_ID
                    });
                    scope.message = null;
                }
            }
        }
    ]);

}())