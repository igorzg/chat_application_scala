/**
 * Created by igi on 19/07/15.
 */
(function () {

    var module = ngAngular.define("at-login");

    module.controller('atLogin', [
        "$scope", "atTransport",
        function (scope, atTransport) {
            scope.login = function login() {
                atTransport.logIn({nick: scope.nick});
            };
        }
    ]);

}())