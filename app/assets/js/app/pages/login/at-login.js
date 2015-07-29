/**
 * Created by igi on 19/07/15.
 */
(function () {

    var module = ngAngular.define("at-login");

    module.controller('atLogin', [
        "$scope", "atTransport",
        function (scope, atTransport) {
            scope.login = function login($event) {
                if (
                    ($event.type === "keydown" && $event.keyCode === 13 ) ||
                    ($event.type === "click")
                ) {
                    if (scope.nick && scope.nick.length > 0) {
                        atTransport.logIn({name: scope.nick});
                    }
                }
            };
        }
    ]);

}())