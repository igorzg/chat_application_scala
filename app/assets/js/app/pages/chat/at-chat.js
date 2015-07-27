/**
 * Created by igi on 19/07/15.
 */
(function () {

    var module = ngAngular.define("at-chat");

    module.controller('atChat', [
        "$scope", "atTransport",
        function (scope, atTransport) {
            scope.context = "CHAT";
        }
    ]);

}())