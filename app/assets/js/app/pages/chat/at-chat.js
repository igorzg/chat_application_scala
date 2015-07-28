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
        }
    ]);

}())